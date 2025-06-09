package valaermortis.model;

import java.sql.Timestamp;
import valaermortis.model.enums.UnitType;
import valaermortis.model.enums.QueueStatus;

public class UnitQueue {
    private long id;
    private long buildingId;
    private UnitType unitType;
    private int quantity;
    private Timestamp startTime;
    private Timestamp endTime;
    private QueueStatus status;

    public UnitQueue() {
    }

    public UnitQueue(long id, long buildingId, UnitType unitType, int quantity,
            Timestamp startTime, Timestamp endTime, QueueStatus status) {
        this.id = id;
        this.buildingId = buildingId;
        this.unitType = unitType;
        this.quantity = quantity;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = status;
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

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
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

    public QueueStatus getStatus() {
        return status;
    }

    public void setStatus(QueueStatus status) {
        this.status = status;
    }
}