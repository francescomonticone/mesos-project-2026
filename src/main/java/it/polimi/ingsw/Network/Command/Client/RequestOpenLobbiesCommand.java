package it.polimi.ingsw.Network.Command.Client;

import it.polimi.ingsw.Server.Network.ClientHandler;
import java.io.Serial;

/**
 * Client-to-Server command used to request the list of currently available lobbies.
 * <p>
 * When executed on the server, it triggers the retrieval of all open matches
 * and sends back a {@code ShowOpenLobbiesCommand} containing the lobby data.
 * </p>
 */
public class RequestOpenLobbiesCommand implements ClientCommand {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Executes the command on the server side using double dispatch.
     *
     * @param handler the server-side handler for the specific client connection.
     */
    @Override
    public void execute(ClientHandler handler) {
        // Double dispatch: the handler knows how to process this specific request.
        handler.handleRequestOpenLobbies();
    }
}