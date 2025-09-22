package app;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    // --- DATABASE CONNECTION DETAILS ---
    // Make sure to change these to your actual database details
    private static final String URL = "jdbc:mysql://localhost:3306/pos_system"; // Your DB name
    private static final String USER = "root"; // Your DB username
    private static final String PASSWORD = ""; // Your DB password (often empty for local XAMPP)

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}