package valaermortis.service;

import valaermortis.core.AppContext;
import valaermortis.dao.UserDao;
import valaermortis.dao.GameStateDao;
import valaermortis.dao.BuildingDao;
import valaermortis.model.User;
import valaermortis.util.InputUtil;
import valaermortis.util.PasswordHasher;
import valaermortis.util.TerminalArt;

public class AuthService {
    private final AppContext ctx;
    private final UserDao userDao = new UserDao();
    private final GameStateDao gameStateDao = new GameStateDao();
    private final BuildingDao buildingDao = new BuildingDao();

    public AuthService(AppContext ctx) { this.ctx = ctx; }

    public void login() {
        System.out.println(TerminalArt.cyan("\n=== LOGIN ==="));
        String username = InputUtil.readString("Username: ");
        String password = InputUtil.readPassword("Password: ");
        User user = userDao.findByUsername(username);
        if (user == null || !PasswordHasher.check(password, user.getPasswordHash())) {
            System.out.println(TerminalArt.red("Login gagal! Username/password salah."));
            return;
        }
        ctx.setCurrentUser(user);
        System.out.println(TerminalArt.green("\nLogin berhasil! Selamat datang, " + user.getUsername() + "!\n"));
    }    public void register() {
        System.out.println(TerminalArt.cyan("\n=== REGISTER ==="));
        String username = InputUtil.readString("Username: ");
        String password = InputUtil.readPassword("Password: ");
        if (userDao.findByUsername(username) != null) {
            System.out.println(TerminalArt.red("Username sudah dipakai!"));
            return;
        }
        String hash = PasswordHasher.hash(password);
        long userId = userDao.insert(username, hash);
        if (userId > 0) {
            gameStateDao.createInitialGameState(userId);
            buildingDao.createInitialBuildings(userId);
            System.out.println(TerminalArt.green("Registrasi berhasil! Silakan login.\n"));
        } else {
            System.out.println(TerminalArt.red("Registrasi gagal!"));
        }
    }
}