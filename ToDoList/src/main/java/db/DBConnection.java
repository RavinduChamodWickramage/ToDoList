package db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    private static DBConnection dbConnection;
    private static Connection connection;

    private DBConnection(){
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/ToDoListDB",
                    "root",
                    "19982001"
            );
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to connect to the database.", e);
        }
    }

    public static synchronized DBConnection getInstance() {
        if (dbConnection == null) {
            dbConnection = new DBConnection();
        }
        return dbConnection;
    }

    public static Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                dbConnection = new DBConnection();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return connection;
    }
}
