package valaermortis.util;

import valaermortis.model.enums.UnitType;
import valaermortis.model.enums.BuildingType;
import java.util.HashMap;
import java.util.Map;

public class GameConfig {

    public static final int[][] TOWNHALL_UPGRADE_COSTS = {
            { 800, 1000, 500, 5 },
            { 1500, 2000, 1000, 6 },
            { 3000, 4000, 2000, 7 },
            { 5000, 7000, 3000, 7 },
            { 8000, 10000, 5000, 8 },
            { 12000, 15000, 7000, 8 },
            { 16000, 20000, 10000, 9 },
            { 24000, 30000, 15000, 9 },
            { 32000, 40000, 20000, 10 }
    };
    public static final int[][] STORAGE_UPGRADE_COSTS = {
            { 600, 800, 400, 2 },
            { 1200, 1500, 800, 3 },
            { 2400, 3000, 1500, 4 },
            { 4800, 6000, 3000, 5 },
            { 9600, 12000, 6000, 6 },
            { 19200, 25000, 12500, 7 },
            { 32000, 40000, 20000, 8 },
            { 54000, 70000, 35000, 9 },
            { 90000, 120000, 60000, 10 },
            { 150000, 200000, 100000, 10 }
    };
    public static final Map<String, int[]> BARRACK_BUILD_COSTS = new HashMap<String, int[]>() {
        {
            put("barbarian_barrack", new int[] { 500, 800, 400, 3, 1 });
            put("archer_barrack", new int[] { 600, 1000, 500, 4, 2 });
            put("mage_barrack", new int[] { 800, 1500, 800, 5, 4 });
            put("knight_barrack", new int[] { 1000, 2000, 1000, 6, 7 });
            put("healer_barrack", new int[] { 900, 1800, 900, 6, 9 });
        }
    };

    public static final int[][] BARRACK_UPGRADE_COSTS = {
            { 400, 500, 200, 2 },
            { 800, 1000, 500, 3 },
            { 1500, 2000, 1000, 4 },
            { 3000, 4000, 2000, 5 },
            { 6000, 8000, 4000, 6 },
            { 12000, 15000, 7500, 7 },
            { 20000, 25000, 12500, 8 },
            { 32000, 40000, 20000, 9 }
    };
    public static final int[] BARRACK_CAPACITIES = { 15, 30, 50, 80, 120, 170, 230, 300 };

    public static final Map<UnitType, int[]> UNIT_TRAINING_COSTS = new HashMap<UnitType, int[]>() {
        {
            put(UnitType.BARBARIAN, new int[] { 50, 0, 30, 3 });
            put(UnitType.ARCHER, new int[] { 40, 20, 0, 3 });
            put(UnitType.MAGE, new int[] { 60, 20, 20, 4 });
            put(UnitType.KNIGHT, new int[] { 55, 0, 35, 4 });
            put(UnitType.HEALER, new int[] { 45, 0, 0, 3 });
        }
    };

    public static final Map<UnitType, double[]> UNIT_ATTRIBUTES = new HashMap<UnitType, double[]>() {
        {
            put(UnitType.BARBARIAN, new double[] { 50, 1.0, 25, 1.0 });
            put(UnitType.ARCHER, new double[] { 30, 1.2, 20, 1.2 });
            put(UnitType.MAGE, new double[] { 20, 1.0, 35, 1.4 });
            put(UnitType.KNIGHT, new double[] { 40, 0.7, 30, 0.8 });
            put(UnitType.HEALER, new double[] { 20, 1.1, 10, 1.6 });
        }
    };

    public static final int[][] TOWNHALL_LIMITS = {
            { 1, 2, 1 },
            { 2, 3, 2 },
            { 2, 3, 3 },
            { 3, 4, 4 },
            { 3, 4, 5 },
            { 4, 5, 6 },
            { 4, 5, 7 },
            { 5, 6, 8 },
            { 5, 7, 9 },
            { 6, 8, 10 }
    };
    public static final long[][] STORAGE_CAPACITIES = {
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
    public static final int MINING_RATE_PER_UNIT = 20;
    public static final int TRAVEL_TIME_PER_DISTANCE = 1;

    public static class UpgradeCost {
        public final int food, wood, stone, timeSeconds;

        public UpgradeCost(int food, int wood, int stone, int timeSeconds) {
            this.food = food;
            this.wood = wood;
            this.stone = stone;
            this.timeSeconds = timeSeconds;
        }
    }

    public static class BuildCost {
        public final int food, wood, stone, timeSeconds, minTownhallLevel;

        public BuildCost(int food, int wood, int stone, int timeSeconds, int minTownhallLevel) {
            this.food = food;
            this.wood = wood;
            this.stone = stone;
            this.timeSeconds = timeSeconds;
            this.minTownhallLevel = minTownhallLevel;
        }
    }

    public static class StorageCapacity {
        public final long maxFood, maxWood, maxStone;

        public StorageCapacity(long maxFood, long maxWood, long maxStone) {
            this.maxFood = maxFood;
            this.maxWood = maxWood;
            this.maxStone = maxStone;
        }
    }

    public static class UnitCost {
        public final int food, wood, stone, timeSeconds;

        public UnitCost(int food, int wood, int stone, int timeSeconds) {
            this.food = food;
            this.wood = wood;
            this.stone = stone;
            this.timeSeconds = timeSeconds;
        }
    }

    public static class UnitStats {
        public final int carryCapacity, attackPower, hp, attack;
        public final double speed, defenseModifier;

        public UnitStats(int carryCapacity, double speed, int attackPower, double defenseModifier) {
            this.carryCapacity = carryCapacity;
            this.speed = speed;
            this.attackPower = attackPower;
            this.defenseModifier = defenseModifier;
            this.hp = attackPower * 2;
            this.attack = attackPower;
        }
    }

    public static UpgradeCost getTownhallUpgradeCost(int toLevel) {
        if (toLevel >= 2 && toLevel <= 10) {
            int[] cost = TOWNHALL_UPGRADE_COSTS[toLevel - 2];
            return new UpgradeCost(cost[0], cost[1], cost[2], cost[3]);
        }
        return new UpgradeCost(0, 0, 0, 0);
    }

    public static UpgradeCost getStorageUpgradeCost(int toLevel) {
        if (toLevel >= 2 && toLevel <= 10) {
            int[] cost = STORAGE_UPGRADE_COSTS[toLevel - 2];
            return new UpgradeCost(cost[0], cost[1], cost[2], cost[3]);
        }
        return new UpgradeCost(0, 0, 0, 0);
    }

    public static UpgradeCost getBarrackUpgradeCost(int toLevel) {
        if (toLevel >= 2 && toLevel <= 8) {
            int[] cost = BARRACK_UPGRADE_COSTS[toLevel - 2];
            return new UpgradeCost(cost[0], cost[1], cost[2], cost[3]);
        }
        return new UpgradeCost(0, 0, 0, 0);
    }

    public static BuildCost getBarrackBuildCost(BuildingType buildingType) {
        String key = buildingType.toString().toLowerCase();
        int[] cost = BARRACK_BUILD_COSTS.get(key);
        if (cost != null) {
            return new BuildCost(cost[0], cost[1], cost[2], cost[3], cost[4]);
        }
        return new BuildCost(0, 0, 0, 0, 1);
    }

    public static StorageCapacity getStorageCapacity(int level) {
        if (level >= 1 && level <= 10) {
            long[] capacity = STORAGE_CAPACITIES[level - 1];
            return new StorageCapacity(capacity[0], capacity[1], capacity[2]);
        }
        return new StorageCapacity(5000, 7000, 3000);
    }

    public static int getMaxBarracksPerType(int townhallLevel) {
        if (townhallLevel >= 1 && townhallLevel <= 10) {
            return TOWNHALL_LIMITS[townhallLevel - 1][0];
        }
        return 1;
    }

    public static int getMaxBarrackLevel(int townhallLevel) {
        if (townhallLevel >= 1 && townhallLevel <= 10) {
            return TOWNHALL_LIMITS[townhallLevel - 1][1];
        }
        return 2;
    }

    public static int getMaxStorageLevel(int townhallLevel) {
        if (townhallLevel >= 1 && townhallLevel <= 10) {
            return TOWNHALL_LIMITS[townhallLevel - 1][2];
        }
        return 1;
    }

    public static int getUnitCarryCapacity(UnitType unitType) {
        return (int) UNIT_ATTRIBUTES.get(unitType)[0];
    }

    public static double getUnitSpeed(UnitType unitType) {
        return UNIT_ATTRIBUTES.get(unitType)[1];
    }

    public static int getUnitAttackPower(UnitType unitType) {
        return (int) UNIT_ATTRIBUTES.get(unitType)[2];
    }

    public static double getUnitDefenseModifier(UnitType unitType) {
        return UNIT_ATTRIBUTES.get(unitType)[3];
    }

    public static boolean isBarrackUnlocked(String barrackType, int townhallLevel) {
        int[] costs = BARRACK_BUILD_COSTS.get(barrackType);
        return costs != null && townhallLevel >= costs[4];
    }

    public static int getBarrackCapacity(int barrackLevel) {
        if (barrackLevel >= 1 && barrackLevel <= BARRACK_CAPACITIES.length) {
            return BARRACK_CAPACITIES[barrackLevel - 1];
        }
        return BARRACK_CAPACITIES[0];
    }

    public static UnitCost getUnitCost(UnitType unitType) {
        int[] cost = UNIT_TRAINING_COSTS.get(unitType);
        if (cost != null) {
            return new UnitCost(cost[0], cost[1], cost[2], cost[3]);
        }
        return new UnitCost(0, 0, 0, 0);
    }

    public static UnitStats getUnitStats(UnitType unitType) {
        double[] stats = UNIT_ATTRIBUTES.get(unitType);
        if (stats != null) {
            return new UnitStats((int) stats[0], stats[1], (int) stats[2], stats[3]);
        }
        return new UnitStats(0, 1.0, 0, 1.0);
    }
}
