package it.polimi.ingsw.Server.Controller;

import it.polimi.ingsw.Network.DTO.LobbyDTO;
import it.polimi.ingsw.Server.DAO.AccountDAO;
import it.polimi.ingsw.Server.DAO.MatchDAO;
import it.polimi.ingsw.Server.Model.CardFactory.BuildingCardFactory;
import it.polimi.ingsw.Server.Model.CardFactory.CharacterCardFactory;
import it.polimi.ingsw.Server.Model.CardFactory.EventCardFactory;
import it.polimi.ingsw.Server.Network.VirtualView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * The global orchestrator of the Server.
 * <p>
 * This class is responsible for managing the server state. It acts as the
 * primary entry point for new client connections, handles global nickname uniqueness,
 * interfaces with the database for authentication, and spawns new lobbies and matches.
 * By using a map of open lobbies, it dynamically fulfills the "Multiple Matches"
 * advanced feature, allowing players to create or join specific game rooms.
 * </p>
 */
public class ServerManager {

    /**
     * Global lock to prevent race conditions during critical sections
     * (e.g., simultaneous logins, lobby creation, or match startups).
     */
    private final Object globalLock = new Object();

    /** Global registry of all connected players mapped to their network interfaces. */
    private final Map<String, VirtualView> globalActivePlayers;

    /** List of all currently ongoing game matches. */
    private final List<MatchController> activeMatches;

    /** Map of all currently open lobbies waiting for players. (Lobby ID -> LobbyController) */
    private final Map<Integer, LobbyController> openLobbies;

    /** Counter used to assign unique IDs to new lobbies. */
    private final AtomicInteger lobbyIdCounter = new AtomicInteger(0); //incremental lobby ID //atomic to be thread safe

    //factories for card generation, loaded from JSON once at server startup and shared across all matches (Flyweight Pattern)
    /**
     * Factory responsible for generating Character cards.
     * Loaded once at server startup and shared across all matches (Flyweight Pattern).
     */
    private final CharacterCardFactory characterCardFactory;

    /**
     * Factory responsible for generating Building cards.
     * Loaded once at server startup and shared across all matches (Flyweight Pattern).
     */
    private final BuildingCardFactory buildingCardFactory;

    /**
     * Factory responsible for generating Event cards.
     * Loaded once at server startup and shared across all matches (Flyweight Pattern).
     */
    private final EventCardFactory eventCardFactory;


    /**
     * Constructs the ServerManager, initializing the active tracking structures.
     */
    public ServerManager() {
        this.globalActivePlayers = new ConcurrentHashMap<>();
        this.activeMatches = new ArrayList<>();
        this.openLobbies = new ConcurrentHashMap<>();
        this.characterCardFactory = new CharacterCardFactory();
        this.buildingCardFactory = new BuildingCardFactory();
        this.eventCardFactory = new EventCardFactory();
    }

    //getters

    public CharacterCardFactory getCharacterCardFactory() {
        return characterCardFactory; //used by the LobbyController to start a match and calculate distinct character for a set
    }

    public BuildingCardFactory getBuildingCardFactory() {
        return buildingCardFactory;
    }

    public EventCardFactory getEventCardFactory() {
        return eventCardFactory;
    }


    //LOGIN & AUTHENTICATION

    /**
     * Handles the authentication flow for a client.
     * Once successfully logged in, the player is added to the global registry but
     * is NOT automatically placed in a lobby. They must explicitly create or join one.
     *
     * @param nickname      the requested nickname
     * @param passwordHash the hashed password sent by the client
     * @param view          the network interface representing the client
     * @return {@code true} if login/registration was successful, {@code false} otherwise
     */
    public boolean handleLogin(String nickname, String passwordHash, VirtualView view) {
        synchronized (globalLock) {

            //Prevent double login from one account, before check global connected players
            if (globalActivePlayers.containsKey(nickname)) {
                sendDirectError(view, "User is already logged in from another device.");
                return false;
            }

            //DATABASE CHECK: Check if the nickname exists in the database
            int exists = AccountDAO.nicknameExists(nickname);

            if (exists == -1) {
                sendDirectError(view, "Internal server error. Please try again later.");
                return false;
            }
            if (exists == 1) {
                // AUTHENTICATION: Check if the password matches the hash in DB
                int feedback = AccountDAO.authenticateAccount(nickname, passwordHash);
                if (feedback == 0) {
                    sendDirectError(view, "Wrong password.");
                    return false;
                } else if (feedback == -1) {
                    sendDirectError(view, "Internal server error. Please try again later.");
                    return false;
                }
            } else if (exists == 0) {
                //REGISTRATION: First time seeing this user, let's create the account
                AccountDAO.insertAccount(nickname, passwordHash);
            }

            //SUCCESS: the user is now in the Main Menu, waiting to create or join a lobby
            registerNickname(nickname, view);

            //search if the player is already in a match and needs to reconnect to it
            MatchController ongoingMatch = findMatchByPlayer(nickname);
            if (ongoingMatch != null) {
                System.out.println("[ServerManager] Player " + nickname + " logged in and is reconnecting to an active match.");
                ongoingMatch.handleReconnection(nickname, view); //send the matchDTO to the client
                return true;
            }

            return true;
        }
    }

    /**
     * Sends an error message directly to a specific client.
     * If the client is disconnected or unreachable, the exception is safely ignored.
     *
     * @param view    the network interface representing the client
     * @param message the error message to send
     */
    private void sendDirectError(VirtualView view, String message) {
        try {
            view.sendError(message);
        } catch (Exception ignored) {}
    }

    //CONCURRENT LOBBY MANAGEMENT

    /**
     * Finds and removes the specified player from any waiting lobby they might currently be in.
     * This prevents a player from occupying seats in multiple lobbies simultaneously.
     *
     * @param nickname the nickname of the player to remove
     */
    private void removePlayerFromAllLobbies(String nickname) {
        synchronized (globalLock) { //need to be synchronized because more thread can modify the lobby list at the same time (e.g., multiple players logging in simultaneously, or a player switching lobbies quickly)
            // Iterate safely through all open lobbies
            for (LobbyController lobby : openLobbies.values()) {
                //this loop is not really efficient, but we expect a very small number of open lobbies at any time, so it should be fine for our use case.
                // If we wanted to optimize this, we could maintain a separate map of player-to-lobby for O(1) lookups, but that adds complexity and potential for bugs if not handled carefully.

                // Check if the lobby currently contains this player
                if (lobby.getPlayers().containsKey(nickname)) {
                    lobby.removePlayer(nickname); //the player is removed from the previous lobbies (e.g., has created / joined a new lobby -> in this case the remove player will appoint another person to be the host if there are still players)
                    System.out.println("[ServerManager] Removed " + nickname + " from Lobby " + lobby.getLobbyID() + " because they switched lobbies.");

                    // If the lobby became empty after the creator left, destroy it
                    if (lobby.getCurrentPlayerCount() == 0) {
                        openLobbies.remove(lobby.getLobbyID());
                        System.out.println("[ServerManager] Lobby " + lobby.getLobbyID() + " was empty and has been destroyed.");
                    }
                    break; // A player can only be in one lobby, so we can stop searching
                }
            }
        }
    }

    /**
     * Creates a new lobby and automatically adds the creator to it.
     *
     * @param nickname   the nickname of the player creating the lobby
     * @param targetSize the number of players required to start this match
     */
    public void createLobby(String nickname, int targetSize) {
        synchronized (globalLock) {
            VirtualView view = globalActivePlayers.get(nickname);
            if (view == null) return;

            removePlayerFromAllLobbies(nickname); //remove the player from any previous lobby before creating a new one

            int newLobbyId = lobbyIdCounter.incrementAndGet(); //increment lobby ID and get it

            LobbyController newLobby = new LobbyController(this, newLobbyId, targetSize);
            openLobbies.put(newLobbyId, newLobby);

            newLobby.addPlayer(nickname, view); //add host player

            broadcastOpenLobbies(); //update the lobby browser for all clients

            try {
                view.showMessage("Lobby created successfully! Share this Lobby ID with your friends: " + newLobbyId);
            } catch (Exception e) { //if the connection is lost now we handle it
                handleDisconnection(nickname);
            }

        }
    }

    /**
     * Attempts to join an existing open lobby.
     *
     * @param nickname the nickname of the player trying to join
     * @param lobbyId  the ID of the target lobby
     * @return {@code true} if joined successfully, {@code false} if the lobby doesn't exist or is full
     */
    public boolean joinLobby(String nickname, int lobbyId) {
        synchronized (globalLock) {
            VirtualView view = globalActivePlayers.get(nickname);
            if (view == null) return false;

            //check if the player tries to join to the same lobby in which he is in
            LobbyController currentLobby = findLobbyByPlayer(nickname);
            if (currentLobby != null && currentLobby.getLobbyID() == lobbyId) {
                sendDirectError(view, "You are already in this lobby: " + lobbyId + ".");
                return false;
            }

            LobbyController lobby = openLobbies.get(lobbyId);
            if (lobby == null) {
                sendDirectError(view, "Lobby " + lobbyId + " does not exist or has already started.");
                return false;
            }

            removePlayerFromAllLobbies(nickname); //remove the player from other lobbies before joining

            boolean success = lobby.addPlayer(nickname, view);
            broadcastOpenLobbies(); //update the lobby browser for all clients (in case this lobby is now full and should disappear from the list)

            return success;
        }
    }

    /**
     * Broadcasts the list of currently open and available lobbies to all connected clients
     * who are not currently engaged in an active match.
     * <p>
     * This updates the lobby browser in the clients' user interfaces, allowing them
     * to see real-time changes when lobbies are created, joined, or closed.
     * </p>
     */
    public void broadcastOpenLobbies() {
        synchronized (globalLock) {
            List<LobbyDTO> list = getAvailableLobbiesDTO(); //list of updated lobbyDTO

            for (Map.Entry<String, VirtualView> entry : globalActivePlayers.entrySet()) {
                String nick = entry.getKey();
                VirtualView view = entry.getValue();
                //We only filter out players that are in an active match. We give the possibility to change the lobby and all is handled properly by the ServerManager.
                if (findMatchByPlayer(nick) == null) { // findLobbyByPlayer(nick) == null : with also this condition we don't update the clients that are in a lobby but without this we permit them to change the lobby
                    try {
                        view.showOpenLobbies(list);
                    } catch (Exception ignored) {}
                }
            }
        }
    }

    // GLOBAL REGISTRY MANAGEMENT

    /**
     * Registers a newly connected and authenticated player into the global tracking registry.
     *
     * @param nickname the unique nickname of the player
     * @param view     the network interface representing the client connection
     */
    public void registerNickname(String nickname, VirtualView view) {
        globalActivePlayers.put(nickname, view);
    }

    /**
     * Removes a player from the global tracking registry, usually upon disconnection.
     *
     * @param nickname the unique nickname of the player to remove
     */
    public void unregisterNickname(String nickname) {
        globalActivePlayers.remove(nickname);
    }

    //MATCH ORCHESTRATION
    /**
     * Transitions a filled lobby into an active match.
     * Removes the lobby from the open list and adds the new match to the active tracking list.
     *
     * @param closedLobby the lobby that has reached its target player count
     * @param newMatch    the newly created match controller
     */
    public void startMatch(LobbyController closedLobby, MatchController newMatch) {
        synchronized (globalLock) {
            openLobbies.remove(closedLobby.getLobbyID());
            activeMatches.add(newMatch);
            System.out.println("Game is starting...");
        }
    }

    /**
     * Removes a finished match from the list of active matches.*
     * @param finishedMatch the MatchController of the game that has concluded
     */
    public void removeFinishedMatch(MatchController finishedMatch) {
        synchronized (globalLock) {
            activeMatches.remove(finishedMatch);
            System.out.println("[ServerManager] Match successfully removed. Remaining active matches: " + activeMatches.size());
        }
    }

    /**
     * Removes a lobby entirely if it crashes (e.g., due to a disconnection during setup).
     *
     * @param crashedLobby the lobby that suffered a critical disconnection
     */
    public void resetLobby(LobbyController crashedLobby) {
        synchronized (globalLock) {
            openLobbies.remove(crashedLobby.getLobbyID());
        }
    }

    /**
     * Handles the cleanup process when a player disconnects from the server.
     * <p>
     * The method identifies where the player was (Lobby or Match) and triggers
     * the appropriate cleanup logic. Finally, it removes the player from the
     * global active registry.
     * </p>
     *
     * @param nickname the nickname of the player who lost connection
     */
    public void handleDisconnection(String nickname) {
        if (nickname == null) return;

        synchronized (globalLock) {
            System.out.println("Handling disconnection for: " + nickname);

            //check if the player was in a Lobby
            LobbyController lobby = findLobbyByPlayer(nickname);
            if (lobby != null) {
                lobby.removePlayer(nickname);

                //if the lobby is empty after removal, delete it
                if (lobby.getPlayers().isEmpty()) {
                    openLobbies.remove(lobby.getLobbyID());
                    broadcastOpenLobbies(); //update the lobby for all clients, this player was in lobby and disconnected
                    System.out.println("Lobby " + lobby.getLobbyID() + " removed because it's empty(Host Disconnected).");
                }
            }

            // check if the player was in an active Match
            MatchController match = findMatchByPlayer(nickname);
            if (match != null) {
                // We notify the match controller to handle the disconnection according to the game rules
                match.handleDisconnection(nickname);
            }

            // Final step: remove from the global registry
            unregisterNickname(nickname);
        }
    }


    //LOBBY

    /**
     * Converts internal LobbyController objects into a list of lightweight DTOs.
     * <p>
     * This method iterates through the 'openLobbies' map, filters out full matches,
     * and creates a new {@link LobbyDTO} for each active controller to be sent over the network.
     * </p>
     *
     * @return A list of {@link LobbyDTO} representing current joinable matches.
     */
    public List<LobbyDTO> getAvailableLobbiesDTO() {
        synchronized (globalLock) {
            return openLobbies.values().stream()
                    // Filter: only show lobbies that haven't reached their target size yet
                    .filter(controller -> !controller.isFull())
                    // Mapping: Transform LobbyController -> LobbyDTO
                    .map(controller -> new LobbyDTO(
                            controller.getLobbyID(),
                            controller.getHostNickname(),
                            controller.getPlayers().size(),
                            controller.getTargetSize(),
                            new ArrayList<>(controller.getPlayers().keySet()) // List of nicknames
                    ))
                    .toList();
        }
    }

    // --- HELPER METHODS FOR DISCONNECTION ---

    /**
     * Searches through all currently open lobbies to find the one containing the specified player.
     *
     * @param nickname the nickname of the player to locate
     * @return the {@link LobbyController} hosting the player, or {@code null} if the player is not in any lobby
     */
    private LobbyController findLobbyByPlayer(String nickname) {
        return openLobbies.values().stream()
                .filter(l -> l.getPlayers().containsKey(nickname))
                .findFirst()
                .orElse(null);
    }

    /**
     * Searches through all ongoing matches to find the one containing the specified player.
     *
     * @param nickname the nickname of the player to locate
     * @return the {@link MatchController} managing the player's match, or {@code null} if the player is not in an active match
     */
    private MatchController findMatchByPlayer(String nickname) {
        return activeMatches.stream()
                .filter(m -> m.isPlayerInMatch(nickname))
                .findFirst()
                .orElse(null);
    }



    //DB INTEGRATION

    /**
     * Requests a new unique Match ID from the database.
     *
     * @param numPlayers the number of players that will participate in this match
     * @return the unique match ID assigned by the database, or -1 if an error occurs
     */
    public int generateNewMatchId(int numPlayers) {
        return MatchDAO.createMatch(numPlayers);
    }
}