package it.polimi.ingsw.Server.Network;

import it.polimi.ingsw.Server.Controller.MatchController;
import it.polimi.ingsw.Server.Controller.ServerManager;

import java.util.List;

/**
 * Abstract base class for handling client requests.
 * <p>
 * This class implements the generic command routing logic, preventing code
 * duplication across different network protocols (e.g., RMI and Sockets).
 * It delegates the actual execution of game logic to the {@link ServerManager}.
 * </p>
 */
public abstract class AbstractClientHandler implements ClientHandler, VirtualView {

    protected String nickname; //visible to RMIClientHandler and SocketClientHandler
    protected final ServerManager serverManager;
    protected MatchController matchController = null; // null until a match actually starts

    /**
     * Constructs a new AbstractClientHandler linked to the main server manager.
     *
     * @param serverManager the central manager handling lobbies, global server state, and client routing
     */
    public AbstractClientHandler(ServerManager serverManager) {
        this.serverManager = serverManager;
    }

    /**
     * Gets the nickname associated with this client handler.
     *
     * @return the client's registered nickname, or {@code null} if the client is not yet authenticated
     */
    @Override
    public String getNickname() {
        return nickname;
    }

    /**
     * Links this client handler to a specific active match.
     * <p>
     * This method is called by the server when a lobby successfully reaches its target size
     * and transitions into a full match, allowing the handler to route gameplay actions directly.
     * </p>
     *
     * @param matchController the controller managing the game logic for the current match
     */
    @Override
    public void setMatchController(MatchController matchController) {
        this.matchController = matchController;
    }

    // COMMON LOGIC FOR ALL PROTOCOLS

    /**
     * Processes a client's request to create a new lobby.
     * Delegates the execution to the {@link ServerManager}.
     *
     * @param targetSize the requested number of players for the new lobby
     */
    @Override
    public void handleCreateLobby(int targetSize) {
        if (nickname == null) return;
        System.out.println(nickname + " is creating a new lobby for " + targetSize + " players.");
        serverManager.createLobby(nickname, targetSize);
    }

    /**
     * Processes a client's request to join an existing lobby.
     * Delegates the execution to the {@link ServerManager}.
     *
     * @param lobbyId the unique identifier of the lobby the player wants to join
     */
    @Override
    public void handleJoinLobby(int lobbyId) {
        if (nickname == null) return;
        System.out.println(nickname + " is attempting to join lobby " + lobbyId);
        serverManager.joinLobby(nickname, lobbyId);
    }


    // MATCH ACTIONS (Delegated directly to MatchController)

    /**
     * Processes a client's request to place their totem on a specific board tile.
     * Delegates the action to the {@link MatchController}.
     *
     * @param tileId the identifier of the tile where the player wants to place the totem
     */
    @Override
    public void handlePlaceTotem(char tileId) {
        if (nickname == null || matchController == null) return;
        matchController.executePlaceTotem(nickname, tileId);
    }

    /**
     * Processes a client's request to resolve an offer (selecting cards from the board).
     * Delegates the action to the {@link MatchController}.
     *
     * @param upCards    the list of selected face-up card IDs
     * @param downCards  the list of selected face-down card IDs
     * @param orderedIds the ordered list of card IDs representing the player's final arrangement on their board
     */
    @Override
    public void handleResolveOffer(List<String> upCards, List<String> downCards, List<String> orderedIds) {
        if (nickname == null || matchController == null) return;
        matchController.executeResolveOffer(nickname, upCards, downCards, orderedIds);
    }

    /**
     * Processes a client's request to pick an extra card during a special phase.
     * Delegates the action to the {@link MatchController}.
     *
     * @param cardId     the ID of the extra card chosen
     * @param isUpperRow a boolean flag indicating if the card is drawn from the upper row (true) or lower row (false)
     */
    @Override
    public void handleExtraCardPick(String cardId, boolean isUpperRow) {
        if (nickname == null || matchController == null) return;
        matchController.executeExtraCardPick(nickname, cardId, isUpperRow);
    }

    /**
     * Processes a client's request to explicitly skip picking an extra card.
     * Delegates the action to the {@link MatchController}.
     */
    @Override
    public void handleSkipExtra() {
        if (nickname == null || matchController == null) return;
        matchController.executeSkipExtra(nickname);
    }
}