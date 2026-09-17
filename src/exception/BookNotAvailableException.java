package exception;

/**
 * Thrown when a member attempts to issue a book that is already
 * issued to someone else or does not exist in the catalog.
 */
public class BookNotAvailableException extends Exception {
    public BookNotAvailableException(String message) {
        super(message);
    }
}
