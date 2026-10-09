package it.polimi.ingsw.Client.Network;

import it.polimi.ingsw.Client.ClientController.ClientController;
import it.polimi.ingsw.Network.Command.Client.*;
import it.polimi.ingsw.Network.Command.Server.ServerCommand;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;

/**
 * Socket implementation of {@link VirtualServer}.
 * <p>
 * Each method wraps its arguments in the appropriate {@link ClientCommand}
 * and serializes it to the server via {@link ObjectOutputStream}.
 * The server's {@code SocketClientHandler} deserializes it and calls
 * {@code command.execute(this)}.
 * </p>
 */
public class SocketVirtualServer implements VirtualServer {

    private final Socket socket;
    private final ObjectOutputStream out;
    private final ObjectInputStream in;

    /**
     * Constructs a new SocketVirtualServer and establishes a socket connection to the server.
     * <p>
     * Initializes the input and output streams. The {@link ObjectOutputStream} is deliberately
     * created and flushed before the {@link ObjectInputStream} to prevent a deadlock during
     * the initial socket handshake.
     * </p>
     *
     * @param host the IP address or hostname of the server
     * @param port the port number the server is listening on
     * @throws IOException if an I/O error occurs when creating the socket or streams
     */
    public SocketVirtualServer(String host, int port) throws IOException {
        socket = new Socket(host, port); //creating the socket (ip,port)

        // Important: ObjectOutputStream before ObjectInputStream
        out = new ObjectOutputStream(socket.getOutputStream());
        out.flush();
        in = new ObjectInputStream(socket.getInputStream());
    }

    /**
     * Sends a login request to the server by serializing a {@link LoginCommand}.
     *
     * @param nickname     the chosen username
     * @param passwordHash the hashed password for authentication
     * @throws Exception if an I/O error occurs while sending the command
     */
    @Override
    public void login(String nickname, String passwordHash) throws Exception {
        send(new LoginCommand(nickname, passwordHash));
    }

    /**
     * Requests the server to create a new match lobby by serializing a {@link CreateLobbyCommand}.
     *
     * @param targetSize the desired number of players for the match (e.g., 2 to 5)
     * @throws Exception if an I/O error occurs while sending the command
     */
    @Override
    public void createLobby(int targetSize) throws Exception {
        send(new CreateLobbyCommand(targetSize));
    }

    /**
     * Requests to join an existing lobby by serializing a {@link JoinLobbyCommand}.
     *
     * @param lobbyId the unique identifier of the target lobby
     * @throws Exception if an I/O error occurs while sending the command
     */
    @Override
    public void joinLobby(int lobbyId) throws Exception {
        send(new JoinLobbyCommand(lobbyId));
    }

    /**
     * Requests the current list of open lobbies from the server by serializing a {@link RequestOpenLobbiesCommand}.
     *
     * @throws Exception if an I/O error occurs while sending the command
     */
    @Override
    public void requestOpenLobbies() throws Exception {
        send(new RequestOpenLobbiesCommand());
    }

    //COMMANDS FOR ACTIONS
    /**
     * Sends the player's choice for totem placement to the server via a {@link PlaceTotemCommand}.
     *
     * @param tileId the character identifier of the chosen offer tile (e.g., 'A', 'B')
     * @throws Exception if an I/O error occurs while sending the command
     */
    @Override
    public void placeTotem(char tileId) throws Exception {
        send(new PlaceTotemCommand(tileId));
    }

    /**
     * Sends the player's chosen cards to the server via a {@link ResolveOfferTileCommand}.
     *
     * @param upCards      the list of IDs for cards picked from the upper row
     * @param downCards    the list of IDs for cards picked from the lower row
     * @param orderedCards the raw list of requested cards in the exact order they were chosen
     * @throws Exception if an I/O error occurs while sending the command
     */
    @Override
    public void resolveOffer(List<String> upCards, List<String> downCards, List<String> orderedCards) throws Exception {
        send(new ResolveOfferTileCommand(upCards, downCards, orderedCards));
    }

    /**
     * Requests to pick an extra card from the board via an {@link ExtraCardPickCommand}.
     *
     * @param cardId     the ID of the extra card to pick
     * @param isUpperRow true if the card is located in the upper row, false otherwise
     * @throws Exception if an I/O error occurs while sending the command
     */
    @Override
    public void pickExtraCard(String cardId, boolean isUpperRow) throws Exception {
        send(new ExtraCardPickCommand(cardId, isUpperRow));
    }

    /**
     * Notifies the server that the player explicitly chooses to skip their extra card pick
     * via a {@link SkipExtraCardPickCommand}.
     *
     * @throws Exception if an I/O error occurs while sending the command
     */
    @Override
    public void skipExtraCard() throws Exception {
        send(new SkipExtraCardPickCommand());
    }

    /**
     * Helper method to safely send a command over the socket stream.
     * <p>
     * This method is synchronized to prevent overlapping writes from different threads.
     * It strictly calls {@code out.reset()} before writing to prevent the stream from
     * caching and sending stale object references.
     * </p>
     *
     * @param command the {@link ClientCommand} to serialize and send
     * @throws IOException if an I/O error occurs while writing to the stream
     */
    private synchronized void send(ClientCommand command) throws IOException {
        out.reset();
        out.writeObject(command);
        out.flush();
    }

    /**
     * Starts a dedicated background thread to continuously listen for incoming
     * {@link ServerCommand}s from the socket stream.
     * <p>
     * When a command is received, it is immediately executed on the provided controller.
     * If the connection drops or an exception occurs, it safely notifies the view.
     * Note: this method activates the specific network listeners.
     * For Socket: Starts the background reading thread.
     * For RMI: Links the controller to the exported callback object.
     * </p>
     *
     * @param controller the main {@link ClientController} that handles incoming server actions
     */
    //listening loop with a new thread
    @Override
    public void setControllerRMILoopSocket(ClientController controller) {
        new Thread(() -> {
            try {
                while (!socket.isClosed()) {
                    ServerCommand serverCommand = (ServerCommand) in.readObject();
                    serverCommand.executeOnClient(controller); //controller will handle the command
                }
            } catch (Exception e) {
                controller.getView().showError("Server connection lost.");
            }
        }).start();
    }

}