package valaermortis.model;

import valaermortis.model.enums.MissionType;
import valaermortis.model.enums.MissionStatus;
import java.sql.Timestamp;

public class Mission {
    private long id;
    private long userId;
    private MissionType missionType;
    private MissionStatus status;
    private String unitsDeployed;
    private long targetAreaId;
    private Timestamp startTime;
    private Timestamp endTime;
    private int resourcesGainedFood;
    private int resourcesGainedWood;
    private int resourcesGainedStone;
    private String unitsReturning;
    private String createdAt;

    public Mission() {}

    public Mission(long id, long userId, MissionType missionType, MissionStatus status, 
                    String unitsDeployed, long targetAreaId, Timestamp startTime, Timestamp endTime,
                    int resourcesGainedFood, int resourcesGainedWood, int resourcesGainedStone,
                    String unitsReturning, String createdAt) {
        this.id = id;
        this.userId = userId;
        this.missionType = missionType;
        this.status = status;
        this.unitsDeployed = unitsDeployed;
        this.targetAreaId = targetAreaId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.resourcesGainedFood = resourcesGainedFood;
        this.resourcesGainedWood = resourcesGainedWood;
        this.resourcesGainedStone = resourcesGainedStone;
        this.unitsReturning = unitsReturning;
        this.createdAt = createdAt;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    
    public long getUserId() { return userId; }
    public void setUserId(long userId) { this.userId = userId; }
    
    public MissionType getMissionType() { return missionType; }
    public void setMissionType(MissionType missionType) { this.missionType = missionType; }
    
    public MissionStatus getStatus() { return status; }
    public void setStatus(MissionStatus status) { this.status = status; }
    
    public String getUnitsDeployed() { return unitsDeployed; }
    public void setUnitsDeployed(String unitsDeployed) { this.unitsDeployed = unitsDeployed; }
    
    public long getTargetAreaId() { return targetAreaId; }
    public void setTargetAreaId(long targetAreaId) { this.targetAreaId = targetAreaId; }
    
    public Timestamp getStartTime() { return startTime; }
    public void setStartTime(Timestamp startTime) { this.startTime = startTime; }
    
    public Timestamp getEndTime() { return endTime; }
    public void setEndTime(Timestamp endTime) { this.endTime = endTime; }
    
    public int getResourcesGainedFood() { return resourcesGainedFood; }
    public void setResourcesGainedFood(int resourcesGainedFood) { this.resourcesGainedFood = resourcesGainedFood; }
    
    public int getResourcesGainedWood() { return resourcesGainedWood; }
    public void setResourcesGainedWood(int resourcesGainedWood) { this.resourcesGainedWood = resourcesGainedWood; }
    
    public int getResourcesGainedStone() { return resourcesGainedStone; }
    public void setResourcesGainedStone(int resourcesGainedStone) { this.resourcesGainedStone = resourcesGainedStone; }
    
    public String getUnitsReturning() { return unitsReturning; }
    public void setUnitsReturning(String unitsReturning) { this.unitsReturning = unitsReturning; }
    
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    
    public MissionType getType() { return missionType; }
    public void setType(MissionType type) { this.missionType = type; }
    
    public Long getMiningAreaId() { 
        return missionType == MissionType.MINING ? targetAreaId : null; 
    }
    public void setMiningAreaId(Long miningAreaId) { 
        if (miningAreaId != null) this.targetAreaId = miningAreaId; 
    }
    
    public Long getCreatureId() { 
        return missionType == MissionType.ATTACK ? targetAreaId : null; 
    }
    public void setCreatureId(Long creatureId) { 
        if (creatureId != null) this.targetAreaId = creatureId; 
    }
    
    public void setTargetId(long targetId) { this.targetAreaId = targetId; }
    public int getResourceRewardFood() { return resourcesGainedFood; }
    public int getResourceRewardWood() { return resourcesGainedWood; }
    public int getResourceRewardStone() { return resourcesGainedStone; }
    public void setResourceRewardFood(int food) { this.resourcesGainedFood = food; }
    public void setResourceRewardWood(int wood) { this.resourcesGainedWood = wood; }
    public void setResourceRewardStone(int stone) { this.resourcesGainedStone = stone; }
}
