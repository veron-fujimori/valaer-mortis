package valaermortis.model;

import valaermortis.model.enums.UnitType;
import java.util.Map;

public class BattleResult {
    public final boolean victory;
    public final Map<UnitType, Integer> unitsLost;
    public final Map<UnitType, Integer> survivingUnits;

    public BattleResult(boolean victory, Map<UnitType, Integer> unitsLost, Map<UnitType, Integer> survivingUnits) {
        this.victory = victory;
        this.unitsLost = unitsLost;
        this.survivingUnits = survivingUnits;
    }
}
