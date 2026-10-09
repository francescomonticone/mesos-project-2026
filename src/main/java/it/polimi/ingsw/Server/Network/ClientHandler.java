package it.polimi.ingsw.Server.Network;

import java.util.List;

/**
 * Universal interface representing a connected client on the server.
 * <p>
 * This interface abstracts the underlying network protocol (Socket or RMI),
 * allowing the server logic to handle all clients uniformly regardless of how
 * they are physically connected.
 * </p>
 */
public interface ClientHandler {

    /**
     * Retrieves the nickname of the client associated with this handler.
     *
     * @return the client's registered nickname, or {@code null} if the client has not yet logged in
     */
    String getNickname();

    /**
     * Handles the login process specifically for Socket connections.
     * <p>
     * By default, this method does nothing so that RMI connections (which handle
     * login synchronously via direct method invocation) can safely ignore it.
     * </p>
     *
     * @param nickname     the requested username
     * @param passwordHash the hashed password for authentication
     */
    default void handleSocketLogin(String nickname, String passwordHash) {
        // Default: no-operation, only socket will override this
    }

    // LOBBY

    /**
     * Handles a request from the client to retrieve all currently open lobbies.
     * <p>
     * The default implementation does nothing, as RMI clients receive this data
     * via direct method returns. Socket handlers will override this to serialize
     * and send the lobby data back over the network.
     * </p>
     */
    default void handleRequestOpenLobbies() {
        // Default: no-operation, only socket will override this
    }

    /**
     * Processes a client's request to create a new match lobby.
     *
     * @param targetSize the desired number of players for the new lobby
     */
    void handleCreateLobby(int targetSize);

    /**
     * Processes a client's request to join an existing match lobby.
     *
     * @param lobbyId the unique identifier of the lobby the player wants to join
     */
    void handleJoinLobby(int lobbyId);

    // MATCH ACTIONS
    /**
     * Processes a client's request to place their totem on a specific board tile.
     *
     * @param tileId the identifier of the tile where the player wants to place the totem
     */
    void handlePlaceTotem(char tileId);

    /**
     * Processes a client's request to resolve an offer (selecting cards from the board).
     *
     * @param upCards    the list of selected face-up card IDs
     * @param downCards  the list of selected face-down card IDs
     * @param orderedIds the ordered list of card IDs representing the player's final arrangement on their board
     */
    void handleResolveOffer(List<String> upCards, List<String> downCards,  List<String> orderedIds);

    /**
     * Processes a client's request to pick an extra card during a special game phase.
     *
     * @param cardId     the ID of the extra card chosen
     * @param isUpperRow a boolean flag indicating if the card is drawn from the upper row (true) or lower row (false)
     */
    void handleExtraCardPick(String cardId, boolean isUpperRow);

    /**
     * Processes a client's request to explicitly skip picking an extra card.
     */
    void handleSkipExtra();

    // NETWORK
    /**
     * Forcibly disconnects the client from the server, closing any underlying
     * network resources (such as active Sockets) and cleaning up references.
     */
    void disconnect();
}