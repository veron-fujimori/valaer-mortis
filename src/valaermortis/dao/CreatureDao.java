package valaermortis.dao;

import valaermortis.model.Creature;
import valaermortis.util.DB;
import valaermortis.util.ErrorHandler;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CreatureDao {
    public List<Creature> findAliveCreatures() {
        List<Creature> creatures = new ArrayList<>();
        try (Connection conn = DB.getInstance().getConnection();
                PreparedStatement ps = conn.prepareStatement(
                        "SELECT * FROM creatures ORDER BY level")) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Creature creature = new Creature();
                creature.setId(rs.getLong("id"));
                creature.setLevel(rs.getInt("level"));
                creature.setMaxHp(rs.getInt("max_hp"));
                creature.setAttackPower(rs.getInt("attack_power"));
                creature.setDistance(rs.getInt("distance"));
                creature.setRewardFood(rs.getInt("reward_food"));
                creature.setRewardWood(rs.getInt("reward_wood"));
                creature.setRewardStone(rs.getInt("reward_stone"));
                creature.setMaxBattleTime(rs.getInt("max_battle_time"));
                creatures.add(creature);
            }
        } catch (SQLException e) {
            ErrorHandler.logDatabaseError("finding alive creatures", e);
        }
        return creatures;
    }

    public List<Creature> getAvailableCreatures(int townhallLevel) {
        List<Creature> creatures = new ArrayList<>();

        int minLevel, maxLevel;
        if (townhallLevel == 1) {
            minLevel = 1;
            maxLevel = 1;
        } else if (townhallLevel <= 3) {
            minLevel = 1;
            maxLevel = townhallLevel;
        } else {
            minLevel = Math.max(1, townhallLevel - 2);
            maxLevel = townhallLevel;
        }
        try (Connection conn = DB.getInstance().getConnection();
                PreparedStatement ps = conn.prepareStatement(
                        "SELECT * FROM creatures WHERE level BETWEEN ? AND ? ORDER BY level LIMIT 5")) {
            ps.setInt(1, minLevel);
            ps.setInt(2, maxLevel);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Creature creature = new Creature();
                creature.setId(rs.getLong("id"));
                creature.setLevel(rs.getInt("level"));
                creature.setMaxHp(rs.getInt("max_hp"));
                creature.setAttackPower(rs.getInt("attack_power"));
                creature.setDistance(rs.getInt("distance"));
                creature.setRewardFood(rs.getInt("reward_food"));
                creature.setRewardWood(rs.getInt("reward_wood"));
                creature.setRewardStone(rs.getInt("reward_stone"));
                creature.setMaxBattleTime(rs.getInt("max_battle_time"));
                creatures.add(creature);
            }
        } catch (SQLException e) {
            ErrorHandler.logDatabaseError("getting available creatures by townhall level", e);
        }
        return creatures;
    }

    public Creature findById(long id) {
        try (Connection conn = DB.getInstance().getConnection();
                PreparedStatement ps = conn.prepareStatement(
                        "SELECT * FROM creatures WHERE id = ?")) {
            ps.setLong(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Creature creature = new Creature();
                creature.setId(rs.getLong("id"));
                creature.setLevel(rs.getInt("level"));
                creature.setMaxHp(rs.getInt("max_hp"));
                creature.setAttackPower(rs.getInt("attack_power"));
                creature.setDistance(rs.getInt("distance"));
                creature.setRewardFood(rs.getInt("reward_food"));
                creature.setRewardWood(rs.getInt("reward_wood"));
                creature.setRewardStone(rs.getInt("reward_stone"));
                creature.setMaxBattleTime(rs.getInt("max_battle_time"));
                return creature;
            }
        } catch (SQLException e) {
            ErrorHandler.logDatabaseError("finding creature by ID", e);
        }
        return null;
    }

    public boolean deleteCreature(long id) {
        try (Connection conn = DB.getInstance().getConnection();
                PreparedStatement ps = conn.prepareStatement(
                        "DELETE FROM creatures WHERE id = ?")) {
            ps.setLong(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            ErrorHandler.logDatabaseError("deleting creature", e);
        }
        return false;
    }
}
