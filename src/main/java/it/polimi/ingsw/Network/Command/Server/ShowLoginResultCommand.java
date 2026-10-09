package it.polimi.ingsw.Network.Command.Server;

import it.polimi.ingsw.Client.ClientController.ClientController;

import java.io.Serial;

//Command Server -> Client

/**
 * A network command sent from the server to the client, indicating the result of a login
 * or registration attempt.
 * <p>
 * Following the Command Pattern, this class represents a server-initiated response to
 * the client's authentication request. It encapsulates a boolean flag indicating success
 * or failure, along with the approved nickname. Upon reception, the client executes this
 * command to update its internal state and transition the user interface accordingly
 * (e.g., advancing to the lobby menu or showing an error).
 * </p>
 */
public class ShowLoginResultCommand implements ServerCommand {

    @Serial
    private static final long serialVersionUID = 1L;
    private final boolean success;
    private final String nickname;

    /**
     * Constructs a new command containing the login outcome.
     *
     * @param success  {@code true} if the login or registration was accepted by the server, {@code false} otherwise
     * @param nickname the approved nickname associated with the successful login, or the attempted nickname if it failed
     */
    public ShowLoginResultCommand(boolean success, String nickname) {
        this.success = success;
        this.nickname = nickname;
    }

    /**
     * Executes the command on the client side.
     * <p>
     * This method is invoked by the client's network listener once the command object
     * is fully received from the server. If the login was successful, it first updates
     * the {@link ClientController} with the confirmed nickname. Then, it triggers the
     * UI callback to process the login result.
     * </p>
     *
     * @param controller the main client-side controller responsible for managing the game state and UI
     */
    @Override
    public void executeOnClient(ClientController controller) {

        if (success) {
           controller.setMyNickname(nickname);
        }
        controller.onLoginResult(success, nickname);
    }
}