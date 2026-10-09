package it.polimi.ingsw.Server.DAO;

import it.polimi.ingsw.Server.DB.DatabaseConnection;
import java.sql.*;

/**
 * Data Access Object (DAO) for the {@code match} table in the Mesos database.
 *
 * <p>Handles persistence of game sessions. Each match is recorded with its
 * creation date. The generated primary key is returned so that other DAOs
 * (e.g., a future {@code MatchResultDAO}) can associate player results
 * with the correct match.</p>
 *
 * <p>This class supports the advanced feature <em>DB Leaderboard</em>,
 * which requires storing match history including date and player count.</p>
 */
public class MatchDAO {

    /**
     * Creates a new match record in the database with today's date.
     *
     * <p>The method uses {@link Statement#RETURN_GENERATED_KEYS} to retrieve
     * the auto-incremented primary key assigned by the database, which must
     * be used to link player results to this match via a join table.</p>
     *
     * @return the auto-generated match ID ({@code >= 1}) on success,
     *         or {@code -1} if the insert failed
     */
    public static int createMatch(int numPlayers) {
        String sql = "INSERT INTO mesos.match (date, numPlayers) VALUES (DATE(NOW()), ?)"; //match is probably a keyword in SQL so we use the prefix mesos to specify it's the table match of the mesos scheme
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) { //RETURN_GENERATED_KEYS will allow this method to return the generated key

            stmt.setInt(1, numPlayers);
            stmt.executeUpdate();

            // Retrieve the auto-generated primary key
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }

        } catch (SQLException e) {
            System.err.println("Error creating match record: " + e.getMessage());
            e.printStackTrace();
        }
        return -1; // signals failure to the caller
    }
}
