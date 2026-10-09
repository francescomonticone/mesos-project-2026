package it.polimi.ingsw.Server.Controller;

import it.polimi.ingsw.Server.Model.CardFactory.BuildingCardFactory;
import it.polimi.ingsw.Server.Model.CardFactory.CharacterCardFactory;
import it.polimi.ingsw.Server.Model.CardFactory.EventCardFactory;
import it.polimi.ingsw.Server.Model.CardFactory.MainDeckBuilder;
import it.polimi.ingsw.Server.Model.Cards.Card;
import it.polimi.ingsw.Server.Model.Cards.Deck;
import it.polimi.ingsw.Server.Model.Match.Board.Board;
import it.polimi.ingsw.Server.Model.Match.GameState.BoardRegenerationState;
import it.polimi.ingsw.Server.Model.Match.MatchModel;
import it.polimi.ingsw.Server.Model.Match.Player;
import it.polimi.ingsw.Server.Model.Match.TotemColour;
import it.polimi.ingsw.Server.Network.VirtualView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller responsible for managing the pre-game phase (the waiting room).
 * <p>
 * It handles new client connections, ensures nickname uniqueness, prompts the
 * first player for the desired match size, and starts the match once the
 * required number of players is reached.
 * </p>
 */
public class LobbyController {

    /** Reference to the main server manager to check global nickname uniqueness and register started matches */
    private final ServerManager serverManager;

    /** * Ordered map of players currently waiting in this lobby.
     * LinkedHashMap is used to preserve the order of connection (the first entry is the creator).
     */
    private final Map<String, VirtualView> waitingPlayers;

    /** The nickname of the first player who creates the match */
    private String hostNickname;

    /** The target number of players chosen by the creator. -1 means not chosen yet. */
    private int targetPlayerCount;

    /** Lock for thread-safe operations on the lobby state */
    private final Object lock = new Object();

    /** * The unique identifier for this lobby instance.
     */
    private final int lobbyID;

    /**
     * Constructs a new LobbyController for a forming match.
     *
     * @param serverManager     the main server manager
     * @param lobbyID           the unique identifier for this lobby
     * @param targetPlayerCount the number of players required to start the match
     */
    public LobbyController(ServerManager serverManager,  int lobbyID, int targetPlayerCount) {
        this.serverManager = serverManager;
        this.waitingPlayers = new LinkedHashMap<>();
        this.targetPlayerCount = -1;
        this.hostNickname = null;
        this.lobbyID = lobbyID;
        this.targetPlayerCount = targetPlayerCount;
    }

    //getters
    public int getLobbyID() {
        return lobbyID;
    }
    public int getTargetSize() { return this.targetPlayerCount; }
    public int getCurrentPlayerCount() { return this.waitingPlayers.size(); }

    /**
     * Returns a read-only view of the players currently in the lobby.
     * This is used by the ServerManager to check if the lobby is empty.
     *
     * @return an unmodifiable map of nicknames to VirtualViews
     */
    public Map<String, VirtualView> getPlayers() {
        synchronized (lock) {
            return Collections.unmodifiableMap(waitingPlayers);
        }
    }

    /**
     * Returns the nickname of the player who created the lobby (the first one).
     */
    public String getHostNickname() {
        return waitingPlayers.keySet().stream().findFirst().orElse("Unknown");
    }

    /**
     * Checks if the lobby has reached its target capacity.
     *
     * @return {@code true} if the lobby is full and ready to start, {@code false} otherwise
     */
    public boolean isFull() {
        return waitingPlayers.size() >= targetPlayerCount;
    }

    /**
     * Adds a player to this lobby.
     * If it's the first player, they are automatically assigned as the host.
     * After adding, it checks if the lobby is full to eventually start the match.
     *
     * @param nickname the unique nickname of the player
     * @param view     the VirtualView associated with the client
     * @return {@code true} if the player was successfully added, {@code false} if the lobby is already full
     */
    public boolean addPlayer(String nickname, VirtualView view) {
        synchronized (lock) {
            //if the lobby is already full, reject the new player (this can happen if multiple players try to join at the same time when the lobby is almost full)
            if (targetPlayerCount != -1 && waitingPlayers.size() >= targetPlayerCount) {
                return false;
            }

            //player added to the lobby
            waitingPlayers.put(nickname, view);

            if (waitingPlayers.size() == 1) { //if it is the first player, assign it as host
                hostNickname = nickname;
            }
            //send a notification to all the other players
            broadcastMessage("Player " + nickname + " joined the lobby ("
                    + waitingPlayers.size() + "/" + targetPlayerCount + ").");

            //check if the target match number of players is reached, if so start the match
            checkAndStartMatch();
            return true;
        }
    }

    /**
     * Removes a single player from the lobby.
     * If the player leaving was the host, the next player in the connection order
     * (the LinkedHashMap's iterator) automatically becomes the new host.
     *
     * @param nickname the nickname of the player to remove
     */
    public void removePlayer(String nickname) {
        synchronized (lock) {
            if (!waitingPlayers.containsKey(nickname)) return;

            waitingPlayers.remove(nickname);

            // If the host left and there are other players, assign a new host
            if (nickname.equals(hostNickname)) {
                if (!waitingPlayers.isEmpty()) {
                    hostNickname = waitingPlayers.keySet().iterator().next();
                    broadcastMessage("The host has left. " + hostNickname + " is now the new host.");
                } else {
                    hostNickname = null;
                }
            }

            String sizeInfo = (targetPlayerCount == -1) ? "?" : String.valueOf(targetPlayerCount);
            broadcastMessage("Player " + nickname + " left the lobby (" + waitingPlayers.size() + "/" + sizeInfo + ").");
        }
    }


    /**
     * Checks if the lobby has reached the target player count and starts the game if so.
     */
    private void checkAndStartMatch() {
        if (targetPlayerCount != -1 && waitingPlayers.size() == targetPlayerCount) { //when the lobby is full
            broadcastMessage("The match is starting!");

            //initialize the game model (creating players, decks, etc.)
            MatchModel newModel = createInitialModel();

            //create the MatchController and pass the specific clients
            MatchController matchController = new MatchController(newModel, waitingPlayers, serverManager::removeFinishedMatch); //consumer that the MatchController will call when the match ends to remove it from the active matches list

            //hand over the match to the ServerManager to remove players from the lobby and register the match in the active matches list
            serverManager.startMatch(this, matchController);

            //start the match in a new thread to free up the lobby controller to handle possible disconnections
            new Thread(matchController::startGame, "Match-Thread-" + lobbyID).start(); //thread for that match (lambda, name)
        }
    }

    /**
     * Handles a client disconnection during the lobby phase.
     * As per requirements, if anyone disconnects while starting, the match is aborted.
     *
     * @param nickname the nickname of the disconnected player
     */
    public void handleDisconnection(String nickname) {
        synchronized (lock) {
            if (!waitingPlayers.containsKey(nickname)) return;

            // Notify everyone that the lobby is crashing
            broadcastError("Player " + nickname + " disconnected. The lobby has been aborted.");

            // Free all nicknames so players can reconnect
            for (String p : waitingPlayers.keySet()) {
                serverManager.unregisterNickname(p);
            }

            waitingPlayers.clear();

            // Tell the ServerManager to destroy this lobby and prepare a new one
            serverManager.resetLobby(this);
        }
    }

    //utilities
    /**
     * Factory method to initialize a new MatchModel with the currently waiting players.
     * It requests a unique Match ID from the ServerManager (which queries the DB),
     * passing the exact number of players so it can be stored correctly.
     *
     * @return the newly initialized MatchModel
     * @throws IllegalStateException if the database fails to generate a valid match ID
     */
    private MatchModel createInitialModel() {
        // calculate the actual number of players currently in the lobby
        int numPlayers = waitingPlayers.size();

        // request the official match ID from the DB,
        // passing the number of players to store it in the database.
        int matchId = serverManager.generateNewMatchId(numPlayers);

        // handle potential database connection errors
        MatchModel newModel = getMatchModel(matchId, numPlayers);

        CharacterCardFactory charFactory = serverManager.getCharacterCardFactory();
        BuildingCardFactory buildingFactory = serverManager.getBuildingCardFactory();
        EventCardFactory eventFactory = serverManager.getEventCardFactory();

        // initialize the Main Deck using the Builder
        Deck<Card> mainDeck = MainDeckBuilder.build(
                charFactory.getAllCards(),
                eventFactory.getAllNonFinalCards(),
                eventFactory.getFinalCards(),
                numPlayers
        );
        newModel.setMainDeck(mainDeck);

        // initialize Building Decks using the Flyweight Factories
        newModel.setBuildingDecksByEra(buildingFactory.createBuildingDecksByEra(numPlayers));

        // initialize the Board
        Board board = new Board(numPlayers);
        newModel.setBoard(board);

        // define Initial Turn Order (Randomized for the first round)
        List<Player> startingOrder = new ArrayList<>(newModel.getPlayers());
        Collections.shuffle(startingOrder); //randomize the order for the first round
        board.getTurnOrderTile().setPlayerOrderTopBottom(startingOrder);


        //Set initial food: IT IS INDEPENDENT OF NUMBER OF PLAYERS
        int[] startingFoodValues = {2, 3, 3, 4, 4};

        //assign the food for each real player
        for (int i = 0; i < startingOrder.size(); i++) {
            startingOrder.get(i).addFood(startingFoodValues[i]);
        }

        // set the Initial Game State: The game starts with Board Regeneration state to load cards!
        newModel.setCurrentState(new BoardRegenerationState());

        return newModel;
    }

    /**
     * Initializes and configures the core {@link MatchModel} for the new game.
     * <p>
     * This method handles the creation of the model using the provided Match ID
     * from the database. It also instantiates the players, assigns them random
     * totem colors, and marks them as connected.
     * </p>
     *
     * @param matchId    the unique identifier generated by the database for this match
     * @param numPlayers the total number of players participating in the match
     * @return the fully configured {@link MatchModel} instance ready for the game
     * @throws IllegalStateException if the database fails to provide a valid match ID (e.g., returns -1)
     */
    private MatchModel getMatchModel(int matchId, int numPlayers) {
        if (matchId == -1) {
            // If the DB is unreachable right when the match is about to start
            throw new IllegalStateException("Failed to generate a valid Match ID from the database.");
        }

        CharacterCardFactory charFactory = serverManager.getCharacterCardFactory();
        int distinctCharactersCount = charFactory.computeDistinctCharactersCount();

        // instantiate the MatchModel with the retrieved ID
        MatchModel newModel = new MatchModel(matchId, numPlayers, distinctCharactersCount);

        //totem colour random shuffle and assignment
        List<TotemColour> availableColours = Arrays.asList(TotemColour.values());
        List<TotemColour> shuffledColours = new ArrayList<>(availableColours); //create a modifiable copy to avoid changing the original enum array
        Collections.shuffle(shuffledColours);

        int colorIndex = 0;

        // create Player entities and add them to the model
        for (String nickname : waitingPlayers.keySet()) {
            Player player = new Player(nickname);

            //set the random totem colour
            player.setTotem(shuffledColours.get(colorIndex));
            colorIndex++;

            // Since they just passed the lobby validation, we guarantee they are connected
            player.setConnected(true);
            newModel.addPlayer(player);
        }
        return newModel;
    }

    /**
     * Broadcasts a general message to all players currently waiting.
     * <p>
     * This method creates a snapshot of the {@code waitingPlayers} map to safely iterate
     * over the players without causing a {@link java.util.ConcurrentModificationException}.
     * It assumes that the caller (e.g., the lobby controller) has already acquired the
     * necessary synchronized lock.
     * <p>
     * If a player's view throws an exception during the message dispatch (indicating a
     * lost connection), their nickname is temporarily collected. Once the iteration is
     * complete, all collected players are subsequently disconnected.
     *
     * @param message The message string to be broadcasted to all waiting players.
     */
    private void broadcastMessage(String message) {
        //we are already inside a synchronized lock (called by lobbyController), we create a snapshot of the map before iterating to avoid concurrent modification of waiting players
        List<Map.Entry<String, VirtualView>> snapshot = new ArrayList<>(waitingPlayers.entrySet());
        List<String> toDisconnect = new ArrayList<>();

        for (Map.Entry<String, VirtualView> entry : snapshot) {
            try {
                entry.getValue().showMessage(message);
            } catch (Exception e) {
                toDisconnect.add(entry.getKey()); //we don't modify the map while iterating, we collect the nicknames to disconnect after the iteration
            }
        }
        //now we can handle the disconnection
        for (String nick : toDisconnect) { //note that this will call waitingPlayers.remove(nickname) so it's for this cause that we avoid removing while iterating (concurrent modification)
            handleDisconnection(nick); //note that this will also broadcast an error message to the other players, so we don't need to do it here
        }
    }

    /**
     * Broadcasts an error message to all players currently waiting.
     * <p>
     * Similar to {@link #broadcastMessage(String)}, this method uses a snapshot of the
     * waiting players' views to safely iterate over them. If an exception occurs while
     * sending the error to a specific client (e.g., the client is already disconnected),
     * it is safely ignored because we don't want to send an error to a disconnected player.
     *
     * @param message The error message string to be broadcasted.
     */
    private void broadcastError(String message) {
        List<VirtualView> snapshot = new ArrayList<>(waitingPlayers.values());
        for (VirtualView view : snapshot) {
            try {
                view.sendError(message);
            } catch (Exception ignored) {} //ignore if the player is disconnected
        }
    }
}