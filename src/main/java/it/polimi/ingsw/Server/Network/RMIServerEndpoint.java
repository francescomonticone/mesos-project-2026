package it.polimi.ingsw.Server.Network;

import it.polimi.ingsw.Network.DTO.LobbyDTO;
import it.polimi.ingsw.Network.RMI.ClientRemote;
import it.polimi.ingsw.Network.RMI.ServerRemote;
import it.polimi.ingsw.Server.Controller.ServerManager;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The concrete implementation of the {@link ServerRemote} interface.
 * <p>
 * This object is bound to the RMI Registry. It receives direct method calls
 * from clients and routes them directly to the underlying controllers
 * (ServerManager or MatchController).
 * </p>
 */
public class RMIServerEndpoint extends UnicastRemoteObject implements ServerRemote {

    private final ServerManager serverManager;

    /**
     * Map linking the client nickname to their specific handler, created during the login phase.
     */
    private final Map<String, RMIClientHandler> activeHandlers;

    /**
     * Constructs the RMI Server endpoint and exports it to receive remote calls.
     *
     * @param serverManager the global orchestrator of the server
     * @throws RemoteException if the object cannot be exported
     */
    public RMIServerEndpoint(ServerManager serverManager) throws RemoteException {
        super(); // This automatically exports the object to receive remote calls
        this.serverManager = serverManager;
        this.activeHandlers = new ConcurrentHashMap<>();
    }


    // SETUP & LOBBY METHODS
    /**
     * Authenticates an RMI client and links their remote callback to the server.
     * <p>
     * A temporary handler is created to allow the ServerManager to send login
     * feedback (success or error messages). The handler is only saved permanently
     * in the active registry if the authentication is successful.
     * Note that we save the mapping between the client's nickname and their handler to pass the clientStub only during the login phase.
     * Then we will use simply the nickname and this is important for the resilience of disconnections.
     * </p>
     *
     * @param nickname     the requested username
     * @param passwordHash the hashed password
     * @param callback     the remote stub of the client to send messages back
     * @throws RemoteException if a network communication error occurs
     */
    @Override
    public boolean login(String nickname, String passwordHash, ClientRemote callback) throws RemoteException {

        // Create a temporary handler for that player, which will be used during the login process to send feedback messages.
        RMIClientHandler tempHandler = new RMIClientHandler(callback, serverManager, nickname);

        // Attempt the login through the global orchestrator
        boolean loginSuccessful = serverManager.handleLogin(nickname, passwordHash, tempHandler);

        // only if the login is fully successful, we keep track of this handler
        // for future game actions so we will pass only the nickname in the RMI network
        if (loginSuccessful) {
            activeHandlers.put(nickname, tempHandler);
            System.out.println("[RMI] Permanent handler registered for: " + nickname);
        } else {
            // If it fails, we do nothing. The tempHandler is garbage collected,
            // and the map remains clean!
            System.out.println("[RMI] Login failed for " + nickname + ". Handler discarded.");
        }
        return loginSuccessful;
    }

    /**
     * Processes a request from an RMI client to create a new lobby.
     * Retrieves the specific handler for the client using their nickname and delegates the action.
     *
     * @param nickname   the username of the player creating the lobby
     * @param targetSize the desired number of players for the match
     * @throws RemoteException if a network error occurs during the remote call
     */
    @Override
    public void createLobby(String nickname, int targetSize) throws RemoteException {
        RMIClientHandler handler = activeHandlers.get(nickname);
        if (handler != null) {
            handler.handleCreateLobby(targetSize);
        } else {
            System.err.println("[RMI Error] Unknown client trying to create lobby: " + nickname);
        }
    }

    /**
     * Processes a request from an RMI client to join an existing lobby.
     * Routes the request through the client's registered handler.
     *
     * @param nickname the username of the player attempting to join
     * @param lobbyId  the unique identifier of the lobby to join
     * @throws RemoteException if a network error occurs during the remote call
     */
    @Override
    public void joinLobby(String nickname, int lobbyId) throws RemoteException {
        RMIClientHandler handler = activeHandlers.get(nickname);
        if (handler != null) {
            handler.handleJoinLobby(lobbyId);
        }
    }

    /**
     * Retrieves the list of currently open and joinable lobbies directly from the server manager.
     *
     * @return a list of {@link LobbyDTO} containing basic information about each available lobby
     * @throws RemoteException if a network error occurs during the remote call
     */
    @Override
    public List<LobbyDTO> getOpenLobbies() throws RemoteException {
        return serverManager.getAvailableLobbiesDTO(); //create and return with RMI the LobbiesDTO object to view open lobbies in the client
    }


    // MATCH ACTIONS

    /**
     * Routes a player's totem placement action to their specific handler.
     *
     * @param nickname the username of the player performing the action
     * @param tileId   the identifier of the board tile where the totem is placed
     * @throws RemoteException if a network error occurs during the remote call
     */
    @Override
    public void placeTotem(String nickname, char tileId) throws RemoteException {
        RMIClientHandler handler = activeHandlers.get(nickname);
        if (handler != null) {
            handler.handlePlaceTotem(tileId);
        }
    }

    /**
     * Routes a player's offer resolution action (selecting cards) to their specific handler.
     *
     * @param nickname     the username of the player performing the action
     * @param upCards      the list of card identifiers selected from the face-up pool
     * @param downCards    the list of card identifiers selected from the face-down pool
     * @param orderedCards the ordered list of card identifiers indicating how the player arranges their selection
     * @throws RemoteException if a network error occurs during the remote call
     */
    @Override
    public void resolveOffer(String nickname, List<String> upCards, List<String> downCards, List<String> orderedCards) throws RemoteException {
        RMIClientHandler handler = activeHandlers.get(nickname);
        if (handler != null) {
            handler.handleResolveOffer(upCards, downCards, orderedCards);
        }

    }

    /**
     * Routes a player's extra card pick action to their specific handler.
     *
     * @param nickname   the username of the player performing the action
     * @param cardId     the identifier of the selected extra card
     * @param isUpperRow a boolean indicating if the card was drawn from the upper row (true) or lower row (false)
     * @throws RemoteException if a network error occurs during the remote call
     */
    @Override
    public void pickExtraCard(String nickname, String cardId, boolean isUpperRow) throws RemoteException {
        RMIClientHandler handler = activeHandlers.get(nickname);
        if (handler != null) {
            handler.handleExtraCardPick(cardId, isUpperRow);
        }
    }

    /**
     * Routes a player's decision to skip picking an extra card to their specific handler.
     *
     * @param nickname the username of the player skipping the action
     * @throws RemoteException if a network error occurs during the remote call
     */
    @Override
    public void skipExtra(String nickname) throws RemoteException {
        RMIClientHandler handler = activeHandlers.get(nickname);
        if (handler != null) {
            handler.handleSkipExtra();
        }
    }

}
