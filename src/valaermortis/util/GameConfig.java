package valaermortis.util;

import valaermortis.model.enums.UnitType;
import valaermortis.model.enums.BuildingType;
import java.util.HashMap;
import java.util.Map;

public class GameConfig {
    
    // Townhall upgrade costs [food, wood, stone, time]
    public static final int[][] TOWNHALL_UPGRADE_COSTS = {
        {800, 1000, 500, 5},      // 1→2
        {1500, 2000, 1000, 6},    // 2→3
        {3000, 4000, 2000, 7},    // 3→4
        {5000, 7000, 3000, 7},    // 4→5
        {8000, 10000, 5000, 8},   // 5→6
        {12000, 15000, 7000, 8},  // 6→7
        {16000, 20000, 10000, 9}, // 7→8
        {24000, 30000, 15000, 9}, // 8→9
        {32000, 40000, 20000, 10} // 9→10
    };
    
    // Storage upgrade costs [food, wood, stone, time]
    public static final int[][] STORAGE_UPGRADE_COSTS = {
        {600, 800, 400, 2},       // 1→2
        {1200, 1500, 800, 3},     // 2→3
        {2400, 3000, 1500, 4},    // 3→4
        {4800, 6000, 3000, 5},    // 4→5
        {9600, 12000, 6000, 6},   // 5→6
        {19200, 25000, 12500, 7}, // 6→7
        {32000, 40000, 20000, 8}, // 7→8
        {54000, 70000, 35000, 9}, // 8→9
        {90000, 120000, 60000, 10}, // 9→10
        {150000, 200000, 100000, 10} // 10→11 (max)
    };
    
    // Barrack build costs [food, wood, stone, time, min_townhall_level]
    public static final Map<String, int[]> BARRACK_BUILD_COSTS = new HashMap<String, int[]>() {{
        put("barbarian_barrack", new int[]{500, 800, 400, 3, 1});
        put("archer_barrack", new int[]{600, 1000, 500, 4, 2});
        put("mage_barrack", new int[]{800, 1500, 800, 5, 4});
        put("knight_barrack", new int[]{1000, 2000, 1000, 6, 7});
        put("healer_barrack", new int[]{900, 1800, 900, 6, 9});
    }};
    
    // Barrack upgrade costs by level [food, wood, stone, time]
    public static final int[][] BARRACK_UPGRADE_COSTS = {
        {400, 500, 200, 2},       // 1→2
        {800, 1000, 500, 3},      // 2→3
        {1500, 2000, 1000, 4},    // 3→4
        {3000, 4000, 2000, 5},    // 4→5
        {6000, 8000, 4000, 6},    // 5→6
        {12000, 15000, 7500, 7},  // 6→7
        {20000, 25000, 12500, 8}, // 7→8
        {32000, 40000, 20000, 9}  // 8→9
    };
    
    // Barrack capacities by level
    public static final int[] BARRACK_CAPACITIES = {15, 30, 50, 80, 120, 170, 230, 300};
    
    // Unit training costs [food, wood, stone, time]
    public static final Map<UnitType, int[]> UNIT_TRAINING_COSTS = new HashMap<UnitType, int[]>() {{
        put(UnitType.BARBARIAN, new int[]{50, 0, 30, 3});
        put(UnitType.ARCHER, new int[]{40, 20, 0, 3});
        put(UnitType.MAGE, new int[]{60, 20, 20, 4});
        put(UnitType.KNIGHT, new int[]{55, 0, 35, 4});
        put(UnitType.HEALER, new int[]{45, 0, 0, 3});
    }};
    
    // Unit attributes [carry_capacity, speed, attack_power, defense_modifier]
    public static final Map<UnitType, double[]> UNIT_ATTRIBUTES = new HashMap<UnitType, double[]>() {{
        put(UnitType.BARBARIAN, new double[]{50, 1.0, 25, 1.0});
        put(UnitType.ARCHER, new double[]{30, 1.2, 20, 1.2});
        put(UnitType.MAGE, new double[]{20, 1.0, 35, 1.4});
        put(UnitType.KNIGHT, new double[]{40, 0.7, 30, 0.8});
        put(UnitType.HEALER, new double[]{20, 1.1, 10, 1.6});
    }};
    
    // Townhall progression limits [max_barrack_per_type, max_barrack_level, max_storage_level]
    public static final int[][] TOWNHALL_LIMITS = {
        {1, 2, 1},   // Level 1
        {2, 3, 2},   // Level 2
        {2, 3, 3},   // Level 3
        {3, 4, 4},   // Level 4
        {3, 4, 5},   // Level 5
        {4, 5, 6},   // Level 6
        {4, 5, 7},   // Level 7
        {5, 6, 8},   // Level 8
        {5, 7, 9},   // Level 9
        {6, 8, 10}   // Level 10
    };
    
    // Storage capacities by level [food, wood, stone]
    public static final long[][] STORAGE_CAPACITIES = {
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
    
    // Mining constants
    public static final int MINING_RATE_PER_UNIT = 20; // resource per second per unit
    public static final int TRAVEL_TIME_PER_DISTANCE = 1; // seconds per distance unit
    
    // Helper classes for structured return values
    public static class UpgradeCost {
        public final int food, wood, stone, timeSeconds;
        public UpgradeCost(int food, int wood, int stone, int timeSeconds) {
            this.food = food; this.wood = wood; this.stone = stone; this.timeSeconds = timeSeconds;
        }
    }
    
    public static class BuildCost {
        public final int food, wood, stone, timeSeconds, minTownhallLevel;
        public BuildCost(int food, int wood, int stone, int timeSeconds, int minTownhallLevel) {
            this.food = food; this.wood = wood; this.stone = stone; 
            this.timeSeconds = timeSeconds; this.minTownhallLevel = minTownhallLevel;
        }
    }
    
    public static class StorageCapacity {
        public final long maxFood, maxWood, maxStone;
        public StorageCapacity(long maxFood, long maxWood, long maxStone) {
            this.maxFood = maxFood; this.maxWood = maxWood; this.maxStone = maxStone;
        }
    }
    
    public static class UnitCost {
        public final int food, wood, stone, timeSeconds;
        public UnitCost(int food, int wood, int stone, int timeSeconds) {
            this.food = food; this.wood = wood; this.stone = stone; this.timeSeconds = timeSeconds;
        }
    }    public static class UnitStats {
        public final int carryCapacity, attackPower, hp, attack;
        public final double speed, defenseModifier;
        public UnitStats(int carryCapacity, double speed, int attackPower, double defenseModifier) {
            this.carryCapacity = carryCapacity; 
            this.speed = speed; 
            this.attackPower = attackPower; 
            this.defenseModifier = defenseModifier;
            this.hp = attackPower * 2; // Simple HP calculation based on attack power
            this.attack = attackPower; // Legacy compatibility
        }
    }
    
    // Get townhall upgrade cost
    public static UpgradeCost getTownhallUpgradeCost(int toLevel) {
        if (toLevel >= 2 && toLevel <= 10) {
            int[] cost = TOWNHALL_UPGRADE_COSTS[toLevel - 2];
            return new UpgradeCost(cost[0], cost[1], cost[2], cost[3]);
        }
        return new UpgradeCost(0, 0, 0, 0);
    }
    
    // Get storage upgrade cost
    public static UpgradeCost getStorageUpgradeCost(int toLevel) {
        if (toLevel >= 2 && toLevel <= 10) {
            int[] cost = STORAGE_UPGRADE_COSTS[toLevel - 2];
            return new UpgradeCost(cost[0], cost[1], cost[2], cost[3]);
        }
        return new UpgradeCost(0, 0, 0, 0);
    }
    
    // Get barrack upgrade cost
    public static UpgradeCost getBarrackUpgradeCost(int toLevel) {
        if (toLevel >= 2 && toLevel <= 8) {
            int[] cost = BARRACK_UPGRADE_COSTS[toLevel - 2];
            return new UpgradeCost(cost[0], cost[1], cost[2], cost[3]);
        }
        return new UpgradeCost(0, 0, 0, 0);
    }
    
    // Get barrack build cost
    public static BuildCost getBarrackBuildCost(BuildingType buildingType) {
        String key = buildingType.toString().toLowerCase();
        int[] cost = BARRACK_BUILD_COSTS.get(key);
        if (cost != null) {
            return new BuildCost(cost[0], cost[1], cost[2], cost[3], cost[4]);
        }
        return new BuildCost(0, 0, 0, 0, 1);
    }
    
    // Get storage capacity
    public static StorageCapacity getStorageCapacity(int level) {
        if (level >= 1 && level <= 10) {
            long[] capacity = STORAGE_CAPACITIES[level - 1];
            return new StorageCapacity(capacity[0], capacity[1], capacity[2]);
        }
        return new StorageCapacity(5000, 7000, 3000);
    }
    
    // Get max barracks per type based on townhall level
    public static int getMaxBarracksPerType(int townhallLevel) {
        if (townhallLevel >= 1 && townhallLevel <= 10) {
            return TOWNHALL_LIMITS[townhallLevel - 1][0];
        }
        return 1;
    }
    
    // Get max barrack level based on townhall level
    public static int getMaxBarrackLevel(int townhallLevel) {
        if (townhallLevel >= 1 && townhallLevel <= 10) {
            return TOWNHALL_LIMITS[townhallLevel - 1][1];
        }
        return 2;
    }
    
    // Get max storage level based on townhall level
    public static int getMaxStorageLevel(int townhallLevel) {
        if (townhallLevel >= 1 && townhallLevel <= 10) {
            return TOWNHALL_LIMITS[townhallLevel - 1][2];
        }
        return 1;
    }
    
    // Get unit carry capacity
    public static int getUnitCarryCapacity(UnitType unitType) {
        return (int) UNIT_ATTRIBUTES.get(unitType)[0];
    }
    
    // Get unit speed
    public static double getUnitSpeed(UnitType unitType) {
        return UNIT_ATTRIBUTES.get(unitType)[1];
    }
    
    // Get unit attack power
    public static int getUnitAttackPower(UnitType unitType) {
        return (int) UNIT_ATTRIBUTES.get(unitType)[2];
    }
    
    // Get unit defense modifier
    public static double getUnitDefenseModifier(UnitType unitType) {
        return UNIT_ATTRIBUTES.get(unitType)[3];
    }
    
    // Check if barrack type is unlocked at townhall level
    public static boolean isBarrackUnlocked(String barrackType, int townhallLevel) {
        int[] costs = BARRACK_BUILD_COSTS.get(barrackType);
        return costs != null && townhallLevel >= costs[4];
    }
    
    // Get barrack capacity by level
    public static int getBarrackCapacity(int barrackLevel) {
        if (barrackLevel >= 1 && barrackLevel <= BARRACK_CAPACITIES.length) {
            return BARRACK_CAPACITIES[barrackLevel - 1];
        }
        return BARRACK_CAPACITIES[0];
    }
    
    // Get unit training cost
    public static UnitCost getUnitCost(UnitType unitType) {
        int[] cost = UNIT_TRAINING_COSTS.get(unitType);
        if (cost != null) {
            return new UnitCost(cost[0], cost[1], cost[2], cost[3]);
        }
        return new UnitCost(0, 0, 0, 0);
    }
    
    // Get unit stats
    public static UnitStats getUnitStats(UnitType unitType) {
        double[] stats = UNIT_ATTRIBUTES.get(unitType);
        if (stats != null) {
            return new UnitStats((int)stats[0], stats[1], (int)stats[2], stats[3]);
        }
        return new UnitStats(0, 1.0, 0, 1.0);
    }
}
