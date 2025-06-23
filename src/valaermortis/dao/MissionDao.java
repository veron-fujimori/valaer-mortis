package valaermortis.dao;

import valaermortis.model.Mission;
import valaermortis.model.enums.MissionType;
import valaermortis.model.enums.MissionStatus;
import valaermortis.util.DB;
import valaermortis.util.ErrorHandler;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MissionDao {
    public boolean create(Mission mission) {
        String generatedId = java.util.UUID.randomUUID().toString();
        mission.setId(generatedId);

        String sql = "INSERT INTO missions (id, user_id, type, status, start_time, end_time) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DB.getInstance().getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, generatedId);
            stmt.setString(2, mission.getUserId());
            stmt.setString(3, mission.getType().toString().toLowerCase());
            stmt.setString(4, mission.getStatus().toString().toLowerCase());
            stmt.setTimestamp(5, mission.getStartTime());
            stmt.setTimestamp(6, mission.getEndTime());

            int result = stmt.executeUpdate();

            if (result > 0) {
                return true;
            }
        } catch (SQLException e) {
            ErrorHandler.logDatabaseError("creating mission", e);
        }
        return false;
    }

    public boolean update(Mission mission) {
        String sql = "UPDATE missions SET status = ? WHERE id = ?";

        try (Connection conn = DB.getInstance().getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, mission.getStatus().toString());
            stmt.setString(2, mission.getId());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            ErrorHandler.logDatabaseError("updating mission", e);
        }
        return false;
    }

    public Mission getById(String id) {
        String sql = "SELECT * FROM missions WHERE id = ?";

        try (Connection conn = DB.getInstance().getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToMission(rs);
            }
        } catch (SQLException e) {
            ErrorHandler.logDatabaseError("getting mission by ID", e);
        }
        return null;
    }

    public List<Mission> getByUserId(String userId) {
        String sql = "SELECT * FROM missions WHERE user_id = ? ORDER BY start_time DESC";
        List<Mission> missions = new ArrayList<>();

        try (Connection conn = DB.getInstance().getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                missions.add(mapResultSetToMission(rs));
            }
        } catch (SQLException e) {
            ErrorHandler.logDatabaseError("getting missions by user ID", e);
        }
        return missions;
    }

    public List<Mission> getActiveMissions(String userId) {
        String sql = "SELECT * FROM missions WHERE user_id = ? AND status = ? ORDER BY end_time ASC";
        List<Mission> missions = new ArrayList<>();
        try (Connection conn = DB.getInstance().getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, userId);
            stmt.setString(2, MissionStatus.IN_PROGRESS.toString());
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                missions.add(mapResultSetToMission(rs));
            }
        } catch (SQLException e) {
            ErrorHandler.logDatabaseError("getting active missions", e);
        }
        return missions;
    }

    public List<Mission> getCompletedMissions(String userId) {
        String sql = "SELECT * FROM missions WHERE user_id = ? AND status = ? ORDER BY end_time DESC";
        List<Mission> missions = new ArrayList<>();

        try (Connection conn = DB.getInstance().getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, userId);
            stmt.setString(2, MissionStatus.COMPLETED.toString());
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                missions.add(mapResultSetToMission(rs));
            }
        } catch (SQLException e) {
            ErrorHandler.logDatabaseError("getting completed missions", e);
        }
        return missions;
    }

    public boolean delete(String id) {
        String sql = "DELETE FROM missions WHERE id = ?";

        try (Connection conn = DB.getInstance().getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            ErrorHandler.logDatabaseError("deleting mission", e);
        }
        return false;
    }

    public boolean completeMission(String missionId) {
        String sql = "UPDATE missions SET status = ? WHERE id = ?";
        try (Connection conn = DB.getInstance().getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, MissionStatus.COMPLETED.toString());
            stmt.setString(2, missionId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            ErrorHandler.logDatabaseError("completing mission", e);
        }
        return false;
    }

    private Mission mapResultSetToMission(ResultSet rs) throws SQLException {
        Mission mission = new Mission();
        mission.setId(rs.getString("id"));
        mission.setUserId(rs.getString("user_id"));
        mission.setType(MissionType.valueOf(rs.getString("type").toUpperCase()));
        mission.setStatus(MissionStatus.valueOf(rs.getString("status").toUpperCase()));
        mission.setStartTime(rs.getTimestamp("start_time"));
        mission.setEndTime(rs.getTimestamp("end_time"));
        return mission;
    }
}
