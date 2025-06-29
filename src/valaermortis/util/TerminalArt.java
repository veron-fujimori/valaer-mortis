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
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_WHITE = "\u001B[37m";

    public static final String ANSI_DARK_GREEN = "\u001B[32m";
    public static final String ANSI_DARK_BLUE = "\u001B[34m";
    public static final String ANSI_DARK_CYAN = "\u001B[36m";
    public static final String ANSI_DARK_YELLOW = "\u001B[33m";
    public static final String ANSI_DARK_RED = "\u001B[31m";
    public static final String ANSI_DARK_MAGENTA = "\u001B[35m";

    public static final String ANSI_WHITE_BACKGROUND = "\u001B[47m";
    public static final String ANSI_BLACK_TEXT = "\u001B[30m";

    public static final String WHITE_BG_RESET = ANSI_WHITE_BACKGROUND + ANSI_BLACK_TEXT;

    public static String cyan(String s) {
        return ANSI_CYAN + s + ANSI_RESET;
    }

    public static String yellow(String s) {
        return ANSI_YELLOW + s + ANSI_RESET;
    }

    public static String green(String s) {
        return ANSI_GREEN + s + ANSI_RESET;
    }

    public static String red(String s) {
        return ANSI_RED + s + ANSI_RESET;
    }

    public static String magenta(String s) {
        return ANSI_MAGENTA + s + ANSI_RESET;
    }

    public static String blue(String s) {
        return ANSI_BLUE + s + ANSI_RESET;
    }

    public static String white(String s) {
        return s;
    }

    public static String brightGreen(String s) {
        return ANSI_GREEN + s + ANSI_RESET;
    }

    public static String brightBlue(String s) {
        return ANSI_BLUE + s + ANSI_RESET;
    }

    public static String brightCyan(String s) {
        return ANSI_CYAN + s + ANSI_RESET;
    }

    public static String brightYellow(String s) {
        return ANSI_YELLOW + s + ANSI_RESET;
    }

    public static void printLine(String text) {
        System.out.println(text);
    }

    public static void print(String text) {
        System.out.print(text);
    }

    public static void printBanner() {
        System.out.println("             _                                     _   _     ");
        System.out.println(" /\\   /\\__ _| | __ _  ___ _ __    /\\/\\   ___  _ __| |_(_)___ ");
        System.out.println(" \\ \\ / / _` | |/ _` |/ _ \\ '__|  /    \\ / _ \\| '__| __| / __|");
        System.out.println("  \\ V / (_| | | (_| |  __/ |    / /\\/\\ \\ (_) | |  | |_| \\__ \\");
        System.out.println("   \\_/ \\__,_|_|\\__,_|\\___|_|    \\/    \\/\\___/|_|   \\__|_|___/");
        System.out.println("                                                             ");
    }

    public static void printMainHeader(User user, GameState state) {
        String foodStr = String.format("%,d/%,d", state.getFood(), state.getMaxFood());
        String woodStr = String.format("%,d/%,d", state.getWood(), state.getMaxWood());
        String stoneStr = String.format("%,d/%,d", state.getStone(), state.getMaxStone());

        String header = String.format("| %-15s | Townhall Lv.%-2d | Food: %-15s Wood: %-15s Stone: %-15s |",
                user.getUsername(),
                state.getTownhallLvl(),
                foodStr,
                woodStr,
                stoneStr);
        System.out.println(
                "=========================================================================================================");
        System.out.println(header);
        System.out.println(
                "=========================================================================================================");
    }

    public static void goodbye() {
        System.out.println(brightCyan("\nThank you for playing Valaer Mortis. See you later!\n"));
        resetTerminalColors();
    }

    public static void setFullScreen() {
        try {
            System.out.print("\033[2J\033[H\u001B[?25l");
            System.out.flush();
        } catch (Exception e) {

        }
    }

    public static void exitFullScreen() {
        try {
            System.out.print("\u001B[?25h");
            System.out.flush();
        } catch (Exception e) {

        }
    }

    public static void resetTerminalColors() {
        System.out.print(ANSI_RESET);
        System.out.flush();
    }

    public static void clearScreen() {
        try {
            String os = System.getProperty("os.name").toLowerCase();

            if (os.contains("win")) {
                new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
            } else {
                new ProcessBuilder("clear").inheritIO().start().waitFor();
            }
        } catch (Exception e) {
            for (int i = 0; i < 50; i++) {
                System.out.println();
            }
        }
        System.out.flush();
    }
}