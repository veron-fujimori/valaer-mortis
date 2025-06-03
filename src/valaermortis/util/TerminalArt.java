package valaermortis.util;

import valaermortis.model.User;
import valaermortis.model.GameState;

public class TerminalArt {
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_CYAN = "\u001B[36m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_MAGENTA = "\u001B[35m";

    public static String cyan(String s) { return ANSI_CYAN + s + ANSI_RESET; }
    public static String yellow(String s) { return ANSI_YELLOW + s + ANSI_RESET; }
    public static String green(String s) { return ANSI_GREEN + s + ANSI_RESET; }
    public static String red(String s) { return ANSI_RED + s + ANSI_RESET; }
    public static String magenta(String s) { return ANSI_MAGENTA + s + ANSI_RESET; }

    public static void print(String s) { System.out.println(s); }

    public static void printBanner() {
        System.out.println(ANSI_CYAN + 
                "╔══════════════════════════════════════════════╗\n" +
                "║            VALAER MORTIS CLI RPG            ║\n" +
                "╚══════════════════════════════════════════════╝" + 
                ANSI_RESET);
    }

    public static void printMainHeader(User user, GameState state) {
        System.out.println(ANSI_YELLOW +
                "╔════════════════════════════════════════════════════════════╗\n" +
                "║ " + cyan("User: " + user.getUsername()) +
                " | Townhall Lv." + state.getTownhallLvl() +
                " | Storage: " + state.getFood() + "/" + state.getMaxFood() + " Food, "
                + state.getWood() + "/" + state.getMaxWood() + " Wood, "
                + state.getStone() + "/" + state.getMaxStone() + " Stone" + " ║\n" +
                "╚════════════════════════════════════════════════════════════╝" + ANSI_RESET);
    }

    public static void goodbye() {
        System.out.println(magenta("\nTerima kasih telah bermain Valaer Mortis. Sampai jumpa!\n"));
    }
}