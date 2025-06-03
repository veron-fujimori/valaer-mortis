package valaermortis.dao;

import valaermortis.model.Building;
import valaermortis.model.enums.BuildingType;
import valaermortis.util.DB;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BuildingDao {
    
    public List<Building> getByUserId(long userId) {
        return findByUserId(userId);
    }
      public List<Building> findByUserId(long userId) {
        List<Building> buildings = new ArrayList<>();
        try (Connection conn = DB.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(
                "SELECT * FROM buildings WHERE user_id = ? ORDER BY type, level DESC")) {
            ps.setLong(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Building building = new Building();
                building.setId(rs.getLong("id"));
                building.setUserId(rs.getLong("user_id"));
                building.setType(BuildingType.valueOf(rs.getString("type").toUpperCase()));
                building.setLevel(rs.getInt("level"));
                building.setUpgradeEndTime(rs.getTimestamp("upgrade_end"));
                building.setCreatedAt(rs.getString("created_at"));
                buildings.add(building);
            }
        } catch (SQLException e) { 
            e.printStackTrace(); 
        }
        return buildings;
    }
      public Building findByUserIdAndType(long userId, String type) {
        try (Connection conn = DB.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(
                "SELECT * FROM buildings WHERE user_id = ? AND type = ? ORDER BY level DESC LIMIT 1")) {
            ps.setLong(1, userId);
            ps.setString(2, type);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Building building = new Building();
                building.setId(rs.getLong("id"));
                building.setUserId(rs.getLong("user_id"));
                building.setType(BuildingType.valueOf(rs.getString("type").toUpperCase()));
                building.setLevel(rs.getInt("level"));
                building.setUpgradeEndTime(rs.getTimestamp("upgrade_end"));
                building.setCreatedAt(rs.getString("created_at"));
                return building;
            }
        } catch (SQLException e) { 
            e.printStackTrace(); 
        }
        return null;
    }
    
    public Building getByUserIdAndType(long userId, BuildingType type) {
        return findByUserIdAndType(userId, type.name().toLowerCase());
    }
    
    public long createInitialBuildings(long userId) {
        try (Connection conn = DB.getInstance().getConnection()) {
            // Create Townhall Level 1
            try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO buildings (user_id, type, level) VALUES (?, 'townhall', 1)")) {
                ps.setLong(1, userId);
                ps.executeUpdate();
            }
            
            // Create Storage Level 1
            try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO buildings (user_id, type, level) VALUES (?, 'storage', 1)")) {
                ps.setLong(1, userId);
                ps.executeUpdate();
            }
            
            // Create Barbarian Barrack Level 1
            try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO buildings (user_id, type, level) VALUES (?, 'barbarian_barrack', 1)", 
                Statement.RETURN_GENERATED_KEYS)) {
                ps.setLong(1, userId);
                ps.executeUpdate();
                ResultSet rs = ps.getGeneratedKeys();
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        } catch (SQLException e) { 
            e.printStackTrace(); 
        }
        return -1;
    }
    
    public long buildBarrack(long userId, String barrackType) {
        try (Connection conn = DB.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO buildings (user_id, type, level) VALUES (?, ?, 1)", 
                Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, userId);
            ps.setString(2, barrackType);
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                return rs.getLong(1);
            }
        } catch (SQLException e) { 
            e.printStackTrace(); 
        }
        return -1;
    }    public boolean create(Building building) {
        try (Connection conn = DB.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO buildings (user_id, type, level, is_upgrading, upgrade_end) VALUES (?, ?, ?, ?, ?)", 
                Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, building.getUserId());
            ps.setString(2, building.getType().name().toLowerCase());
            ps.setInt(3, building.getLevel());
            ps.setBoolean(4, building.isUpgrading());
            ps.setTimestamp(5, building.getUpgradeEndTime());
            int result = ps.executeUpdate();
            
            if (result > 0) {
                ResultSet rs = ps.getGeneratedKeys();
                if (rs.next()) {
                    building.setId(rs.getLong(1));
                }
                return true;
            }
        } catch (SQLException e) { 
            e.printStackTrace(); 
        }
        return false;
    }    public boolean update(Building building) {
        try (Connection conn = DB.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(
                "UPDATE buildings SET level = ?, is_upgrading = ?, upgrade_end = ? WHERE id = ?")) {
            ps.setInt(1, building.getLevel());
            ps.setBoolean(2, building.isUpgrading());
            ps.setTimestamp(3, building.getUpgradeEndTime());
            ps.setLong(4, building.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { 
            e.printStackTrace(); 
        }
        return false;
    }

    public int countBarracksByType(long userId, String barrackType) {
        try (Connection conn = DB.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(
                "SELECT COUNT(*) FROM buildings WHERE user_id = ? AND type = ?")) {
            ps.setLong(1, userId);
            ps.setString(2, barrackType);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) { 
            e.printStackTrace(); 
        }
        return 0;
    }
}
