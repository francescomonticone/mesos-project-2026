package it.polimi.ingsw.Server.Network;

import it.polimi.ingsw.Network.DTO.LobbyDTO;
import it.polimi.ingsw.Network.DTO.MatchDTO;
import it.polimi.ingsw.Network.DTO.MatchResultDTO;
import it.polimi.ingsw.Network.RMI.ClientRemote;
import it.polimi.ingsw.Server.Controller.ServerManager;

import java.rmi.RemoteException;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


/**
 * Handles the connection to a single client using RMI (Remote Method Invocation).
 * <p>
 * It acts as a VirtualView by invoking methods directly on the
 * Client's remote stub, bypassing the Command Pattern used by Sockets.
 * </p>
 */
public class RMIClientHandler extends AbstractClientHandler {

    /**
     * The remote reference (Stub) that allows the Server to invoke methods
     * directly on the Client's machine.
     */
    private final ClientRemote clientStub;

    /**
     * The timer used to schedule periodic heartbeat ping tasks.
     */
    private Timer heartbeatTimer;

    /**
     * A single-thread executor used to run the ping tasks with a strict timeout,
     * preventing the server thread from hanging on unreachable RMI clients.
     */
    private java.util.concurrent.ExecutorService pingExecutor;

    /**
     * The interval in milliseconds between consecutive heartbeat pings to the client.
     */
    private static final long PING_INTERVAL_MS = 2000; //check every 2 seconds if the player is reachable

    /**
     * Constructs a new RMIClientHandler.
     *
     * @param clientStub    the remote object exposed by the connected client
     * @param serverManager the global orchestrator of the server
     * @param nickname      the nickname of the client
     */
    public RMIClientHandler(ClientRemote clientStub, ServerManager serverManager, String nickname) {
        super(serverManager);
        this.clientStub = clientStub;
        this.nickname = nickname;

        // Start the heartbeat as soon as the handler is created and the client is logged in
        startHeartbeat();
    }

    // HEARTBEAT LOGIC

    /**
     * Starts a background timer that pings the client every PING_INTERVAL_MS seconds.
     * If the ping fails, the client is assumed to be crashed (hard-killed)
     * and the disconnection process is triggered automatically.
     */
    private void startHeartbeat() {
        //clear previous thread executor
        stopHeartbeat();

        //create the executor for the timer
        pingExecutor = java.util.concurrent.Executors.newSingleThreadExecutor();
        heartbeatTimer = new Timer(true);

        heartbeatTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                //submit task to the thread pool
                java.util.concurrent.Future<?> pingFuture = pingExecutor.submit(() -> {
                    try {
                        clientStub.ping();
                    } catch (RemoteException e) {
                        throw new RuntimeException(e);
                    }
                });

                try {
                    //we wait for a maximum period of 3 second, to overcome RMI and OS scheduling delays(without this the expected time is about 9-10s because of OS limitations)
                    pingFuture.get(3, java.util.concurrent.TimeUnit.SECONDS);
                } catch (Exception e) {
                    //TimeoutException if we get over 3 seconds, ExecutionException o InterruptedException
                    System.err.println("[RMI HEARTBEAT] Timeout reached or closed network for: " + nickname);

                    pingFuture.cancel(true);

                    stopHeartbeat(); //this shuts down the timer and the executor
                    disconnect();
                }
            }
        }, PING_INTERVAL_MS, PING_INTERVAL_MS);

    }

    /**
     * Stops the heartbeat timer.
     */
    private void stopHeartbeat() {
        if (heartbeatTimer != null) {
            heartbeatTimer.cancel();
            heartbeatTimer = null;
        }
        if (pingExecutor != null && !pingExecutor.isShutdown()) { //shuts down the executor
            pingExecutor.shutdownNow();
            pingExecutor = null;
        }
    }

    //VIRTUAL VIEW METHODS (Network Specific)

    /**
     * Sends the updated game state directly to the client's remote interface.
     *
     * @param snapshot the lightweight DTO representing the current match state
     * @throws Exception if the remote call fails, indicating a lost connection
     */
    @Override
    public void sendGameUpdate(MatchDTO snapshot) throws Exception {
        try {
            //We pass the DTO directly to the client's method!
            clientStub.updateModel(snapshot);
        } catch (RemoteException e) {
            throw new Exception("RMI connection lost for client: " + nickname, e);
        }
    }

    /**
     * Sends an error message to be displayed on the client's screen.
     *
     * @param message the text of the error to display
     * @throws Exception if the remote call fails, indicating a lost connection
     */
    @Override
    public void sendError(String message) throws Exception {
        try {
            clientStub.showError(message);
        } catch (RemoteException e) {
            throw new Exception("RMI connection lost for client: " + nickname, e);
        }
    }

    /**
     * Sends a generic informational message to be displayed on the client's screen.
     *
     * @param message the text of the message to display
     * @throws Exception if the remote call fails, indicating a lost connection
     */
    @Override
    public void showMessage(String message) throws Exception {
        try {
            clientStub.showMessage(message);
        } catch (RemoteException e) {
            throw new Exception("RMI connection lost for client: " + nickname, e);
        }
    }

    /**
     * Instructs the client to prompt the player for a totem placement action.
     *
     * @throws Exception if the remote call fails, indicating a lost connection
     */
    @Override
    public void askPlaceTotem() throws Exception {
        try {
            clientStub.askPlaceTotem(); //call the remote method directly
        } catch (RemoteException e) {
            throw new Exception("RMI connection lost for client: " + nickname, e);
        }
    }

    /**
     * Instructs the client to prompt the player to resolve an offer (select cards).
     *
     * @throws Exception if the remote call fails, indicating a lost connection
     */
    @Override
    public void askResolveOffer() throws Exception {
        try {
            clientStub.askResolveOffer();
        } catch (RemoteException e) {
            throw new Exception("RMI connection lost for client: " + nickname, e);
        }
    }

    /**
     * Instructs the client to prompt the player to pick an extra card.
     *
     * @throws Exception if the remote call fails, indicating a lost connection
     */
    @Override
    public void askExtraCard() throws Exception {
        try {
            clientStub.askExtraCard();
        } catch (RemoteException e) {
            throw new Exception("RMI connection lost for client: " + nickname, e);
        }
    }


    /**
     * Sends the final match results and global leaderboard to the client at the end of the game.
     *
     * @param matchRanking      the final standings of the current match
     * @param globalLeaderboard the all-time high scores from the database
     * @param numPlayers        the total number of players in the match
     * @throws Exception if the remote call fails, indicating a lost connection
     */
    @Override
    public void sendGameOver(List<MatchResultDTO> matchRanking, List<MatchResultDTO> globalLeaderboard, int numPlayers) throws Exception {
        try {
            clientStub.endGame(matchRanking, globalLeaderboard, numPlayers);
        } catch (RemoteException e) {
            throw new Exception("RMI connection lost for client: " + nickname, e);
        }

    }

    /**
     * Pushes the list of open lobbies directly to the client's remote interface.
     * <p>
     * This implementation uses direct RMI method invocation on the client stub,
     * maintaining the synchronous-like feel of the RMI protocol.
     * </p>
     *
     * @param lobbies the list of {@link LobbyDTO} objects to display.
     * @throws Exception if the remote call fails due to connection issues.
     */
    @Override
    public void showOpenLobbies(List<LobbyDTO> lobbies) throws Exception {
        try {
            // Direct call to the client!
            clientStub.showOpenLobbies(lobbies);
        } catch (RemoteException e) {
            // If the call fails, we wrap it in a generic Exception to notify the logic layer
            throw new Exception("RMI connection lost while sending lobby list to: " + nickname, e);
        }
    }

    //DISCONNECTION LOGIC

    /**
     * Forcibly disconnects this RMI client, stops the heartbeat monitoring,
     * and notifies the server manager to handle game-side cleanup.
     */
    @Override
    public void disconnect() {
        stopHeartbeat(); //stop thread-executor, very important

        System.out.println("Disconnecting RMI client: " + (nickname != null ? nickname : "Unknown"));

        // Notify the ServerManager to handle game-side cleanup (inherited from abstract class)
        if (serverManager != null && nickname != null) {
            serverManager.handleDisconnection(nickname);
        }
    }
}