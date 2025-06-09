package valaermortis.service;

import valaermortis.core.AppContext;
import valaermortis.dao.MissionDao;
import valaermortis.dao.MiningAreaDao;
import valaermortis.dao.CreatureDao;
import valaermortis.dao.MissionUnitsDao;
import valaermortis.dao.MissionResultsDao;
import valaermortis.dao.UnitQueueDao;
import valaermortis.model.Mission;
import valaermortis.model.MiningArea;
import valaermortis.model.Creature;
import valaermortis.model.BattleResult;
import valaermortis.model.User;
import valaermortis.model.UnitQueue;
import valaermortis.model.enums.MissionType;
import valaermortis.model.enums.MissionStatus;
import valaermortis.model.enums.UnitType;
import valaermortis.model.enums.ResourceType;
import valaermortis.util.GameConfig;
import valaermortis.util.InputUtil;
import valaermortis.util.TerminalArt;
import valaermortis.util.DB;
import valaermortis.util.ErrorHandler;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class MissionService {
    private final AppContext ctx;
    private final MissionDao missionDao = new MissionDao();
    private final MiningAreaDao miningAreaDao = new MiningAreaDao();
    private final CreatureDao creatureDao = new CreatureDao();
    private final MissionUnitsDao missionUnitsDao = new MissionUnitsDao();
    private final MissionResultsDao missionResultsDao = new MissionResultsDao();
    private final UnitQueueDao unitQueueDao = new UnitQueueDao();

    private final UnitService unitService;
    private final ResourceService resourceService;
    private final BattleService battleService;

    public MissionService(AppContext ctx, UnitService unitService, ResourceService resourceService,
            BattleService battleService) {
        this.ctx = ctx;
        this.unitService = unitService;
        this.resourceService = resourceService;
        this.battleService = battleService;
    }

    public List<Mission> getActiveMissions() {
        return missionDao.getActiveMissions(ctx.getCurrentUserId());
    }

    public void miningMissionsMenu() {
        ctx.gameService.refreshGameState();
        InputUtil.clearTerminal();
        TerminalArt.printMainHeader(ctx.getCurrentUser(), ctx.gameService.getGameState());

        List<MiningArea> areas = miningAreaDao.getAvailableAreas();

        if (areas.isEmpty()) {
            InputUtil.displayError("No mining areas available!");
            InputUtil.pressEnterToContinue();
            return;
        }

        InputUtil.printSectionSeparator("MINING AREAS");

        for (int i = 0; i < areas.size(); i++) {
            MiningArea area = areas.get(i);
            System.out.println("[" + (i + 1) + "] " + area.getResourceType() + " Level " + area.getAreaLevel() +
                    " - Stock: " + area.getCurrentStock() + " - Distance: " + area.getDistance() + " units");
        }
        System.out.println("[0] Cancel");

        System.out.println();
        int choice = InputUtil.readIntWithMenuPrompt(InputUtil.createMenuPrompt("Choose option"), 0, areas.size());

        if (choice == 0) {
            InputUtil.displayInfo("Mining cancelled.");
            return;
        }
        MiningArea selectedArea = areas.get(choice - 1);
        startMiningMission(selectedArea);
    }

    public boolean startMiningMission(MiningArea miningArea) {
        ctx.gameService.refreshGameState();
        InputUtil.clearTerminal();
        TerminalArt.printMainHeader(ctx.getCurrentUser(), ctx.gameService.getGameState());
        Map<UnitType, Integer> availableUnits = unitService.getAvailableUnitsForMission();
        if (availableUnits.isEmpty()) {
            List<UnitQueue> completedQueues = unitQueueDao.getCompletedQueues(ctx.getCurrentUserId());
            if (!completedQueues.isEmpty()) {
                InputUtil.displayError("\nNo units available for mining!");
                InputUtil.displayInfo("You have " + completedQueues.size()
                        + " completed training queue(s). Go to train units menu and collect completed units first.");
            } else {
                InputUtil.displayError("No units available for mining! Train units first.");
            }
            InputUtil.pressEnterToContinue();
            return false;
        }

        InputUtil.printSectionSeparator("MINING MISSION SETUP");

        InputUtil.printSubsectionSeparator("Mining Area Information");
        System.out.println("Resource Type: " + miningArea.getResourceType());
        System.out.println("Area Level: " + miningArea.getAreaLevel());
        System.out.println("Available Stock: " + miningArea.getCurrentStock() + " resources");
        System.out.println("Distance: " + miningArea.getDistance() + " units");

        long maxStock = miningArea.getAreaLevel() == 1 ? 100000 : 250000;
        double stockPercentage = (double) miningArea.getCurrentStock() / maxStock * 100;
        String stockStatus;
        if (stockPercentage >= 80) {
            stockStatus = "ABUNDANT";
        } else if (stockPercentage >= 50) {
            stockStatus = "MODERATE";
        } else if (stockPercentage >= 20) {
            stockStatus = "LIMITED";
        } else {
            stockStatus = "DEPLETED";
        }

        System.out.println("Stock Status: " + stockStatus + " (" + String.format("%.1f", stockPercentage) + "%)");

        Map<UnitType, Integer> selectedUnits = selectUnitsForMission(availableUnits);
        if (selectedUnits.isEmpty()) {
            System.out.println("Mission cancelled.");
            return false;
        }
        int totalCarryCapacity = calculateTotalCarryCapacity(selectedUnits);

        long resourcesWillMine = Math.min(totalCarryCapacity, miningArea.getCurrentStock());

        int missionTimeSeconds = calculateMiningTime(miningArea.getDistance(), selectedUnits, resourcesWillMine);

        InputUtil.printSubsectionSeparator("Final Mission Details");
        int totalUnits = selectedUnits.values().stream().mapToInt(Integer::intValue).sum();

        System.out.println("Total Units: " + totalUnits);
        System.out.println("Carry Capacity: " + totalCarryCapacity + " resources");
        System.out.println("Will Mine: " + resourcesWillMine + " " + miningArea.getResourceType());
        System.out.println("Mission Time: " + formatMissionTime(missionTimeSeconds));
        System.out.println("Target Area: " + miningArea.getResourceType() + " Level " + miningArea.getAreaLevel() +
                " (" + miningArea.getDistance() + " units away)");

        System.out.println();
        boolean confirm = InputUtil.readConfirmation("Start mining mission? (y/n): ");
        if (!confirm) {
            System.out.println("Mission cancelled.");
            return false;
        }
        if (!unitService.deployUnitsForMission(selectedUnits)) {
            InputUtil.displayError("Failed to deploy units!");
            return false;
        }

        Mission mission = new Mission();
        mission.setUserId(ctx.getCurrentUserId());
        mission.setType(MissionType.MINING);
        mission.setStatus(MissionStatus.IN_PROGRESS);
        mission.setMiningAreaId(miningArea.getId());
        mission.setStartTime(Timestamp.valueOf(LocalDateTime.now()));
        mission.setEndTime(Timestamp.valueOf(LocalDateTime.now().plusSeconds(missionTimeSeconds)));

        boolean missionCreated = false;
        try {
            missionCreated = missionDao.create(mission);
        } catch (Exception e) {
            e.printStackTrace();
            unitService.returnUnitsFromMission(selectedUnits);
            return false;
        }
        if (missionCreated) {
            if (mission.getId() == null) {
                InputUtil.displayError("Error: Mission ID not set after creation!");
                unitService.returnUnitsFromMission(selectedUnits);
                return false;
            }

            try {
                missionUnitsDao.createMissionUnits(mission.getId(), selectedUnits);

                missionResultsDao.createMissionResult(mission.getId(),
                        miningArea.getResourceType() == ResourceType.FOOD ? (int) resourcesWillMine : 0,
                        miningArea.getResourceType() == ResourceType.WOOD ? (int) resourcesWillMine : 0,
                        miningArea.getResourceType() == ResourceType.STONE ? (int) resourcesWillMine : 0, true);

                InputUtil
                        .displaySuccess("Mining mission started! Will complete in " + missionTimeSeconds + " seconds.");
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                unitService.returnUnitsFromMission(selectedUnits);
                return false;
            }
        } else {
            unitService.returnUnitsFromMission(selectedUnits);
            InputUtil.displayError("Failed to create mission! Check database connection and table structure.");
            return false;
        }
    }

    public void attackMissionsMenu() {
        ctx.gameService.refreshGameState();
        InputUtil.clearTerminal();
        TerminalArt.printMainHeader(ctx.getCurrentUser(), ctx.gameService.getGameState());

        int townhallLevel = resourceService.getCurrentResources().getTownhallLvl();
        List<Creature> creatures = creatureDao.getAvailableCreatures(townhallLevel);
        if (creatures.isEmpty()) {
            InputUtil.displayError("No creatures available to attack!");
            InputUtil.pressEnterToContinue();
            return;
        }
        InputUtil.printSectionSeparator("AVAILABLE CREATURES");
        for (int i = 0; i < creatures.size(); i++) {
            Creature creature = creatures.get(i);
            System.out.println("[" + (i + 1) + "] " + creature.getName() + " Level " + creature.getLevel() +
                    " - HP: " + creature.getHp() + " - Rewards: " + creature.getRewardFood() + "/" +
                    creature.getRewardWood() + "/" + creature.getRewardStone() +
                    " - Distance: " + creature.getDistance() + " units");
        }
        System.out.println("[0] Cancel");

        System.out.println();
        int choice = InputUtil.readIntWithMenuPrompt(InputUtil.createMenuPrompt("Choose option"), 0, creatures.size());
        if (choice == 0) {
            System.out.println("Attack cancelled.");
            return;
        }

        Creature selectedCreature = creatures.get(choice - 1);
        startAttackMission(selectedCreature);
    }

    public boolean startAttackMission(Creature creature) {
        ctx.gameService.refreshGameState();
        InputUtil.clearTerminal();
        TerminalArt.printMainHeader(ctx.getCurrentUser(), ctx.gameService.getGameState());
        Map<UnitType, Integer> availableUnits = unitService.getAvailableUnitsForMission();
        if (availableUnits.isEmpty()) {
            List<UnitQueue> completedQueues = unitQueueDao.getCompletedQueues(ctx.getCurrentUserId());
            if (!completedQueues.isEmpty()) {
                InputUtil.displayError("No units available for attack!");
                InputUtil.displayInfo("You have " + completedQueues.size()
                        + " completed training queue(s). Go to train units menu and collect completed units first.");
            } else {
                InputUtil.displayError("No units available for attack! Train units first.");
            }
            InputUtil.pressEnterToContinue();
            return false;
        }

        InputUtil.printSectionSeparator("ATTACK MISSION SETUP");
        System.out.println("Target: " + creature.getName() + " Level " + creature.getLevel());
        System.out.println("Enemy HP: " + creature.getHp());
        System.out.println("Potential Rewards: " + creature.getRewardFood() + "/" +
                creature.getRewardWood() + "/" + creature.getRewardStone());
        System.out.println("Distance: " + creature.getDistance() + " units");

        System.out.println("\nAvailable units:");
        for (Map.Entry<UnitType, Integer> entry : availableUnits.entrySet()) {
            GameConfig.UnitStats stats = GameConfig.getUnitStats(entry.getKey());
            System.out.println("  " + entry.getKey() + ": " + entry.getValue() +
                    " (Attack: " + stats.attack + ", HP: " + stats.hp + ")");
        }

        Map<UnitType, Integer> selectedUnits = selectUnitsForMission(availableUnits);
        if (selectedUnits.isEmpty()) {
            System.out.println("Mission cancelled.");
            return false;
        }

        BattleResult battleResult = battleService.simulateBattle(selectedUnits, creature);

        int missionTimeSeconds = calculateAttackTime(creature.getDistance(), selectedUnits,
                creature.getMaxBattleTime());

        System.out.println("\nBattle Simulation:");
        System.out.println("Victory Chance: " + (battleResult.victory ? "VICTORY" : "DEFEAT"));
        System.out.println("Estimated Losses: "
                + battleResult.unitsLost.values().stream().mapToInt(Integer::intValue).sum() + " units");
        System.out.println("Mission Time: " + missionTimeSeconds + " seconds");
        boolean confirm = InputUtil.readConfirmation("Start attack mission? (y/n): ");
        if (!confirm) {
            System.out.println("Mission cancelled.");
            return false;
        }
        if (!unitService.deployUnitsForMission(selectedUnits)) {
            InputUtil.displayError("Failed to deploy units!");
            return false;
        }

        Mission mission = new Mission();
        mission.setUserId(ctx.getCurrentUserId());
        mission.setType(MissionType.ATTACK);
        mission.setStatus(MissionStatus.IN_PROGRESS);
        mission.setCreatureId(creature.getId());
        mission.setStartTime(Timestamp.valueOf(LocalDateTime.now()));
        mission.setEndTime(Timestamp.valueOf(LocalDateTime.now().plusSeconds(missionTimeSeconds)));
        if (missionDao.create(mission)) {
            if (mission.getId() == null) {
                InputUtil.displayError("Error: Mission ID not set after creation!");
                unitService.returnUnitsFromMission(selectedUnits);
                return false;
            }

            missionUnitsDao.createMissionUnits(mission.getId(), selectedUnits);

            int foodReward = battleResult.victory ? creature.getRewardFood() : 0;
            int woodReward = battleResult.victory ? creature.getRewardWood() : 0;
            int stoneReward = battleResult.victory ? creature.getRewardStone() : 0;
            missionResultsDao.createMissionResult(mission.getId(), foodReward, woodReward, stoneReward,
                    battleResult.victory);

            missionUnitsDao.updateMissionUnitsResult(mission.getId(), battleResult.unitsLost,
                    battleResult.survivingUnits);

            InputUtil.displaySuccess("Attack mission started! Will complete in " + missionTimeSeconds + " seconds.");
            return true;
        } else {
            unitService.returnUnitsFromMission(selectedUnits);
            InputUtil.displayError("Failed to create mission!");
            return false;
        }
    }

    private Map<UnitType, Integer> selectUnitsForMission(Map<UnitType, Integer> availableUnits) {
        Map<UnitType, Integer> selectedUnits = new HashMap<>();

        for (UnitType unitType : availableUnits.keySet()) {
            int available = availableUnits.get(unitType);
            if (available <= 0)
                continue;

            GameConfig.UnitStats stats = GameConfig.getUnitStats(unitType);

            System.out.println();
            System.out.println(unitType + " Selection:");
            System.out.println("- Available Units: " + available);
            System.out.println("- Carry Capacity: " + stats.carryCapacity + " per unit");
            System.out.println("- Max Total Load: " + (stats.carryCapacity * available) + " resources");
            while (true) {
                String input = InputUtil
                        .readString("How many " + unitType + "s to deploy? (0-" + available + ", or 'cancel')");

                if (input.equalsIgnoreCase("cancel")) {
                    System.out.println("Mission cancelled by user.");
                    return new HashMap<>();
                }

                try {
                    int count = Integer.parseInt(input);
                    if (count < 0 || count > available) {
                        InputUtil.displayError("Invalid amount! Enter a number between 0 and " + available);
                        continue;
                    }
                    if (count > 0) {
                        selectedUnits.put(unitType, count);
                        System.out.println("Selected " + count + " " + unitType + "(s)");
                    } else {
                        System.out.println("  Skipped " + unitType);
                    }
                    break;

                } catch (NumberFormatException e) {
                    InputUtil.displayError("Invalid input! Enter a number or 'cancel'");
                }
            }
            System.out.println();
        }

        if (!selectedUnits.isEmpty()) {
            InputUtil.printSubsectionSeparator("Selection Confirmed");
            for (Map.Entry<UnitType, Integer> entry : selectedUnits.entrySet()) {
                System.out.println(entry.getKey() + ": " + entry.getValue() + " units");
            }
        } else {
            System.out.println("No units selected for mission.");
        }
        return selectedUnits;
    }

    private int calculateTotalCarryCapacity(Map<UnitType, Integer> units) {
        int totalCapacity = 0;
        for (Map.Entry<UnitType, Integer> entry : units.entrySet()) {
            GameConfig.UnitStats stats = GameConfig.getUnitStats(entry.getKey());
            totalCapacity += stats.carryCapacity * entry.getValue();
        }
        return totalCapacity;
    }

    private double calculateMissionSpeed(Map<UnitType, Integer> units) {
        double minSpeed = Double.MAX_VALUE;
        for (UnitType unitType : units.keySet()) {
            GameConfig.UnitStats stats = GameConfig.getUnitStats(unitType);
            minSpeed = Math.min(minSpeed, stats.speed);
        }
        return minSpeed;
    }

    private int calculateMiningTime(int distance, Map<UnitType, Integer> units, long resourcesWillMine) {
        double missionSpeed = calculateMissionSpeed(units);
        int totalUnits = units.values().stream().mapToInt(Integer::intValue).sum();

        double travelTime = distance * 1.0 / missionSpeed;

        double miningTime = resourcesWillMine / (totalUnits * 20.0);
        return Math.max(3, (int) Math.ceil((travelTime * 2) + miningTime));
    }

    private int calculateAttackTime(int distance, Map<UnitType, Integer> units, int battleTime) {
        double missionSpeed = calculateMissionSpeed(units);

        double travelTime = distance * 1.0 / missionSpeed;
        return Math.max(3, (int) Math.ceil((travelTime * 2) + battleTime));
    }

    public boolean completeMission(Mission mission) {
        if (mission.getStatus() != MissionStatus.IN_PROGRESS) {
            return false;
        }
        mission.setStatus(MissionStatus.COMPLETED);
        mission.setEndTime(Timestamp.valueOf(LocalDateTime.now()));

        MissionResultsDao.MissionResult missionResult = missionResultsDao.getMissionResult(mission.getId());
        Map<UnitType, Integer> returningUnits;

        if (mission.getType() == MissionType.ATTACK) {
            returningUnits = missionUnitsDao.getSurvivingUnits(mission.getId());
        } else {
            returningUnits = missionUnitsDao.getMissionUnits(mission.getId());
        }

        if (mission.getType() == MissionType.MINING && mission.getMiningAreaId() != null) {
            handleMiningAreaDepletion(mission, missionResult);
        }

        if (mission.getType() == MissionType.ATTACK && mission.getCreatureId() != null) {
            if (missionResult != null && missionResult.isSuccess()) {
                creatureDao.killCreature(mission.getCreatureId());
                InputUtil.displaySuccess("Creature defeated! Generating new creature...");

                generateNewCreature();
            }
        }

        if (missionResult != null && (missionResult.getFoodGained() > 0 || missionResult.getWoodGained() > 0
                || missionResult.getStoneGained() > 0)) {
            resourceService.addResources(
                    missionResult.getFoodGained(),
                    missionResult.getWoodGained(),
                    missionResult.getStoneGained());
        }

        if (returningUnits != null && !returningUnits.isEmpty()) {
            unitService.returnUnitsFromMission(returningUnits);
        }
        return missionDao.update(mission);
    }

    private void handleMiningAreaDepletion(Mission mission, MissionResultsDao.MissionResult missionResult) {
        Long miningAreaId = mission.getMiningAreaId();
        if (miningAreaId == null)
            return;
        MiningArea area = miningAreaDao.findById(miningAreaId);
        if (area == null)
            return;

        int resourcesMined = 0;
        if (missionResult != null) {
            resourcesMined = missionResult.getFoodGained() + missionResult.getWoodGained()
                    + missionResult.getStoneGained();
        }
        long newStock = Math.max(0, area.getCurrentStock() - resourcesMined);

        if (newStock <= 0) {
            miningAreaDao.updateStock(miningAreaId, 0);
            InputUtil.displayInfo("Mining area depleted! Generating new area...");

            generateNewMiningArea();
        } else {
            miningAreaDao.updateStock(miningAreaId, newStock);
        }
    }

    private void generateNewMiningArea() {
        int currentActiveCount = miningAreaDao.countActiveAreas();

        if (currentActiveCount >= 5) {
            return;
        }

        int townhallLevel = resourceService.getCurrentResources().getTownhallLvl();

        ResourceType[] resourceTypes = { ResourceType.FOOD, ResourceType.WOOD, ResourceType.STONE };
        ResourceType randomType = resourceTypes[(int) (Math.random() * resourceTypes.length)];

        int areaLevel = townhallLevel <= 5 ? 1 : 2;

        long stockAmount = areaLevel == 1 ? 80000 : 200000;
        long maxStock = areaLevel == 1 ? 100000 : 250000;
        int distance = 1 + (int) (Math.random() * 3);

        MiningArea newArea = new MiningArea();
        newArea.setResourceType(randomType);
        newArea.setAreaLevel(areaLevel);
        newArea.setCurrentStock(stockAmount);
        newArea.setMaxStock(maxStock);
        newArea.setDistance(distance);

        if (miningAreaDao.createMiningArea(newArea)) {
            InputUtil.displaySuccess("New " + randomType + " Level " + areaLevel + " mining area generated!");
        }
    }

    private void generateNewCreature() {
        int currentActiveCount = creatureDao.findAliveCreatures().size();

        if (currentActiveCount >= 5) {
            return;
        }

        int townhallLevel = resourceService.getCurrentResources().getTownhallLvl();

        int creatureLevel;
        if (townhallLevel == 1) {
            creatureLevel = 1;
        } else if (townhallLevel <= 3) {
            creatureLevel = 1 + (int) (Math.random() * townhallLevel);
        } else {
            int minLevel = Math.max(1, townhallLevel - 2);
            creatureLevel = minLevel + (int) (Math.random() * (townhallLevel - minLevel + 1));
        }

        int hp, attackPower, rewardFood, rewardWood, rewardStone, maxBattleTime;

        switch (creatureLevel) {
            case 1:
                hp = 400;
                attackPower = 15;
                rewardFood = 150;
                rewardWood = 100;
                rewardStone = 50;
                maxBattleTime = 3;
                break;
            case 2:
                hp = 700;
                attackPower = 25;
                rewardFood = 300;
                rewardWood = 200;
                rewardStone = 100;
                maxBattleTime = 3;
                break;
            case 3:
                hp = 1200;
                attackPower = 40;
                rewardFood = 500;
                rewardWood = 350;
                rewardStone = 175;
                maxBattleTime = 4;
                break;
            case 4:
                hp = 2000;
                attackPower = 60;
                rewardFood = 800;
                rewardWood = 550;
                rewardStone = 275;
                maxBattleTime = 4;
                break;
            case 5:
                hp = 3200;
                attackPower = 85;
                rewardFood = 1200;
                rewardWood = 800;
                rewardStone = 400;
                maxBattleTime = 5;
                break;
            case 6:
                hp = 5000;
                attackPower = 120;
                rewardFood = 1800;
                rewardWood = 1200;
                rewardStone = 600;
                maxBattleTime = 5;
                break;
            case 7:
                hp = 7500;
                attackPower = 160;
                rewardFood = 2700;
                rewardWood = 1800;
                rewardStone = 900;
                maxBattleTime = 6;
                break;
            case 8:
                hp = 11000;
                attackPower = 210;
                rewardFood = 4000;
                rewardWood = 2700;
                rewardStone = 1350;
                maxBattleTime = 6;
                break;
            case 9:
                hp = 16000;
                attackPower = 280;
                rewardFood = 6000;
                rewardWood = 4000;
                rewardStone = 2000;
                maxBattleTime = 7;
                break;
            case 10:
                hp = 25000;
                attackPower = 370;
                rewardFood = 9000;
                rewardWood = 6000;
                rewardStone = 3000;
                maxBattleTime = 7;
                break;
            default:
                hp = 400;
                attackPower = 15;
                rewardFood = 150;
                rewardWood = 100;
                rewardStone = 50;
                maxBattleTime = 3;
                break;
        }

        int distance = 1 + (int) (Math.random() * 3);

        try (Connection conn = DB.getInstance().getConnection();
                PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO creatures (level, max_hp, attack_power, distance, reward_food, reward_wood, reward_stone, max_battle_time, is_alive) VALUES (?, ?, ?, ?, ?, ?, ?, ?, true)")) {
            ps.setInt(1, creatureLevel);
            ps.setInt(2, hp);
            ps.setInt(3, attackPower);
            ps.setInt(4, distance);
            ps.setInt(5, rewardFood);
            ps.setInt(6, rewardWood);
            ps.setInt(7, rewardStone);
            ps.setInt(8, maxBattleTime);
            if (ps.executeUpdate() > 0) {
                InputUtil.displayInfo("New Level " + creatureLevel + " creature has spawned!");
            }
        } catch (SQLException e) {
            ErrorHandler.logDatabaseError("generating new creature", e);
        }
    }

    public void showLiveMissionStatus() {
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
            User user = ctx.getCurrentUser();
            TerminalArt.printMainHeader(user, ctx.gameService.getGameState());

            InputUtil.printSectionSeparator("LIVE MISSION STATUS");

            List<Mission> activeMissions = missionDao.getActiveMissions(ctx.getCurrentUserId());
            boolean hasActiveMissions = false;
            boolean hasCompletedMissions = false;
            if (!activeMissions.isEmpty()) {
                hasActiveMissions = true;
                InputUtil.printSubsectionSeparator("Active Missions");

                for (Mission mission : activeMissions) {
                    if (mission.getEndTime() != null) {
                        LocalDateTime endTime = mission.getEndTime().toLocalDateTime();
                        LocalDateTime now = LocalDateTime.now();
                        long remainingSeconds = ChronoUnit.SECONDS.between(now, endTime);

                        String missionInfo = getMissionDisplayInfo(mission);
                        if (remainingSeconds > 0) {
                            long minutes = remainingSeconds / 60;
                            long seconds = remainingSeconds % 60;
                            String timeDisplay = String.format("%d:%02d", minutes, seconds);
                            System.out.println("- " + missionInfo + " - " + timeDisplay + " remaining");
                        } else {
                            InputUtil.displaySuccess("Mission completed! Units returning: " + missionInfo);
                            hasCompletedMissions = true;
                        }
                    }
                }
            }
            if (!hasActiveMissions) {
                InputUtil.displayInfo("No active missions");
            }
            if (hasCompletedMissions) {
                InputUtil.displaySuccess("Some missions completed! Units and rewards will be automatically collected.");
            }
            InputUtil.displayInfo("Press enter to go back");

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

    private String getMissionDisplayInfo(Mission mission) {
        StringBuilder info = new StringBuilder();
        info.append(mission.getType().toString());

        if (mission.getType() == MissionType.MINING && mission.getMiningAreaId() != null) {
            MiningArea area = miningAreaDao.findById(mission.getMiningAreaId());
            if (area != null) {
                info.append(" (").append(area.getResourceType()).append(" Lv").append(area.getAreaLevel()).append(")");
            }
        } else if (mission.getType() == MissionType.ATTACK && mission.getCreatureId() != null) {
            Creature creature = creatureDao.findById(mission.getCreatureId());
            if (creature != null) {
                info.append(" (").append(creature.getName()).append(" Lv").append(creature.getLevel()).append(")");
            }
        }
        return info.toString();
    }

    public void showLiveMiningAreasMenu() {
        final boolean[] userWantsToExit = { false };

        Thread inputThread = new Thread(() -> {
            try {
                System.in.read();
                while (System.in.available() > 0) {
                    System.in.read();
                }
                userWantsToExit[0] = true;
            } catch (Exception e) {
            }
        });
        inputThread.setDaemon(true);
        inputThread.start();

        while (!userWantsToExit[0]) {
            InputUtil.clearTerminal();
            ctx.gameService.refreshGameState();
            TerminalArt.printMainHeader(ctx.getCurrentUser(), ctx.gameService.getGameState());

            InputUtil.printSectionSeparator("LIVE MINING AREAS STATUS");

            List<MiningArea> areas = miningAreaDao.getAvailableAreas();
            Map<UnitType, Integer> availableUnits = unitService.getAvailableUnitsForMission();
            if (areas.isEmpty()) {
                InputUtil.displayError("No mining areas available!");
            } else {
                InputUtil.printSubsectionSeparator("Available Mining Areas");
                for (int i = 0; i < areas.size(); i++) {
                    MiningArea area = areas.get(i);
                    System.out.println("[" + (i + 1) + "] " + area.getResourceType() + " Level " + area.getAreaLevel() +
                            " - Stock: " + area.getCurrentStock() +
                            " - Distance: " + area.getDistance() + " units");
                }
            }
            InputUtil.printSubsectionSeparator("Available Units for Missions");
            if (availableUnits.isEmpty()) {
                List<UnitQueue> completedQueues = unitQueueDao.getCompletedQueues(ctx.getCurrentUserId());
                if (!completedQueues.isEmpty()) {
                    InputUtil.displayError("No units available! Collect " + completedQueues.size()
                            + " completed training queue(s) first.");
                } else {
                    InputUtil.displayError("No units available! Train units first.");
                }
            } else {
                for (Map.Entry<UnitType, Integer> entry : availableUnits.entrySet()) {
                    GameConfig.UnitStats stats = GameConfig.getUnitStats(entry.getKey());
                    System.out.println("- " + entry.getKey() + ": " + entry.getValue() +
                            " (Capacity: " + stats.carryCapacity + " each)");
                }
            }

            List<Mission> activeMissions = missionDao.getActiveMissions(ctx.getCurrentUserId());
            if (!activeMissions.isEmpty()) {
                InputUtil.printSubsectionSeparator("Units Currently on Missions");
                for (Mission mission : activeMissions) {
                    Map<UnitType, Integer> missionUnits = missionUnitsDao.getMissionUnits(mission.getId());
                    if (!missionUnits.isEmpty()) {
                        int totalUnits = missionUnits.values().stream().mapToInt(Integer::intValue).sum();
                        String missionInfo = getMissionDisplayInfo(mission);

                        LocalDateTime endTime = mission.getEndTime().toLocalDateTime();
                        LocalDateTime now = LocalDateTime.now();
                        long remainingSeconds = ChronoUnit.SECONDS.between(now, endTime);
                        if (remainingSeconds > 0) {
                            long minutes = remainingSeconds / 60;
                            long seconds = remainingSeconds % 60;
                            String timeDisplay = String.format("%d:%02d", minutes, seconds);
                            System.out.println("- " + totalUnits + " units on " + missionInfo + " (" + timeDisplay
                                    + " remaining)");
                        } else {
                            System.out.println("- " + totalUnits + " units on " + missionInfo + " (RETURNING)");
                        }
                    }
                }
            }
            InputUtil.displayInfo("Live update active. Press any key to start a mission or return to menu.");

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
        miningMissionsMenu();
    }

    public void showLiveAttackTargetsMenu() {
        ctx.gameService.refreshGameState();
        InputUtil.clearTerminal();
        TerminalArt.printMainHeader(ctx.getCurrentUser(), ctx.gameService.getGameState());

        InputUtil.printSectionSeparator("ATTACK TARGETS STATUS");

        int townhallLevel = resourceService.getCurrentResources().getTownhallLvl();
        List<Creature> creatures = creatureDao.getAvailableCreatures(townhallLevel);
        Map<UnitType, Integer> availableUnits = unitService.getAvailableUnitsForMission();

        if (creatures.isEmpty()) {
            InputUtil.displayError("No creatures available to attack!");
            InputUtil.pressEnterToContinue();
            return;
        }

        InputUtil.printSubsectionSeparator("Available Creatures to Attack");
        for (int i = 0; i < creatures.size(); i++) {
            Creature creature = creatures.get(i);
            System.out.println("[" + (i + 1) + "] " + creature.getName() + " Level " + creature.getLevel() +
                    " - HP: " + creature.getHp() +
                    " - Rewards: " + creature.getRewardFood() + "/" +
                    creature.getRewardWood() + "/" + creature.getRewardStone() +
                    " - Distance: " + creature.getDistance() + " units");
        }

        InputUtil.printSubsectionSeparator("Available Units for Missions");
        if (availableUnits.isEmpty()) {
            List<UnitQueue> completedQueues = unitQueueDao.getCompletedQueues(ctx.getCurrentUserId());
            if (!completedQueues.isEmpty()) {
                InputUtil.displayError("No units available! Collect " + completedQueues.size()
                        + " completed training queue(s) first.");
            } else {
                InputUtil.displayError("No units available! Train units first.");
            }
        } else {
            for (Map.Entry<UnitType, Integer> entry : availableUnits.entrySet()) {
                GameConfig.UnitStats stats = GameConfig.getUnitStats(entry.getKey());
                System.out.println("- " + entry.getKey() + ": " + entry.getValue() +
                        " (Attack: " + stats.attack + ", HP: " + stats.hp + ")");
            }
        }

        List<Mission> activeMissions = missionDao.getActiveMissions(ctx.getCurrentUserId());
        if (!activeMissions.isEmpty()) {
            InputUtil.printSubsectionSeparator("Units Currently on Missions");
            for (Mission mission : activeMissions) {
                Map<UnitType, Integer> missionUnits = missionUnitsDao.getMissionUnits(mission.getId());
                if (!missionUnits.isEmpty()) {
                    int totalUnits = missionUnits.values().stream().mapToInt(Integer::intValue).sum();
                    String missionInfo = getMissionDisplayInfo(mission);

                    LocalDateTime endTime = mission.getEndTime().toLocalDateTime();
                    LocalDateTime now = LocalDateTime.now();
                    long remainingSeconds = ChronoUnit.SECONDS.between(now, endTime);
                    if (remainingSeconds > 0) {
                        long minutes = remainingSeconds / 60;
                        long seconds = remainingSeconds % 60;
                        String timeDisplay = String.format("%d:%02d", minutes, seconds);
                        System.out.println("- " + totalUnits + " units on " + missionInfo + " (" + timeDisplay
                                + " remaining)");
                    } else {
                        System.out.println("- " + totalUnits + " units on " + missionInfo + " (RETURNING)");
                    }
                }
            }
        }

        System.out.println("[0] Cancel");
        System.out.println();
        int choice = InputUtil.readIntWithMenuPrompt(InputUtil.createMenuPrompt("Choose option"), 0, creatures.size());
        if (choice == 0) {
            System.out.println("Attack cancelled.");
            return;
        }

        Creature selectedCreature = creatures.get(choice - 1);
        startAttackMission(selectedCreature);
    }

    private String formatMissionTime(int seconds) {
        long minutes = seconds / 60;
        long remainingSeconds = seconds % 60;
        return String.format("%d:%02d", minutes, remainingSeconds);
    }
}
