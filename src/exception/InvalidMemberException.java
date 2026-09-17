package exception;

/**
 * Thrown when a member ID is unknown, a member has exceeded their
 * borrowing quota, or a member-related operation is otherwise invalid.
 */
public class InvalidMemberException extends Exception {
    public InvalidMemberException(String message) {
        super(message);
    }
}
