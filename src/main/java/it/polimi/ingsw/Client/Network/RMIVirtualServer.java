package it.polimi.ingsw.Client.Network;

import it.polimi.ingsw.Client.ClientController.ClientController;
import it.polimi.ingsw.Network.DTO.LobbyDTO;
import it.polimi.ingsw.Network.DTO.MatchDTO;
import it.polimi.ingsw.Network.DTO.MatchResultDTO;
import it.polimi.ingsw.Network.RMI.ClientRemote;
import it.polimi.ingsw.Network.RMI.ServerRemote;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;

/**
 * RMI implementation of the VirtualServer interface.
 * <p>
 * This class translates user actions into direct RMI method calls to the Server.
 * Simultaneously, it instantiates and exports a Callback object ({@link RMIClientCallback})
 * that allows the Server to invoke methods directly on this Client's UI.
 * </p>
 */
public class RMIVirtualServer implements VirtualServer {

    private final ServerRemote serverStub;
    private final ClientRemote callbackStub; //is the client's remote object that the server will call to update the UI
    private final RMIClientCallback callbackImpl;

    private String myNickname;

    public RMIVirtualServer(String host, int port) throws Exception {
        Registry registry = LocateRegistry.getRegistry(host, port); //get reference to the registry used by RMI
        this.serverStub = (ServerRemote) registry.lookup("MesosServer"); //get the serverStub from the registry
        this.callbackImpl = new RMIClientCallback();
        this.callbackStub = (ClientRemote) UnicastRemoteObject.exportObject(callbackImpl, 0); //exposed to the RMI network
    }

    /**
     * Links the main client controller to the RMI callback implementation.
     * This allows the network layer to forward server updates directly to the application logic.
     *
     * @param controller the main {@link ClientController} of the application
     */
    public void setControllerRMILoopSocket(ClientController controller) {
        this.callbackImpl.setController(controller);
    }


    // VirtualServer methods (Client -> Server)

    /**
     * Sends a login request to the server using RMI.
     * If successful, saves the nickname locally for subsequent remote calls.
     *
     * @param nickname     the chosen username
     * @param passwordHash the hashed password for authentication
     * @throws Exception if a network or remote execution error occurs
     */
    @Override
    public void login(String nickname, String passwordHash) throws Exception {
        // Synchronous RMI call
        boolean success = serverStub.login(nickname, passwordHash, callbackStub);

        if (success) {
            this.myNickname = nickname; // RMI needs this for subsequent remote calls
        }

        // Delegate the result handling to the Controller
        if (callbackImpl.getController() != null) {
            callbackImpl.getController().onLoginResult(success, nickname);
        }
    }

    /**
     * Requests the server to create a new match lobby.
     *
     * @param targetSize the desired number of players for the match (e.g., 2 to 5)
     * @throws Exception if a network or remote execution error occurs
     */
    @Override
    public void createLobby(int targetSize) throws Exception {
        if (myNickname != null) serverStub.createLobby(myNickname, targetSize);
    }

    /**
     * Requests the server to add this client to an existing lobby.
     *
     * @param lobbyId the unique identifier of the target lobby
     * @throws Exception if a network or remote execution error occurs
     */
    @Override
    public void joinLobby(int lobbyId) throws Exception {
        if (myNickname != null) serverStub.joinLobby(myNickname, lobbyId);
    }

    /**
     * Synchronously retrieves the list of open lobbies from the server
     * and immediately forwards them to the View for display.
     *
     * @throws Exception if a network or remote execution error occurs
     */
    @Override
    public void requestOpenLobbies() throws Exception {
        try {
            // Direct remote call to the server
            List<LobbyDTO> lobbies = serverStub.getOpenLobbies(); //this method is in RMIServerEndpoint

            // As soon as the call returns, update the UI via the controller
            if (callbackImpl.getController() != null) {
                callbackImpl.getController().getView().showOpenLobbies(lobbies);
            }
        } catch (RemoteException e) {
            throw new Exception("Failed to retrieve lobby list via RMI", e);
        }
    }

    // MATCH ACTIONS

    /**
     * Sends the player's choice for totem placement to the server.
     *
     * @param tileId the character identifier of the chosen offer tile (e.g., 'A', 'B')
     * @throws Exception if a network or remote execution error occurs
     */
    @Override
    public void placeTotem(char tileId) throws Exception {
        if (myNickname != null) serverStub.placeTotem(myNickname, tileId);
    }

    /**
     * Sends the player's choice of cards to pick from the board to the server.
     *
     * @param upCards      the list of IDs for cards picked from the upper row
     * @param downCards    the list of IDs for cards picked from the lower row
     * @param orderedCards the raw list of requested cards in the order they were chosen
     * @throws Exception if a network or remote execution error occurs
     */
    @Override
    public void resolveOffer(List<String> upCards, List<String> downCards, List<String> orderedCards) throws Exception {
        if (myNickname != null) serverStub.resolveOffer(myNickname, upCards, downCards, orderedCards);
    }

    /**
     * Requests to pick an extra card from the board, generally triggered by a specific effect.
     *
     * @param cardId     the ID of the extra card to pick
     * @param isUpperRow true if the card is located in the upper row, false otherwise
     * @throws Exception if a network or remote execution error occurs
     */
    @Override
    public void pickExtraCard(String cardId, boolean isUpperRow) throws Exception {
        if (myNickname != null) serverStub.pickExtraCard(myNickname, cardId, isUpperRow);
    }

    /**
     * Notifies the server that the player explicitly chooses to skip their extra card pick.
     *
     * @throws Exception if a network or remote execution error occurs
     */
    @Override
    public void skipExtraCard() throws Exception {
        if (myNickname != null) serverStub.skipExtra(myNickname);
    }


    // INNER CLASS: RMI CALLBACK IMPLEMENTATION (Server -> Client)

    /**
     * Inner class implementing the {@link ClientRemote} interface.
     * <p>
     * Instead of receiving generic ServerCommands, this object receives direct
     * method calls from the Server (e.g., updateModel) and passes them to the controller.
     * </p>
     */
    private class RMIClientCallback implements ClientRemote {
        private ClientController controller;

        /**
         * Binds the client controller to this callback instance.
         *
         * @param controller the {@link ClientController} to handle incoming server actions
         */
        public void setController(ClientController controller) {
            this.controller = controller;
        }

        /**
         * Retrieves the currently bound client controller.
         *
         * @return the {@link ClientController} instance
         */
        public ClientController getController() {
            return controller;
        }

        /**
         * Acts as a heartbeat mechanism.
         * The server calls this method periodically to check if the client is still actively connected.
         *
         * @throws RemoteException if the client is unreachable
         */
        @Override
        public void ping() throws RemoteException {
            // no-operation! It just returns instantly.
            // The server only cares that this method returns without throwing an error.
        }

        /**
         * Receives an updated snapshot of the match state from the server and forwards it to the controller.
         *
         * @param snapshot the updated {@link MatchDTO}
         * @throws RemoteException if a network error occurs during the callback
         */
        @Override
        public void updateModel(MatchDTO snapshot) throws RemoteException {
            if (controller != null) controller.onModelUpdated(snapshot); // DELEGATE TO CONTROLLER
        }

        /**
         * Receives an error message from the server to be displayed to the user.
         *
         * @param message the text of the error
         * @throws RemoteException if a network error occurs during the callback
         */
        @Override
        public void showError(String message) throws RemoteException {
            if (controller != null) controller.onServerMessage(message, true);
        }

        /**
         * Receives an informational message from the server to be displayed to the user.
         *
         * @param message the text of the message
         * @throws RemoteException if a network error occurs during the callback
         */
        @Override
        public void showMessage(String message) throws RemoteException {
            if (controller != null) controller.onServerMessage(message, false);
        }

        /**
         * Receives a prompt from the server notifying the client that it is their turn to place a totem.
         *
         * @throws RemoteException if a network error occurs during the callback
         */
        @Override
        public void askPlaceTotem() throws RemoteException {
            if (controller != null) controller.onAskPlaceTotem();
        }

        /**
         * Receives a prompt from the server notifying the client that it is their turn to resolve an offer.
         *
         * @throws RemoteException if a network error occurs during the callback
         */
        @Override
        public void askResolveOffer() throws RemoteException {
            if (controller != null) controller.onAskResolveOffer();
        }

        /**
         * Receives a prompt from the server notifying the client that they can pick an extra card.
         *
         * @throws RemoteException if a network error occurs during the callback
         */
        @Override
        public void askExtraCard() throws RemoteException {
            if (controller != null) controller.onAskExtraCardPick();
        }

        /**
         * Receives the list of currently open lobbies from the server and pushes them to the view.
         *
         * @param lobbies the list of available {@link LobbyDTO}s
         * @throws RemoteException if a network error occurs during the callback
         */
        @Override
        public void showOpenLobbies(List<LobbyDTO> lobbies) throws RemoteException {
            if (controller != null) controller.onOpenLobbiesReceived(lobbies);
        }

        /**
         * Receives the final results of the match from the server.
         *
         * @param matchRanking      the final standings of the current match
         * @param globalLeaderboard the global database leaderboard
         * @param numPlayers        the total number of players that participated in the match
         * @throws RemoteException if a network error occurs during the callback
         */
        @Override
        public void endGame(List<MatchResultDTO> matchRanking, List<MatchResultDTO> globalLeaderboard, int numPlayers) throws RemoteException {
            if (controller != null) controller.onEndGame(matchRanking, globalLeaderboard, numPlayers);
        }
    }
}