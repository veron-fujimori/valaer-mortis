package valaermortis.dao;

import valaermortis.model.Building;
import valaermortis.model.enums.BuildingType;
import valaermortis.util.DB;
import valaermortis.util.ErrorHandler;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BuildingDao {
    public List<Building> getByUserId(String userId) {
        List<Building> buildings = new ArrayList<>();
        String sql = "SELECT id, user_id, type, level, upgrade_end FROM buildings WHERE user_id = ? ORDER BY type, level DESC";

        try (Connection conn = DB.getInstance().getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, userId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Building building = new Building();
                    building.setId(rs.getLong("id"));
                    building.setUserId(rs.getString("user_id"));
                    String typeStr = rs.getString("type");
                    if (typeStr != null) {
                        try {
                            building.setType(BuildingType.valueOf(typeStr.toUpperCase()));
                        } catch (IllegalArgumentException e) {
                            ErrorHandler.logError("Invalid building type: " + typeStr, e);
                            continue;
                        }
                    }

                    building.setLevel(rs.getInt("level"));

                    Timestamp upgradeEnd = rs.getTimestamp("upgrade_end");
                    building.setUpgradeEndTime(upgradeEnd);

                    buildings.add(building);
                }
            }
        } catch (SQLException e) {
            ErrorHandler.logDatabaseError("finding buildings by user ID", e);
        }
        return buildings;
    }

    public Building findByUserIdAndType(String userId, String type) {
        String sql = "SELECT id, user_id, type, level, upgrade_end FROM buildings WHERE user_id = ? AND type = ? ORDER BY level DESC LIMIT 1";

        try (Connection conn = DB.getInstance().getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, userId);
            ps.setString(2, type);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Building building = new Building();
                    building.setId(rs.getLong("id"));
                    building.setUserId(rs.getString("user_id"));

                    String typeStr = rs.getString("type");
                    if (typeStr != null) {
                        try {
                            building.setType(BuildingType.valueOf(typeStr.toUpperCase()));
                        } catch (IllegalArgumentException e) {
                            ErrorHandler.logError("Invalid building type: " + typeStr, e);
                            return null;
                        }
                    }

                    building.setLevel(rs.getInt("level"));
                    Timestamp upgradeEnd = rs.getTimestamp("upgrade_end");
                    building.setUpgradeEndTime(upgradeEnd);

                    return building;
                }
            }
        } catch (SQLException e) {
            ErrorHandler.logDatabaseError("finding building by user ID and type", e);
        }
        return null;
    }

    public Building getByUserIdAndType(String userId, BuildingType type) {
        return findByUserIdAndType(userId, type.name().toLowerCase());
    }

    public long createInitialBuildings(String userId) {
        try (Connection conn = DB.getInstance().getConnection()) {
            try (PreparedStatement ps1 = conn
                    .prepareStatement("INSERT INTO buildings (user_id, type, level) VALUES (?, 'townhall', 1)")) {
                ps1.setString(1, userId);
                ps1.executeUpdate();
            }

            try (PreparedStatement ps2 = conn
                    .prepareStatement("INSERT INTO buildings (user_id, type, level) VALUES (?, 'storage', 1)")) {
                ps2.setString(1, userId);
                ps2.executeUpdate();
            }

            try (PreparedStatement ps3 = conn.prepareStatement(
                    "INSERT INTO buildings (user_id, type, level) VALUES (?, 'barbarian_barrack', 1)",
                    Statement.RETURN_GENERATED_KEYS)) {
                ps3.setString(1, userId);
                ps3.executeUpdate();
                try (ResultSet rs = ps3.getGeneratedKeys()) {
                    if (rs.next()) {
                        return rs.getLong(1);
                    }
                }
            }
        } catch (SQLException e) {
            ErrorHandler.logDatabaseError("creating initial buildings", e);
        }
        return -1;
    }

    public long buildBarrack(String userId, String barrackType) {
        try (Connection conn = DB.getInstance().getConnection();
                PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO buildings (user_id, type, level) VALUES (?, ?, 1)",
                        Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, userId);
            ps.setString(2, barrackType);
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        } catch (SQLException e) {
            ErrorHandler.logDatabaseError("building barrack", e);
        }
        return -1;
    }

    public boolean create(Building building) {
        try (Connection conn = DB.getInstance().getConnection();
                PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO buildings (user_id, type, level, is_upgrading, upgrade_end) VALUES (?, ?, ?, ?, ?)",
                        Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, building.getUserId());
            ps.setString(2, building.getType().name().toLowerCase());
            ps.setInt(3, building.getLevel());
            ps.setBoolean(4, building.isUpgrading());
            ps.setTimestamp(5, building.getUpgradeEndTime());
            int result = ps.executeUpdate();

            if (result > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        building.setId(rs.getLong(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            ErrorHandler.logDatabaseError("creating building", e);
        }
        return false;
    }

    public boolean update(Building building) {
        try (Connection conn = DB.getInstance().getConnection();
                PreparedStatement ps = conn.prepareStatement(
                        "UPDATE buildings SET level = ?, is_upgrading = ?, upgrade_end = ? WHERE id = ?")) {

            ps.setInt(1, building.getLevel());
            ps.setBoolean(2, building.isUpgrading());
            ps.setTimestamp(3, building.getUpgradeEndTime());
            ps.setLong(4, building.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            ErrorHandler.logDatabaseError("updating building", e);
        }
        return false;
    }

    public int countBarracksByType(String userId, String barrackType) {
        try (Connection conn = DB.getInstance().getConnection();
                PreparedStatement ps = conn.prepareStatement(
                        "SELECT COUNT(*) FROM buildings WHERE user_id = ? AND type = ?")) {

            ps.setString(1, userId);
            ps.setString(2, barrackType);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            ErrorHandler.logDatabaseError("counting barracks by type", e);
        }
        return 0;
    }

    public Building getBuildingById(long id) {
        String sql = "SELECT id, user_id, type, level, upgrade_end FROM buildings WHERE id = ?";

        try (Connection conn = DB.getInstance().getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Building building = new Building();
                    building.setId(rs.getLong("id"));
                    building.setUserId(rs.getString("user_id"));

                    String typeStr = rs.getString("type");
                    if (typeStr != null) {
                        try {
                            building.setType(BuildingType.valueOf(typeStr.toUpperCase()));
                        } catch (IllegalArgumentException e) {
                            ErrorHandler.logError("Invalid building type: " + typeStr, e);
                            return null;
                        }
                    }

                    building.setLevel(rs.getInt("level"));
                    building.setUpgradeEndTime(rs.getTimestamp("upgrade_end"));

                    return building;
                }
            }
        } catch (SQLException e) {
            ErrorHandler.logDatabaseError("getting building by ID", e);
        }

        return null;
    }
}
