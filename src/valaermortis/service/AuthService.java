package valaermortis.service;

import valaermortis.core.AppContext;
import valaermortis.dao.UserDao;
import valaermortis.dao.GameStateDao;
import valaermortis.dao.BuildingDao;
import valaermortis.model.User;
import valaermortis.util.ErrorHandler;
import valaermortis.util.InputUtil;
import valaermortis.util.PasswordHasher;

public class AuthService {
    private final AppContext ctx;
    private final UserDao userDao = new UserDao();
    private final GameStateDao gameStateDao = new GameStateDao();
    private final BuildingDao buildingDao = new BuildingDao();

    public AuthService(AppContext ctx) {
        this.ctx = ctx;
    }

    public void login() {
        InputUtil.printSectionSeparator("LOGIN");
        String username = InputUtil.readString("Username");
        String password = InputUtil.readPassword("Password");

        User user = userDao.findByUsername(username);
        if (user == null) {
            System.out.println();
            InputUtil.displayError("Login gagal! Username tidak ditemukan.");
            return;
        }
        if (!PasswordHasher.check(password, user.getPasswordHash())) {
            System.out.println();
            InputUtil.displayError("Login gagal! Password salah.");
            return;
        }

        ctx.setCurrentUser(user);
        System.out.println();
        InputUtil.displaySuccess("Login berhasil! Selamat datang, " + user.getUsername() + "!");
    }

    public void register() {
        InputUtil.printSectionSeparator("REGISTER");
        String username = InputUtil.readString("Username");
        String password = InputUtil.readPassword("Password");

        if (userDao.findByUsername(username) != null) {
            System.out.println();
            InputUtil.displayError("Username sudah dipakai!");
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
                    InputUtil.displaySuccess("Registrasi berhasil! Silakan login.");
                } else {
                    System.out.println();
                    InputUtil.displayError("Registrasi gagal! Error creating initial data.");
                }
            } catch (Exception e) {
                System.out.println();
                InputUtil.displayError("Registrasi gagal! Error during initialization.");
                ErrorHandler.logError("Creating initial user data", e);
            }
        } else {
            System.out.println();
            InputUtil.displayError("Registrasi gagal! Error inserting user.");
        }
    }
}