package util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles all disk I/O for the system: reading/writing pipe-delimited
 * text files used as a lightweight persistence layer (data/books.txt,
 * data/members.txt, data/transactions.txt).
 *
 * Kept separate from the domain classes (BookCatalog, IssueManager)
 * so the storage format can be swapped later (e.g. for a real database)
 * without touching business logic - supporting Maintainability.
 */
public class FileHandler {

    private FileHandler() {
    }

    /** Reads every non-blank line of a file. Returns an empty list if the file does not exist yet. */
    public static List<String> readLines(String path) {
        List<String> lines = new ArrayList<>();
        try {
            if (!Files.exists(Paths.get(path))) {
                return lines;
            }
            try (BufferedReader br = new BufferedReader(new FileReader(path))) {
                String line;
                while ((line = br.readLine()) != null) {
                    if (!line.isBlank()) {
                        lines.add(line);
                    }
                }
            }
        } catch (IOException e) {
            Logger.log("ERROR reading " + path + ": " + e.getMessage());
        }
        return lines;
    }

    /** Overwrites a file with the given lines (used to persist the full in-memory state). */
    public static synchronized void writeLines(String path, List<String> lines) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(path, false))) {
            for (String line : lines) {
                pw.println(line);
            }
        } catch (IOException e) {
            Logger.log("ERROR writing " + path + ": " + e.getMessage());
        }
    }

    /** Appends a single line to a file, creating it if needed (used for the transaction log). */
    public static synchronized void appendLine(String path, String line) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(path, true))) {
            pw.println(line);
        } catch (IOException e) {
            Logger.log("ERROR appending to " + path + ": " + e.getMessage());
        }
    }
}
