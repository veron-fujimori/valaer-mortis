package valaermortis.service;

import valaermortis.core.AppContext;
import valaermortis.dao.BuildingDao;
import valaermortis.model.Building;
import valaermortis.model.User;
import valaermortis.model.enums.BuildingType;
import valaermortis.util.GameConfig;
import valaermortis.util.InputUtil;
import valaermortis.util.TerminalArt;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;

public class BuildingService {
    private final AppContext ctx;
    private final BuildingDao buildingDao = new BuildingDao();
    private final ResourceService resourceService;
    private final ProgressService progressService;

    public BuildingService(AppContext ctx, ResourceService resourceService, ProgressService progressService) {
        this.ctx = ctx;
        this.resourceService = resourceService;
        this.progressService = progressService;
    }

    public List<Building> getUserBuildings() {
        return buildingDao.getByUserId(ctx.getCurrentUserId());
    }

    public Building getBuildingByType(BuildingType type) {
        return buildingDao.getByUserIdAndType(ctx.getCurrentUserId(), type);
    }

    public void displayBuildings() {
        ctx.gameService.refreshGameState();

        final boolean[] userWantsToExit = { false };
        final boolean[] hasUpgrading = { false };

        List<Building> buildings = getUserBuildings();
        for (Building building : buildings) {
            if (building.getUpgradeEndTime() != null) {
                hasUpgrading[0] = true;
                break;
            }
        }
        if (!hasUpgrading[0]) {
            InputUtil.clearTerminal();
            User user = ctx.getCurrentUser();
            TerminalArt.printMainHeader(user, ctx.gameService.getGameState());

            InputUtil.printSectionSeparator("YOUR BUILDINGS");
            for (Building building : buildings) {
                System.out.println(building.getType() + " Level " + building.getLevel());
            }
            System.out.println("========================================\n");
            InputUtil.pressEnterToContinue();
            return;
        }

        Thread inputThread = new Thread(() -> {
            InputUtil.waitForEnterInThread();
            userWantsToExit[0] = true;
        });
        inputThread.setDaemon(true);
        inputThread.start();
        while (!userWantsToExit[0]) {
            InputUtil.clearTerminal();
            ctx.gameService.refreshGameState();
            User user = ctx.getCurrentUser();
            TerminalArt.printMainHeader(user, ctx.gameService.getGameState());
            buildings = getUserBuildings();

            boolean stillHasUpgrading = false;

            InputUtil.printSectionSeparator("YOUR BUILDINGS");
            for (Building building : buildings) {
                if (building.getUpgradeEndTime() != null) {
                    long remainingSeconds = progressService.getBuildingUpgradeRemainingTime(building);
                    if (remainingSeconds > 0) {
                        long minutes = remainingSeconds / 60;
                        long seconds = remainingSeconds % 60;
                        String timeDisplay = String.format("%d:%02d", minutes, seconds);
                        System.out.println(building.getType() + " Level " + building.getLevel() +
                                TerminalArt.yellow(" [UPGRADING - " + timeDisplay + "]"));
                        stillHasUpgrading = true;
                    } else {
                        System.out.println(building.getType() + " Level " + building.getLevel() +
                                TerminalArt.green(" [UPGRADE COMPLETED!]"));
                    }
                } else {
                    System.out.println(building.getType() + " Level " + building.getLevel());
                }
            }
            System.out.println("====================");

            if (stillHasUpgrading) {
                System.out.println(TerminalArt.brightCyan("\nPress any key to stop live view and return to menu"));
                System.out.println(TerminalArt.brightCyan("(Upgrades continue in background)"));
            } else {
                System.out.println(TerminalArt.green("\nAll upgrades completed!"));
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                }
                break;
            }

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }

        if (inputThread.isAlive()) {
            inputThread.interrupt();
        }

        InputUtil.clearInputBuffer();

        try {
            Thread.sleep(100);
        } catch (Exception e) {
        }
    }

    public boolean upgradeTownhall() {
        Building townhall = getBuildingByType(BuildingType.TOWNHALL);
        if (townhall == null) {
            System.out.println(TerminalArt.red("Error: Townhall not found!"));
            return false;
        }

        if (townhall.getUpgradeEndTime() != null) {
            return false;
        }

        int currentLevel = townhall.getLevel();
        int nextLevel = currentLevel + 1;

        if (nextLevel > 10) {
            return false;
        }

        GameConfig.UpgradeCost cost = GameConfig.getTownhallUpgradeCost(nextLevel);

        if (!resourceService.hasEnoughResources(cost)) {
            return false;
        }

        if (!resourceService.deductResources(cost)) {
            System.out.println(TerminalArt.red("Failed to deduct resources!"));
            return false;
        }
        return startBuildingUpgrade(townhall, cost.timeSeconds);
    }

    public boolean upgradeStorage() {
        Building storage = getBuildingByType(BuildingType.STORAGE);
        if (storage == null) {
            System.out.println(TerminalArt.red("Error: Storage not found!"));
            return false;
        }

        if (storage.getUpgradeEndTime() != null) {
            return false;
        }

        int currentLevel = storage.getLevel();
        int nextLevel = currentLevel + 1;
        int maxLevel = GameConfig.getMaxStorageLevel(getBuildingByType(BuildingType.TOWNHALL).getLevel());

        if (nextLevel > maxLevel || nextLevel > 10) {
            return false;
        }

        GameConfig.UpgradeCost cost = GameConfig.getStorageUpgradeCost(nextLevel);

        if (!resourceService.hasEnoughResources(cost)) {
            return false;
        }

        if (!resourceService.deductResources(cost)) {
            System.out.println(TerminalArt.red("Failed to deduct resources!"));
            return false;
        }
        return startBuildingUpgrade(storage, cost.timeSeconds);
    }

    public List<BuildingType> getAvailableBarracksToBuild() {
        List<BuildingType> available = new ArrayList<>();
        List<Building> existing = getUserBuildings();

        Map<BuildingType, Integer> barrackCounts = new HashMap<>();
        for (Building building : existing) {
            if (building.getType() != BuildingType.TOWNHALL && building.getType() != BuildingType.STORAGE) {
                barrackCounts.put(building.getType(), barrackCounts.getOrDefault(building.getType(), 0) + 1);
            }
        }

        int townhallLevel = getBuildingByType(BuildingType.TOWNHALL).getLevel();
        int maxBarracksPerType = GameConfig.getMaxBarracksPerType(townhallLevel);

        if (townhallLevel >= 1 && barrackCounts.getOrDefault(BuildingType.BARBARIAN_BARRACK, 0) < maxBarracksPerType) {
            available.add(BuildingType.BARBARIAN_BARRACK);
        }
        if (townhallLevel >= 2 && barrackCounts.getOrDefault(BuildingType.ARCHER_BARRACK, 0) < maxBarracksPerType) {
            available.add(BuildingType.ARCHER_BARRACK);
        }
        if (townhallLevel >= 4 && barrackCounts.getOrDefault(BuildingType.MAGE_BARRACK, 0) < maxBarracksPerType) {
            available.add(BuildingType.MAGE_BARRACK);
        }
        if (townhallLevel >= 7 && barrackCounts.getOrDefault(BuildingType.KNIGHT_BARRACK, 0) < maxBarracksPerType) {
            available.add(BuildingType.KNIGHT_BARRACK);
        }
        if (townhallLevel >= 9 && barrackCounts.getOrDefault(BuildingType.HEALER_BARRACK, 0) < maxBarracksPerType) {
            available.add(BuildingType.HEALER_BARRACK);
        }

        return available;
    }

    public boolean buildBarrack(BuildingType type) {
        List<BuildingType> available = getAvailableBarracksToBuild();
        if (!available.contains(type)) {
            System.out.println(TerminalArt.red("\nCannot build this barrack type!"));
            return false;
        }

        int townhallLevel = getBuildingByType(BuildingType.TOWNHALL).getLevel();
        GameConfig.BuildCost cost = GameConfig.getBarrackBuildCost(type, townhallLevel);
        if (!resourceService.hasEnoughResources(cost)) {
            System.out.println(TerminalArt.red("\nNot enough resources!"));
            resourceService.displayResourceComparison(cost.food, cost.wood, cost.stone);
            return false;
        }

        if (!resourceService.deductResources(cost)) {
            System.out.println(TerminalArt.red("\nFailed to deduct resources!"));
            return false;
        }

        LocalDateTime endTime = LocalDateTime.now().plusSeconds(cost.timeSeconds);
        Building newBarrack = new Building(0, ctx.getCurrentUserId(), type, 0, Timestamp.valueOf(endTime));

        if (buildingDao.create(newBarrack)) {
            return true;
        }
        return false;
    }

    public List<Building> getUpgradeableBarracks() {
        List<Building> barracks = getUserBuildings();
        barracks.removeIf(b -> b.getType() == BuildingType.TOWNHALL || b.getType() == BuildingType.STORAGE);

        int townhallLevel = getBuildingByType(BuildingType.TOWNHALL).getLevel();
        int maxLevel = GameConfig.getMaxBarrackLevel(townhallLevel);

        barracks.removeIf(b -> b.getUpgradeEndTime() != null || b.getLevel() >= maxLevel);
        return barracks;
    }

    public List<Building> getAllBarracks() {
        List<Building> barracks = getUserBuildings();
        barracks.removeIf(b -> b.getType() == BuildingType.TOWNHALL || b.getType() == BuildingType.STORAGE);
        return barracks;
    }

    public boolean upgradeBarrack(Building barrack) {
        if (barrack.getUpgradeEndTime() != null) {
            System.out.println(TerminalArt.red("This barrack is already being upgraded!"));
            return false;
        }

        int townhallLevel = getBuildingByType(BuildingType.TOWNHALL).getLevel();
        int maxLevel = GameConfig.getMaxBarrackLevel(townhallLevel);

        if (barrack.getLevel() >= maxLevel) {
            System.out.println(TerminalArt.red("This barrack is already at maximum level for your townhall!"));
            return false;
        }

        GameConfig.UpgradeCost cost = GameConfig.getBarrackUpgradeCost(barrack.getLevel() + 1);

        if (!resourceService.hasEnoughResources(cost)) {
            System.out.println(TerminalArt.red("Not enough resources!"));
            return false;
        }

        if (!resourceService.deductResources(cost)) {
            System.out.println(TerminalArt.red("Failed to deduct resources!"));
            return false;
        }
        return startBuildingUpgrade(barrack, cost.timeSeconds);
    }

    private boolean startBuildingUpgrade(Building building, int durationSeconds) {
        LocalDateTime endTime = LocalDateTime.now().plusSeconds(durationSeconds);
        building.setUpgradeEndTime(Timestamp.valueOf(endTime));

        if (buildingDao.update(building)) {
            return true;
        }
        return false;
    }

    public boolean completeBuildingUpgrade(Building building) {
        building.setLevel(building.getLevel() + 1);
        building.setUpgradeEndTime(null);
        if (buildingDao.update(building)) {
            if (building.getLevel() == 1) {
                ctx.messageService.sendMessage(
                        "Building Construction Completed",
                        building.getType() + " has been constructed (level 1)!");
            } else {
                ctx.messageService.sendMessage(
                        "Building Upgrade Completed",
                        building.getType() + " has been upgraded to level " + building.getLevel() + "!");
            }

            if (building.getType() == BuildingType.STORAGE) {
                resourceService.updateStorageCapacities(building.getLevel());
            }

            return true;
        }
        return false;
    }

    public void checkAndCompleteUpgrades() {
        progressService.checkCompletedBuildingUpgrades();
    }
}
