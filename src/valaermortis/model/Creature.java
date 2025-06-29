package valaermortis.model;

public class Creature {
  private long id;
  private int level;
  private int maxHp;
  private int attackPower;
  private int distance;
  private int rewardFood;
  private int rewardWood;
  private int rewardStone;
  private int maxBattleTime;

  public Creature() {
  }

  public Creature(long id, int level, int maxHp, int attackPower, int distance,
      int rewardFood, int rewardWood, int rewardStone, int maxBattleTime) {
    this.id = id;
    this.level = level;
    this.maxHp = maxHp;
    this.attackPower = attackPower;
    this.distance = distance;
    this.rewardFood = rewardFood;
    this.rewardWood = rewardWood;
    this.rewardStone = rewardStone;
    this.maxBattleTime = maxBattleTime;
  }

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public int getLevel() {
    return level;
  }

  public void setLevel(int level) {
    this.level = level;
  }

  public int getMaxHp() {
    return maxHp;
  }

  public void setMaxHp(int maxHp) {
    this.maxHp = maxHp;
  }

  public int getAttackPower() {
    return attackPower;
  }

  public void setAttackPower(int attackPower) {
    this.attackPower = attackPower;
  }

  public int getDistance() {
    return distance;
  }

  public void setDistance(int distance) {
    this.distance = distance;
  }

  public int getRewardFood() {
    return rewardFood;
  }

  public void setRewardFood(int rewardFood) {
    this.rewardFood = rewardFood;
  }

  public int getRewardWood() {
    return rewardWood;
  }

  public void setRewardWood(int rewardWood) {
    this.rewardWood = rewardWood;
  }

  public int getRewardStone() {
    return rewardStone;
  }

  public void setRewardStone(int rewardStone) {
    this.rewardStone = rewardStone;
  }

  public int getMaxBattleTime() {
    return maxBattleTime;
  }

  public void setMaxBattleTime(int maxBattleTime) {
    this.maxBattleTime = maxBattleTime;
  }

  public String getName() {
    if (level >= 1 && level <= 3) {
      return "Goblin";
    } else if (level >= 4 && level <= 7) {
      return "Orc";
    } else if (level >= 8 && level <= 10) {
      return "Troll";
    } else {
      return "Unknown";
    }
  }

  public int getHp() {
    return maxHp;
  }
}
