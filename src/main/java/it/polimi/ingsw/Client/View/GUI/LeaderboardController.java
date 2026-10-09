package it.polimi.ingsw.Client.View.GUI;

import it.polimi.ingsw.Network.DTO.MatchResultDTO;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.text.SimpleDateFormat;
import java.util.List;

/**
 * Controller responsible for rendering the end-game leaderboard view.
 * Displays local match rankings side-by-side with global high scores from the database.
 */
public class LeaderboardController {

    @FXML private VBox localRankingBox;
    @FXML private VBox globalLeaderboardBox;

    /** Reference to the main GUI manager to trigger scene transitions */
    private GuiView guiView; //this is to play again

    /**
     * Initializes the leaderboard screen by populating both local and global tables.
     *
     * @param matchRanking      the final ranks computed for this specific match
     * @param globalLeaderboard the top historical scores fetched from the database
     * @param myNickname        the nickname of the local player to highlight their result
     * @param guiView           the main view manager to allow returning to the lobby
     */
    public void init(List<MatchResultDTO> matchRanking, List<MatchResultDTO> globalLeaderboard, String myNickname, GuiView guiView) {
        this.guiView = guiView;
        populateLocalRanking(matchRanking, myNickname);
        populateGlobalLeaderboard(globalLeaderboard, myNickname);
    }

    /**
     * Handles the "Play Again" button click.
     * Returns the user to the lobby selection screen, cleanly exiting full-screen mode.
     */
    @FXML
    private void onPlayAgainClicked() {
        System.out.println("[GUI] Returning to lobby selection...");
        // Instructs the GuiView to swap the scene back to the Lobby
        guiView.showLobbyScene();
    }

    /**
     * Handles the bottom bar exit action to shut down the application safely.
     */
    @FXML
    private void onExitClicked() {
        System.out.println("[GUI] Closing application from leaderboard view.");
        System.exit(0);
    }

    // ── Private Rendering Helpers ─────────────────────────────────────────────

    /**
     * Dynamically builds the graphic cards for the current match standing.
     *
     * @param ranking    the list of results for the current match
     * @param myNickname the nickname of the local player to highlight
     */
    private void populateLocalRanking(List<MatchResultDTO> ranking, String myNickname) {
        localRankingBox.getChildren().clear();

        if (ranking == null || ranking.isEmpty()) return;

        for (int i = 0; i < ranking.size(); i++) {
            MatchResultDTO result = ranking.get(i);
            boolean isMe = result.nickname().equals(myNickname);
            int rankPosition = i + 1; //find my position

            // Row Container setup
            HBox row = new HBox(12);
            row.setPadding(new Insets(10, 14, 10, 14));
            row.setAlignment(Pos.CENTER_LEFT);

            // Apply distinct colors if the row belongs to the local player
            String bgColor = isMe ? "#D5963FFF" : "#ffffff";
            String borderColor = isMe ? "#501D0CFF" : "#e3e1dd";
            String textColor = isMe ? "#ffffff" : "#000000";
            row.setStyle("-fx-background-color: " + bgColor + "; -fx-background-radius: 6; -fx-border-color: " + borderColor + "; -fx-border-radius: 6;");

            // Assign trophies for top 3 positions, standard numbers for the rest
            String medal = switch (rankPosition) {
                case 1 -> "🥇";
                case 2 -> "🥈";
                case 3 -> "🥉";
                default -> "  " + rankPosition + ".";
            };

            Label rankLabel = new Label(medal);
            rankLabel.setStyle("-fx-font-size: 14; -fx-font-weight: bold;");

            // Nickname display label
            Label nameLabel = new Label((isMe ? "★ " : "") + result.nickname());
            nameLabel.setStyle("-fx-font-size: 13; -fx-font-weight: bold; -fx-text-fill: " + textColor + ";");

            // Flexible space filler to align scores to the right
            HBox spacer = new HBox();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            // Metrics payload label
            Label scoresLabel = new Label(result.prestigePoints() + " PP  |  " + result.food() + " Food");
            scoresLabel.setStyle("-fx-font-size: 12; -fx-text-fill: " + textColor + "; -fx-font-weight: bold;");

            row.getChildren().addAll(rankLabel, nameLabel, spacer, scoresLabel);
            localRankingBox.getChildren().add(row);
        }
    }

    /**
     * Dynamically builds the graphic cards for the worldwide database high scores.
     * If the local player is outside the Top 20, they are appended at the very bottom
     * with a visual separator and their actual real world rank.
     *
     * @param leaderboard the full list of global match results from the database DTO
     * @param myNickname  the nickname of the local player to search and highlight
     */
    private void populateGlobalLeaderboard(List<MatchResultDTO> leaderboard, String myNickname) {
        globalLeaderboardBox.getChildren().clear();

        if (leaderboard == null || leaderboard.isEmpty()) {
            Label noData = new Label("No historical data registered yet.");
            noData.setStyle("-fx-text-fill: #7a7974; -fx-font-style: italic;");
            globalLeaderboardBox.getChildren().add(noData);
            return;
        }

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        int maxDisplay = Math.min(20, leaderboard.size());
        boolean meFoundInTop20 = false;

        // 1. First loop: Render the standard Top 20 players
        for (int i = 0; i < maxDisplay; i++) {
            MatchResultDTO result = leaderboard.get(i);
            boolean isMe = result.nickname().equals(myNickname);
            if (isMe) {
                meFoundInTop20 = true;
            }

            // Build and add the row using the shared helper method
            HBox row = createGlobalRow(result, i + 1, isMe, sdf);
            globalLeaderboardBox.getChildren().add(row);
        }

        // 2. Out of ranking check: If the local player is outside the Top 20, append them at the bottom
        if (!meFoundInTop20) {
            int myGlobalRank = -1;
            MatchResultDTO myResult = null;

            // Scan the rest of the database records from position 21 onwards
            for (int i = maxDisplay; i < leaderboard.size(); i++) {
                if (leaderboard.get(i).nickname().equals(myNickname)) {
                    myGlobalRank = i + 1;
                    myResult = leaderboard.get(i);
                    break; // Player found, we can stop the loop
                }
            }

            // If the player has a recorded score outside the top 20, render the detached card
            if (myResult != null) {
                // Add a beautiful ellipsis text separator to signal the ranking gap
                Label ellipsisLabel = new Label("  •   •   •   •   •");
                ellipsisLabel.setStyle("-fx-text-fill: #7a7974; -fx-font-size: 14; -fx-padding: 6 0 6 14; -fx-font-weight: bold;");
                globalLeaderboardBox.getChildren().add(ellipsisLabel);

                // Build and append the special detached row showing the real worldwide rank
                HBox myDetachedRow = createGlobalRow(myResult, myGlobalRank, true, sdf);
                globalLeaderboardBox.getChildren().add(myDetachedRow);
            }
        }
    }

    /**
     * Helper method to build a single graphical HBox row for the global leaderboard view.
     * Prevents code duplication between the Top 20 loop and the detached bottom row.
     *
     * @param result    the specific match result data container
     * @param worldRank the computed positioning number to display on the left
     * @param isMe      true if this row belongs to the local client player
     * @param sdf       the date formatter instance
     * @return the fully assembled and styled HBox container ready for JavaFX
     */
    private HBox createGlobalRow(MatchResultDTO result, int worldRank, boolean isMe, SimpleDateFormat sdf) {
        HBox row = new HBox(12);
        row.setPadding(new Insets(8, 14, 8, 14));
        row.setAlignment(Pos.CENTER_LEFT);

        // Dynamic style allocation based on player identity
        String bgColor = isMe ? "#D5963FFF" : "#ffffff";
        String borderColor = isMe ? "#501D0CFF" : "#e3e1dd";
        String textColor = isMe ? "#ffffff" : "#000000";
        row.setStyle("-fx-background-color: " + bgColor + "; -fx-background-radius: 4; -fx-border-color: " + borderColor + "; -fx-border-radius: 4;");

        // Rank layout column (#1, #2, #105, etc.)
        Label rankLabel = new Label("#" + worldRank);
        rankLabel.setStyle("-fx-font-size: 11; -fx-font-weight: bold; -fx-text-fill: " + textColor + "; -fx-min-width: 40;");

        // Player identity name label column
        Label nameLabel = new Label((isMe ? "★ " : "") + result.nickname());
        nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 12; -fx-text-fill: " + textColor + " ;");

        // Horizontal spacer to guarantee right-side anchoring for telemetry
        HBox spacer = new HBox();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Score data and timestamp calendar column
        String dateStr = result.date() != null ? sdf.format(result.date()) : "—";
        Label statsLabel = new Label(result.prestigePoints() + " PP | " + result.food() + " food (" + dateStr + ")");
        statsLabel.setStyle("-fx-font-size: 11; -fx-text-fill: " + textColor + ";");

        row.getChildren().addAll(rankLabel, nameLabel, spacer, statsLabel);
        return row;
    }
}