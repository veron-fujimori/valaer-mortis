package valaermortis.util;

import java.util.Scanner;

public class InputUtil {
    private static final Scanner sc = new Scanner(System.in);

    public static String readString(String prompt) {
        System.out.print(prompt);
        return sc.nextLine();
    }
    public static String readPassword(String prompt) {
        System.out.print(prompt);
        return sc.nextLine();
    }
    public static int readInt(String prompt) {
        System.out.print(prompt);
        while (!sc.hasNextInt()) { sc.next(); System.out.print(prompt); }
        int i = sc.nextInt(); sc.nextLine(); return i;
    }
}