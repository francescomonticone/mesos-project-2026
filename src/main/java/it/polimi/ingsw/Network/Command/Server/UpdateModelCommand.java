package it.polimi.ingsw.Network.Command.Server;

import it.polimi.ingsw.Client.ClientController.ClientController;
import it.polimi.ingsw.Network.DTO.MatchDTO;

import java.io.Serial;

//Command Server -> Client

/**
 * A network command sent from the server to the client, providing the most recent state of the game.
 * <p>
 * Following the Command Pattern, this class represents a server-initiated data update.
 * It encapsulates a Data Transfer Object ({@link MatchDTO}) that acts as a complete snapshot
 * of the current match, including the board, decks, and player statistics. Upon reception,
 * the client executes this command to synchronize its local model and refresh the graphical
 * or command-line user interface.
 * </p>
 */
public class UpdateModelCommand implements ServerCommand {

    @Serial
    private static final long serialVersionUID = 1L;
    private final MatchDTO snapshot;

    /**
     * Constructs a new command containing the latest match snapshot.
     *
     * @param snapshot the {@link MatchDTO} representing the current state of the game
     */
    public UpdateModelCommand(MatchDTO snapshot) {
        this.snapshot = snapshot;
    }

    /**
     * Executes the command on the client side.
     * <p>
     * This method is invoked by the client's network listener once the command object
     * is fully received from the server. It delegates the processing of the state update
     * to the {@link ClientController}, which ensures the user interface accurately
     * reflects the latest moves, scores, and board state.
     * </p>
     *
     * @param controller the main client-side controller responsible for managing the game state and UI
     */
    @Override
    public void executeOnClient(ClientController controller) {
        controller.onModelUpdated(snapshot);
    }
}