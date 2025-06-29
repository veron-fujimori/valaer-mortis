package valaermortis.cli;

import valaermortis.core.AppContext;
import valaermortis.model.User;
import valaermortis.util.InputUtil;
import valaermortis.util.TerminalArt;
import valaermortis.model.Building;
import valaermortis.model.enums.BuildingType;
import valaermortis.util.GameConfig;
import java.util.List;

public class Menu {
    private final AppContext ctx;

    public Menu(AppContext ctx) {
        this.ctx = ctx;
    }

    public void start() {
        while (true) {
            if (!ctx.isLoggedIn()) {
                showAuthMenu();
            } else {
                showMainMenu();
            }
        }
    }

    private void showAuthMenu() {
        InputUtil.clearTerminal();
        TerminalArt.printBanner();
        InputUtil.printSectionSeparator("Landing PAGE");
        System.out.println(TerminalArt.white("[1] Login"));
        System.out.println(TerminalArt.white("[2] Register"));
        System.out.println(TerminalArt.white("[3] Exit"));

        System.out.println();
        int ch = InputUtil.readIntWithMenuPrompt(InputUtil.createMenuPrompt("Choose Menu"), 1, 3);
        switch (ch) {
            case 1:
                InputUtil.clearTerminal();
                ctx.authService.login();
                InputUtil.pressEnterToContinue();
                break;
            case 2:
                InputUtil.clearTerminal();
                ctx.authService.register();
                InputUtil.pressEnterToContinue();
                break;
            case 3:
                InputUtil.clearTerminal();
                TerminalArt.goodbye();
                InputUtil.pressEnterToContinue();
                System.exit(0);
                break;
            default:
                InputUtil.displayError("Invalid choice.");
                InputUtil.pressEnterToContinue();
                break;
        }
    }

    private void showMainMenu() {
        User user = ctx.getCurrentUser();
        ctx.gameService.refreshGameState();

        InputUtil.clearTerminal();
        TerminalArt.printMainHeader(user, ctx.gameService.getGameState());
        InputUtil.printSectionSeparator("HOME PAGE");
        System.out.println(TerminalArt.white("[1] Upgrade Townhall"));
        System.out.println(TerminalArt.white("[2] Build/Upgrade Barrack"));
        System.out.println(TerminalArt.white("[3] Upgrade Storage"));
        System.out.println(TerminalArt.white("[4] Train Troops"));
        System.out.println(TerminalArt.white("[5] Send Troops: Mining"));
        System.out.println(TerminalArt.white("[6] Send Troops: Attack Creature"));
        System.out.println(TerminalArt.white("[7] Check Mission Status"));
        System.out.println(TerminalArt.white("[8] Messages"));
        System.out.println(TerminalArt.white("[9] Logout"));

        System.out.println();
        int ch = InputUtil.readIntWithMenuPrompt(InputUtil.createMenuPrompt("Choose Menu"), 1, 9);
        switch (ch) {
            case 1:
                townhallUpgradeMenu();
                break;
            case 2:
                manageBuildingsMenu();
                break;
            case 3:
                storageUpgradeMenu();
                break;
            case 4:
                ctx.gameService.trainUnitsMenu();
                break;
            case 5:
                ctx.gameService.miningMissionsMenu();
                break;
            case 6:
                ctx.gameService.attackMissionsMenu();
                break;
            case 7:
                ctx.gameService.showMissionStatus();
                break;
            case 8:
                messagesMenu();
                break;
            case 9:
                InputUtil.clearTerminal();
                InputUtil.displaySuccess("Logout successful. Goodbye, " + user.getUsername() + "!");
                TerminalArt.resetTerminalColors();
                TerminalArt.exitFullScreen();
                ctx.setCurrentUser(null);
                InputUtil.pressEnterToContinue();
                break;
            default:
                InputUtil.displayError("Invalid choice.");
                InputUtil.pressEnterToContinue();
                break;
        }
    }

    private void manageBuildingsMenu() {
        ctx.gameService.refreshGameState();

        while (true) {
            InputUtil.clearTerminal();
            User user = ctx.getCurrentUser();
            TerminalArt.printMainHeader(user, ctx.gameService.getGameState());
            InputUtil.printSectionSeparator("BUILDING MANAGEMENT");
            System.out.println(TerminalArt.white("[1] View Buildings     "));
            System.out.println(TerminalArt.white("[2] Build New Barrack  "));
            System.out.println(TerminalArt.white("[3] Upgrade Barrack    "));
            System.out.println(TerminalArt.white("[4] Back to Home Page"));

            System.out.println();
            int choice = InputUtil.readIntWithMenuPrompt(InputUtil.createMenuPrompt("Choose Menu"), 1, 4);
            switch (choice) {
                case 1:
                    ctx.buildingService.displayBuildings();
                    break;
                case 2:
                    buildBarrackMenu();
                    break;
                case 3:
                    upgradeBarrackMenu();
                    break;
                case 4:
                    return;
                default:
                    InputUtil.displayError("Invalid choice!");
            }
        }
    }

    private void buildBarrackMenu() {
        ctx.gameService.refreshGameState();
        InputUtil.clearTerminal();
        User user = ctx.getCurrentUser();
        TerminalArt.printMainHeader(user, ctx.gameService.getGameState());
        List<BuildingType> available = ctx.buildingService.getAvailableBarracksToBuild();
        if (available.isEmpty()) {
            InputUtil.displayError("\nNo new barracks available at your townhall level!");
            InputUtil.pressEnterToContinue();
            return;
        }

        InputUtil.printSectionSeparator("BUILD BARRACK");
        for (int i = 0; i < available.size(); i++) {
            BuildingType type = available.get(i);
            int townhallLevel = ctx.buildingService.getBuildingByType(BuildingType.TOWNHALL).getLevel();
            GameConfig.BuildCost cost = GameConfig.getBarrackBuildCost(type, townhallLevel);
            System.out.println(TerminalArt.white("[" + (i + 1) + "] " + type.toString() +
                    " - Cost: " + cost.food + " Food, " + cost.wood + " Wood, " + cost.stone + " Stone" +
                    " | Build Time: " + cost.timeSeconds + " seconds"));
        }
        System.out.println(TerminalArt.white("[0] Cancel"));
        System.out.println();
        int choice = InputUtil.readIntWithMenuPrompt(InputUtil.createMenuPrompt("Choose Menu"), 0, available.size());
        if (choice <= 0 || choice > available.size()) {
            InputUtil.displayInfo("Build cancelled.");
            return;
        }
        BuildingType selectedType = available.get(choice - 1);
        boolean success = ctx.buildingService.buildBarrack(selectedType);
        if (success) {
            showBarrackConstructionProgress(selectedType);
        } else {
            InputUtil.pressEnterToContinue();

        }
    }

    private void upgradeBarrackMenu() {
        ctx.gameService.refreshGameState();
        InputUtil.clearTerminal();
        User user = ctx.getCurrentUser();
        TerminalArt.printMainHeader(user, ctx.gameService.getGameState());
        List<Building> allBarracks = ctx.buildingService.getAllBarracks();
        if (allBarracks.isEmpty()) {
            InputUtil.displayError("No barracks found! Build barracks first.");
            return;
        }
        InputUtil.printSectionSeparator("UPGRADE BARRACK");

        Building townhall = ctx.buildingService.getBuildingByType(BuildingType.TOWNHALL);
        int townhallLevel = townhall != null ? townhall.getLevel() : 1;
        boolean hasUpgrading = allBarracks.stream().anyMatch(b -> b.getUpgradeEndTime() != null);

        if (hasUpgrading) {
            showBarrackListWithLiveTimers(allBarracks, townhallLevel);
        } else {
            displayStaticBarrackList(allBarracks, townhallLevel);
        }
    }

    private void townhallUpgradeMenu() {
        ctx.gameService.refreshGameState();
        InputUtil.clearTerminal();
        User user = ctx.getCurrentUser();
        TerminalArt.printMainHeader(user, ctx.gameService.getGameState());
        Building townhall = ctx.buildingService.getBuildingByType(BuildingType.TOWNHALL);
        if (townhall == null) {
            InputUtil.displayError("Townhall not found!");
            return;
        }

        int currentLevel = townhall.getLevel();
        int nextLevel = currentLevel + 1;
        if (townhall.getUpgradeEndTime() != null) {
            showTownhallUpgradeProgress(townhall, currentLevel, nextLevel);
            return;
        }

        boolean isAtMaxLevel = (nextLevel > 10);

        GameConfig.UpgradeCost cost;
        if (isAtMaxLevel) {
            cost = GameConfig.getTownhallUpgradeCost(10);
            nextLevel = 10;
        } else {
            cost = GameConfig.getTownhallUpgradeCost(nextLevel);
        }
        InputUtil.printSectionSeparator("UPGRADE TOWNHALL");
        System.out.println(TerminalArt.white("Current Level   : " + currentLevel));
        System.out.println(TerminalArt.white("Next Level      : " + nextLevel));
        System.out.println(TerminalArt
                .white("Cost            : " + cost.food + " Food, " + cost.wood + " Wood, " + cost.stone + " Stone"));
        System.out.println(TerminalArt.white("Upgrade Time    : " + cost.timeSeconds + " seconds"));
        InputUtil.printSubsectionSeparator("Upgrade Benefits");

        int currentMaxBarrackPerType = GameConfig.getMaxBarracksPerType(currentLevel);
        int newMaxBarrackPerType = GameConfig.getMaxBarracksPerType(nextLevel);
        int currentMaxBarrackLevel = GameConfig.getMaxBarrackLevel(currentLevel);
        int newMaxBarrackLevel = GameConfig.getMaxBarrackLevel(nextLevel);
        int currentMaxStorageLevel = GameConfig.getMaxStorageLevel(currentLevel);
        int newMaxStorageLevel = GameConfig.getMaxStorageLevel(nextLevel);

        if (isAtMaxLevel) {
            newMaxBarrackPerType = currentMaxBarrackPerType;
            newMaxBarrackLevel = currentMaxBarrackLevel;
            newMaxStorageLevel = currentMaxStorageLevel;
        }

        System.out.println("- Max Barracks Per Type: " + currentMaxBarrackPerType + " -> " + newMaxBarrackPerType);
        System.out.println("- Max Barrack Level: " + currentMaxBarrackLevel + " -> " + newMaxBarrackLevel);
        System.out.println("- Max Storage Level: " + currentMaxStorageLevel + " -> " + newMaxStorageLevel);

        if (!isAtMaxLevel) {
            if (nextLevel == 2) {
                System.out.println("- Unlocks: Archer Barrack");
            } else if (nextLevel == 4) {
                System.out.println("- Unlocks: Mage Barrack");
            } else if (nextLevel == 7) {
                System.out.println("- Unlocks: Knight Barrack");
            } else if (nextLevel == 9) {
                System.out.println("- Unlocks: Healer Barrack");
            }
        }

        if (isAtMaxLevel) {
            System.out.println();
            InputUtil.displayError("Townhall is already at maximum level!");
            InputUtil.pressEnterToContinue();
            return;
        }
        boolean confirm = InputUtil.readConfirmation("\nConfirm upgrade? (y/n)");
        if (!confirm) {
            InputUtil.displayInfo("Upgrade cancelled.");
            return;
        }

        if (!ctx.resourceService.hasEnoughResources(cost)) {
            InputUtil.displayError("Not enough resources for upgrade!");
            ctx.resourceService.displayResourceComparison(cost.food, cost.wood, cost.stone);
            InputUtil.pressEnterToContinue();
            return;
        }
        boolean success = ctx.buildingService.upgradeTownhall();
        if (success) {
            ctx.gameService.refreshGameState();
            Building updatedTownhall = ctx.buildingService.getBuildingByType(BuildingType.TOWNHALL);
            if (updatedTownhall != null && updatedTownhall.isUpgrading()) {
                showTownhallUpgradeProgress(updatedTownhall, currentLevel, nextLevel);
            }
        } else {
            InputUtil.displayError("Failed to start townhall upgrade.");
        }
    }

    private void showTownhallUpgradeProgress(Building townhall, int currentLevel, int nextLevel) {
        final boolean[] isLive = { true };
        final boolean[] userWantsToExit = { false };

        Thread inputThread = new Thread(() -> {
            try {
                InputUtil.waitForEnterInThread();
                userWantsToExit[0] = true;
            } catch (Exception e) {
            }
        });
        inputThread.setDaemon(true);
        inputThread.start();
        while (isLive[0] && !userWantsToExit[0]) {
            InputUtil.clearTerminal();
            ctx.gameService.refreshGameState();
            User user = ctx.getCurrentUser();
            TerminalArt.printMainHeader(user, ctx.gameService.getGameState());

            Building freshTownhall = ctx.buildingService.getBuildingByType(BuildingType.TOWNHALL);
            if (freshTownhall == null || freshTownhall.getUpgradeEndTime() == null) {
                InputUtil.printSectionSeparator("TOWNHALL UPGRADE PAGE");
                System.out.println(TerminalArt.white("Current Level   : " + currentLevel));
                System.out.println(TerminalArt.white("Upgrading to    : " + nextLevel));
                InputUtil.displaySuccess("\nTownhall upgrade completed to level " + nextLevel + "!");
                InputUtil.displayInfo("Press Enter to Continue...");
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                }
                isLive[0] = false;
                break;
            }
            long remainingSeconds = ctx.progressService.getBuildingUpgradeRemainingTime(freshTownhall);
            InputUtil.printSectionSeparator("TOWNHALL UPGRADE PAGE");
            System.out.println(TerminalArt.white("Current Level   : " + currentLevel));
            System.out.println(TerminalArt.white("Upgrading to    : " + nextLevel));
            if (remainingSeconds > 0) {
                long minutes = remainingSeconds / 60;
                long seconds = remainingSeconds % 60;
                String timeDisplay = String.format("%d:%02d", minutes, seconds);
                System.out.println(TerminalArt.white("Time Remaining  : " + timeDisplay));
                System.out.println();
                InputUtil.displayInfo("Press enter to go back");

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            } else {
                InputUtil.displaySuccess("\nTownhall upgrade completed to level " + nextLevel + "!");
                InputUtil.displayInfo("Press Enter to Continue...");
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                }
                isLive[0] = false;
                if (inputThread.isAlive()) {
                    inputThread.interrupt();
                }
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

    private void showBarrackUpgradeProgress(Building barrack, int currentLevel, int nextLevel) {
        final boolean[] isLive = { true };
        final boolean[] userWantsToExit = { false };

        Thread inputThread = new Thread(() -> {
            try {
                InputUtil.waitForEnterInThread();
                userWantsToExit[0] = true;
            } catch (Exception e) {
            }
        });
        inputThread.setDaemon(true);
        inputThread.start();
        while (isLive[0] && !userWantsToExit[0]) {
            InputUtil.clearTerminal();
            ctx.gameService.refreshGameState();
            User user = ctx.getCurrentUser();
            TerminalArt.printMainHeader(user, ctx.gameService.getGameState());

            Building freshBarrack = null;
            List<Building> allBarracks = ctx.buildingService.getAllBarracks();
            for (Building b : allBarracks) {
                if (b.getId() == barrack.getId()) {
                    freshBarrack = b;
                    break;
                }
            }
            if (freshBarrack == null || freshBarrack.getUpgradeEndTime() == null) {
                InputUtil.displaySuccess("\nBarrack upgrade completed to level " + nextLevel + "!");
                InputUtil.displayInfo("Press Enter to Continue...");
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                }
                isLive[0] = false;
                break;
            }
            long remainingSeconds = ctx.progressService.getBuildingUpgradeRemainingTime(freshBarrack);
            InputUtil.printSectionSeparator("BARRACK UPGRADE IN PROGRESS");
            System.out.println(TerminalArt.white("Barrack Type    : " + freshBarrack.getType()));
            System.out.println(TerminalArt.white("Current Level   : " + currentLevel));
            System.out.println(TerminalArt.white("Upgrading to    : " + nextLevel));

            if (remainingSeconds > 0) {
                long minutes = remainingSeconds / 60;
                long seconds = remainingSeconds % 60;
                String timeDisplay = String.format("%d:%02d", minutes, seconds);
                System.out.println(TerminalArt.white("Time Remaining  : " + timeDisplay));
                System.out.println();
                InputUtil.displayInfo("Press enter to go back");

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            } else {
                InputUtil.displaySuccess("Barrack upgrade completed to level " + nextLevel + "!");
                InputUtil.displayInfo("Press Enter to Continue...");
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                }
                isLive[0] = false;
                if (inputThread.isAlive()) {
                    inputThread.interrupt();
                }
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

    private void storageUpgradeMenu() {
        ctx.gameService.refreshGameState();
        InputUtil.clearTerminal();
        User user = ctx.getCurrentUser();
        TerminalArt.printMainHeader(user, ctx.gameService.getGameState());
        Building storage = ctx.buildingService.getBuildingByType(BuildingType.STORAGE);
        if (storage == null) {
            InputUtil.displayError("Storage not found!");
            return;
        }

        int currentLevel = storage.getLevel();
        int nextLevel = currentLevel + 1;
        Building townhall = ctx.buildingService.getBuildingByType(BuildingType.TOWNHALL);
        int maxLevel = GameConfig.getMaxStorageLevel(townhall.getLevel());

        if (storage.getUpgradeEndTime() != null) {
            showStorageUpgradeProgress(storage, currentLevel, nextLevel);
            return;
        }

        boolean isAtTownhallMaxLevel = (nextLevel > maxLevel);
        boolean isAtAbsoluteMaxLevel = (nextLevel > 10);
        boolean isAtMaxLevel = isAtTownhallMaxLevel || isAtAbsoluteMaxLevel;

        GameConfig.UpgradeCost cost;
        GameConfig.StorageCapacity currentCapacity = GameConfig.getStorageCapacity(currentLevel);
        GameConfig.StorageCapacity newCapacity;
        if (isAtMaxLevel) {
            if (isAtAbsoluteMaxLevel) {
                cost = GameConfig.getStorageUpgradeCost(10);
                newCapacity = GameConfig.getStorageCapacity(10);
                nextLevel = 10;
            } else {
                cost = GameConfig.getStorageUpgradeCost(maxLevel);
                newCapacity = GameConfig.getStorageCapacity(maxLevel);
                nextLevel = maxLevel;
            }
        } else {
            cost = GameConfig.getStorageUpgradeCost(nextLevel);
            newCapacity = GameConfig.getStorageCapacity(nextLevel);
        }

        InputUtil.printSectionSeparator("UPGRADE STORAGE");
        System.out.println(TerminalArt.white("Current Level   : " + currentLevel));
        System.out.println(TerminalArt.white("Next Level      : " + nextLevel));
        System.out.println(TerminalArt
                .white("Cost            : " + cost.food + " Food, " + cost.wood + " Wood, " + cost.stone + " Stone"));
        System.out.println(TerminalArt.white("Upgrade Time    : " + cost.timeSeconds + " seconds"));

        InputUtil.printSubsectionSeparator("Storage Capacity Increase");
        if (isAtMaxLevel) {
            System.out.println(
                    "- Food Capacity: " + currentCapacity.maxFood + " -> " + currentCapacity.maxFood + " (+0)");
            System.out.println(
                    "- Wood Capacity: " + currentCapacity.maxWood + " -> " + currentCapacity.maxWood + " (+0)");
            System.out.println(
                    "- Stone Capacity: " + currentCapacity.maxStone + " -> " + currentCapacity.maxStone + " (+0)");
        } else {
            System.out.println("- Food Capacity: " + currentCapacity.maxFood + " -> " + newCapacity.maxFood +
                    " (+" + (newCapacity.maxFood - currentCapacity.maxFood) + ")");
            System.out.println("- Wood Capacity: " + currentCapacity.maxWood + " -> " + newCapacity.maxWood +
                    " (+" + (newCapacity.maxWood - currentCapacity.maxWood) + ")");
            System.out.println("- Stone Capacity: " + currentCapacity.maxStone + " -> " + newCapacity.maxStone +
                    " (+" + (newCapacity.maxStone - currentCapacity.maxStone) + ")");
        }

        if (isAtMaxLevel) {
            System.out.println();
            if (isAtAbsoluteMaxLevel) {
                InputUtil.displayError("Storage is already at maximum level!");
            } else {
                InputUtil.displayError("Storage is already at maximum level for your townhall!");
                InputUtil.displayInfo("Upgrade your townhall to unlock higher storage levels.");
            }
            InputUtil.pressEnterToContinue();
            return;
        }

        boolean confirm = InputUtil.readConfirmation("\nConfirm upgrade? (y/n)");
        if (!confirm) {
            InputUtil.displayInfo("Upgrade cancelled.");
            return;
        }

        if (!ctx.resourceService.hasEnoughResources(cost)) {
            InputUtil.displayError("Not enough resources for upgrade!");
            ctx.resourceService.displayResourceComparison(cost.food, cost.wood, cost.stone);
            InputUtil.pressEnterToContinue();
            return;
        }
        boolean success = ctx.buildingService.upgradeStorage();
        if (success) {

            ctx.gameService.refreshGameState();
            Building updatedStorage = ctx.buildingService.getBuildingByType(BuildingType.STORAGE);
            if (updatedStorage != null && updatedStorage.getUpgradeEndTime() != null) {
                showStorageUpgradeProgress(updatedStorage, currentLevel, nextLevel);
            }
        } else {
            InputUtil.displayError("Failed to start storage upgrade.");
            InputUtil.pressEnterToContinue();
        }
    }

    private void messagesMenu() {
        InputUtil.clearTerminal();

        User user = ctx.getCurrentUser();
        ctx.gameService.refreshGameState();
        TerminalArt.printMainHeader(user, ctx.gameService.getGameState());

        ctx.messageService.displayMessages();

        InputUtil.pressEnterToContinue();
    }

    private void showBarrackListWithLiveTimers(List<Building> allBarracks, int townhallLevel) {
        final boolean[] userWantsToExit = { false };

        Thread inputThread = new Thread(() -> {
            try {
                InputUtil.waitForEnterInThread();
                userWantsToExit[0] = true;
            } catch (Exception e) {

            }
        });
        inputThread.setDaemon(true);
        inputThread.start();

        while (!userWantsToExit[0]) {

            InputUtil.clearTerminal();
            ctx.gameService.refreshGameState();
            User user = ctx.getCurrentUser();
            TerminalArt.printMainHeader(user, ctx.gameService.getGameState());

            List<Building> freshBarracks = ctx.buildingService.getAllBarracks();
            boolean stillHasUpgrading = false;
            InputUtil.printSectionSeparator("UPGRADE BARRACK");

            for (int i = 0; i < freshBarracks.size(); i++) {
                Building barrack = freshBarracks.get(i);
                int maxLevel = GameConfig.getMaxBarrackLevel(townhallLevel);
                String statusInfo = "";

                if (barrack.getUpgradeEndTime() != null) {
                    long remainingSeconds = ctx.progressService.getBuildingUpgradeRemainingTime(barrack);
                    if (remainingSeconds > 0) {
                        stillHasUpgrading = true;
                        long minutes = remainingSeconds / 60;
                        long seconds = remainingSeconds % 60;
                        String timeDisplay = String.format("%d:%02d", minutes, seconds);
                        statusInfo = " - UPGRADING (" + timeDisplay + " remaining)";
                    } else {
                        statusInfo = " - UPGRADE COMPLETED!";
                    }
                } else if (barrack.getLevel() >= maxLevel) {
                    statusInfo = " - MAX LEVEL (Upgrade Townhall First)";
                } else {
                    statusInfo = " - Ready to Upgrade";
                }

                System.out.println("[" + (i + 1) + "] " + barrack.getType() + " Level " + barrack.getLevel() +
                        " (Max: " + maxLevel + ")" + statusInfo);
            }
            if (stillHasUpgrading) {
                System.out.println("[0] Cancel");
                System.out.println();
                InputUtil.displayInfo("Press any key to stop live view and continue with selection");

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            } else {

                System.out.println("[0] Cancel");
                InputUtil.displaySuccess("All upgrades completed!");
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                }
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

        continueWithBarrackSelection(allBarracks, townhallLevel);
    }

    private void displayStaticBarrackList(List<Building> allBarracks, int townhallLevel) {
        for (int i = 0; i < allBarracks.size(); i++) {
            Building barrack = allBarracks.get(i);
            int maxLevel = GameConfig.getMaxBarrackLevel(townhallLevel);
            String statusInfo = "";

            if (barrack.getUpgradeEndTime() != null) {
                statusInfo = " - UPGRADING";
            } else if (barrack.getLevel() >= maxLevel) {
                statusInfo = " - MAX LEVEL (Upgrade Townhall First)";
            } else {
                statusInfo = " - Ready to Upgrade";
            }

            System.out.println("[" + (i + 1) + "] " + barrack.getType() + " Level " + barrack.getLevel() +
                    " (Max: " + maxLevel + ")" + statusInfo);
        }

        System.out.println("[0] Cancel");
        continueWithBarrackSelection(allBarracks, townhallLevel);
    }

    private void continueWithBarrackSelection(List<Building> allBarracks, int townhallLevel) {
        System.out.println();
        int choice = InputUtil.readIntWithMenuPrompt(InputUtil.createMenuPrompt("Choose Menu"), 0,
                allBarracks.size());
        if (choice <= 0 || choice > allBarracks.size()) {
            InputUtil.displayInfo("Upgrade cancelled.");
            return;
        }

        Building selectedBarrack = allBarracks.get(choice - 1);

        if (selectedBarrack.getUpgradeEndTime() != null) {
            int currentLevel = selectedBarrack.getLevel();
            int nextLevel = currentLevel + 1;
            showBarrackUpgradeProgress(selectedBarrack, currentLevel, nextLevel);
            return;
        }
        int maxLevel = GameConfig.getMaxBarrackLevel(townhallLevel);
        if (selectedBarrack.getLevel() >= maxLevel) {
            InputUtil.displayError("\nThis barrack is already at maximum level for your townhall!");
            InputUtil.displayInfo(
                    "Upgrade your townhall to level to unlock higher barrack levels.");
            InputUtil.pressEnterToContinue();
            return;
        }

        int currentLevel = selectedBarrack.getLevel();
        int nextLevel = currentLevel + 1;

        GameConfig.UpgradeCost cost = GameConfig.getBarrackUpgradeCost(nextLevel);

        InputUtil.clearTerminal();
        User user = ctx.getCurrentUser();
        TerminalArt.printMainHeader(user, ctx.gameService.getGameState());

        InputUtil.printSectionSeparator("BARRACK UPGRADE DETAILS");
        System.out.println(TerminalArt.white("Barrack Type    : " + selectedBarrack.getType()));
        System.out.println(TerminalArt.white("Current Level   : " + currentLevel));
        System.out.println(TerminalArt.white("Next Level      : " + nextLevel));
        System.out.println(TerminalArt
                .white("Cost            : " + cost.food + " Food, " + cost.wood + " Wood, " + cost.stone + " Stone"));
        System.out.println(TerminalArt.white("Upgrade Time    : " + cost.timeSeconds + " seconds"));

        InputUtil.printSubsectionSeparator("Upgrade Benefits");
        int currentCapacity = GameConfig.getBarrackCapacity(currentLevel);
        int nextCapacity = GameConfig.getBarrackCapacity(nextLevel);
        System.out.println("- Unit Capacity: " + currentCapacity +
                " -> " + nextCapacity +
                " (+" + (nextCapacity - currentCapacity) + ")");

        boolean confirm = InputUtil.readConfirmation("\nConfirm upgrade? (y/n)");
        if (!confirm) {
            InputUtil.displayInfo("Upgrade cancelled.");
            return;
        }

        if (!ctx.resourceService.hasEnoughResources(cost)) {
            InputUtil.displayError("Not enough resources for upgrade!");
            ctx.resourceService.displayResourceComparison(cost.food, cost.wood, cost.stone);
            InputUtil.pressEnterToContinue();
            return;
        }
        boolean success = ctx.buildingService.upgradeBarrack(selectedBarrack);
        if (success) {
            showBarrackUpgradeProgress(selectedBarrack, currentLevel, nextLevel);
        } else {
            InputUtil.displayError("Failed to start barrack upgrade.");
            InputUtil.pressEnterToContinue();
        }
    }

    private void showStorageUpgradeProgress(Building storage, int currentLevel, int nextLevel) {
        final boolean[] isLive = { true };
        final boolean[] userWantsToExit = { false };

        Thread inputThread = new Thread(() -> {
            try {
                InputUtil.waitForEnterInThread();
                userWantsToExit[0] = true;
            } catch (Exception e) {

            }
        });
        inputThread.setDaemon(true);
        inputThread.start();

        while (isLive[0] && !userWantsToExit[0]) {

            InputUtil.clearTerminal();
            ctx.gameService.refreshGameState();
            User user = ctx.getCurrentUser();
            TerminalArt.printMainHeader(user, ctx.gameService.getGameState());

            Building freshStorage = ctx.buildingService.getBuildingByType(BuildingType.STORAGE);
            if (freshStorage == null || freshStorage.getUpgradeEndTime() == null) {

                InputUtil.displaySuccess("Storage upgrade completed to level " + nextLevel + "!");
                System.out.println();
                InputUtil.displayInfo("Press Enter to Continue...");
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                }
                isLive[0] = false;
                break;
            }
            long remainingSeconds = ctx.progressService.getBuildingUpgradeRemainingTime(freshStorage);
            InputUtil.printSectionSeparator("STORAGE UPGRADE IN PROGRESS");
            System.out.println(TerminalArt.white("Current Level   : " + currentLevel));
            System.out.println(TerminalArt.white("Upgrading to    : " + nextLevel));

            if (remainingSeconds > 0) {
                long minutes = remainingSeconds / 60;
                long seconds = remainingSeconds % 60;
                String timeDisplay = String.format("%d:%02d", minutes, seconds);
                System.out.println(TerminalArt.white("Time Remaining  : " + timeDisplay));
                System.out.println();
                InputUtil.displayInfo("Press enter to go back");

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            } else {

                System.out.println();
                InputUtil.displaySuccess("Storage upgrade completed to level " + nextLevel + "!");
                InputUtil.displayInfo("Press Enter to Continue...");
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                }
                isLive[0] = false;

                if (inputThread.isAlive()) {
                    inputThread.interrupt();
                }
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

    private void showBarrackConstructionProgress(BuildingType barrackType) {
        final boolean[] isLive = { true };
        final boolean[] userWantsToExit = { false };

        Thread inputThread = new Thread(() -> {
            try {
                InputUtil.waitForEnterInThread();
                userWantsToExit[0] = true;
            } catch (Exception e) {

            }
        });
        inputThread.setDaemon(true);
        inputThread.start();

        while (isLive[0] && !userWantsToExit[0]) {

            InputUtil.clearTerminal();
            ctx.gameService.refreshGameState();
            User user = ctx.getCurrentUser();
            TerminalArt.printMainHeader(user, ctx.gameService.getGameState());

            Building constructingBarrack = null;
            List<Building> allBuildings = ctx.buildingService.getUserBuildings();
            for (Building building : allBuildings) {
                if (building.getType() == barrackType && building.getLevel() == 0
                        && building.getUpgradeEndTime() != null) {
                    constructingBarrack = building;
                    break;
                }
            }
            if (constructingBarrack == null || constructingBarrack.getUpgradeEndTime() == null) {

                System.out.println();
                InputUtil.displaySuccess("Barrack construction completed!");
                InputUtil.displayInfo("Press Enter to Continue...");
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                }
                isLive[0] = false;

                if (inputThread.isAlive()) {
                    inputThread.interrupt();
                }
                break;
            }

            long remainingSeconds = ctx.progressService.getBuildingUpgradeRemainingTime(constructingBarrack);

            InputUtil.printSectionSeparator("BARRACK CONSTRUCTION PAGE");
            System.out.println(TerminalArt.white("Barrack Type    : " + constructingBarrack.getType()));
            System.out.println(TerminalArt.white("Construction Status : In Progress"));

            if (remainingSeconds > 0) {
                long minutes = remainingSeconds / 60;
                long seconds = remainingSeconds % 60;
                String timeDisplay = String.format("%d:%02d", minutes, seconds);
                System.out.println(TerminalArt.white("Time Remaining  : " + timeDisplay));
                System.out.println();
                InputUtil.displayInfo("Press enter to go back");

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            } else {

                System.out.println();
                InputUtil.displaySuccess("Barrack construction completed!");
                InputUtil.displayInfo("Press Enter to Continue...");
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                }
                isLive[0] = false;
                if (inputThread.isAlive()) {
                    inputThread.interrupt();
                }
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
