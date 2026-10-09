package it.polimi.ingsw.Network.Command.Server;

import it.polimi.ingsw.Client.ClientController.ClientController;

import java.io.Serial;

//Command Server -> Client

/**
 * A network command sent from the server to the client, instructing it to display a generic informational message.
 * <p>
 * Following the Command Pattern, this class represents a server-initiated notification.
 * It encapsulates standard text information (e.g., game phase updates, lobby status,
 * or turn notifications) and is transmitted over the network. Upon reception, the client
 * executes this command to display the information to the player via the user interface.
 * </p>
 */
public class ShowMessageCommand implements ServerCommand {

    @Serial
    private static final long serialVersionUID = 1L;
    private final String message;

    /**
     * Constructs a new command to display a generic message.
     *
     * @param message the string containing the information to be shown
     */
    public ShowMessageCommand(String message) {
        this.message = message;
    }

    /**
     * Executes the command on the client side.
     * <p>
     * This method is invoked by the client's network listener once the command object
     * is fully received from the server. It delegates the UI updates to the
     * {@link ClientController}, passing the message and flagging it to be displayed
     * as a standard notification (as opposed to an error alert).
     * </p>
     *
     * @param controller the main client-side controller responsible for managing the game state and UI
     */
    @Override
    public void executeOnClient(ClientController controller) {
        controller.onServerMessage(message, false);
    }
}