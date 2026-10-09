package it.polimi.ingsw.Network.Command.Server;

import it.polimi.ingsw.Client.ClientController.ClientController;

import java.io.Serial;

/**
 * A network command sent from the server to the client, instructing them to place their initial totem.
 * <p>
 * Following the Command Pattern, this class represents a server-initiated request.
 * It is transmitted over the network to the active client. Upon reception, the client
 * executes this command to prompt the player via the user interface, requiring them
 * to select a valid starting tile on the board for their totem.
 * </p>
 */
public class AskPlaceTotemCommand implements ServerCommand {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Executes the command on the client side.
     * <p>
     * This method is invoked by the client's network listener once the command object
     * is fully received from the server. It delegates the UI updates and user prompting
     * to the {@link ClientController}, which handles the specific logic for the
     * totem placement phase.
     * </p>
     *
     * @param controller the main client-side controller responsible for managing the game state and UI
     */
    @Override
    public void executeOnClient(ClientController controller) {
        controller.onAskPlaceTotem();
    }
}