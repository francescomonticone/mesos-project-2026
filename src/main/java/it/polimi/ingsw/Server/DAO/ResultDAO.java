package it.polimi.ingsw.Server.DAO;

import it.polimi.ingsw.Network.DTO.MatchResultDTO;
import it.polimi.ingsw.Server.DB.DatabaseConnection;
import it.polimi.ingsw.Server.Model.Match.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object (DAO) for the result table.
 *
 * <p>Provides operations related to results of matches, including
 * insert results of the match and get leaderboard by number of players.
 * Each method opens its own connection and closes it automatically
 * via try-with-resources.</p>
 *
 * <p>This class is part of the advanced feature <em>DB Leaderboard</em>
 * described in the project requirements.</p>
 */
public class ResultDAO {
    /**
     * inserts the result of players' scores in the result table
     *
     * @param players the list of players who played the match
     * @param matchId the match id of the finished match
     */
    public static void insertResult(List<Player> players, int matchId) {
        String sql = "INSERT INTO result (nickname, matchId, prestigePoint, food) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            for (Player player : players) {
                stmt.setString(1, player.getNickname());
                stmt.setInt(2, matchId);
                stmt.setInt(3, player.getPrestigePoint());
                stmt.setInt(4, player.getFoodToken());
                stmt.executeUpdate();
            }

        } catch (SQLException e) {
            System.err.println("Error inserting result for match id " + matchId + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * method to get the leaderboard, list of scores from matches with the number of players
     * set as a parameter
     *
     * @param numPlayers matches with the number of players
     * @return leaderboard with all score from matches with the same number of players
     */
    public static List<MatchResultDTO> getLeaderboard(int numPlayers){
        List<MatchResultDTO> results = new ArrayList<>();

        String sql = "SELECT nickname, prestigePoint, food, date " +
                    "FROM result as r INNER JOIN mesos.match as m ON r.matchId = m.id " +
                    "WHERE numPlayers = ? " +
                    "ORDER BY prestigePoint DESC, food DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, numPlayers);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    MatchResultDTO result = new MatchResultDTO(
                            rs.getString("nickname"),
                            rs.getInt("prestigePoint"),
                            rs.getInt("food"),
                            rs.getDate("date")
                    );
                    results.add(result);
                }
            }

        } catch (SQLException e) {
            System.err.println("Error getting leaderboard: " + e.getMessage());
            e.printStackTrace();
        }

        return results;
    }
}
