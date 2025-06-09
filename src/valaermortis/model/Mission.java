package valaermortis.model;

import valaermortis.model.enums.MissionType;
import valaermortis.model.enums.MissionStatus;
import java.sql.Timestamp;

public class Mission {
    private String id;
    private String userId;
    private MissionType type;
    private MissionStatus status;
    private Long miningAreaId;
    private Long creatureId;
    private Timestamp startTime;
    private Timestamp endTime;

    public Mission() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public MissionType getType() {
        return type;
    }

    public void setType(MissionType type) {
        this.type = type;
    }

    public MissionStatus getStatus() {
        return status;
    }

    public void setStatus(MissionStatus status) {
        this.status = status;
    }

    public Long getMiningAreaId() {
        return miningAreaId;
    }

    public void setMiningAreaId(Long miningAreaId) {
        this.miningAreaId = miningAreaId;
    }

    public Long getCreatureId() {
        return creatureId;
    }

    public void setCreatureId(Long creatureId) {
        this.creatureId = creatureId;
    }

    public Timestamp getStartTime() {
        return startTime;
    }

    public void setStartTime(Timestamp startTime) {
        this.startTime = startTime;
    }

    public Timestamp getEndTime() {
        return endTime;
    }

    public void setEndTime(Timestamp endTime) {
        this.endTime = endTime;
    }
}
