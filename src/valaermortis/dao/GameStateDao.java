package valaermortis.dao;

import valaermortis.model.GameState;
import valaermortis.util.DB;
import java.sql.*;

public class GameStateDao {
    
    public GameState getByUserId(long userId) {
        return findByUserId(userId);
    }
    
    public GameState findByUserId(long userId) {
        try (Connection conn = DB.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(
                "SELECT * FROM game_state WHERE user_id = ?")) {
            ps.setLong(1, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                GameState gs = new GameState();
                gs.setId(rs.getLong("id"));
                gs.setUserId(rs.getLong("user_id"));
                gs.setTownhallLvl(rs.getInt("townhall_lvl"));
                gs.setStorageLvl(rs.getInt("storage_lvl"));
                gs.setFood(rs.getLong("food"));
                gs.setWood(rs.getLong("wood"));
                gs.setStone(rs.getLong("stone"));
                gs.setMaxFood(rs.getLong("max_food"));
                gs.setMaxWood(rs.getLong("max_wood"));
                gs.setMaxStone(rs.getLong("max_stone"));
                gs.setUpdatedAt(rs.getString("updated_at"));
                return gs;
            }
        } catch (SQLException e) { 
            e.printStackTrace(); 
        }
        return null;
    }
      public long createInitialGameState(long userId) {
        try (Connection conn = DB.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO game_state (user_id, townhall_lvl, storage_lvl, food, wood, stone, max_food, max_wood, max_stone) VALUES (?, 1, 1, 2000, 2000, 1000, 5000, 7000, 3000)", 
                Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, userId);
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                return rs.getLong(1);
            }
        } catch (SQLException e) { 
            e.printStackTrace(); 
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
            ps.setLong(9, gameState.getUserId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { 
            e.printStackTrace(); 
        }
        return false;
    }
    
    public boolean updateResources(long userId, long food, long wood, long stone) {
        try (Connection conn = DB.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(
                "UPDATE game_state SET food = ?, wood = ?, stone = ? WHERE user_id = ?")) {
            ps.setLong(1, food);
            ps.setLong(2, wood);
            ps.setLong(3, stone);
            ps.setLong(4, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { 
            e.printStackTrace(); 
        }
        return false;
    }
    
    public boolean upgradeTownhall(long userId, int newLevel) {
        // Calculate new storage capacities based on townhall level
        long[] capacities = calculateStorageCapacities(newLevel);
        
        try (Connection conn = DB.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(
                "UPDATE game_state SET townhall_lvl = ?, storage_lvl = ?, max_food = ?, max_wood = ?, max_stone = ? WHERE user_id = ?")) {
            ps.setInt(1, newLevel);
            ps.setInt(2, newLevel); // Storage level auto-increases with townhall
            ps.setLong(3, capacities[0]); // max_food
            ps.setLong(4, capacities[1]); // max_wood
            ps.setLong(5, capacities[2]); // max_stone
            ps.setLong(6, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { 
            e.printStackTrace(); 
        }
        return false;
    }
    
    private long[] calculateStorageCapacities(int storageLevel) {
        // Based on README.md storage capacity table
        long[][] capacities = {
            {5000, 7000, 3000},       // Level 1
            {12000, 16000, 8000},     // Level 2
            {25000, 35000, 18000},    // Level 3
            {50000, 70000, 35000},    // Level 4
            {100000, 140000, 70000},  // Level 5
            {200000, 280000, 140000}, // Level 6
            {350000, 490000, 245000}, // Level 7
            {600000, 840000, 420000}, // Level 8
            {1000000, 1400000, 700000}, // Level 9
            {1600000, 2240000, 1120000} // Level 10
        };
        
        if (storageLevel >= 1 && storageLevel <= 10) {
            return capacities[storageLevel - 1];
        }
        return capacities[0]; // Default to level 1
    }
}
