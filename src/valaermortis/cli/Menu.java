package valaermortis.cli;

import valaermortis.core.AppContext;
import valaermortis.model.User;
import valaermortis.util.InputUtil;
import valaermortis.util.TerminalArt;

public class Menu {
    private final AppContext ctx;

    public Menu(AppContext ctx) { this.ctx = ctx; }

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
        TerminalArt.printBanner();
        System.out.println(TerminalArt.cyan("[1] Login"));
        System.out.println(TerminalArt.cyan("[2] Register"));
        System.out.println(TerminalArt.cyan("[3] Keluar"));
        int ch = InputUtil.readInt(TerminalArt.green("Pilih menu: "));
        switch (ch) {
            case 1:
                ctx.authService.login();
                break;
            case 2:
                ctx.authService.register();
                break;
            case 3:
                TerminalArt.goodbye();
                System.exit(0);
                break;
            default:
                System.out.println(TerminalArt.red("Pilihan tidak valid."));
                break;
        }
    }

    private void showMainMenu() {
        User user = ctx.getCurrentUser();
        System.out.println(TerminalArt.yellow(
                "[1] Status Base & Resource\n" +
                "[2] Upgrade Townhall\n" +
                "[3] Build/Upgrade Barrack\n" +
                "[4] Upgrade Storage\n" +
                "[5] Latih Pasukan\n" +
                "[6] Kirim Pasukan: Mining\n" +
                "[7] Kirim Pasukan: Attack Creature\n" +
                "[8] Cek Status Pasukan & Misi\n" +
                "[9] Logout"));
        int ch = InputUtil.readInt(TerminalArt.green("Pilih menu: "));        
        switch (ch) {
            case 1:
                break;
            case 2:
                break;
            case 3:
                break;
            case 4:
                break;
            case 5:
                break;
            case 6:
                break;
            case 7:
                break;
            case 8:
                break;
            case 9:
                TerminalArt.print(TerminalArt.magenta("Logout berhasil. Sampai jumpa, " + user.getUsername() + "!"));
                ctx.setCurrentUser(null);
                break;
            default:
                System.out.println(TerminalArt.red("Pilihan tidak valid."));
                break;
        }
    }
}