package valaermortis.service;

import valaermortis.core.AppContext;
import valaermortis.dao.GameStateDao;
import valaermortis.model.GameState;
import valaermortis.model.enums.ResourceType;
import valaermortis.util.GameConfig;
import valaermortis.util.TerminalArt;

public class ResourceService {
    private final AppContext ctx;
    private final GameStateDao gameStateDao = new GameStateDao();

    public ResourceService(AppContext ctx) {
        this.ctx = ctx;
    }

    public GameState getCurrentResources() {
        return gameStateDao.getByUserId(ctx.getCurrentUserId());
    }

    public boolean hasEnoughResources(long food, long wood, long stone) {
        GameState gs = getCurrentResources();
        return gs.getFood() >= food && gs.getWood() >= wood && gs.getStone() >= stone;
    }

    public boolean hasEnoughResources(GameConfig.UpgradeCost cost) {
        return hasEnoughResources(cost.food, cost.wood, cost.stone);
    }

    public boolean hasEnoughResources(GameConfig.BuildCost cost) {
        return hasEnoughResources(cost.food, cost.wood, cost.stone);
    }

    public boolean deductResources(long food, long wood, long stone) {
        GameState gs = getCurrentResources();

        if (!hasEnoughResources(food, wood, stone)) {
            return false;
        }

        gs.setFood(gs.getFood() - food);
        gs.setWood(gs.getWood() - wood);
        gs.setStone(gs.getStone() - stone);
        return gameStateDao.update(gs);
    }

    public boolean deductResources(GameConfig.UpgradeCost cost) {
        return deductResources(cost.food, cost.wood, cost.stone);
    }

    public boolean deductResources(GameConfig.BuildCost cost) {
        return deductResources(cost.food, cost.wood, cost.stone);
    }

    public boolean addResources(long food, long wood, long stone) {
        GameState gs = getCurrentResources();

        long newFood = Math.min(gs.getFood() + food, gs.getMaxFood());
        long newWood = Math.min(gs.getWood() + wood, gs.getMaxWood());
        long newStone = Math.min(gs.getStone() + stone, gs.getMaxStone());

        gs.setFood(newFood);
        gs.setWood(newWood);
        gs.setStone(newStone);
        return gameStateDao.update(gs);
    }

    public boolean addResource(ResourceType type, long amount) {
        switch (type) {
            case FOOD:
                return addResources(amount, 0, 0);
            case WOOD:
                return addResources(0, amount, 0);
            case STONE:
                return addResources(0, 0, amount);
            default:
                return false;
        }
    }

    public boolean updateStorageCapacities(int storageLevel) {
        GameState gs = getCurrentResources();
        GameConfig.StorageCapacity capacity = GameConfig.getStorageCapacity(storageLevel);

        gs.setMaxFood(capacity.maxFood);
        gs.setMaxWood(capacity.maxWood);
        gs.setMaxStone(capacity.maxStone);
        gs.setStorageLvl(storageLevel);
        return gameStateDao.update(gs);
    }

    public GameConfig.StorageCapacity getCurrentCapacity() {
        GameState gs = getCurrentResources();
        return GameConfig.getStorageCapacity(gs.getStorageLvl());
    }

    public void displayResourceStatus() {
        GameState gs = getCurrentResources();
        System.out.println(TerminalArt.cyan("=== RESOURCES ==="));
        System.out.println("Food  : " + gs.getFood() + " / " + gs.getMaxFood());
        System.out.println("Wood  : " + gs.getWood() + " / " + gs.getMaxWood());
        System.out.println("Stone : " + gs.getStone() + " / " + gs.getMaxStone());
        System.out.println(TerminalArt.cyan("=================="));
    }

    public void displayResourceComparison(long requiredFood, long requiredWood, long requiredStone) {
        GameState gs = getCurrentResources();
        System.out.println("Required: " + requiredFood + "/" + requiredWood + "/" + requiredStone);
        System.out.println("Current:  " + gs.getFood() + "/" + gs.getWood() + "/" + gs.getStone());
        System.out.println();
    }

    public boolean isAtCapacity() {
        GameState gs = getCurrentResources();
        return gs.getFood() >= gs.getMaxFood() &&
                gs.getWood() >= gs.getMaxWood() && gs.getStone() >= gs.getMaxStone();
    }

    public long[] getAvailableStorage() {
        GameState gs = getCurrentResources();
        return new long[] {
                gs.getMaxFood() - gs.getFood(),
                gs.getMaxWood() - gs.getWood(),
                gs.getMaxStone() - gs.getStone()
        };
    }
}
