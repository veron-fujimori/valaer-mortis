package valaermortis.dao;

import valaermortis.util.DB;
import valaermortis.util.ErrorHandler;
import java.sql.*;

public class MissionResultsDao {

    public boolean createMissionResult(String missionId, int foodGained, int woodGained, int stoneGained,
            boolean success) {
        String sql = "INSERT INTO mission_results (mission_id, food_gained, wood_gained, stone_gained, success) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DB.getInstance().getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, missionId);
            stmt.setInt(2, foodGained);
            stmt.setInt(3, woodGained);
            stmt.setInt(4, stoneGained);
            stmt.setBoolean(5, success);

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            ErrorHandler.logDatabaseError("creating mission result", e);
        }
        return false;
    }

    public MissionResult getMissionResult(String missionId) {
        String sql = "SELECT food_gained, wood_gained, stone_gained, success FROM mission_results WHERE mission_id = ?";

        try (Connection conn = DB.getInstance().getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, missionId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return new MissionResult(
                        rs.getInt("food_gained"),
                        rs.getInt("wood_gained"),
                        rs.getInt("stone_gained"),
                        rs.getBoolean("success"));
            }

        } catch (SQLException e) {
            ErrorHandler.logDatabaseError("getting mission result", e);
        }
        return null;
    }

    public static class MissionResult {
        public final int foodGained;
        public final int woodGained;
        public final int stoneGained;
        public final boolean success;

        public MissionResult(int foodGained, int woodGained, int stoneGained, boolean success) {
            this.foodGained = foodGained;
            this.woodGained = woodGained;
            this.stoneGained = stoneGained;
            this.success = success;
        }

        public int getFoodGained() {
            return foodGained;
        }

        public int getWoodGained() {
            return woodGained;
        }

        public int getStoneGained() {
            return stoneGained;
        }

        public boolean isSuccess() {
            return success;
        }
    }
}