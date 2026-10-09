package it.polimi.ingsw.Server.DB;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Utility class that provides JDBC connections to the Mesos MySQL database.
 *
 * <p>This class follows the singleton-utility pattern: it cannot be instantiated
 * and exposes only static factory methods. All DAO classes obtain their
 * connections through {@link #getConnection()}.</p>
 *
 * <p><strong>Note:</strong> credentials should be externalized to a
 * {@code config.properties} file before production use.</p>
 */
public class DatabaseConnection {

    /** JDBC URL pointing to the local Mesos database instance. */
    private static final String URL;

    /** Database username. */
    private static final String USER;

    /** Database password. */
    private static final String PASSWORD;

    /*
     * Static initializer: loads credentials from config.properties.
     * Throws IllegalStateException at startup if the file is missing
     * or a required property is absent, failing fast before any DB call.
     */
    static {
        Properties props = new Properties(); //class that can read config file (key->value)

        //look for the external config file for JAR in the working directory of the JAR
        File externalConfig = new File("config.properties");
        if (externalConfig.exists()) { //if the file exists(for JAR)
            try (InputStream input = new FileInputStream(externalConfig)) {
                props.load(input);
                System.out.println("[DB] Loaded external config.properties");
            } catch (IOException e) {
                throw new IllegalStateException("Failed to load external config.properties", e);
            }
        }
        else{
            //this is for testing directly in IDE
            // Try loading from classpath first, then from working directory
            InputStream input = DatabaseConnection.class.getClassLoader()
                    .getResourceAsStream("config.properties"); //finds the file and it will return it as InputStream

            if (input == null) {
                throw new IllegalStateException(
                        "\n=======================================================\n" +
                                " ERROR: config.properties not found!\n" +
                                " Fill in your DB credentials and restart the server.\n" +
                                "======================================================="
                );
            }

            try {
                props.load(input);
            } catch (IOException e) {
                throw new IllegalStateException("Failed to load config.properties", e);
            }
        }

        URL      = requireProperty(props, "db.url");
        USER     = requireProperty(props, "db.user");
        PASSWORD = requireProperty(props, "db.password");
    }

    /**
     * Reads a required property from a {@link Properties} object.
     *
     * @param props the properties object to read from
     * @param key   the property key to look up
     * @return the trimmed property value
     * @throws IllegalStateException if the property is missing or blank
     */
    private static String requireProperty(Properties props, String key) {
        String value = props.getProperty(key); //extract value from key
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(
                    "Missing required property '" + key + "' in config.properties"
            );
        }
        return value.trim(); //removes spaces from start and end of the String
    }

    /** Prevents instantiation of this utility class. */
    private DatabaseConnection() {}

    /**
     * Opens and returns a new {@link Connection} to the Mesos database.
     *
     * <p>The caller is responsible for closing the connection (preferably
     * via try-with-resources) to avoid resource leaks.</p>
     *
     * @return a new {@link Connection} instance, or {@code null} if the
     *         connection attempt fails
     */
    public static Connection getConnection() {
        try {
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (SQLException e) {
            System.err.println("Failed to establish database connection: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}