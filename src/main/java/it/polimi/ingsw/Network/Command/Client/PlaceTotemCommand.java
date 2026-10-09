package it.polimi.ingsw.Network.Command.Client;

import it.polimi.ingsw.Server.Network.ClientHandler;
import java.io.Serial;

/**
 * A network command representing a client's request to place their initial totem on the board.
 * <p>
 * Following the Command Pattern, this class encapsulates the data required for the
 * action (the specific tile identifier) and is transmitted over the network from
 * the client to the server. Upon reception, the server executes this command to
 * process the placement logic within the game state.
 * </p>
 */
public class PlaceTotemCommand implements ClientCommand {

    @Serial
    private static final long serialVersionUID = 1L;

    private final char tileId;

    /**
     * Constructs a new command to place the totem.
     *
     * @param tileId the character identifier representing the specific board tile chosen by the player
     */
    public PlaceTotemCommand(char tileId) {
        this.tileId = tileId;
    }

    /**
     * Executes the command on the server side.
     * <p>
     * This method is invoked by the server's network listener once the command object
     * is fully received. It delegates the processing of the totem placement to the
     * specific {@link ClientHandler} managing this client's network session.
     * </p>
     *
     * @param handler the server-side handler managing the client's network connection
     */
    @Override
    public void execute(ClientHandler handler) {
        handler.handlePlaceTotem(tileId);
    }
}