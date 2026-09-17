package model;

/**
 * Faculty borrowing policy: up to 6 books, Rs. 2/day fine on overdue books
 * (lower fine and higher quota reflects longer-term research use).
 */
public class Faculty extends Member {

    private static final int MAX_BOOKS = 6;
    private static final double FINE_PER_DAY = 2.0;

    public Faculty(String memberId, String name) {
        super(memberId, name);
    }

    @Override
    public int getMaxBooksAllowed() {
        return MAX_BOOKS;
    }

    @Override
    public double getFinePerDay() {
        return FINE_PER_DAY;
    }

    @Override
    public String getRole() {
        return "FACULTY";
    }
}
