package it.polimi.ingsw.Network.Command.Client;

import it.polimi.ingsw.Server.Network.ClientHandler;

import java.io.Serial;

//Command Client -> Server

/**
 * Command used by the client to request a connection to the lobby
 * with a specific nickname.
 */
public class LoginCommand implements ClientCommand {
    @Serial
    private static final long serialVersionUID = 1L;

    private final String nickname;
    private final String passwordHash;

    /**
     * Constructs the login command.
     *
     * @param nickname the chosen nickname for the player
     */
    public LoginCommand(String nickname, String passwordHash) {
        this.nickname = nickname;
        this.passwordHash = passwordHash;
    }

    /**
     * Delegates the socket login request to the client handler.
     */
    @Override
    public void execute(ClientHandler handler) {
        // The handler is responsible for routing this to the LobbyController
        handler.handleSocketLogin(nickname, passwordHash);
    }
}