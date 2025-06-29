package valaermortis.service;

import valaermortis.core.AppContext;
import valaermortis.dao.UserDao;
import valaermortis.dao.GameStateDao;
import valaermortis.dao.BuildingDao;
import valaermortis.model.User;
import valaermortis.util.ErrorHandler;
import valaermortis.util.InputUtil;
import valaermortis.util.PasswordHasher;
import valaermortis.util.TerminalArt;

public class AuthService {
    private final AppContext ctx;
    private final UserDao userDao = new UserDao();
    private final GameStateDao gameStateDao = new GameStateDao();
    private final BuildingDao buildingDao = new BuildingDao();

    public AuthService(AppContext ctx) {
        this.ctx = ctx;
    }

    public void login() {
        TerminalArt.printBanner();
        InputUtil.printSectionSeparator("LOGIN PAGE");
        String username = InputUtil.readString("Username");
        String password = InputUtil.readPassword("Password");

        User user = userDao.findByUsername(username);
        if (user == null) {
            System.out.println();
            InputUtil.displayError("Login failed! Username not found.");
            return;
        }
        if (!PasswordHasher.check(password, user.getPasswordHash())) {
            System.out.println();
            InputUtil.displayError("Login failed! Incorect password.");
            return;
        }

        ctx.setCurrentUser(user);
        System.out.println();
        InputUtil.displaySuccess("Login successful! Welcome, " + user.getUsername() + "!");
    }

    public void register() {
        TerminalArt.printBanner();
        InputUtil.printSectionSeparator("REGISTER PAGE");
        String username = InputUtil.readString("Username");
        String password = InputUtil.readPassword("Password");

        if (userDao.findByUsername(username) != null) {
            System.out.println();
            InputUtil.displayError("Username is already taken!");
            return;
        }

        String hash = PasswordHasher.hash(password);
        String userId = userDao.insert(username, hash);
        if (userId != null && !userId.isEmpty()) {
            try {
                long gameStateId = gameStateDao.createInitialGameState(userId);
                long buildingId = buildingDao.createInitialBuildings(userId);

                if (gameStateId > 0 && buildingId > 0) {
                    System.out.println();
                    InputUtil.displaySuccess("Registrasi successful! Please log in.");
                } else {
                    System.out.println();
                    InputUtil.displayError("Registrasi failed! Error creating initial data.");
                }
            } catch (Exception e) {
                System.out.println();
                InputUtil.displayError("Registrasi failed! Error during initialization.");
                ErrorHandler.logError("Creating initial user data", e);
            }
        } else {
            System.out.println();
            InputUtil.displayError("Registrasi failed! Error inserting user.");
        }
    }
}