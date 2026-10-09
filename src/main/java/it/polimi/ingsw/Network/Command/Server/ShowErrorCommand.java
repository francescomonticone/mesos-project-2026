package it.polimi.ingsw.Network.Command.Server;

import it.polimi.ingsw.Client.ClientController.ClientController;

import java.io.Serial;

//Command Server -> Client

/**
 * A network command sent from the server to the client, instructing it to display an error message.
 * <p>
 * Following the Command Pattern, this class represents a server-initiated notification.
 * It encapsulates the specific text of the error (e.g., an invalid move, a disconnected
 * opponent, or a server issue) and is transmitted over the network. Upon reception,
 * the client executes this command to alert the player via the user interface.
 * </p>
 */
public class ShowErrorCommand implements ServerCommand {

    @Serial
    private static final long serialVersionUID = 1L;
    private final String message;

    /**
     * Constructs a new command to display an error message.
     *
     * @param message the string containing the details of the error or issue
     */
    public ShowErrorCommand(String message) {
        this.message = message;
    }

    /**
     * Executes the command on the client side.
     * <p>
     * This method is invoked by the client's network listener once the command object
     * is fully received from the server. It delegates the UI updates to the
     * {@link ClientController}, passing the error message and flagging it to be
     * styled or displayed specifically as an alert or error popup.
     * </p>
     *
     * @param controller the main client-side controller responsible for managing the game state and UI
     */
    @Override
    public void executeOnClient(ClientController controller) {
        controller.onServerMessage(message, true);
    }
}