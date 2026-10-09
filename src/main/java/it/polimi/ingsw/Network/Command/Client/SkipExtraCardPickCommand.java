package it.polimi.ingsw.Network.Command.Client;

import it.polimi.ingsw.Server.Network.ClientHandler;
import java.io.Serial;

/**
 * A network command representing a client's decision to skip the extra card picking phase.
 * <p>
 * Following the Command Pattern, this class encapsulates the action of voluntarily
 * passing on an optional extra draw. It is transmitted over the network from the
 * client to the server. Upon reception, the server executes this command to bypass
 * the picking logic and advance the game state.
 * </p>
 */
public class SkipExtraCardPickCommand implements ClientCommand {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Executes the command on the server side.
     * <p>
     * This method is invoked by the server's network listener once the command object
     * is received. It delegates the action of skipping the extra card pick to the
     * specific {@link ClientHandler} managing this client's network connection.
     * </p>
     *
     * @param handler the server-side handler managing the client's network session
     */
    @Override
    public void execute(ClientHandler handler) {
        handler.handleSkipExtra();
    }
}