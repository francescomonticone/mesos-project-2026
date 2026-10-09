package it.polimi.ingsw.Server.Network;

import it.polimi.ingsw.Network.Command.Client.ClientCommand;
import it.polimi.ingsw.Network.Command.Server.*;
import it.polimi.ingsw.Network.DTO.LobbyDTO;
import it.polimi.ingsw.Network.DTO.MatchDTO;
import it.polimi.ingsw.Network.DTO.MatchResultDTO;
import it.polimi.ingsw.Server.Controller.ServerManager;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;

/**
 * Handles the Socket connection for a single client.
 * <p>
 * This class inherits common command routing logic from {@link AbstractClientHandler}.
 * It acts as a VirtualView by sending serialized commands over the network stream,
 * and it acts as a listener by continuously reading incoming ClientCommands in a
 * dedicated thread.
 * </p>
 */
public class SocketClientHandler extends AbstractClientHandler implements Runnable {

    private final Socket socket;
    private final ObjectOutputStream out;
    private final ObjectInputStream in;

    /**
     * Constructs a new SocketClientHandler for the accepted socket.
     *
     * @param socket        the socket connected to the client
     * @param serverManager the global orchestrator of the server
     * @throws IOException if an I/O error occurs when creating the output stream
     */
    public SocketClientHandler(Socket socket, ServerManager serverManager) throws IOException {
        super(serverManager); // Pass the manager to the abstract class

        this.socket = socket;

        // We must ALWAYS create the ObjectOutputStream before the ObjectInputStream.
        // Otherwise, the two ends of the socket will deadlock waiting for headers.
        this.out = new ObjectOutputStream(socket.getOutputStream());
        this.in = new ObjectInputStream(socket.getInputStream());
    }

    // VIRTUAL VIEW METHODS (Network Specific, sending to client)

    /**
     * Sends the updated game state to the client.
     * Wraps the DTO in an {@link UpdateModelCommand} and sends it over the socket.
     *
     * @param snapshot the lightweight DTO representing the current match state
     * @throws Exception if a network error occurs, indicating a lost connection
     */
    @Override
    public void sendGameUpdate(MatchDTO snapshot) throws Exception {
        sendCommand(new UpdateModelCommand(snapshot));
    }

    /**
     * Sends an error message to be displayed on the client's screen.
     * Wraps the message in a {@link ShowErrorCommand} and sends it over the socket.
     *
     * @param message the text of the error to display
     * @throws Exception if a network error occurs, indicating a lost connection
     */
    @Override
    public void sendError(String message) throws Exception {
        sendCommand(new ShowErrorCommand(message));
    }

    /**
     * Sends a generic informational message to be displayed on the client's screen.
     * Wraps the message in a {@link ShowMessageCommand} and sends it over the socket.
     *
     * @param message the text of the message to display
     * @throws Exception if a network error occurs, indicating a lost connection
     */
    @Override
    public void showMessage(String message) throws Exception {
        sendCommand(new ShowMessageCommand(message));
    }

    /**
     * Instructs the client to prompt the player for a totem placement action.
     * Wraps the request in an {@link AskPlaceTotemCommand} and sends it over the socket.
     *
     * @throws Exception if a network error occurs, indicating a lost connection
     */
    @Override
    public void askPlaceTotem() throws Exception {
        sendCommand(new AskPlaceTotemCommand());
    }
    /**
     * Instructs the client to prompt the player to resolve an offer (select cards).
     * Wraps the request in an {@link AskResolveOfferCommand} and sends it over the socket.
     *
     * @throws Exception if a network error occurs, indicating a lost connection
     */
    @Override
    public void askResolveOffer() throws Exception {
        sendCommand(new AskResolveOfferCommand());
    }
    /**
     * Instructs the client to prompt the player to pick an extra card.
     * Wraps the request in an {@link AskExtraCardPickCommand} and sends it over the socket.
     *
     * @throws Exception if a network error occurs, indicating a lost connection
     */
    @Override
    public void askExtraCard() throws Exception {
        sendCommand(new AskExtraCardPickCommand());
    }

    /**
     * Sends the final match results and global leaderboard to the client at the end of the game.
     * Wraps the data in an {@link EndGameCommand} and sends it over the socket.
     *
     * @param matchRanking      the final standings of the current match
     * @param globalLeaderboard the all-time high scores from the database
     * @param numPlayers        the total number of players in the match
     * @throws Exception if a network error occurs, indicating a lost connection
     */
    @Override
    public void sendGameOver(List<MatchResultDTO> matchRanking, List<MatchResultDTO> globalLeaderboard, int numPlayers) throws Exception {
        sendCommand(new EndGameCommand(matchRanking,globalLeaderboard, numPlayers));
    }

    /**
     * Sends the list of open lobbies to the client.
     * This fulfills the VirtualView interface requirement for Sockets.
     *
     * @param lobbies the list of lobby DTOs to display
     * @throws Exception if a network error occurs
     */
    @Override
    public void showOpenLobbies(List<LobbyDTO> lobbies) throws Exception {
        sendCommand(new ShowOpenLobbiesCommand(lobbies));
    }

    //only for socket method (RMI will use boolean directly)

    /**
     * Sends the result of a login attempt to the client.
     * <p>
     * This method is specific to Socket connections, as RMI handles login synchronously
     * via method return values. It wraps the boolean result in a {@link ShowLoginResultCommand}.
     * </p>
     *
     * @param success true if authentication was successful, false otherwise
     * @throws Exception if a network error occurs, indicating a lost connection
     */
    public void showLoginResult(boolean success) throws Exception {
        sendCommand(new ShowLoginResultCommand(success, nickname));
    }

    /**
     * Helper method to send a command safely over the network stream.
     * <p>
     * It uses out.reset() to prevent Java from caching objects (like MatchDTO).
     * This ensures the client always receives the most updated state, not a stale reference.
     * </p>
     *
     * @param command the {@link ServerCommand} to be serialized and sent to the client
     * @throws Exception if an I/O error occurs during writing or flushing the stream
     */
    private synchronized void sendCommand(ServerCommand command) throws Exception {
        try {
            out.writeObject(command);
            out.reset(); // Clears the stream cache
            out.flush(); // Forces data over the network immediately
        } catch (IOException e) {
            disconnect();
            throw new Exception("Socket connection lost for client: " + getNickname(), e);
        }
    }

    //LISTENING LOOP (RECEIVING DATA FROM CLIENT)

    /**
     * The listening loop that runs in a dedicated thread.
     * It waits for objects from the client and processes them.
     */
    @Override
    public void run() {
        try {
            while (!socket.isClosed()) {
                // Read an object from the input stream and cast it to a generic ClientCommand
                ClientCommand command = (ClientCommand) in.readObject();

                // Execute the command using Double Dispatch.
                // It will call methods defined in AbstractClientHandler.
                command.execute(this);
            }
        } catch (IOException | ClassNotFoundException | ClassCastException e) {
            System.out.println("Client " + (getNickname() != null ? getNickname() : "Unknown") + " disconnected.");
            disconnect();
        }
    }

    /**
     * Processes a login request specifically for a Socket client.
     * <p>
     * It delegates the authentication logic to the {@link ServerManager}. If successful,
     * it registers the nickname internally. Finally, it attempts to send the result back
     * to the client, disconnecting them if the network fails during transmission.
     * </p>
     *
     * @param requestedNickname     the nickname the client is attempting to register
     * @param requestedPasswordHash the hashed password provided by the client
     */
    public void handleSocketLogin(String requestedNickname, String requestedPasswordHash) {
        boolean success = serverManager.handleLogin(requestedNickname, requestedPasswordHash, this);
        if (success) {
            this.nickname = requestedNickname; //nickname saved in AbstractClientHandler, but it is protected so this class can see it
        }
        try {
            showLoginResult(success); // Abstract method
        } catch (Exception e) {
            disconnect(); // Abstract method
        }
    }

    /**
     * Concrete implementation for Socket clients.
     * It retrieves the lobby list from the manager, wraps it in a command, and sends it back.
     */
    @Override
    public void handleRequestOpenLobbies() {
        // get the list of DTOs from the server manager
        List<LobbyDTO> availableLobbies = serverManager.getAvailableLobbiesDTO();
        try {
            showOpenLobbies(availableLobbies);
            System.out.println("[Socket] Sent " + availableLobbies.size() + " lobbies to " + getNickname());
        } catch (Exception e) {
            System.err.println("[Socket] Error sending lobby list: " + e.getMessage());
            disconnect();
        }
    }

    //DISCONNECTION LOGIC

    /**
     * Safely closes the socket and notifies the server manager.
     */
    @Override
    public synchronized void disconnect() {
        if (socket.isClosed()) return; //if socket is already closed no operation

        System.out.println("Disconnecting Socket client: " + (getNickname() != null ? getNickname() : "Unknown"));

        // Notify the logic layer first (inherited from AbstractClientHandler)
        if (serverManager != null && getNickname() != null) {
            serverManager.handleDisconnection(getNickname());
        }

        // Then close the hardware connection
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            System.err.println("Error while closing socket for " + getNickname());
        }
    }
}