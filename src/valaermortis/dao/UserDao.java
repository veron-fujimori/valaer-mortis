package valaermortis.dao;

import valaermortis.model.User;
import valaermortis.util.DB;
import valaermortis.util.ErrorHandler;
import java.sql.*;

public class UserDao {
    public User findByUsername(String username) {
        try (Connection conn = DB.getInstance().getConnection();
                PreparedStatement ps = conn.prepareStatement(
                        "SELECT * FROM users WHERE username=?")) {
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                User u = new User();
                u.setId(rs.getString("id"));
                u.setUsername(rs.getString("username"));
                u.setPasswordHash(rs.getString("password"));
                return u;
            }
        } catch (SQLException e) {
            ErrorHandler.logDatabaseError("finding user by username", e);
        }
        return null;
    }

    public String insert(String username, String hash) {
        String userId = java.util.UUID.randomUUID().toString();
        try (Connection conn = DB.getInstance().getConnection();
                PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO users(id, username, password) VALUES (?, ?, ?)")) {
            ps.setString(1, userId);
            ps.setString(2, username);
            ps.setString(3, hash);
            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                return userId;
            }
        } catch (SQLException e) {
            ErrorHandler.logDatabaseError("inserting new user", e);
        }
        return null;
    }
}