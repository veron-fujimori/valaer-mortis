package valaermortis.model;

import valaermortis.model.enums.ResourceType;

public class MiningArea {
    private long id;
    private ResourceType resourceType;
    private int areaLevel;
    private long currentStock;
    private long maxStock;
    private int distance;
    private String regeneratedAt;
    private boolean isActive;

    public MiningArea() {}

    public MiningArea(long id, ResourceType resourceType, int areaLevel, long currentStock, long maxStock, int distance, String regeneratedAt, boolean isActive) {
        this.id = id;
        this.resourceType = resourceType;
        this.areaLevel = areaLevel;
        this.currentStock = currentStock;
        this.maxStock = maxStock;
        this.distance = distance;
        this.regeneratedAt = regeneratedAt;
        this.isActive = isActive;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    
    public ResourceType getResourceType() { return resourceType; }
    public void setResourceType(ResourceType resourceType) { this.resourceType = resourceType; }
    
    public int getAreaLevel() { return areaLevel; }
    public void setAreaLevel(int areaLevel) { this.areaLevel = areaLevel; }
    
    public long getCurrentStock() { return currentStock; }
    public void setCurrentStock(long currentStock) { this.currentStock = currentStock; }
    
    public long getMaxStock() { return maxStock; }
    public void setMaxStock(long maxStock) { this.maxStock = maxStock; }
    
    public int getDistance() { return distance; }
    public void setDistance(int distance) { this.distance = distance; }
    
    public String getRegeneratedAt() { return regeneratedAt; }
    public void setRegeneratedAt(String regeneratedAt) { this.regeneratedAt = regeneratedAt; }
      public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }
    
    public int getLevel() { return areaLevel; }
    public long getStock() { return currentStock; }
}
