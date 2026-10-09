package it.polimi.ingsw.Server.DAO;

import it.polimi.ingsw.Server.DB.DatabaseConnection;

import java.sql.*;

/**
 * Data Access Object (DAO) for the {@code account} table.
 *
 * <p>Provides CRUD operations related to player accounts, including
 * account creation, password retrieval, and nickname existence checks.
 * Each method opens its own connection and closes it automatically
 * via try-with-resources.</p>
 *
 * <p>This class is part of the advanced feature <em>DB Leaderboard</em>
 * described in the project requirements.</p>
 */
public class AccountDAO {

    /**
     * Inserts a new player account into the {@code account} table.
     *
     * <p>The nickname must be unique; if a duplicate entry is attempted,
     * the underlying {@link SQLException} is printed and the method
     * returns silently.</p>
     *
     * @param nickname the unique player identifier, must not be {@code null}
     * @param passwordHash the player's password
     */
    public static void insertAccount(String nickname, String passwordHash) {
        String sql = "INSERT INTO account (nickname, passwordHash) VALUES (?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, nickname);
            stmt.setString(2, passwordHash);
            stmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Error inserting account for nickname '" + nickname + "': " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * method to authenticate, checks if nickname and password are correct
     *
     * @param nickname nickname of the user
     * @param passwordHash password hash of the user
     * @return {@code 1} if the nickname and password matches in the {@code account} table,
     *         {@code 0} if no matching record is found,
     *         {@code -1} if a database error occurred during the query
     */
    public static int authenticateAccount(String nickname, String passwordHash) {
        String sql = "SELECT * FROM account WHERE nickname = ? AND passwordHash = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, nickname);
            stmt.setString(2, passwordHash);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return 1;
                } else {
                    return 0;
                }
            }

        } catch (SQLException e) {
            System.err.println("Error authenticating for nickname '" + nickname + "': " + e.getMessage());
            e.printStackTrace();
        }
        return -1;
    }


    /**
     * Checks whether a given nickname is already registered in the database.
     *
     * <p>This method is used by the server to enforce the uniqueness constraint
     * on player nicknames before accepting a new connection, as required by
     * the project specifications.</p>
     *
     * @param nickname the nickname to look up, must not be {@code null}
     * @return {@code 1} if the nickname exists in the {@code account} table,
     *         {@code 0} if no matching record is found,
     *         {@code -1} if a database error occurred during the query
     */
    public static int nicknameExists(String nickname) {
        String sql = "SELECT nickname FROM account WHERE nickname = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, nickname);

            try (ResultSet rs = stmt.executeQuery()) {
                if(rs.next()){
                    return 1; // 1 if at least one row is found
                } else{
                    return 0; // 0 if no row is found
                }
            }

        } catch (SQLException e) {
            System.err.println("Error checking nickname existence for '" + nickname + "': " + e.getMessage());
            e.printStackTrace();
        }
        return -1; // -1 if there has been an error
    }

}