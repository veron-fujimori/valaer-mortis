package valaermortis.service;

import valaermortis.core.AppContext;
import valaermortis.model.GameState;

public class GameService {
    private final AppContext ctx;

    public GameService(AppContext ctx) {
        this.ctx = ctx;
    }

    public GameState getGameState() {
        return ctx.resourceService.getCurrentResources();
    }

    private void executeWithProgressCheck(Runnable operation) {
        ctx.progressService.checkAndCompleteAllActivities();

        operation.run();
    }

    public void upgradeTownhall() {
        executeWithProgressCheck(() -> ctx.buildingService.upgradeTownhall());
    }

    public void upgradeStorage() {
        executeWithProgressCheck(() -> ctx.buildingService.upgradeStorage());
    }

    public void trainUnitsMenu() {
        executeWithProgressCheck(() -> ctx.unitService.unitTrainingMenu());
    }

    public void miningMissionsMenu() {
        executeWithProgressCheck(() -> ctx.missionService.miningMissionsMenu());
    }

    public void attackMissionsMenu() {
        executeWithProgressCheck(() -> ctx.missionService.showLiveAttackTargetsMenu());
    }

    public void showMissionStatus() {
        executeWithProgressCheck(() -> ctx.missionService.showLiveMissionStatus());
    }

    public void refreshGameState() {
        ctx.progressService.checkAndCompleteAllActivities();
    }
}