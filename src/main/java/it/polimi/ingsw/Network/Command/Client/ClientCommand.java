package it.polimi.ingsw.Network.Command.Client;

import it.polimi.ingsw.Server.Network.ClientHandler;

import java.io.Serializable;

/**
 * Interface for commands sent from the Client to the Server.
 * <p>
 * Utilizes the Command Pattern to encapsulate player actions. The server receives
 * these objects and executes them, passing the specific client's handler as context.
 * </p>
 */
public interface ClientCommand extends Serializable {

    /**
     * Executes the requested action on the server.
     *
     * @param handler the network handler (Socket) representing the client
     *                who sent this command. Provides access to the ServerManager.
     */
    void execute(ClientHandler handler);
}