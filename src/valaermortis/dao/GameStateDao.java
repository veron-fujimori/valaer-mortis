package valaermortis.dao;

import valaermortis.model.GameState;
import valaermortis.util.DB;
import valaermortis.util.ErrorHandler;
import java.sql.*;

public class GameStateDao {
    public GameState getByUserId(String userId) {
        try (Connection conn = DB.getInstance().getConnection();
                PreparedStatement ps = conn.prepareStatement(
                        "SELECT * FROM game_state WHERE user_id = ?")) {
            ps.setString(1, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                GameState gs = new GameState();
                gs.setId(rs.getLong("id"));
                gs.setUserId(rs.getString("user_id"));
                gs.setTownhallLvl(rs.getInt("townhall_lvl"));
                gs.setStorageLvl(rs.getInt("storage_lvl"));
                gs.setFood(rs.getLong("food"));
                gs.setWood(rs.getLong("wood"));
                gs.setStone(rs.getLong("stone"));
                gs.setMaxFood(rs.getLong("max_food"));
                gs.setMaxWood(rs.getLong("max_wood"));
                gs.setMaxStone(rs.getLong("max_stone"));
                return gs;
            }
        } catch (SQLException e) {
            ErrorHandler.logDatabaseError("finding game state by user ID", e);
        }
        return null;
    }

    public long createInitialGameState(String userId) {
        try (Connection conn = DB.getInstance().getConnection();
                PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO game_state (user_id, townhall_lvl, storage_lvl, food, wood, stone, max_food, max_wood, max_stone) VALUES (?, 1, 1, 2000, 2000, 1000, 5000, 7000, 3000)",
                        Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, userId);
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                return rs.getLong(1);
            }
        } catch (SQLException e) {
            ErrorHandler.logDatabaseError("creating initial game state", e);
        }
        return -1;
    }

    public boolean update(GameState gameState) {
        try (Connection conn = DB.getInstance().getConnection();
                PreparedStatement ps = conn.prepareStatement(
                        "UPDATE game_state SET townhall_lvl = ?, storage_lvl = ?, food = ?, wood = ?, stone = ?, max_food = ?, max_wood = ?, max_stone = ? WHERE user_id = ?")) {
            ps.setInt(1, gameState.getTownhallLvl());
            ps.setInt(2, gameState.getStorageLvl());
            ps.setLong(3, gameState.getFood());
            ps.setLong(4, gameState.getWood());
            ps.setLong(5, gameState.getStone());
            ps.setLong(6, gameState.getMaxFood());
            ps.setLong(7, gameState.getMaxWood());
            ps.setLong(8, gameState.getMaxStone());
            ps.setString(9, gameState.getUserId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            ErrorHandler.logDatabaseError("updating game state", e);
        }
        return false;
    }

    public boolean updateResources(String userId, long food, long wood, long stone) {
        try (Connection conn = DB.getInstance().getConnection();
                PreparedStatement ps = conn.prepareStatement(
                        "UPDATE game_state SET food = ?, wood = ?, stone = ? WHERE user_id = ?")) {
            ps.setLong(1, food);
            ps.setLong(2, wood);
            ps.setLong(3, stone);
            ps.setString(4, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            ErrorHandler.logDatabaseError("updating resources", e);
        }
        return false;
    }

    public boolean upgradeTownhall(String userId, int newLevel) {
        long[] capacities = calculateStorageCapacities(newLevel);

        try (Connection conn = DB.getInstance().getConnection();
                PreparedStatement ps = conn.prepareStatement(
                        "UPDATE game_state SET townhall_lvl = ?, storage_lvl = ?, max_food = ?, max_wood = ?, max_stone = ? WHERE user_id = ?")) {
            ps.setInt(1, newLevel);
            ps.setInt(2, newLevel);
            ps.setLong(3, capacities[0]);
            ps.setLong(4, capacities[1]);
            ps.setLong(5, capacities[2]);
            ps.setString(6, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            ErrorHandler.logDatabaseError("upgrading townhall", e);
        }
        return false;
    }

    private long[] calculateStorageCapacities(int storageLevel) {
        long[][] capacities = {
                { 5000, 7000, 3000 },
                { 12000, 16000, 8000 },
                { 25000, 35000, 18000 },
                { 50000, 70000, 35000 },
                { 100000, 140000, 70000 },
                { 200000, 280000, 140000 },
                { 350000, 490000, 245000 },
                { 600000, 840000, 420000 },
                { 1000000, 1400000, 700000 },
                { 1600000, 2240000, 1120000 }
        };

        if (storageLevel >= 1 && storageLevel <= 10) {
            return capacities[storageLevel - 1];
        }
        return capacities[0];
    }
}
