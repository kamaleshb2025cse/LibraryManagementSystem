
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

    private static final String URL =
            "jdbc:mysql://localhost:3306/library_db";

    private DBConnection() {
        // Prevent unnecessary object creation.
    }

    public static Connection getConnection() throws SQLException {
        String username = System.getenv("DB_USER");
        String password = System.getenv("DB_PASSWORD");

        if (username == null || username.isBlank()) {
            throw new SQLException(
                    "DB_USER is not set. Configure your database username."
            );
        }

        if (password == null || password.isEmpty()) {
            throw new SQLException(
                    "DB_PASSWORD is not set. Configure your database password."
            );
        }

        return DriverManager.getConnection(URL, username, password);
    }
}