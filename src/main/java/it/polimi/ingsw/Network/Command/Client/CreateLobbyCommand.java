package it.polimi.ingsw.Network.Command.Client;

import it.polimi.ingsw.Server.Network.ClientHandler;

import java.io.Serial;

/**
 * Command sent by the client to request the creation of a new Lobby.
 * It carries the desired size of the match (e.g., 2 to 5 players).
 */
public class CreateLobbyCommand implements ClientCommand {

    @Serial
    private static final long serialVersionUID = 1L;

    private final int targetSize;

    /**
     * Constructs the command.
     *
     * @param targetSize the number of players required to start the match
     */
    public CreateLobbyCommand(int targetSize) {
        this.targetSize = targetSize;
    }

    /**
     * Executes the request on the server side.
     *
     * @param handler the server-side handler managing this client's connection
     */
    @Override
    public void execute(ClientHandler handler) {
        handler.handleCreateLobby(targetSize);
    }
}