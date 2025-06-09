package valaermortis.core;

import valaermortis.dao.BuildingDao;
import valaermortis.dao.MissionDao;
import valaermortis.dao.UnitQueueDao;
import valaermortis.model.Building;
import valaermortis.model.Mission;
import valaermortis.model.UnitQueue;
import valaermortis.util.ErrorHandler;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class BackgroundTaskManager {

    private final AtomicBoolean isRunning = new AtomicBoolean(false);

    private ScheduledExecutorService taskChecker;

    private final BuildingDao buildingDao = new BuildingDao();
    private final MissionDao missionDao = new MissionDao();
    private final UnitQueueDao unitQueueDao = new UnitQueueDao();

    private final AppContext appContext;

    public BackgroundTaskManager(AppContext appContext) {
        this.appContext = appContext;
    }

    public void startBackgroundTasks() {
        if (isRunning.compareAndSet(false, true)) {
            taskChecker = Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r, "TaskChecker");
                t.setDaemon(true);
                return t;
            });

            taskChecker.scheduleAtFixedRate(
                    this::checkAndCompleteAllTasks,
                    0, 1, TimeUnit.SECONDS);
        }
    }

    public void stopBackgroundTasks() {
        if (isRunning.compareAndSet(true, false)) {
            shutdownExecutor(taskChecker, "TaskChecker");
        }
    }

    private void shutdownExecutor(ExecutorService executor, String name) {
        if (executor != null && !executor.isShutdown()) {
            executor.shutdown();
            try {
                if (!executor.awaitTermination(2, TimeUnit.SECONDS)) {
                    executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                executor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }

    private void checkAndCompleteAllTasks() {
        if (!isRunning.get() || !appContext.isLoggedIn()) {
            return;
        }

        try {
            String currentUserId = appContext.getCurrentUserId();
            if (currentUserId == null)
                return;

            checkCompletedBuildings(currentUserId);
            checkCompletedMissions(currentUserId);
            checkCompletedUnitTraining(currentUserId);

        } catch (Exception e) {
            ErrorHandler.logError("Background task execution", e);
        }
    }

    private void checkCompletedBuildings(String userId) {
        try {
            LocalDateTime now = LocalDateTime.now();
            List<Building> buildings = buildingDao.getByUserId(userId);

            for (Building building : buildings) {
                if (building.getUpgradeEndTime() != null &&
                        building.getUpgradeEndTime().toLocalDateTime().isBefore(now)) {
                    appContext.progressService.checkCompletedBuildingUpgrades();
                    break;
                }
            }
        } catch (Exception e) {
            ErrorHandler.logError("Checking completed buildings", e);
        }
    }

    private void checkCompletedMissions(String userId) {
        try {
            LocalDateTime now = LocalDateTime.now();
            List<Mission> activeMissions = missionDao.getActiveMissions(userId);

            for (Mission mission : activeMissions) {
                if (mission.getEndTime() != null && mission.getEndTime().toLocalDateTime().isBefore(now)) {

                    appContext.progressService.checkCompletedMissions();
                    break;
                }
            }
        } catch (Exception e) {
            ErrorHandler.logError("Checking completed missions", e);
        }
    }

    private void checkCompletedUnitTraining(String userId) {
        try {
            LocalDateTime now = LocalDateTime.now();
            List<UnitQueue> activeQueues = unitQueueDao.getActiveQueuesByUserId(userId);

            for (UnitQueue queue : activeQueues) {
                if (queue.getEndTime() != null &&
                        queue.getEndTime().toLocalDateTime().isBefore(now)) {

                    appContext.progressService.checkCompletedUnitTraining();
                    break;
                }
            }
        } catch (Exception e) {
            ErrorHandler.logError("Checking completed unit training", e);
        }
    }

    public boolean isRunning() {
        return isRunning.get();
    }
}
