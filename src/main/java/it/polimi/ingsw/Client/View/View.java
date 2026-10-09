package it.polimi.ingsw.Client.View;

import it.polimi.ingsw.Network.DTO.LobbyDTO;
import it.polimi.ingsw.Network.DTO.MatchDTO;
import it.polimi.ingsw.Network.DTO.MatchResultDTO;
import it.polimi.ingsw.Network.DTO.PlayerDTO;

import java.util.List;

/**
 * The main interface for the client's User Interface (UI).
 * <p>
 * This interface abstracts the visual representation of the game, allowing the
 * network layer to send updates and requests to the player without knowing
 * whether the client is using a Text User Interface (TUI) or a Graphical
 * User Interface (GUI).
 * </p>
 */
public interface View {

    /**
     * Updates the local representation of the game using the latest snapshot.
     * The implementation must render the new board, player stats, and current state.
     *
     * @param snapshot the updated state of the match received from the server
     */
    void updateModel(MatchDTO snapshot);

    /**
     * Displays a generic informational message to the player.
     *
     * @param message the message to display (e.g., "Player X joined the lobby")
     */
    void showMessage(String message);

    /**
     * Displays an error message to the player.
     *
     * @param message the error message (e.g., "Invalid move", "Connection lost")
     */
    void showError(String message);


    /**
     * Displays the list of available lobbies to the user.
     *
     * @param lobbies list of {@link LobbyDTO} containing ID, players count, and creator info.
     */
    void showOpenLobbies(List<LobbyDTO> lobbies);

    /**
     * Notifies the view whether the login attempt was successful.
     * If successful, the view should transition to the lobby waiting screen.
     * If failed, the view should ask the user for a different nickname.
     *
     * @param success {@code true} if the nickname was accepted, {@code false} if already taken
     */
    void showLoginResult(boolean success);

    /**
     * Prompts the player to submit an action because it is their turn.
     * The view should enable the input controls (e.g., enable buttons or wait for CLI input)
     * based on the current phase of the match.
     */
    void askAction(String messageTUI, String messageGUI);

    /**
     * Stores the player's nickname locally in the view.
     * <p>
     * This is called after a successful login so that the view can display the player's
     * identity in the lobby and dynamically format the UI during the match.
     * </p>
     *
     * @param nickname the confirmed nickname of the player
     */
    void setNickname(String nickname); //this is used to set the nickname in the view after a successful login, so that it can be displayed in the lobby and during the match

    /**
     * Displays the end-game screen.
     * <p>
     * This method is called when the server notifies the conclusion of the game.
     * It is responsible for presenting the final standings of the current match to the user,
     * and for handling the global historical leaderboard (from the database)
     * for matches played with the same number of players.
     * </p>
     *
     * @param matchRanking      the sorted list of results for the concluded match,
     * from the winner (first element) to the last player.
     * @param globalLeaderboard the sorted list of the best global scores
     * (stored in the DB) for this specific match size.
     * @param numPlayers        the total number of players who participated in this match.
     */
    void showEndGame(List<MatchResultDTO> matchRanking, List<MatchResultDTO> globalLeaderboard, int numPlayers);

    /**
     * Displays a detailed view of the given players' tribes.
     * @param players the list of players to display, ordered by the controller
     */
    void showAllTribes(List<PlayerDTO> players);



    // ── Special methods for GUI Rendering (TUI ignores them) ───────────────


    /**
     * Displays the specific UI panel required for placing a totem.
     * <p>
     * This is a default no-operation method. It is intentionally ignored by the TUI
     * but must be overridden by the GUI implementation to render the appropriate interactive components.
     * </p>
     */
    default void showPlaceTotemPanel(){
        // no-operation
    }

    /**
     * Displays the specific UI panel required to resolve an offer tile (picking cards).
     * <p>
     * This is a default no-operation method. It is intentionally ignored by the TUI
     * but must be overridden by the GUI implementation to render the appropriate interactive components.
     * </p>
     */
    default void showResolveOfferPanel(){
        // no-operation
    }

    /**
     * Displays the specific UI panel prompting the user to pick an extra card or skip the action.
     * <p>
     * This is a default no-operation method. It is intentionally ignored by the TUI
     * but must be overridden by the GUI implementation to render the appropriate interactive components.
     * </p>
     */
    default void showSkipExtraPanel(){
        // no-operation
    }

}