package valaermortis.service;

import valaermortis.core.AppContext;
import valaermortis.model.BattleResult;
import valaermortis.model.Creature;
import valaermortis.model.enums.UnitType;
import valaermortis.util.GameConfig;
import java.util.HashMap;
import java.util.Map;

public class BattleService {

    public BattleService(AppContext ctx) {
    }

    public BattleResult simulateBattle(Map<UnitType, Integer> attackingUnits, Creature creature) {
        int totalAttackPower = 0;
        for (Map.Entry<UnitType, Integer> entry : attackingUnits.entrySet()) {
            GameConfig.UnitStats stats = GameConfig.getUnitStats(entry.getKey());
            totalAttackPower += stats.attack * entry.getValue();
        }

        int battleTime = creature.getMaxBattleTime();
        int damageToCreature = totalAttackPower * battleTime;
        int damageToUnits = creature.getAttackPower() * battleTime;

        boolean victory = damageToCreature >= creature.getHp();

        Map<UnitType, Integer> unitsLost = new HashMap<>();
        Map<UnitType, Integer> survivingUnits = new HashMap<>();

        double totalDefense = 0;
        for (Map.Entry<UnitType, Integer> entry : attackingUnits.entrySet()) {
            GameConfig.UnitStats stats = GameConfig.getUnitStats(entry.getKey());
            totalDefense += entry.getValue() * (100.0 / stats.defenseModifier);
        }

        for (Map.Entry<UnitType, Integer> entry : attackingUnits.entrySet()) {
            UnitType unitType = entry.getKey();
            int unitCount = entry.getValue();
            GameConfig.UnitStats stats = GameConfig.getUnitStats(unitType);

            double unitDefense = unitCount * (100.0 / stats.defenseModifier);
            double damageRatio = unitDefense / totalDefense;
            double damageToThisType = damageToUnits * damageRatio;

            int lossCount = Math.min(unitCount, (int) Math.ceil(damageToThisType / 100.0));
            int survivingCount = unitCount - lossCount;

            unitsLost.put(unitType, lossCount);
            if (survivingCount > 0) {
                survivingUnits.put(unitType, survivingCount);
            }
        }

        return new BattleResult(victory, unitsLost, survivingUnits);
    }
}