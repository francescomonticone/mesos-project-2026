package it.polimi.ingsw.Network.Command.Server;

import it.polimi.ingsw.Client.ClientController.ClientController;

import java.io.Serializable;

/**
 * Interface for commands sent from the Server to the Client.
 * Implements the Command Pattern to avoid instanceof checks on the client side.
 */
public interface ServerCommand extends Serializable {

    /**
     * Executes the command on the client's view.
     *
     * @param controller the client's controller to interact with the view and update client state as needed
     */
    void executeOnClient(ClientController controller);
}