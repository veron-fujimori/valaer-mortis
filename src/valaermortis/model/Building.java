package valaermortis.model;

import valaermortis.model.enums.BuildingType;
import java.sql.Timestamp;

public class Building {
    private long id;
    private String userId;
    private BuildingType type;
    private int level;
    private Timestamp upgradeEndTime;
    private String createdAt;

    public Building() {
    }

    public Building(long id, String userId, BuildingType type, int level, Timestamp upgradeEndTime) {
        this.id = id;
        this.userId = userId;
        this.type = type;
        this.level = level;
        this.upgradeEndTime = upgradeEndTime;
        this.createdAt = new Timestamp(System.currentTimeMillis()).toString();
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public BuildingType getType() {
        return type;
    }

    public void setType(BuildingType type) {
        this.type = type;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public Timestamp getUpgradeEndTime() {
        return upgradeEndTime;
    }

    public void setUpgradeEndTime(Timestamp upgradeEndTime) {
        this.upgradeEndTime = upgradeEndTime;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isUpgrading() {
        return upgradeEndTime != null;
    }

    public String getUpgradeEnd() {
        return upgradeEndTime != null ? upgradeEndTime.toString() : null;
    }

    public void setUpgradeEnd(String upgradeEnd) {
        this.upgradeEndTime = upgradeEnd != null ? Timestamp.valueOf(upgradeEnd) : null;
    }
}
