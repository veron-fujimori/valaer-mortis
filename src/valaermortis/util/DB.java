package valaermortis.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DB {
    private static DB instance;
    private Connection connection;
    private final String url = "jdbc:mysql://localhost:3306/valaer_mortis";
    private final String user = "root";
    private final String pass = "";

    private DB() {
        connect();
    }

    private void connect() {
        try {
            connection = DriverManager.getConnection(url, user, pass);
        } catch (SQLException ex) {
            throw new RuntimeException("Failed to connect DB", ex);
        }
    }

    public static synchronized DB getInstance() {
        if (instance == null) {
            instance = new DB();
        }
        return instance;
    }

    public Connection getConnection() {
        try {
            if (connection == null || connection.isClosed() || !connection.isValid(2)) {
                connect();
            }
        } catch (SQLException ex) {
            connect();
        }
        return connection;
    }
}