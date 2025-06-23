package valaermortis.util;

import java.util.Scanner;

public class InputUtil {
    private static final Scanner sc = new Scanner(System.in);

    public static String readString(String prompt) {
        while (true) {
            System.out.print(createStyledPrompt(prompt));
            String input = sc.nextLine().trim();

            if (!input.isEmpty()) {
                return input;
            } else {
                displayError("Input cannot be empty. Please enter text.");
            }
        }
    }

    public static String readOptionalString(String prompt) {
        System.out.print(createStyledPrompt(prompt));
        return sc.nextLine().trim();
    }

    public static String readPassword(String prompt) {
        while (true) {
            System.out.print(createStyledPrompt(prompt));
            String input = sc.nextLine().trim();
            if (!input.isEmpty()) {
                return input;
            } else {
                displayError("Input cannot be empty. Please enter password.");
            }
        }
    }

    public static int readInt(String prompt) {
        while (true) {
            System.out.print(createStyledPrompt(prompt));
            String input = sc.nextLine().trim();

            if (input.isEmpty()) {
                displayError("Input cannot be empty. Please enter a number.");
            } else {
                try {
                    return Integer.parseInt(input);
                } catch (NumberFormatException e) {
                    displayError("Input '" + input + "' is not valid. Please enter a valid number.");
                }
            }
        }
    }

    public static int readIntInRange(String prompt, int min, int max) {
        while (true) {
            int value = readInt(prompt);
            if (value >= min && value <= max) {
                return value;
            } else {
                displayError("Please enter a number between " + min + " and " + max + ".");
            }
        }
    }

    public static int readIntWithMenuPrompt(String menuPrompt, int min, int max) {
        while (true) {
            System.out.print(menuPrompt);
            String input = sc.nextLine().trim();
            if (input.isEmpty()) {
                displayError("Input cannot be empty. Please enter a number.");
            } else {
                try {
                    int value = Integer.parseInt(input);
                    if (value >= min && value <= max) {
                        return value;
                    } else {
                        displayError("Please enter a number between " + min + " and " + max + ".");
                    }
                } catch (NumberFormatException e) {
                    displayError("Input '" + input + "' is not valid. Please enter a valid number.");
                }
            }
        }
    }

    public static int readPositiveInt(String prompt) {
        while (true) {
            int value = readInt(prompt);
            if (value > 0) {
                return value;
            } else {
                displayError("Please enter a positive number (greater than 0).");
            }
        }
    }

    public static int readNonNegativeInt(String prompt) {
        while (true) {
            int value = readInt(prompt);
            if (value >= 0) {
                return value;
            } else {
                displayError("Please enter a non-negative number (0 or greater).");
            }
        }
    }

    public static void pressEnterToContinue() {
        pressEnterToContinue("Press Enter to continue...");
    }

    public static void pressEnterToContinue(String message) {
        System.out.print(TerminalArt.white(message + " "));
        sc.nextLine();
        clearInputBuffer();
        System.out.println();
    }

    public static String createStyledPrompt(String prompt) {
        return TerminalArt.white(prompt + ": ");
    }

    public static String createMenuPrompt(String prompt) {
        return TerminalArt.white("== " + prompt + " ==\n>> ");
    }

    public static void displaySuccess(String message) {
        System.out.println(TerminalArt.green(message));
    }

    public static void displayError(String message) {
        System.out.println(TerminalArt.red(message));
    }

    public static void displayInfo(String message) {
        System.out.println(TerminalArt.white(message));
    }

    public static void printSectionSeparator(String title) {
        System.out.println();
        System.out.println(TerminalArt.white("============ " + title + " ============"));
    }

    public static void printSubsectionSeparator(String title) {
        System.out.println();
        System.out.println(TerminalArt.white("-------- " + title + " --------"));
    }

    public static boolean readConfirmation(String prompt) {
        while (true) {
            System.out.print(createStyledPrompt(prompt));
            String input = sc.nextLine().trim().toLowerCase();
            if (input.equals("y") || input.equals("yes")) {
                System.out.println();
                return true;
            } else if (input.equals("n") || input.equals("no")) {
                System.out.println();
                return false;
            } else if (input.isEmpty()) {
                displayError("Please enter 'y' for yes or 'n' for no.");
            } else {
                displayError("Invalid input. Please enter 'y' for yes or 'n' for no.");
            }
        }
    }

    public static boolean readYesNo(String prompt) {
        while (true) {
            System.out.print(createStyledPrompt(prompt));
            String input = sc.nextLine().trim().toLowerCase();

            if (input.equals("y") || input.equals("yes")) {
                return true;
            } else if (input.equals("n") || input.equals("no")) {
                return false;
            } else {
                displayError("Please enter 'y' or 'n'.");
            }
        }
    }

    public static void waitForEnterInThread() {
        try {
            System.in.read();
            while (System.in.available() > 0) {
                System.in.read();
            }
            clearInputBuffer();
        } catch (Exception e) {
        }
    }

    public static void waitForEnter() {
        sc.nextLine();
    }

    public static void clearInputBuffer() {
        try {
            while (System.in.available() > 0) {
                System.in.read();
            }
        } catch (Exception e) {
        }
    }

    public static void clearTerminal() {
        TerminalArt.clearScreen();
    }
}