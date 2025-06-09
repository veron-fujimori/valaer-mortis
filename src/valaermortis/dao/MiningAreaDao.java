package valaermortis.dao;

import valaermortis.model.MiningArea;
import valaermortis.model.enums.ResourceType;
import valaermortis.util.DB;
import valaermortis.util.ErrorHandler;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MiningAreaDao {
    public List<MiningArea> findActiveAreas() {
        List<MiningArea> areas = new ArrayList<>();
        try (Connection conn = DB.getInstance().getConnection();
                PreparedStatement ps = conn.prepareStatement(
                        "SELECT * FROM mining_areas WHERE current_stock > 0 ORDER BY resource_type, area_level")) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                MiningArea area = new MiningArea();
                area.setId(rs.getLong("id"));
                area.setResourceType(ResourceType.valueOf(rs.getString("resource_type").toUpperCase()));
                area.setAreaLevel(rs.getInt("area_level"));
                area.setCurrentStock(rs.getLong("current_stock"));
                area.setMaxStock(rs.getLong("max_stock"));
                area.setDistance(rs.getInt("distance"));
                areas.add(area);
            }
        } catch (SQLException e) {
            ErrorHandler.logDatabaseError("finding active mining areas", e);
        }
        return areas;
    }

    public List<MiningArea> getAvailableAreas() {
        List<MiningArea> allAreas = findActiveAreas();
        if (allAreas.size() > 5) {
            return allAreas.subList(0, 5);
        }
        return allAreas;
    }

    public MiningArea findById(long id) {
        try (Connection conn = DB.getInstance().getConnection();
                PreparedStatement ps = conn.prepareStatement(
                        "SELECT * FROM mining_areas WHERE id = ?")) {
            ps.setLong(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                MiningArea area = new MiningArea();
                area.setId(rs.getLong("id"));
                area.setResourceType(ResourceType.valueOf(rs.getString("resource_type").toUpperCase()));
                area.setAreaLevel(rs.getInt("area_level"));
                area.setCurrentStock(rs.getLong("current_stock"));
                area.setMaxStock(rs.getLong("max_stock"));
                area.setDistance(rs.getInt("distance"));
                return area;
            }
        } catch (SQLException e) {
            ErrorHandler.logDatabaseError("finding mining area by ID", e);
        }
        return null;
    }

    public boolean updateStock(long id, long newStock) {
        try (Connection conn = DB.getInstance().getConnection();
                PreparedStatement ps = conn.prepareStatement(
                        "UPDATE mining_areas SET current_stock = ? WHERE id = ?")) {
            ps.setLong(1, newStock);
            ps.setLong(2, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            ErrorHandler.logDatabaseError("updating mining area stock", e);
        }
        return false;
    }

    public boolean createMiningArea(MiningArea area) {
        try (Connection conn = DB.getInstance().getConnection();
                PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO mining_areas (resource_type, area_level, current_stock, max_stock, distance) VALUES (?, ?, ?, ?, ?)")) {
            ps.setString(1, area.getResourceType().name().toLowerCase());
            ps.setInt(2, area.getAreaLevel());
            ps.setLong(3, area.getCurrentStock());
            ps.setLong(4, area.getMaxStock());
            ps.setInt(5, area.getDistance());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            ErrorHandler.logDatabaseError("creating mining area", e);
        }
        return false;
    }

    public int countActiveAreas() {
        try (Connection conn = DB.getInstance().getConnection();
                PreparedStatement ps = conn.prepareStatement(
                        "SELECT COUNT(*) FROM mining_areas WHERE current_stock > 0")) {
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            ErrorHandler.logDatabaseError("counting active mining areas", e);
        }
        return 0;
    }
}
