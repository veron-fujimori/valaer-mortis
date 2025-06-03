package valaermortis.model;

public class GameState {
    private long id;
    private long userId;
    private int townhallLvl;
    private int storageLvl;
    private long food;
    private long wood;
    private long stone;
    private long maxFood;
    private long maxWood;
    private long maxStone;
    private String updatedAt;

    public GameState() {}
    public GameState(long id, long userId, int townhallLvl, int storageLvl, long food, long wood, long stone, long maxFood, long maxWood, long maxStone, String updatedAt) {
        this.id = id;
        this.userId = userId;
        this.townhallLvl = townhallLvl;
        this.storageLvl = storageLvl;
        this.food = food;
        this.wood = wood;
        this.stone = stone;
        this.maxFood = maxFood;
        this.maxWood = maxWood;
        this.maxStone = maxStone;
        this.updatedAt = updatedAt;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public long getUserId() { return userId; }
    public void setUserId(long userId) { this.userId = userId; }
    public int getTownhallLvl() { return townhallLvl; }
    public void setTownhallLvl(int townhallLvl) { this.townhallLvl = townhallLvl; }
    public int getStorageLvl() { return storageLvl; }
    public void setStorageLvl(int storageLvl) { this.storageLvl = storageLvl; }
    public long getFood() { return food; }
    public void setFood(long food) { this.food = food; }
    public long getWood() { return wood; }
    public void setWood(long wood) { this.wood = wood; }
    public long getStone() { return stone; }
    public void setStone(long stone) { this.stone = stone; }
    public long getMaxFood() { return maxFood; }
    public void setMaxFood(long maxFood) { this.maxFood = maxFood; }
    public long getMaxWood() { return maxWood; }
    public void setMaxWood(long maxWood) { this.maxWood = maxWood; }
    public long getMaxStone() { return maxStone; }
    public void setMaxStone(long maxStone) { this.maxStone = maxStone; }
    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }
}