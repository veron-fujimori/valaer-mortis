package valaermortis.model;

import valaermortis.model.enums.BuildingType;
import java.sql.Timestamp;

public class Building {
    private long id;
    private long userId;
    private BuildingType type;
    private int level;
    private Timestamp upgradeEndTime;
    private String createdAt;

    public Building() {}

    public Building(long id, long userId, BuildingType type, int level, Timestamp upgradeEndTime) {
        this.id = id;
        this.userId = userId;
        this.type = type;
        this.level = level;
        this.upgradeEndTime = upgradeEndTime;
        this.createdAt = new Timestamp(System.currentTimeMillis()).toString();
    }

    public Building(long id, long userId, String type, int level, boolean isUpgrading, String upgradeStart, String upgradeEnd, String createdAt) {
        this.id = id;
        this.userId = userId;
        this.type = BuildingType.valueOf(type.toUpperCase());
        this.level = level;
        this.upgradeEndTime = upgradeEnd != null ? Timestamp.valueOf(upgradeEnd) : null;
        this.createdAt = createdAt;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    
    public long getUserId() { return userId; }
    public void setUserId(long userId) { this.userId = userId; }
    
    public BuildingType getType() { return type; }
    public void setType(BuildingType type) { this.type = type; }
    
    public int getLevel() { return level; }
    public void setLevel(int level) { this.level = level; }
    
    public Timestamp getUpgradeEndTime() { return upgradeEndTime; }
    public void setUpgradeEndTime(Timestamp upgradeEndTime) { this.upgradeEndTime = upgradeEndTime; }
    
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    
    public boolean isUpgrading() { return upgradeEndTime != null; }
    public void setUpgrading(boolean upgrading) { }
    
    public String getUpgradeStart() { return null; }
    public void setUpgradeStart(String upgradeStart) { }
    
    public String getUpgradeEnd() { 
        return upgradeEndTime != null ? upgradeEndTime.toString() : null; 
    }
    public void setUpgradeEnd(String upgradeEnd) { 
        this.upgradeEndTime = upgradeEnd != null ? Timestamp.valueOf(upgradeEnd) : null;
    }
}
