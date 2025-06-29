package valaermortis.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DB {
    private static DB instance;
    private final String url = "jdbc:mysql://localhost:3306/valaer_mortis";
    private final String user = "root";
    private final String pass = "";

    private DB() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    public static synchronized DB getInstance() {
        if (instance == null) {
            instance = new DB();
        }
        return instance;
    }

    public synchronized Connection getConnection() {
        try {
            Connection conn = DriverManager.getConnection(url, user, pass);
            if (conn == null || !conn.isValid(2)) {
                throw new SQLException("Invalid connection created");
            }
            return conn;
        } catch (SQLException ex) {
            ex.printStackTrace();
            System.out.print("\nTekan Enter untuk keluar...");

            try {
                System.in.read();
            } catch (Exception e) {
            }

            System.exit(1);
            return null;
        }
    }
}