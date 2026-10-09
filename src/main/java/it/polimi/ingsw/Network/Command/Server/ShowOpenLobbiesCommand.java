package it.polimi.ingsw.Network.Command.Server;

import it.polimi.ingsw.Client.ClientController.ClientController;
import it.polimi.ingsw.Network.DTO.LobbyDTO;

import java.io.Serial;
import java.util.List;


//Command Server -> Client


/**
 * A network command sent from the server to the client, carrying the current list of available lobbies.
 * <p>
 * Following the Command Pattern, this class represents a server-initiated data update.
 * It encapsulates a collection of lightweight Data Transfer Objects ({@link LobbyDTO})
 * representing game rooms that are currently open and waiting for players. Upon reception,
 * the client executes this command to refresh the lobby browser in the user interface.
 * </p>
 */
public class ShowOpenLobbiesCommand implements ServerCommand {
    @Serial
    private static final long serialVersionUID = 1L;
    private final List<LobbyDTO> lobbies;

    /**
     * Constructs a new command containing the updated list of open lobbies.
     *
     * @param lobbies a list of {@link LobbyDTO} objects containing details (like ID, host, and size) of available lobbies
     */
    public ShowOpenLobbiesCommand(List<LobbyDTO> lobbies) {
        this.lobbies = lobbies;
    }

    /**
     * Executes the command on the client side.
     * <p>
     * This method is invoked by the client's network listener once the command object
     * is fully received from the server. It delegates the processing of the lobby list
     * to the {@link ClientController}, which triggers the UI update so the player can
     * see the most recent multiplayer rooms.
     * </p>
     *
     * @param controller the main client-side controller responsible for managing the game state and UI
     */
    @Override
    public void executeOnClient(ClientController controller) {
        controller.onOpenLobbiesReceived(lobbies); //triggers the UI update on the client
    }
}