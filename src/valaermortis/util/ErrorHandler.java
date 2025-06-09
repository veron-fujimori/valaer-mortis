package valaermortis.util;

import java.sql.SQLException;
import java.util.logging.Logger;
import java.util.logging.Level;

public class ErrorHandler {

    private static final Logger logger = Logger.getLogger(ErrorHandler.class.getName());

    public static void logDatabaseError(String operation, SQLException e) {
        String errorMessage = "Database error while " + operation + ": " + e.getMessage();
        logger.log(Level.SEVERE, errorMessage, e);

        System.err.println("ERROR: " + errorMessage);

        if (isDebugMode()) {
            e.printStackTrace();
        }
    }

    public static void logError(String operation, Exception e) {
        String errorMessage = "Error while " + operation + ": " + e.getMessage();
        logger.log(Level.SEVERE, errorMessage, e);

        System.err.println("ERROR: " + errorMessage);

        if (isDebugMode()) {
            e.printStackTrace();
        }
    }

    private static boolean isDebugMode() {
        String debugProperty = System.getProperty("valaermortis.debug");
        if (debugProperty != null) {
            return Boolean.parseBoolean(debugProperty);
        }

        String debugEnv = System.getenv("VALAERMORTIS_DEBUG");
        if (debugEnv != null) {
            return Boolean.parseBoolean(debugEnv);
        }

        return false;
    }
}
