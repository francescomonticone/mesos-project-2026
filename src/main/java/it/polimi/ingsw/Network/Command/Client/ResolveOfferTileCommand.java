package it.polimi.ingsw.Network.Command.Client;

import it.polimi.ingsw.Server.Network.ClientHandler;
import java.io.Serial;
import java.util.List;

/**
 * A network command representing a client's request to resolve the offer tile phase.
 * <p>
 * Following the Command Pattern, this class encapsulates the complex data required
 * to finalize a player's choice during an offer phase (e.g., separating cards into
 * face-up and face-down piles, and determining their final sequence). It is transmitted
 * over the network from the client to the server. Upon reception, the server executes
 * this command to apply the chosen card distribution within the game state.
 * </p>
 */
public class ResolveOfferTileCommand implements ClientCommand {

    @Serial
    private static final long serialVersionUID = 1L;

    private final List<String> upCards;
    private final List<String> downCards;

    /** The list of card identifiers representing the final ordered sequence chosen by the player. */
    private final List<String> orderedIds;

    /**
     * Constructs a new command to resolve the offer phase.
     *
     * @param upCards    the list of IDs for the cards placed facing up
     * @param downCards  the list of IDs for the cards placed facing down
     * @param orderedIds the final ordered sequence of card IDs chosen by the player
     */
    public ResolveOfferTileCommand(List<String> upCards, List<String> downCards,  List<String> orderedIds) {
        this.upCards = upCards;
        this.downCards = downCards;
        this.orderedIds = orderedIds;
    }

    /**
     * Executes the command on the server side.
     * <p>
     * This method is invoked by the server's network listener once the command object
     * is fully received. It delegates the complex processing of the offer resolution
     * to the specific {@link ClientHandler} managing this client's network connection.
     * </p>
     *
     * @param handler the server-side handler managing the client's network session
     */
    @Override
    public void execute(ClientHandler handler) {
        handler.handleResolveOffer(upCards, downCards, orderedIds);
    }
}