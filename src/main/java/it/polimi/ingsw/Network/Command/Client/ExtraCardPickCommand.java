package it.polimi.ingsw.Network.Command.Client;

import it.polimi.ingsw.Server.Network.ClientHandler;

import java.io.Serial;

/**
 * A network command representing a client's request to pick an extra card.
 * <p>
 * This class follows the Command Pattern. It encapsulates the data required for the
 * action (the specific card and its location row) and is sent over the network from
 * the client to the server. Upon reception, the server executes this command to
 * trigger the corresponding game logic.
 * </p>
 */
public class ExtraCardPickCommand implements ClientCommand {
    @Serial
    private static final long serialVersionUID = 1L;

    private final String cardId;
    private final boolean isUpperRow;

    /**
     * Constructs a new command to pick an extra card.
     *
     * @param cardId     the unique identifier of the card the player wants to pick
     * @param isUpperRow {@code true} if the card is located in the upper row,
     * {@code false} if it is in the lower row
     */
    public ExtraCardPickCommand(String cardId, boolean isUpperRow) {
        this.cardId = cardId;
        this.isUpperRow = isUpperRow;
    }

    /**
     * Executes the command on the server side.
     * <p>
     * This method is called by the server's network listener once the command object
     * is received. It delegates the processing of the extra card pick to the
     * specific {@link ClientHandler} managing this client's connection.
     * </p>
     *
     * @param handler the server-side handler managing the client's network session
     */
    @Override
    public void execute(ClientHandler handler) {
        handler.handleExtraCardPick(cardId, isUpperRow);
    }
}
