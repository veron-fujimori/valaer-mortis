package valaermortis.core;

import valaermortis.model.User;
import valaermortis.service.*;

public class AppContext {
    private User currentUser;

    private final BackgroundTaskManager backgroundTaskManager;

    public final AuthService authService = new AuthService(this);
    public final BattleService battleService = new BattleService(this);

    public final ResourceService resourceService = new ResourceService(this);
    public final ProgressService progressService = new ProgressService(this);
    public final BuildingService buildingService = new BuildingService(this, resourceService, progressService);
    public final UnitService unitService = new UnitService(this, resourceService);
    public final MissionService missionService = new MissionService(this, unitService, resourceService, battleService);
    public final MessageService messageService = new MessageService(this);

    public final GameService gameService = new GameService(this);

    public AppContext() {
        this.backgroundTaskManager = new BackgroundTaskManager(this);
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;

        if (user != null && !backgroundTaskManager.isRunning()) {
            backgroundTaskManager.startBackgroundTasks();
        } else if (user == null && backgroundTaskManager.isRunning()) {
            backgroundTaskManager.stopBackgroundTasks();
        }
    }

    public boolean isLoggedIn() {
        return currentUser != null;
    }

    public String getCurrentUserId() {
        return currentUser != null ? currentUser.getId() : null;
    }

    public BackgroundTaskManager getBackgroundTaskManager() {
        return backgroundTaskManager;
    }

    public void shutdown() {
        backgroundTaskManager.stopBackgroundTasks();
    }
}