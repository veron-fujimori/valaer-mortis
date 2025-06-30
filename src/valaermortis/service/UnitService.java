package valaermortis.service;

import valaermortis.core.AppContext;
import valaermortis.dao.BarrackUnitDao;
import valaermortis.dao.BuildingDao;
import valaermortis.dao.UnitQueueDao;
import valaermortis.model.BarrackUnit;
import valaermortis.model.Building;
import valaermortis.model.UnitQueue;
import valaermortis.model.enums.BuildingType;
import valaermortis.model.enums.QueueStatus;
import valaermortis.model.enums.UnitType;
import valaermortis.util.GameConfig;
import valaermortis.util.InputUtil;
import valaermortis.util.TerminalArt;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.stream.Collectors;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;

public class UnitService {
    private final AppContext ctx;
    private final BarrackUnitDao barrackUnitDao = new BarrackUnitDao();
    private final BuildingDao buildingDao = new BuildingDao();
    private final UnitQueueDao unitQueueDao = new UnitQueueDao();
    private final ResourceService resourceService;

    public UnitService(AppContext ctx, ResourceService resourceService) {
        this.ctx = ctx;
        this.resourceService = resourceService;
    }

    public Map<Long, List<BarrackUnit>> getUserUnitsByBarrack() {
        List<BarrackUnit> allUnits = barrackUnitDao.getByUserId(ctx.getCurrentUserId());
        Map<Long, List<BarrackUnit>> unitsByBarrack = new HashMap<>();

        for (BarrackUnit unit : allUnits) {
            unitsByBarrack.computeIfAbsent(unit.getBarrackId(), k -> new ArrayList<>()).add(unit);
        }

        return unitsByBarrack;
    }

    public Map<UnitType, Integer> getTotalUnitCounts() {
        List<BarrackUnit> allUnits = barrackUnitDao.getByUserId(ctx.getCurrentUserId());
        Map<UnitType, Integer> counts = new HashMap<>();

        for (BarrackUnit unit : allUnits) {
            counts.put(unit.getUnitType(), counts.getOrDefault(unit.getUnitType(), 0) + unit.getQuantity());
        }

        return counts;
    }

    public void displayArmyStatus() {
        Map<UnitType, Integer> totalCounts = getTotalUnitCounts();
        List<Building> barracks = getBarracks();

        InputUtil.printSectionSeparator("ARMY STATUS");
        if (totalCounts.isEmpty()) {
            TerminalArt.printLine("No units trained yet.");
        } else {
            TerminalArt.printLine("Total Army:");
            for (Map.Entry<UnitType, Integer> entry : totalCounts.entrySet()) {
                TerminalArt.printLine("  " + entry.getKey() + ": " + entry.getValue());
            }
        }

        TerminalArt.printLine("\nBarracks Status:");
        for (Building barrack : barracks) {
            int capacity = GameConfig.getBarrackCapacity(barrack.getLevel());
            int currentUnits = getCurrentBarrackUnits(barrack.getId());
            String status = barrack.getUpgradeEndTime() != null ? " (UPGRADING)" : "";

            TerminalArt.printLine("  " + barrack.getType() + " Level " + barrack.getLevel() +
                    ": " + currentUnits + "/" + capacity + " units" + status);
        }
    }

    public List<Building> getBarracks() {
        List<Building> buildings = buildingDao.getByUserId(ctx.getCurrentUserId());
        buildings.removeIf(b -> b.getType() == BuildingType.TOWNHALL || b.getType() == BuildingType.STORAGE);
        return buildings;
    }

    public int getCurrentBarrackUnits(long barrackId) {
        List<BarrackUnit> units = barrackUnitDao.getByBuildingId(barrackId);
        return units.stream().mapToInt(BarrackUnit::getQuantity).sum();
    }

    public int getAvailableBarrackCapacity(Building barrack) {
        int maxCapacity = GameConfig.getBarrackCapacity(barrack.getLevel());
        int currentUnits = getCurrentBarrackUnits(barrack.getId());
        return maxCapacity - currentUnits;
    }

    public UnitType getBarrackUnitType(BuildingType barrackType) {
        switch (barrackType) {
            case BARBARIAN_BARRACK:
                return UnitType.BARBARIAN;
            case ARCHER_BARRACK:
                return UnitType.ARCHER;
            case MAGE_BARRACK:
                return UnitType.MAGE;
            case KNIGHT_BARRACK:
                return UnitType.KNIGHT;
            case HEALER_BARRACK:
                return UnitType.HEALER;
            default:
                return null;
        }
    }

    public UnitQueue trainUnits(Building barrack, int quantity) {
        if (barrack.getUpgradeEndTime() != null) {
            InputUtil.displayError("\nCannot train units while barrack is upgrading!");
            return null;
        }

        UnitType unitType = getBarrackUnitType(barrack.getType());
        if (unitType == null) {
            InputUtil.displayError("\nInvalid barrack type!");
            return null;
        }

        int availableCapacity = getAvailableBarrackCapacity(barrack);
        if (quantity > availableCapacity) {
            InputUtil.displayError("\nNot enough barrack capacity! Available: " + availableCapacity);
            return null;
        }

        GameConfig.UnitCost cost = GameConfig.getUnitCost(unitType);
        long totalFood = cost.food * quantity;
        long totalWood = cost.wood * quantity;
        long totalStone = cost.stone * quantity;
        if (!resourceService.hasEnoughResources(totalFood, totalWood, totalStone)) {
            InputUtil.displayError("\nNot enough resources!");
            resourceService.displayResourceComparison(totalFood, totalWood, totalStone);
            InputUtil.pressEnterToContinue();
            return null;
        }

        if (!resourceService.deductResources(totalFood, totalWood, totalStone)) {
            InputUtil.displayError("\nFailed to deduct resources!");
            InputUtil.pressEnterToContinue();
            return null;
        }

        UnitQueue unitQueue = new UnitQueue();
        unitQueue.setBuildingId(barrack.getId());
        unitQueue.setUnitType(unitType);
        unitQueue.setQuantity(quantity);

        int trainingTimePerUnit = cost.timeSeconds;
        int totalTrainingTime = trainingTimePerUnit * quantity;

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime endTime = now.plusSeconds(totalTrainingTime);

        unitQueue.setStartTime(Timestamp.valueOf(now));
        unitQueue.setEndTime(Timestamp.valueOf(endTime));
        unitQueue.setStatus(QueueStatus.TRAINING);

        boolean success = unitQueueDao.addToQueue(unitQueue);
        if (success) {
            return unitQueue;
        } else {
            InputUtil.displayError("Failed to start training!");
            resourceService.addResources(totalFood, totalWood, totalStone);
            return null;
        }
    }

    private boolean showTrainingProgressForBarrack(Building barrack) {
        final boolean[] userWantsToExit = { false };

        Thread inputThread = new Thread(() -> {
            try {
                InputUtil.waitForEnterInThread();
                userWantsToExit[0] = true;
            } catch (Exception e) {
                userWantsToExit[0] = true;
            }
        });
        inputThread.setDaemon(true);
        inputThread.start();

        try {
            while (!userWantsToExit[0]) {
                List<UnitQueue> activeQueues = unitQueueDao.getQueuesByBuildingId(barrack.getId())
                        .stream()
                        .filter(queue -> queue.getStatus() == QueueStatus.TRAINING)
                        .collect(Collectors.toList());

                if (activeQueues.isEmpty()) {
                    break;
                }

                InputUtil.clearTerminal();
                TerminalArt.printMainHeader(ctx.getCurrentUser(), ctx.resourceService.getCurrentResources());

                displayBarrackInfo(barrack);

                InputUtil.printSubsectionSeparator("Training Progress");

                boolean stillTraining = false;
                for (UnitQueue queue : activeQueues) {
                    long remainingSeconds = ctx.progressService.getUnitTrainingRemainingTime(queue);
                    if (remainingSeconds > 0) {
                        stillTraining = true;
                        long minutes = remainingSeconds / 60;
                        long seconds = remainingSeconds % 60;
                        String timeDisplay = String.format("%d:%02d", minutes, seconds);

                        UnitType unitType = queue.getUnitType();
                        System.out.println("┌─ Units in Training : " + queue.getQuantity() + " " + unitType);
                        System.out.println("└─ Time Remaining    : " + TerminalArt.white(timeDisplay));
                    }
                }

                if (!stillTraining) {
                    InputUtil.displaySuccess("Training completed!\n");
                    InputUtil.pressEnterToContinue();
                    break;
                }

                System.out.println("\nPress Enter to back to Training Troops Page...");

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    break;
                }
            }
        } finally {
            if (inputThread.isAlive()) {
                inputThread.interrupt();
            }

            InputUtil.clearInputBuffer();

            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        return userWantsToExit[0];
    }

    private void displayBarrackInfo(Building barrack) {
        UnitType unitType = getBarrackUnitType(barrack.getType());
        int capacity = GameConfig.getBarrackCapacity(barrack.getLevel());
        int currentUnits = getCurrentBarrackUnits(barrack.getId());
        GameConfig.UnitCost cost = GameConfig.getUnitCost(unitType);
        GameConfig.UnitStats stats = GameConfig.getUnitStats(unitType);

        InputUtil.printSectionSeparator(unitType + " TRAINING FACILITY");

        InputUtil.printSubsectionSeparator("Facility Information");
        System.out.println("┌─ Barrack Level    : Level " + barrack.getLevel());
        System.out.println("├─ Unit Capacity    : " + currentUnits + "/" + capacity + " units");

        String capacityStatus;
        if (currentUnits >= capacity) {
            capacityStatus = "FULL";
        } else if (currentUnits >= capacity * 0.8) {
            capacityStatus = "NEAR FULL";
        } else if (currentUnits >= capacity * 0.5) {
            capacityStatus = "MODERATE";
        } else {
            capacityStatus = "AVAILABLE";
        }
        System.out.println("└─ Status           : " + capacityStatus);

        InputUtil.printSubsectionSeparator("Unit Specifications");
        System.out.println("┌─ Unit Stats");
        System.out.println("│  ├─ HP             : " + stats.hp);
        System.out.println("│  ├─ Attack Power   : " + stats.attackPower);
        System.out.println("│  ├─ Defense        : " + stats.defenseModifier);
        System.out.println("│  ├─ Speed          : " + stats.speed + " units/second");
        System.out.println("│  └─ Carry Capacity : " + stats.carryCapacity + " resources");
        System.out.println("│");
        System.out.println("├─ Training Cost");
        System.out.println("│  ├─ Food        : " + cost.food + " per unit");
        System.out.println("│  ├─ Wood        : " + cost.wood + " per unit");
        System.out.println("│  └─ Stone       : " + cost.stone + " per unit");
        System.out.println("│");
        System.out.println("└─ Training Time  : " + cost.timeSeconds + " seconds per unit");
    }

    public void showTrainingProgressLive(UnitQueue queue) {
        final boolean[] isLive = { true };
        final boolean[] userWantsToExit = { false };

        Thread inputThread = new Thread(() -> {
            InputUtil.waitForEnterInThread();
            userWantsToExit[0] = true;
        });
        inputThread.setDaemon(true);
        inputThread.start();

        while (isLive[0] && !userWantsToExit[0]) {
            InputUtil.clearTerminal();

            TerminalArt.printMainHeader(ctx.getCurrentUser(), ctx.resourceService.getCurrentResources());

            UnitQueue freshQueue = unitQueueDao.getById(queue.getId());
            if (freshQueue == null || freshQueue.getStatus() != QueueStatus.TRAINING) {
                break;
            }

            long remainingSeconds = ctx.progressService.getUnitTrainingRemainingTime(freshQueue);

            Building barrack = buildingDao.getBuildingById(freshQueue.getBuildingId());
            String barrackInfo = barrack != null ? barrack.getType() + " (Level " + barrack.getLevel() + ")"
                    : "Unknown Barrack";

            InputUtil.printSectionSeparator("UNIT TRAINING IN PROGRESS");
            TerminalArt.printLine("Barrack         : " + barrackInfo);
            TerminalArt.printLine("Unit Type       : " + freshQueue.getUnitType());
            TerminalArt.printLine("Quantity        : " + freshQueue.getQuantity());
            if (remainingSeconds > 0) {
                long minutes = remainingSeconds / 60;
                long seconds = remainingSeconds % 60;
                String timeDisplay = String.format("%d:%02d", minutes, seconds);
                TerminalArt.printLine("Time Remaining  : " + TerminalArt.white(timeDisplay));
                InputUtil.displayInfo("\nPress enter to go back");

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            } else {
                InputUtil.displaySuccess("\nTraining completed!");
                System.out.println("Press Enter to Continue...");
                isLive[0] = false;
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

    public void collectCompletedUnits() {
        List<UnitQueue> completedQueues = unitQueueDao.getCompletedQueues(ctx.getCurrentUserId());
        if (completedQueues.isEmpty()) {
            InputUtil.displayInfo("No completed units to collect.");
            return;
        }

        InputUtil.printSectionSeparator("COLLECTING COMPLETED UNITS");
        for (UnitQueue queue : completedQueues) {
            Building barrack = buildingDao.getBuildingById(queue.getBuildingId());
            if (barrack == null) {
                InputUtil.displayError("Error: Barrack not found for completed training!");
                continue;
            }

            UnitType unitType = queue.getUnitType();
            int quantity = queue.getQuantity();

            BarrackUnit existingUnit = barrackUnitDao.getByBuildingAndUnitType(barrack.getId(), unitType);
            if (existingUnit != null) {
                existingUnit.setQuantity(existingUnit.getQuantity() + quantity);
                barrackUnitDao.update(existingUnit);
            } else {
                BarrackUnit newUnit = new BarrackUnit();
                newUnit.setBuildingId(barrack.getId());
                newUnit.setUnitType(unitType);
                newUnit.setCurrentCount(quantity);
                newUnit.setMaxCapacity(GameConfig.getBarrackCapacity(barrack.getLevel()));
                barrackUnitDao.create(newUnit);
            }

            InputUtil.displaySuccess("Collected " + quantity + " " + unitType + "(s)");

            unitQueueDao.deleteQueue(queue.getId());
        }

        System.out.println(TerminalArt.yellow("\nAll completed units have been added to your barracks."));
    }

    public void unitTrainingMenu() {
        barrackSelectionMenu();
    }

    private void barrackSelectionMenu() {
        List<Building> barracks = getBarracks();
        if (barracks.isEmpty()) {
            InputUtil.displayError("No barracks available! Build barracks first.");
            InputUtil.pressEnterToContinue();
            return;
        }
        while (true) {
            InputUtil.clearTerminal();
            TerminalArt.printMainHeader(ctx.getCurrentUser(), ctx.resourceService.getCurrentResources());

            InputUtil.printSectionSeparator("SELECT BARRACK FOR TRAINING");
            for (int i = 0; i < barracks.size(); i++) {
                Building barrack = barracks.get(i);
                UnitType unitType = getBarrackUnitType(barrack.getType());
                int capacity = GameConfig.getBarrackCapacity(barrack.getLevel());
                int currentUnits = getCurrentBarrackUnits(barrack.getId());

                List<UnitQueue> barrackQueues = unitQueueDao.getQueuesByBuildingId(barrack.getId());
                int trainingUnits = 0;
                for (UnitQueue queue : barrackQueues) {
                    if (queue.getStatus() == QueueStatus.TRAINING) {
                        trainingUnits += queue.getQuantity();
                    }
                }

                String status = "";
                if (barrack.getUpgradeEndTime() != null) {
                    status = " (Upgrading)";
                } else if (trainingUnits > 0) {
                    status = " (Training " + trainingUnits + ")";
                } else if (currentUnits >= capacity) {
                    status = " (Full)";
                }
                System.out.println("[" + (i + 1) + "] " + unitType + " Barrack (Level " + barrack.getLevel() +
                        ") - " + currentUnits + "/" + capacity + " units" + status);
            }
            System.out.println("[0] Back to Home Page");
            System.out.println();

            int choice = InputUtil.readIntWithMenuPrompt(InputUtil.createMenuPrompt("Choose Menu"), 0,
                    barracks.size());

            if (choice == 0) {
                return;
            }

            if (choice < 1 || choice > barracks.size()) {
                InputUtil.displayError("Invalid choice!");
                InputUtil.pressEnterToContinue();
                continue;
            }
            Building selectedBarrack = barracks.get(choice - 1);
            barrackDetailTrainingMenu(selectedBarrack);
            InputUtil.clearInputBuffer();
            try {
                Thread.sleep(100);
            } catch (Exception e) {
            }
        }
    }

    private void barrackDetailTrainingMenu(Building barrack) {
        while (true) {
            List<UnitQueue> barrackQueues = unitQueueDao.getQueuesByBuildingId(barrack.getId());
            boolean hasActiveTraining = barrackQueues.stream()
                    .anyMatch(queue -> queue.getStatus() == QueueStatus.TRAINING);

            if (hasActiveTraining) {
                boolean shouldExit = showTrainingProgressForBarrack(barrack);
                if (shouldExit) {
                    return;
                }
                continue;
            }

            InputUtil.clearTerminal();
            TerminalArt.printMainHeader(ctx.getCurrentUser(), ctx.resourceService.getCurrentResources());

            Building freshBarrack = buildingDao.getBuildingById(barrack.getId());
            if (freshBarrack == null) {
                InputUtil.displayError("Error: Barrack not found!");
                InputUtil.pressEnterToContinue();
                return;
            }
            barrack = freshBarrack;

            displayBarrackInfo(barrack);

            if (barrack.getUpgradeEndTime() != null) {
                InputUtil.displayInfo("Barrack is currently upgrading. Cannot train units.");
                InputUtil.displayInfo("Press Enter to go back to menu...");
                InputUtil.waitForEnter();
                return;
            }

            int availableCapacity = getAvailableBarrackCapacity(barrack);
            if (availableCapacity <= 0) {
                InputUtil.displayInfo("Barrack is full. Cannot train more units.");
                InputUtil.displayInfo("Press Enter to go back to menu...");
                InputUtil.waitForEnter();
                return;
            }

            int quantity = InputUtil.readIntInRange(
                    "\nHow many units to train (0 to go back)",
                    0,
                    availableCapacity);

            if (quantity == 0) {
                return;
            }

            UnitQueue trainingQueue = trainUnits(barrack, quantity);
            if (trainingQueue != null) {
                showTrainingProgressLive(trainingQueue);
            }

        }
    }

    public Map<UnitType, Integer> getAvailableUnitsForMission() {
        return getTotalUnitCounts();
    }

    public boolean deployUnitsForMission(Map<UnitType, Integer> unitsToDeplay) {
        Map<UnitType, Integer> available = getAvailableUnitsForMission();

        for (Map.Entry<UnitType, Integer> entry : unitsToDeplay.entrySet()) {
            UnitType unitType = entry.getKey();
            int required = entry.getValue();
            int availableCount = available.getOrDefault(unitType, 0);
            if (required > availableCount) {
                System.out.println(
                        "Not enough " + unitType + "! Required : " + required + ", Available  : " + availableCount);
                return false;
            }
        }

        for (Map.Entry<UnitType, Integer> entry : unitsToDeplay.entrySet()) {
            UnitType unitType = entry.getKey();
            int toDeploy = entry.getValue();

            List<Building> barracks = getBarracks();
            for (Building barrack : barracks) {
                if (toDeploy <= 0)
                    break;
                UnitType barrackUnitType = getBarrackUnitType(barrack.getType());
                if (barrackUnitType != unitType)
                    continue;

                BarrackUnit barrackUnit = barrackUnitDao.getByBuildingAndUnitType(barrack.getId(), unitType);
                if (barrackUnit == null || barrackUnit.getQuantity() <= 0)
                    continue;

                int toRemove = Math.min(toDeploy, barrackUnit.getQuantity());
                barrackUnit.setQuantity(barrackUnit.getQuantity() - toRemove);
                barrackUnitDao.update(barrackUnit);
                toDeploy -= toRemove;
            }
        }

        return true;
    }

    public boolean returnUnitsFromMission(Map<UnitType, Integer> unitsToReturn) {
        for (Map.Entry<UnitType, Integer> entry : unitsToReturn.entrySet()) {
            UnitType unitType = entry.getKey();
            int toReturn = entry.getValue();

            List<Building> barracks = getBarracks();
            for (Building barrack : barracks) {
                if (toReturn <= 0)
                    break;

                UnitType barrackUnitType = getBarrackUnitType(barrack.getType());
                if (barrackUnitType != unitType)
                    continue;

                int availableCapacity = getAvailableBarrackCapacity(barrack);
                if (availableCapacity <= 0)
                    continue;

                int toAdd = Math.min(toReturn, availableCapacity);
                BarrackUnit barrackUnit = barrackUnitDao.getByBuildingAndUnitType(barrack.getId(), unitType);
                if (barrackUnit != null) {
                    barrackUnit.setQuantity(barrackUnit.getQuantity() + toAdd);
                    barrackUnitDao.update(barrackUnit);
                } else {
                    BarrackUnit newUnit = new BarrackUnit();
                    newUnit.setBuildingId(barrack.getId());
                    newUnit.setUnitType(unitType);
                    newUnit.setCurrentCount(toAdd);
                    newUnit.setMaxCapacity(GameConfig.getBarrackCapacity(barrack.getLevel()));
                    barrackUnitDao.create(newUnit);
                }

                toReturn -= toAdd;
            }
            if (toReturn > 0) {
                System.out.println("Warning: " + toReturn + " " + unitType
                        + "(s) could not be returned due to lack of barrack capacity.");
            }
        }

        return true;
    }

    public void showLiveTrainingStatus() {
        List<UnitQueue> activeQueues = unitQueueDao.getActiveQueuesByUserId(ctx.getCurrentUserId());

        if (activeQueues.isEmpty()) {
            System.out.println(TerminalArt.yellow("No active unit training."));
            InputUtil.pressEnterToContinue();
            return;
        }
        final boolean[] userWantsToExit = { false };
        Thread inputThread = new Thread(() -> {
            InputUtil.waitForEnterInThread();
            userWantsToExit[0] = true;
        });
        inputThread.setDaemon(true);
        inputThread.start();

        while (!userWantsToExit[0]) {
            InputUtil.clearTerminal();

            TerminalArt.printMainHeader(ctx.getCurrentUser(), ctx.resourceService.getCurrentResources());

            List<UnitQueue> freshQueues = unitQueueDao.getActiveQueuesByUserId(ctx.getCurrentUserId());
            boolean stillHasTraining = false;

            System.out.println(TerminalArt.yellow("\n=== UNIT TRAINING STATUS ==="));

            if (freshQueues.isEmpty()) {
                System.out.println(TerminalArt.green("All training completed!"));
                InputUtil.pressEnterToContinue();
                break;
            }

            for (int i = 0; i < freshQueues.size(); i++) {
                UnitQueue queue = freshQueues.get(i);
                Building barrack = buildingDao.getBuildingById(queue.getBuildingId());

                if (barrack == null) {
                    System.out.println(TerminalArt.red("Error: Barrack not found for training ID " + queue.getId()));
                    continue;
                }

                System.out.println(TerminalArt.cyan("Training [" + (i + 1) + "]:"));
                System.out.println("  Barrack: " + barrack.getType() + " (Level " + barrack.getLevel() + ")");
                System.out.println("  Unit: " + queue.getUnitType());
                System.out.println("  Quantity: " + queue.getQuantity());

                long remainingSeconds = ctx.progressService.getUnitTrainingRemainingTime(queue);
                if (remainingSeconds > 0) {
                    stillHasTraining = true;
                    long minutes = remainingSeconds / 60;
                    long seconds = remainingSeconds % 60;
                    String timeDisplay = String.format("%d:%02d", minutes, seconds);
                    System.out.println("  Time Remaining: " + TerminalArt.brightYellow(timeDisplay));
                } else {
                    System.out.println("  Status: " + TerminalArt.green("COMPLETED - Ready to collect!"));
                }
                System.out.println();
            }

            if (stillHasTraining) {
                System.out.println(TerminalArt.brightCyan("\nPress Enter to back to menu"));
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            } else {
                System.out.println(TerminalArt.green("\nAll training completed!"));
                InputUtil.pressEnterToContinue();
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
}
