package imsapp.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Singleton utility for MySQL database connections.
 * Update the URL, USER, and PASSWORD constants to match your environment.
 */
public class DBConnection {

    private static final String URL  = "jdbc:mysql://localhost:3306/ims_db?useSSL=false&serverTimezone=Asia/Manila";
    private static final String USER = "root";
    private static final String PASS = "therepenter09";          // change to your MySQL password

    private static Connection connection;

    private DBConnection() { /* utility class */ }

    /**
     * Returns a shared database connection. Creates one if it does not exist
     * or if the previous connection was closed.
     *
     * @return a live Connection to the ims_db database
     * @throws SQLException if a database access error occurs
     */
    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
            } catch (ClassNotFoundException e) {
                throw new SQLException("MySQL JDBC Driver not found. "
                        + "Add mysql-connector-j JAR to your project libraries.", e);
            }
            connection = DriverManager.getConnection(URL, USER, PASS);
        }
        return connection;
    }

    /**
     * Closes the shared connection if it is open.
     */
    public static void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                System.err.println("Error closing DB connection: " + e.getMessage());
            }
        }
    }
}
