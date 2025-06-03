package valaermortis;

import valaermortis.cli.Menu;
import valaermortis.core.AppContext;

public class Main {
    public static void main(String[] args) {
        AppContext ctx = new AppContext();
        new Menu(ctx).start();
    }
}