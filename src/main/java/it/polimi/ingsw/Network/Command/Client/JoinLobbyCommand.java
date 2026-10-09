package it.polimi.ingsw.Network.Command.Client;

import it.polimi.ingsw.Server.Network.ClientHandler;

import java.io.Serial;

/**
 * Command sent by the client to attempt joining an existing open lobby.
 */
public class JoinLobbyCommand implements ClientCommand {

    @Serial
    private static final long serialVersionUID = 1L;

    private final int lobbyId;

    /**
     * Constructs the command.
     *
     * @param lobbyId the unique identifier of the target lobby
     */
    public JoinLobbyCommand(int lobbyId) {
        this.lobbyId = lobbyId;
    }

    /**
     * Executes the request on the server side.
     *
     * @param handler the server-side handler managing this client's connection
     */
    @Override
    public void execute(ClientHandler handler) {
        handler.handleJoinLobby(lobbyId);
    }
}