package model;

/**
 * Student borrowing policy: up to 3 books, Rs. 5/day fine on overdue books.
 */
public class Student extends Member {

    private static final int MAX_BOOKS = 3;
    private static final double FINE_PER_DAY = 5.0;

    public Student(String memberId, String name) {
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
        return "STUDENT";
    }
}
