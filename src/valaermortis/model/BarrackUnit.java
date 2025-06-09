package valaermortis.model;

import valaermortis.model.enums.UnitType;

public class BarrackUnit {
  private long id;
  private long buildingId;
  private UnitType unitType;
  private int currentCount;
  private int maxCapacity;

  public BarrackUnit() {
  }

  public BarrackUnit(long id, long buildingId, UnitType unitType, int currentCount,
      int maxCapacity) {
    this.id = id;
    this.buildingId = buildingId;
    this.unitType = unitType;
    this.currentCount = currentCount;
    this.maxCapacity = maxCapacity;
  }

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public long getBuildingId() {
    return buildingId;
  }

  public void setBuildingId(long buildingId) {
    this.buildingId = buildingId;
  }

  public UnitType getUnitType() {
    return unitType;
  }

  public void setUnitType(UnitType unitType) {
    this.unitType = unitType;
  }

  public int getCurrentCount() {
    return currentCount;
  }

  public void setCurrentCount(int currentCount) {
    this.currentCount = currentCount;
  }

  public int getMaxCapacity() {
    return maxCapacity;
  }

  public void setMaxCapacity(int maxCapacity) {
    this.maxCapacity = maxCapacity;
  }

  public int getQuantity() {
    return currentCount;
  }

  public void setQuantity(int quantity) {
    this.currentCount = quantity;
  }

  public long getBarrackId() {
    return buildingId;
  }
}
