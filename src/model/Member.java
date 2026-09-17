package model;

import java.util.ArrayList;
import java.util.List;

/**
 * Abstract base type for anyone who can borrow books.
 * Concrete subtypes (Student, Faculty) override getMaxBooksAllowed()
 * and getFinePerDay() to express role-specific policy - this is the
 * polymorphism the borrowing logic in IssueManager relies on.
 */
public abstract class Member {

    private final String memberId;
    private final String name;
    private final List<String> issuedBookIds = new ArrayList<>();

    protected Member(String memberId, String name) {
        this.memberId = memberId;
        this.name = name;
    }

    public String getMemberId() {
        return memberId;
    }

    public String getName() {
        return name;
    }

    public List<String> getIssuedBookIds() {
        return issuedBookIds;
    }

    public void addIssuedBook(String bookId) {
        issuedBookIds.add(bookId);
    }

    public void removeIssuedBook(String bookId) {
        issuedBookIds.remove(bookId);
    }

    /** Maximum number of books this member type may hold at once. */
    public abstract int getMaxBooksAllowed();

    /** Fine, in rupees, charged per day overdue for this member type. */
    public abstract double getFinePerDay();

    /** Short role label used in reports and persistence ("STUDENT" / "FACULTY"). */
    public abstract String getRole();

    @Override
    public String toString() {
        return String.format("%s | %-10s | %-20s | books held: %d/%d",
                memberId, getRole(), name, issuedBookIds.size(), getMaxBooksAllowed());
    }
}
