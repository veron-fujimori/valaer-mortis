package valaermortis.service;

import valaermortis.core.AppContext;
import valaermortis.dao.BarrackUnitDao;
import valaermortis.dao.BuildingDao;
import valaermortis.dao.GameStateDao;
import valaermortis.dao.MissionDao;
import valaermortis.dao.MissionResultsDao;
import valaermortis.dao.MissionUnitsDao;
import valaermortis.dao.UnitQueueDao;
import valaermortis.model.BarrackUnit;
import valaermortis.model.Building;
import valaermortis.model.Mission;
import valaermortis.model.UnitQueue;
import valaermortis.model.enums.BuildingType;
import valaermortis.model.enums.MissionType;
import valaermortis.util.GameConfig;
import valaermortis.util.InputUtil;
import valaermortis.util.TerminalArt;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class ProgressService {
    private final AppContext ctx;
    private final BuildingDao buildingDao = new BuildingDao();
    private final GameStateDao gameStateDao = new GameStateDao();
    private final MissionDao missionDao = new MissionDao();
    private final UnitQueueDao unitQueueDao = new UnitQueueDao();

    public ProgressService(AppContext ctx) {
        this.ctx = ctx;
    }

    public void checkAndCompleteAllActivities() {
        checkCompletedBuildingUpgrades();
        checkCompletedMissions();
        checkCompletedUnitTraining();
    }

    public void checkCompletedBuildingUpgrades() {
        LocalDateTime now = LocalDateTime.now();
        List<Building> buildings = buildingDao.getByUserId(ctx.getCurrentUserId());

        for (Building building : buildings) {
            if (building.getUpgradeEndTime() != null &&
                    building.getUpgradeEndTime().toLocalDateTime().isBefore(now)) {
                completeBuildingUpgrade(building);
            }
        }
    }

    private void completeBuildingUpgrade(Building building) {
        if (!ctx.buildingService.completeBuildingUpgrade(building)) {
            System.err.println("Failed to complete building upgrade in BuildingService");
            return;
        }

        if (building.getType() == BuildingType.TOWNHALL) {
            boolean updateResult = gameStateDao.upgradeTownhall(ctx.getCurrentUserId(), building.getLevel());
            if (!updateResult) {
                System.err.println("ERROR: Failed to update townhall level in game_state table");
            }
        }

        if (building.getType() == BuildingType.STORAGE) {
            ctx.resourceService.updateStorageCapacities(building.getLevel());
        }
    }

    public void checkCompletedMissions() {
        LocalDateTime now = LocalDateTime.now();
        List<Mission> missions = missionDao.getActiveMissions(ctx.getCurrentUserId());

        for (Mission mission : missions) {
            if (mission.getEndTime() != null &&
                    mission.getEndTime().toLocalDateTime().isBefore(now)) {
                completeMission(mission);
            }
        }
    }

    private void completeMission(Mission mission) {
        boolean success = ctx.missionService.completeMission(mission);

        if (success) {
            MissionResultsDao missionResultsDao = new MissionResultsDao();
            MissionResultsDao.MissionResult result = missionResultsDao.getMissionResult(mission.getId());
            MissionUnitsDao missionUnitsDao = new MissionUnitsDao();
            java.util.Map<valaermortis.model.enums.UnitType, Integer> unitsSent = missionUnitsDao
                    .getMissionUnits(mission.getId());
            java.util.Map<valaermortis.model.enums.UnitType, Integer> unitsReturned = missionUnitsDao
                    .getSurvivingUnits(mission.getId());

            StringBuilder details = new StringBuilder();

            if (!unitsSent.isEmpty()) {
                details.append("\n\nTROOPS SENT");
                details.append("\n--------------");
                for (java.util.Map.Entry<valaermortis.model.enums.UnitType, Integer> entry : unitsSent.entrySet()) {
                    details.append("\n").append(entry.getValue()).append(" ").append(entry.getKey().name());
                }
            }

            if (mission.getType() == MissionType.MINING && result != null) {
                int totalResources = result.getFoodGained() + result.getWoodGained() + result.getStoneGained();
                String resourceType = "";
                if (result.getFoodGained() > 0)
                    resourceType = "FOOD";
                else if (result.getWoodGained() > 0)
                    resourceType = "WOOD";
                else if (result.getStoneGained() > 0)
                    resourceType = "STONE";

                details.append("\n\nMINING RESULTS");
                details.append("\n--------------");
                details.append("\n- ").append(totalResources).append(" ").append(resourceType);

                if (!unitsReturned.isEmpty()) {
                    details.append("\n\nTROOPS RETURNED");
                    details.append("\n--------------");
                    for (java.util.Map.Entry<valaermortis.model.enums.UnitType, Integer> entry : unitsReturned
                            .entrySet()) {
                        details.append("\n").append(entry.getValue()).append(" ").append(entry.getKey().name());
                    }
                }
            } else if (mission.getType() == MissionType.ATTACK && result != null) {
                boolean victorious = result.isSuccess();
                java.util.Map<valaermortis.model.enums.UnitType, Integer> casualties = new java.util.HashMap<>();
                int totalCasualties = 0;
                for (java.util.Map.Entry<valaermortis.model.enums.UnitType, Integer> entry : unitsSent.entrySet()) {
                    valaermortis.model.enums.UnitType unitType = entry.getKey();
                    int sent = entry.getValue();
                    int returned = unitsReturned.getOrDefault(unitType, 0);
                    int lost = sent - returned;
                    if (lost > 0) {
                        casualties.put(unitType, lost);
                        totalCasualties += lost;
                    }
                }

                if (totalCasualties > 0) {
                    details.append("\n\nLOSSES");
                    details.append("\n--------------\n");
                    details.append(totalCasualties).append(" units");
                    for (java.util.Map.Entry<valaermortis.model.enums.UnitType, Integer> entry : casualties
                            .entrySet()) {
                        details.append("\n- ").append(entry.getValue()).append(" ").append(entry.getKey().name());
                    }
                } else {
                    details.append("\nNo casualties - all troops returned safely!");
                }

                if (!unitsReturned.isEmpty()) {
                    details.append("\n\nTROOPS RETURNED");
                    details.append("\n--------------");
                    int totalSurvivors = 0;
                    for (java.util.Map.Entry<valaermortis.model.enums.UnitType, Integer> entry : unitsReturned
                            .entrySet()) {
                        details.append("\n").append(entry.getValue()).append(" ").append(entry.getKey().name());
                        totalSurvivors += entry.getValue();
                    }
                    details.append("\nTotal survivors: ").append(totalSurvivors).append(" units");
                }

                if (victorious) {
                    int totalRewards = result.getFoodGained() + result.getWoodGained() + result.getStoneGained();
                    if (totalRewards > 0) {
                        details.append("\n\nREWARDS");
                        details.append("\n--------------");
                        details.append("\nTotal plunder: ").append(totalRewards).append(" resources");
                        if (result.getFoodGained() > 0)
                            details.append(" (Food: ").append(result.getFoodGained());
                        if (result.getWoodGained() > 0)
                            details.append(", Wood: ").append(result.getWoodGained());
                        if (result.getStoneGained() > 0)
                            details.append(", Stone: ").append(result.getStoneGained());
                        details.append(")");
                    }
                    details.append("\n\nMISSION SUCCESS - VICTORY!");
                } else {
                    details.append("\n\nNo rewards - MISSION FAILED");
                }
            }

            ctx.messageService.sendMissionCompletedMessage(mission.getType().toString(), details.toString());} 
            else {
            System.err.println("ERROR: Failed to complete mission " + mission.getId());
        }
    }

    public void checkCompletedUnitTraining() {
        LocalDateTime now = LocalDateTime.now();
        List<UnitQueue> activeQueues = unitQueueDao.getActiveQueuesByUserId(ctx.getCurrentUserId());

        for (UnitQueue queue : activeQueues) {
            if (queue.getEndTime() != null &&
                    queue.getEndTime().toLocalDateTime().isBefore(now)) {

                completeUnitTraining(queue);
            }
        }
    }

    private void completeUnitTraining(UnitQueue queue) {
        unitQueueDao.completeTraining(queue);

        Building barrack = buildingDao.getBuildingById(queue.getBuildingId());
        if (barrack != null) {
            BarrackUnitDao barrackUnitDao = new BarrackUnitDao();
            BarrackUnit existingUnit = barrackUnitDao.getByBuildingAndUnitType(barrack.getId(), queue.getUnitType());

            if (existingUnit != null) {
                existingUnit.setQuantity(existingUnit.getQuantity() + queue.getQuantity());
                barrackUnitDao.update(existingUnit);
            } else {
                BarrackUnit newUnit = new BarrackUnit();
                newUnit.setBuildingId(barrack.getId());
                newUnit.setUnitType(queue.getUnitType());
                newUnit.setCurrentCount(queue.getQuantity());
                newUnit.setMaxCapacity(GameConfig.getBarrackCapacity(barrack.getLevel()));
                barrackUnitDao.create(newUnit);
            }

            unitQueueDao.deleteQueue(queue.getId());
        }

        String message = queue.getQuantity() + " " + queue.getUnitType() + " units completed training!";
        ctx.messageService.sendMessage("Unit Training Completed", message);
    }

    public void displayActiveBuildingUpgrades() {
        List<Building> buildings = buildingDao.getByUserId(ctx.getCurrentUserId());
        boolean hasActive = false;
        for (Building building : buildings) {
            if (building.getUpgradeEndTime() != null) {
                if (!hasActive) {
                    InputUtil.printSectionSeparator("ACTIVE BUILDING UPGRADES");
                    hasActive = true;
                }

                LocalDateTime endTime = building.getUpgradeEndTime().toLocalDateTime();
                LocalDateTime now = LocalDateTime.now();
                long remainingSeconds = ChronoUnit.SECONDS.between(now, endTime);

                if (remainingSeconds > 0) {
                    TerminalArt.printLine(building.getType() + " upgrade: " + remainingSeconds + " seconds remaining");
                } else {
                    TerminalArt.printLine(building.getType() + " upgrade: Ready to complete!");
                }
            }
        }

        if (hasActive) {
            System.out.println("====================\n");
        }
    }

    public void displayActiveMissions() {
        List<Mission> missions = missionDao.getActiveMissions(ctx.getCurrentUserId());
        boolean hasActive = false;

        for (Mission mission : missions) {
            if (mission.getEndTime() != null) {
                if (!hasActive) {
                    System.out.println(TerminalArt.cyan("=== ACTIVE MISSIONS ==="));
                    hasActive = true;
                }

                LocalDateTime endTime = mission.getEndTime().toLocalDateTime();
                LocalDateTime now = LocalDateTime.now();
                long remainingSeconds = ChronoUnit.SECONDS.between(now, endTime);

                if (remainingSeconds > 0) {
                    TerminalArt.printLine(mission.getType() + " mission: " + remainingSeconds + " seconds remaining");
                } else {
                    TerminalArt.printLine(mission.getType() + " mission: Ready to complete!");
                }
            }
        }

        if (hasActive) {
            System.out.println(TerminalArt.cyan("=====================\n"));
        }
    }

    public void displayActiveUnitTraining() {
        List<UnitQueue> queues = unitQueueDao.getActiveQueuesByUserId(ctx.getCurrentUserId());
        boolean hasActive = false;

        for (UnitQueue queue : queues) {
            if (!hasActive) {
                System.out.println(TerminalArt.cyan("=== UNITS IN TRAINING ==="));
                hasActive = true;
            }

            LocalDateTime endTime = queue.getEndTime().toLocalDateTime();
            LocalDateTime now = LocalDateTime.now();
            long remainingSeconds = ChronoUnit.SECONDS.between(now, endTime);

            if (remainingSeconds > 0) {
                long minutes = remainingSeconds / 60;
                long seconds = remainingSeconds % 60;
                String timeDisplay = String.format("%d:%02d", minutes, seconds);
                TerminalArt.printLine(queue.getQuantity() + " " + queue.getUnitType() +
                        " training: " + TerminalArt.brightYellow(timeDisplay) + " remaining");
            } else {
                TerminalArt.printLine(queue.getQuantity() + " " + queue.getUnitType() +
                        " training: " + TerminalArt.green("Ready to collect!"));
            }
        }

        if (hasActive) {
            System.out.println(TerminalArt.cyan("=====================\n"));
        }
    }

    public void displayProgressStatus() {
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
            ctx.gameService.refreshGameState();
            TerminalArt.printMainHeader(ctx.getCurrentUser(), ctx.gameService.getGameState());

            System.out.println(TerminalArt.yellow("\n=== PROGRESS STATUS ==="));
            displayActiveBuildingUpgrades();
            displayActiveMissions();
            displayActiveUnitTraining();
            System.out.println(TerminalArt.yellow("=======================\n"));
            System.out.println(TerminalArt.brightCyan("Live update active. Press any key to return to main menu."));
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

    public long getBuildingUpgradeRemainingTime(Building building) {
        if (building.getUpgradeEndTime() == null) {
            return 0;
        }

        LocalDateTime endTime = building.getUpgradeEndTime().toLocalDateTime();
        LocalDateTime now = LocalDateTime.now();

        return Math.max(0, ChronoUnit.SECONDS.between(now, endTime));
    }

    public long getMissionRemainingTime(Mission mission) {
        if (mission.getEndTime() == null) {
            return 0;
        }

        LocalDateTime endTime = mission.getEndTime().toLocalDateTime();
        LocalDateTime now = LocalDateTime.now();

        return Math.max(0, ChronoUnit.SECONDS.between(now, endTime));
    }

    public long getUnitTrainingRemainingTime(UnitQueue queue) {
        if (queue.getEndTime() == null) {
            return 0;
        }

        LocalDateTime endTime = queue.getEndTime().toLocalDateTime();
        LocalDateTime now = LocalDateTime.now();

        return Math.max(0, ChronoUnit.SECONDS.between(now, endTime));
    }
}
