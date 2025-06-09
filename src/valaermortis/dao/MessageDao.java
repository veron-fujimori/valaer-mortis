package valaermortis.dao;

import valaermortis.model.Message;
import valaermortis.util.DB;
import valaermortis.util.ErrorHandler;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MessageDao {

    public boolean addMessage(String userId, String title, String messageText) {
        String sql = "INSERT INTO messages (user_id, title, message) VALUES (?, ?, ?)";

        try (Connection conn = DB.getInstance().getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, userId);
            ps.setString(2, title);
            ps.setString(3, messageText);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            ErrorHandler.logDatabaseError("adding message", e);
            return false;
        }
    }

    public List<Message> getMessagesByUserId(String userId) {
        List<Message> messages = new ArrayList<>();
        String sql = "SELECT id, user_id, title, message, created_at FROM messages WHERE user_id = ? ORDER BY created_at DESC";

        try (Connection conn = DB.getInstance().getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, userId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Message message = new Message();
                    message.setId(rs.getLong("id"));
                    message.setUserId(rs.getString("user_id"));
                    message.setTitle(rs.getString("title"));
                    message.setMessage(rs.getString("message"));
                    message.setCreatedAt(rs.getTimestamp("created_at"));
                    messages.add(message);
                }
            }
        } catch (SQLException e) {
            ErrorHandler.logDatabaseError("getting messages by user ID", e);
        }

        return messages;
    }

    public List<Message> getRecentMessages(String userId, int limit) {
        List<Message> messages = new ArrayList<>();
        String sql = "SELECT id, user_id, title, message, created_at FROM messages WHERE user_id = ? ORDER BY created_at DESC LIMIT ?";

        try (Connection conn = DB.getInstance().getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, userId);
            ps.setInt(2, limit);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Message message = new Message();
                    message.setId(rs.getLong("id"));
                    message.setUserId(rs.getString("user_id"));
                    message.setTitle(rs.getString("title"));
                    message.setMessage(rs.getString("message"));
                    message.setCreatedAt(rs.getTimestamp("created_at"));
                    messages.add(message);
                }
            }
        } catch (SQLException e) {
            ErrorHandler.logDatabaseError("getting recent messages", e);
        }

        return messages;
    }

    public boolean deleteOldMessages(String userId, int daysOld) {
        String sql = "DELETE FROM messages WHERE user_id = ? AND created_at < DATE_SUB(NOW(), INTERVAL ? DAY)";
        try (Connection conn = DB.getInstance().getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, userId);
            ps.setInt(2, daysOld);

            return ps.executeUpdate() >= 0;

        } catch (SQLException e) {
            ErrorHandler.logDatabaseError("deleting old messages", e);
            return false;
        }
    }

    public int getMessageCount(String userId) {
        String sql = "SELECT COUNT(*) FROM messages WHERE user_id = ?";

        try (Connection conn = DB.getInstance().getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, userId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            ErrorHandler.logDatabaseError("getting message count", e);
        }

        return 0;
    }
}
