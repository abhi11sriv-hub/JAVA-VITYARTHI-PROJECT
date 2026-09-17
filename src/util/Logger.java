package util;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Minimal append-only logger written to data/activity.log.
 * Synchronized so multiple issue/return threads can log safely
 * (supports the Logging / Monitoring non-functional requirement).
 */
public class Logger {

    private static final String LOG_FILE = "data/activity.log";
    private static final DateTimeFormatter FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // Private constructor: this class is a static utility, never instantiated.
    private Logger() {
    }

    public static synchronized void log(String message) {
        String line = "[" + LocalDateTime.now().format(FORMAT) + "] " + message;
        System.out.println(line);
        try (FileWriter fw = new FileWriter(LOG_FILE, true);
             PrintWriter pw = new PrintWriter(fw)) {
            pw.println(line);
        } catch (IOException e) {
            // Logging must never crash the application; report to stderr instead.
            System.err.println("Warning: could not write to log file - " + e.getMessage());
        }
    }
}
