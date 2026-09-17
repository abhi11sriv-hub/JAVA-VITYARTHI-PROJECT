package service;

import model.Book;
import util.FileHandler;
import util.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Module 2: Book Catalog Management.
 * Provides Create / Read / Update / Delete operations on the book
 * collection and persists them to data/books.txt.
 *
 * Backed by a ConcurrentHashMap so catalog reads used by IssueManager's
 * worker threads never see a partially-updated map (Performance +
 * thread-safety without a global lock).
 */
public class BookCatalog {

    private static final String DATA_FILE = "data/books.txt";
    private final Map<String, Book> books = new ConcurrentHashMap<>();

    public BookCatalog() {
        load();
    }

    // ---------- CRUD ----------

    public void addBook(Book book) {
        books.put(book.getBookId(), book);
        Logger.log("Book added: " + book.getBookId() + " - " + book.getTitle());
        save();
    }

    public Book getBook(String bookId) {
        return books.get(bookId);
    }

    public boolean updateBook(String bookId, String newTitle, String newAuthor) {
        Book book = books.get(bookId);
        if (book == null) {
            return false;
        }
        book.setTitle(newTitle);
        book.setAuthor(newAuthor);
        Logger.log("Book updated: " + bookId);
        save();
        return true;
    }

    public boolean removeBook(String bookId) {
        Book removed = books.remove(bookId);
        if (removed != null) {
            Logger.log("Book removed: " + bookId);
            save();
            return true;
        }
        return false;
    }

    public Collection<Book> getAllBooks() {
        return books.values();
    }

    public List<Book> searchByTitle(String keyword) {
        List<Book> results = new ArrayList<>();
        String lower = keyword.toLowerCase();
        for (Book b : books.values()) {
            if (b.getTitle().toLowerCase().contains(lower)) {
                results.add(b);
            }
        }
        return results;
    }

    // ---------- persistence ----------
    // Format: bookId|title|author|available|issuedTo|issueDate|timesIssued

    private void load() {
        List<String> lines = FileHandler.readLines(DATA_FILE);
        for (String line : lines) {
            String[] parts = line.split("\\|", -1);
            if (parts.length < 7) continue;
            Book book = new Book(parts[0], parts[1], parts[2]);
            boolean available = Boolean.parseBoolean(parts[3]);
            if (!available) {
                book.markIssued();
                book.assignTo(parts[4].isEmpty() ? null : parts[4],
                               parts[5].isEmpty() ? null : parts[5]);
            }
            books.put(book.getBookId(), book);
        }
    }

    public synchronized void save() {
        List<String> lines = new ArrayList<>();
        for (Book b : books.values()) {
            lines.add(String.join("|",
                    b.getBookId(),
                    b.getTitle(),
                    b.getAuthor(),
                    String.valueOf(b.isAvailable()),
                    b.getIssuedTo() == null ? "" : b.getIssuedTo(),
                    b.getIssueDate() == null ? "" : b.getIssueDate(),
                    String.valueOf(b.getTimesIssued())));
        }
        FileHandler.writeLines(DATA_FILE, lines);
    }
}
