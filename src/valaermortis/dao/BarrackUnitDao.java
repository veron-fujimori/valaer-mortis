package valaermortis.dao;

import valaermortis.model.BarrackUnit;
import valaermortis.model.enums.UnitType;
import valaermortis.util.DB;
import valaermortis.util.ErrorHandler;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BarrackUnitDao {
    public boolean create(BarrackUnit barrackUnit) {
        String sql = "INSERT INTO barrack_units (building_id, unit_type, current_count, max_capacity) " +
                "VALUES (?, ?, ?, ?)";

        try (Connection conn = DB.getInstance().getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setLong(1, barrackUnit.getBuildingId());
            stmt.setString(2, barrackUnit.getUnitType().toString());
            stmt.setInt(3, barrackUnit.getCurrentCount());
            stmt.setInt(4, barrackUnit.getMaxCapacity());

            int result = stmt.executeUpdate();
            if (result > 0) {
                ResultSet keys = stmt.getGeneratedKeys();
                if (keys.next()) {
                    barrackUnit.setId(keys.getLong(1));
                }
                return true;
            }
        } catch (SQLException e) {
            ErrorHandler.logDatabaseError("creating barrack unit", e);
        }
        return false;
    }

    public boolean update(BarrackUnit barrackUnit) {
        String sql = "UPDATE barrack_units SET current_count = ?, max_capacity = ? " +
                "WHERE id = ?";
        try (Connection conn = DB.getInstance().getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, barrackUnit.getCurrentCount());
            stmt.setInt(2, barrackUnit.getMaxCapacity());
            stmt.setLong(3, barrackUnit.getId());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            ErrorHandler.logDatabaseError("updating barrack unit", e);
        }
        return false;
    }

    public BarrackUnit getById(long id) {
        String sql = "SELECT * FROM barrack_units WHERE id = ?";
        try (Connection conn = DB.getInstance().getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return mapResultSetToBarrackUnit(rs);
            }
        } catch (SQLException e) {
            ErrorHandler.logDatabaseError("getting barrack unit by ID", e);
        }
        return null;
    }

    public List<BarrackUnit> getByBuildingId(long buildingId) {
        String sql = "SELECT * FROM barrack_units WHERE building_id = ? ORDER BY unit_type";
        List<BarrackUnit> units = new ArrayList<>();
        try (Connection conn = DB.getInstance().getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, buildingId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                units.add(mapResultSetToBarrackUnit(rs));
            }
        } catch (SQLException e) {
            ErrorHandler.logDatabaseError("getting barrack units by building ID", e);
        }
        return units;
    }

    public BarrackUnit getByBuildingAndUnitType(long buildingId, UnitType unitType) {
        String sql = "SELECT * FROM barrack_units WHERE building_id = ? AND unit_type = ?";
        try (Connection conn = DB.getInstance().getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, buildingId);
            stmt.setString(2, unitType.toString());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return mapResultSetToBarrackUnit(rs);
            }
        } catch (SQLException e) {
            ErrorHandler.logDatabaseError("getting barrack unit by building and unit type", e);
        }
        return null;
    }

    public List<BarrackUnit> getByUserId(String userId) {
        String sql = "SELECT bu.* FROM barrack_units bu " +
                "JOIN buildings b ON bu.building_id = b.id " +
                "WHERE b.user_id = ? ORDER BY bu.unit_type";
        List<BarrackUnit> units = new ArrayList<>();

        try (Connection conn = DB.getInstance().getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                units.add(mapResultSetToBarrackUnit(rs));
            }
        } catch (SQLException e) {
            ErrorHandler.logDatabaseError("getting barrack units by user ID", e);
        }
        return units;
    }

    public boolean updateUnitCount(long buildingId, UnitType unitType, int newCount) {
        String sql = "UPDATE barrack_units SET current_count = ? " +
                "WHERE building_id = ? AND unit_type = ?";

        try (Connection conn = DB.getInstance().getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, newCount);
            stmt.setLong(2, buildingId);
            stmt.setString(3, unitType.toString());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            ErrorHandler.logDatabaseError("updating unit count", e);
        }
        return false;
    }

    public boolean addUnits(long buildingId, UnitType unitType, int quantity) {
        String sql = "UPDATE barrack_units SET current_count = current_count + ? " +
                "WHERE building_id = ? AND unit_type = ?";

        try (Connection conn = DB.getInstance().getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, quantity);
            stmt.setLong(2, buildingId);
            stmt.setString(3, unitType.toString());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            ErrorHandler.logDatabaseError("adding units", e);
        }
        return false;
    }

    public boolean removeUnits(long buildingId, UnitType unitType, int quantity) {
        String sql = "UPDATE barrack_units SET current_count = GREATEST(0, current_count - ?) " +
                "WHERE building_id = ? AND unit_type = ?";

        try (Connection conn = DB.getInstance().getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, quantity);
            stmt.setLong(2, buildingId);
            stmt.setString(3, unitType.toString());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            ErrorHandler.logDatabaseError("removing units", e);
        }
        return false;
    }

    public boolean updateCapacity(long buildingId, int newCapacity) {
        String sql = "UPDATE barrack_units SET max_capacity = ? " +
                "WHERE building_id = ?";

        try (Connection conn = DB.getInstance().getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, newCapacity);
            stmt.setLong(2, buildingId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            ErrorHandler.logDatabaseError("updating capacity", e);
        }
        return false;
    }

    public boolean delete(long id) {
        String sql = "DELETE FROM barrack_units WHERE id = ?";

        try (Connection conn = DB.getInstance().getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            ErrorHandler.logDatabaseError("deleting barrack unit", e);
        }
        return false;
    }

    public boolean deleteByBuildingId(long buildingId) {
        String sql = "DELETE FROM barrack_units WHERE building_id = ?";

        try (Connection conn = DB.getInstance().getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, buildingId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            ErrorHandler.logDatabaseError("deleting barrack units by building ID", e);
        }
        return false;
    }

    private BarrackUnit mapResultSetToBarrackUnit(ResultSet rs) throws SQLException {
        BarrackUnit unit = new BarrackUnit();
        unit.setId(rs.getLong("id"));
        unit.setBuildingId(rs.getLong("building_id"));
        unit.setUnitType(UnitType.valueOf(rs.getString("unit_type").toUpperCase()));
        unit.setCurrentCount(rs.getInt("current_count"));
        unit.setMaxCapacity(rs.getInt("max_capacity"));
        return unit;
    }
}
