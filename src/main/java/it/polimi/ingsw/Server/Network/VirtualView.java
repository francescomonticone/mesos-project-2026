package it.polimi.ingsw.Server.Network;

import it.polimi.ingsw.Network.DTO.LobbyDTO;
import it.polimi.ingsw.Network.DTO.MatchDTO;
import it.polimi.ingsw.Network.DTO.MatchResultDTO;
import it.polimi.ingsw.Server.Controller.MatchController;
import java.util.List;

/**
 * Interface representing the view of a single player from the server's perspective.
 * <p>
 * Following the MVC pattern adapted for distributed systems, this Virtual View
 * abstracts the underlying network protocol (Socket or RMI). It is used primarily
 * by the {@link MatchController} to push state updates and prompt actions to the
 * client without needing to know how the client is physically connected.
 * </p>
 */
public interface VirtualView {

    /**
     * Links this virtual view to the active match controller.
     *
     * @param controller the {@link MatchController} managing the game logic for this client
     */
    void setMatchController(MatchController controller);

    // Server -> Client messages
    /**
     * Sends the updated game state to the client.
     *
     * @param snapshot the lightweight DTO representing the current state of the match
     * @throws Exception if a network communication error occurs, indicating a lost connection
     */
    void sendGameUpdate(MatchDTO snapshot) throws Exception;

    /**
     * Sends an error message to be displayed on the client's screen.
     *
     * @param message the text of the error to display
     * @throws Exception if a network communication error occurs, indicating a lost connection
     */
    void sendError(String message) throws Exception;

    /**
     * Sends a generic informational message to be displayed on the client's screen.
     *
     * @param message the text of the message to display
     * @throws Exception if a network communication error occurs, indicating a lost connection
     */
    void showMessage(String message) throws Exception;

    //ACTIONS
    /**
     * Prompts the client to perform the totem placement action.
     *
     * @throws Exception if a network communication error occurs, indicating a lost connection
     */
    void askPlaceTotem()  throws Exception;

    /**
     * Prompts the client to resolve an offer (select cards from the board).
     *
     * @throws Exception if a network communication error occurs, indicating a lost connection
     */
    void askResolveOffer() throws Exception;

    /**
     * Prompts the client to pick an extra card during a special game phase.
     *
     * @throws Exception if a network communication error occurs, indicating a lost connection
     */
    void askExtraCard() throws Exception;

    /**
     * Sends the list of currently open and joinable lobbies to the client.
     *
     * @param lobbies the list of {@link LobbyDTO} objects representing available games
     * @throws Exception if a network communication error occurs, indicating a lost connection
     */
    void showOpenLobbies(List<LobbyDTO> lobbies) throws Exception;

    /**
     * Sends the final match results and global leaderboard to the client at the end of the game.
     *
     * @param matchRanking      the final standings of the current match
     * @param globalLeaderboard the all-time high scores retrieved from the database
     * @param numPlayers        the total number of players in the match
     * @throws Exception if a network communication error occurs, indicating a lost connection
     */
    void sendGameOver(List<MatchResultDTO> matchRanking, List<MatchResultDTO> globalLeaderboard, int numPlayers) throws Exception;
}