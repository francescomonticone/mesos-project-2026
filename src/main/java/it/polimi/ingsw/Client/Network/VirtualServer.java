package it.polimi.ingsw.Client.Network;


import it.polimi.ingsw.Client.ClientController.ClientController;

import java.util.List;

/**
 * Interface representing the server from the client's perspective.
 * <p>
 * It abstracts the network protocol (RMI or Socket), allowing the Client
 * (GUI or TUI) to send commands to the Server regardless of the connection type.
 * It acts as a pure Client-side abstraction for network transmission.
 * </p>
 */
public interface VirtualServer {

    /**
     * Requests to authenticate and join the server.
     *
     * @param nickname     the chosen unique player name
     * @param passwordHash the hashed password sent by the client
     * @throws Exception if a network error occurs during transmission
     */
    void login(String nickname, String passwordHash) throws Exception;

    /**
     * Depending on the implementation, this either binds the RMI callback
     * or starts the Socket listening thread, allowing the network layer
     * to forward incoming server messages to the logic layer.
     *
     * @param controller the main {@link ClientController} of the application
     */
    void setControllerRMILoopSocket(ClientController controller);

    /**
     * Sends a request to the server to create a new match lobby.
     *
     * @param targetSize the desired number of players for this match (usually between 2 and 4)
     * @throws Exception if a network error occurs during transmission
     */
    void createLobby(int targetSize) throws Exception;

    /**
     * Sends a request to the server to join an existing open lobby.
     *
     * @param lobbyId the unique identifier of the lobby to join
     * @throws Exception if a network error occurs during transmission
     */
    void joinLobby(int lobbyId) throws Exception;

    /**
     * Requests the list of all currently available (open) lobbies from the server.
     * <p>
     * For RMI: This will be a synchronous call returning data to the view immediately.
     * For Sockets: This will send a RequestCommand, and the response will arrive
     * asynchronously via the receiving thread.
     * </p>
     *
     * @throws Exception if the request cannot be sent or the connection is lost.
     */
    void requestOpenLobbies() throws Exception;

    //ACTION METHODS
    /**
     * Sends the player's choice to place their totem on a specific offer tile.
     *
     * @param tileId the character identifier of the chosen tile (e.g., 'A', 'B')
     * @throws Exception if a network error occurs during transmission
     */
    void placeTotem(char tileId) throws Exception;

    /**
     * Sends the player's selection of cards to pick from the board to resolve their offer.
     *
     * @param upCards      the list of IDs for cards picked from the upper row
     * @param downCards    the list of IDs for cards picked from the lower row
     * @param orderedCards the raw list of requested cards in the exact order they were chosen
     * @throws Exception if a network error occurs during transmission
     */
    void resolveOffer(List<String> upCards, List<String> downCards, List<String> orderedCards) throws Exception;

    /**
     * Sends a request to pick an extra card from the board, usually triggered by a specific game effect.
     *
     * @param cardId     the ID of the extra card to pick
     * @param isUpperRow true if the card is located in the upper row, false otherwise
     * @throws Exception if a network error occurs during transmission
     */
    void pickExtraCard(String cardId, boolean isUpperRow) throws Exception;

    /**
     * Notifies the server that the player explicitly chooses to skip their extra card pick action.
     *
     * @throws Exception if a network error occurs during transmission
     */
    void skipExtraCard() throws Exception;


}