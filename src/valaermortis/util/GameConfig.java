package valaermortis.util;

import valaermortis.model.enums.UnitType;
import valaermortis.model.enums.BuildingType;
import java.util.HashMap;
import java.util.Map;

public class GameConfig {
    public static final Map<String, Integer> BARRACK_MIN_TOWNHALL_LEVEL = new HashMap<String, Integer>() {
        {
            put("barbarian_barrack", 1);
            put("archer_barrack", 2);
            put("mage_barrack", 4);
            put("knight_barrack", 7);
            put("healer_barrack", 9);
        }
    };

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

    public static class CreatureStats {
        public final int hp, attackPower, rewardFood, rewardWood, rewardStone, maxBattleTime;

        public CreatureStats(int hp, int attackPower, int rewardFood, int rewardWood, int rewardStone,
                int maxBattleTime) {
            this.hp = hp;
            this.attackPower = attackPower;
            this.rewardFood = rewardFood;
            this.rewardWood = rewardWood;
            this.rewardStone = rewardStone;
            this.maxBattleTime = maxBattleTime;
        }
    }

    public static UpgradeCost getTownhallUpgradeCost(int toLevel) {
        if (toLevel >= 2 && toLevel <= 10) {
            StorageCapacity cap = getStorageCapacity(toLevel - 1);
            int food = (int) (cap.maxFood * 0.3);
            int wood = (int) (cap.maxWood * 0.3);
            int stone = (int) (cap.maxStone * 0.3);
            int timeSeconds = 4 + toLevel;
            return new UpgradeCost(food, wood, stone, timeSeconds);
        }
        return new UpgradeCost(0, 0, 0, 0);
    }

    public static UpgradeCost getStorageUpgradeCost(int toLevel) {
        if (toLevel >= 2 && toLevel <= 10) {
            StorageCapacity cap = getStorageCapacity(toLevel-1);
            int food = (int) (cap.maxFood * 0.10);
            int wood = (int) (cap.maxWood * 0.10);
            int stone = (int) (cap.maxStone * 0.10);
            int timeSeconds = 2 + toLevel;
            return new UpgradeCost(food, wood, stone, timeSeconds);
        }
        return new UpgradeCost(0, 0, 0, 0);
    }

    public static UpgradeCost getBarrackUpgradeCost(int toLevel) {
        if (toLevel >= 2 && toLevel <= 10) {
            StorageCapacity cap = getStorageCapacity(toLevel-1);
            int food = (int) (cap.maxFood * 0.07);
            int wood = (int) (cap.maxWood * 0.07);
            int stone = (int) (cap.maxStone * 0.07);
            int timeSeconds = 2 + toLevel;
            return new UpgradeCost(food, wood, stone, timeSeconds);
        }
        return new UpgradeCost(0, 0, 0, 0);
    }

    public static BuildCost getBarrackBuildCost(BuildingType buildingType, int townhallLevel) {
        String key = buildingType.toString().toLowerCase();
        int minTownhallLevel = BARRACK_MIN_TOWNHALL_LEVEL.getOrDefault(key, 1);

        StorageCapacity cap = getStorageCapacity(townhallLevel);
        int food = (int) (cap.maxFood * 0.05);
        int wood = (int) (cap.maxWood * 0.05);
        int stone = (int) (cap.maxStone * 0.05);
        int timeSeconds = 3 + townhallLevel;

        return new BuildCost(food, wood, stone, timeSeconds, minTownhallLevel);
    }

    public static StorageCapacity getStorageCapacity(int level) {
        long baseFood = 5000;
        long baseWood = 7000;
        long baseStone = 3000;
        double growth = 2.2;
        long maxFood = Math.round(baseFood * Math.pow(growth, level - 1));
        long maxWood = Math.round(baseWood * Math.pow(growth, level - 1));
        long maxStone = Math.round(baseStone * Math.pow(growth, level - 1));

        return new StorageCapacity(maxFood, maxWood, maxStone);
    }

    public static int getMaxBarracksPerType(int townhallLevel) {
        return 1 + (townhallLevel - 1) / 2;
    }

    public static int getMaxBarrackLevel(int townhallLevel) {
        return townhallLevel;
    }

    public static int getMaxStorageLevel(int townhallLevel) {
        return townhallLevel;
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
        Integer minLevel = BARRACK_MIN_TOWNHALL_LEVEL.get(barrackType);
        return minLevel != null && townhallLevel >= minLevel;
    }

    public static int getBarrackCapacity(int barrackLevel) {
        return (int) Math.round(15 * Math.pow(1.2, barrackLevel - 1));
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

    public static CreatureStats getCreatureStats(int level) {
        int hp = (int) Math.round(400 * Math.pow(1.6, level - 1));
        int attackPower = (int) Math.round(15 * Math.pow(1.5, level - 1));
        int rewardFood = 150 * level;
        int rewardWood = 100 * level;
        int rewardStone = 50 * level;
        int maxBattleTime = 3 + (level - 1) / 2;
        return new CreatureStats(hp, attackPower, rewardFood, rewardWood, rewardStone, maxBattleTime);
    }
}
