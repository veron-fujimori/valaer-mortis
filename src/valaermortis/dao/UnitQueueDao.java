package valaermortis.dao;

import valaermortis.model.UnitQueue;
import valaermortis.model.enums.QueueStatus;
import valaermortis.model.enums.UnitType;
import valaermortis.util.DB;
import valaermortis.util.ErrorHandler;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UnitQueueDao {

    public boolean addToQueue(UnitQueue unitQueue) {
        String sql = "INSERT INTO unit_queue (building_id, unit_type, quantity, end_time, status) " +
                "VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DB.getInstance().getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setLong(1, unitQueue.getBuildingId());
            stmt.setString(2, unitQueue.getUnitType().toString());
            stmt.setInt(3, unitQueue.getQuantity());
            stmt.setTimestamp(4, unitQueue.getEndTime());
            stmt.setString(5, unitQueue.getStatus().toString());

            int affectedRows = stmt.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        unitQueue.setId(generatedKeys.getLong(1));
                        return true;
                    }
                }
            }
        } catch (SQLException e) {
            ErrorHandler.logDatabaseError("adding unit to queue", e);
        }
        return false;
    }

    public boolean updateStatus(long id, QueueStatus status) {
        String sql = "UPDATE unit_queue SET status = ? WHERE id = ?";

        try (Connection conn = DB.getInstance().getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, status.toString());
            stmt.setLong(2, id);

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            ErrorHandler.logDatabaseError("updating unit queue status", e);
        }
        return false;
    }

    public List<UnitQueue> getActiveQueuesByUserId(String userId) {
        String sql = "SELECT uq.* FROM unit_queue uq " +
                "JOIN buildings b ON uq.building_id = b.id " +
                "WHERE b.user_id = ? AND uq.status = 'TRAINING'";

        List<UnitQueue> result = new ArrayList<>();

        try (Connection conn = DB.getInstance().getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, userId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    result.add(mapResultSetToUnitQueue(rs));
                }
            }
        } catch (SQLException e) {
            ErrorHandler.logDatabaseError("getting active unit queues", e);
        }
        return result;
    }

    public List<UnitQueue> getQueuesByBuildingId(long buildingId) {
        String sql = "SELECT * FROM unit_queue WHERE building_id = ?";
        List<UnitQueue> result = new ArrayList<>();

        try (Connection conn = DB.getInstance().getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, buildingId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    result.add(mapResultSetToUnitQueue(rs));
                }
            }
        } catch (SQLException e) {
            ErrorHandler.logDatabaseError("getting unit queues by building id", e);
        }
        return result;
    }

    public List<UnitQueue> getCompletedQueues(String userId) {
        String sql = "SELECT uq.* FROM unit_queue uq " +
                "JOIN buildings b ON uq.building_id = b.id " +
                "WHERE b.user_id = ? AND uq.status = 'COMPLETED'";

        List<UnitQueue> result = new ArrayList<>();

        try (Connection conn = DB.getInstance().getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, userId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    result.add(mapResultSetToUnitQueue(rs));
                }
            }
        } catch (SQLException e) {
            ErrorHandler.logDatabaseError("getting completed unit queues", e);
        }
        return result;
    }

    public boolean deleteQueue(long id) {
        String sql = "DELETE FROM unit_queue WHERE id = ?";

        try (Connection conn = DB.getInstance().getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            ErrorHandler.logDatabaseError("deleting unit queue", e);
        }
        return false;
    }

    public boolean completeTraining(UnitQueue unitQueue) {
        boolean updated = updateStatus(unitQueue.getId(), QueueStatus.COMPLETED);
        return updated;
    }

    public UnitQueue getById(long id) {
        String sql = "SELECT * FROM unit_queue WHERE id = ?";

        try (Connection conn = DB.getInstance().getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToUnitQueue(rs);
                }
            }
        } catch (SQLException e) {
            ErrorHandler.logDatabaseError("getting unit queue by id", e);
        }
        return null;
    }

    private UnitQueue mapResultSetToUnitQueue(ResultSet rs) throws SQLException {
        UnitQueue queue = new UnitQueue();
        queue.setId(rs.getLong("id"));
        queue.setBuildingId(rs.getLong("building_id"));
        queue.setUnitType(UnitType.valueOf(rs.getString("unit_type").toUpperCase()));
        queue.setQuantity(rs.getInt("quantity"));
        queue.setEndTime(rs.getTimestamp("end_time"));
        queue.setStatus(QueueStatus.valueOf(rs.getString("status").toUpperCase()));
        return queue;
    }
}