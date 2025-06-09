package valaermortis;

import valaermortis.cli.Menu;
import valaermortis.core.AppContext;
import valaermortis.util.TerminalArt;

public class Main {
    public static void main(String[] args) {
        AppContext ctx = new AppContext();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            ctx.shutdown();
            TerminalArt.resetTerminalColors();
        }));

        try {
            new Menu(ctx).start();
        } finally {
            ctx.shutdown();
            TerminalArt.resetTerminalColors();
        }
    }
}