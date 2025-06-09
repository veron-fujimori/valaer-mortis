package valaermortis.dao;

import valaermortis.model.enums.UnitType;
import valaermortis.util.DB;
import valaermortis.util.ErrorHandler;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class MissionUnitsDao {

    public boolean createMissionUnits(String missionId, Map<UnitType, Integer> units) {
        String sql = "INSERT INTO mission_units (mission_id, unit_type, units_sent) VALUES (?, ?, ?)";

        try (Connection conn = DB.getInstance().getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            for (Map.Entry<UnitType, Integer> entry : units.entrySet()) {
                stmt.setString(1, missionId);
                stmt.setString(2, capitalizeFirst(entry.getKey().name()));
                stmt.setInt(3, entry.getValue());
                stmt.addBatch();
            }

            int[] results = stmt.executeBatch();
            return results.length > 0;

        } catch (SQLException e) {
            ErrorHandler.logDatabaseError("creating mission units", e);
        }
        return false;
    }

    public Map<UnitType, Integer> getMissionUnits(String missionId) {
        String sql = "SELECT unit_type, units_sent FROM mission_units WHERE mission_id = ?";
        Map<UnitType, Integer> units = new HashMap<>();

        try (Connection conn = DB.getInstance().getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, missionId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                UnitType unitType = UnitType.valueOf(rs.getString("unit_type").toUpperCase());
                int count = rs.getInt("units_sent");
                units.put(unitType, count);
            }

        } catch (SQLException e) {
            ErrorHandler.logDatabaseError("getting mission units", e);
        }
        return units;
    }

    public Map<UnitType, Integer> getSurvivingUnits(String missionId) {
        String sql = "SELECT unit_type, units_returned FROM mission_units WHERE mission_id = ? AND units_returned IS NOT NULL";
        Map<UnitType, Integer> units = new HashMap<>();

        try (Connection conn = DB.getInstance().getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, missionId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                UnitType unitType = UnitType.valueOf(rs.getString("unit_type").toUpperCase());
                int count = rs.getInt("units_returned");
                if (count > 0) {
                    units.put(unitType, count);
                }
            }

        } catch (SQLException e) {
            ErrorHandler.logDatabaseError("getting surviving units", e);
        }

        return units;
    }

    public boolean updateMissionUnitsResult(String missionId, Map<UnitType, Integer> unitsLost,
            Map<UnitType, Integer> unitsReturned) {
        String sql = "UPDATE mission_units SET units_lost = ?, units_returned = ? WHERE mission_id = ? AND unit_type = ?";

        try (Connection conn = DB.getInstance().getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            Map<UnitType, Integer> allUnits = getMissionUnits(missionId);

            for (UnitType unitType : allUnits.keySet()) {
                int lost = unitsLost.getOrDefault(unitType, 0);
                int returned = unitsReturned.getOrDefault(unitType, 0);

                stmt.setInt(1, lost);
                stmt.setInt(2, returned);
                stmt.setString(3, missionId);
                stmt.setString(4, capitalizeFirst(unitType.name()));
                stmt.addBatch();
            }

            int[] results = stmt.executeBatch();
            return results.length > 0;

        } catch (SQLException e) {
            ErrorHandler.logDatabaseError("updating mission units result", e);
        }
        return false;
    }

    private String capitalizeFirst(String str) {
        if (str == null || str.isEmpty())
            return str;
        return str.charAt(0) + str.substring(1).toLowerCase();
    }
}