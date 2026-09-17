package service;

import interfaces.Reportable;
import model.Book;
import model.Member;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Reporting / Analytics half of Module 3. Implements Reportable so
 * it can be plugged into any future dashboard that expects that
 * interface.
 */
public class ReportGenerator implements Reportable {

    private static final int LOAN_PERIOD_DAYS = 14;
    private final BookCatalog catalog;
    private final MemberDirectory members;

    public ReportGenerator(BookCatalog catalog, MemberDirectory members) {
        this.catalog = catalog;
        this.members = members;
    }

    @Override
    public String generateReport() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== LIBRARY SUMMARY REPORT ===\n");
        sb.append("Total books: ").append(catalog.getAllBooks().size()).append("\n");
        sb.append("Total members: ").append(members.getAllMembers().size()).append("\n");
        long issuedCount = catalog.getAllBooks().stream().filter(b -> !b.isAvailable()).count();
        sb.append("Books currently issued: ").append(issuedCount).append("\n\n");

        sb.append(overdueReport()).append("\n");
        sb.append(popularBooksReport());
        return sb.toString();
    }

    public String overdueReport() {
        StringBuilder sb = new StringBuilder("-- Overdue Books --\n");
        LocalDate today = LocalDate.now();
        boolean any = false;
        for (Book b : catalog.getAllBooks()) {
            if (b.isAvailable() || b.getIssueDate() == null) continue;
            LocalDate issueDate = LocalDate.parse(b.getIssueDate(), DateTimeFormatter.ISO_LOCAL_DATE);
            long daysHeld = java.time.temporal.ChronoUnit.DAYS.between(issueDate, today);
            if (daysHeld > LOAN_PERIOD_DAYS) {
                any = true;
                sb.append(String.format("  %s (%s) held by %s - %d day(s) overdue%n",
                        b.getBookId(), b.getTitle(), b.getIssuedTo(), daysHeld - LOAN_PERIOD_DAYS));
            }
        }
        if (!any) sb.append("  None\n");
        return sb.toString();
    }

    public String popularBooksReport() {
        StringBuilder sb = new StringBuilder("-- Most Borrowed Books --\n");
        List<Book> sorted = catalog.getAllBooks().stream()
                .sorted(Comparator.comparingInt(Book::getTimesIssued).reversed())
                .limit(5)
                .collect(Collectors.toList());
        for (Book b : sorted) {
            if (b.getTimesIssued() == 0) continue;
            sb.append(String.format("  %s (%s) - issued %d time(s)%n",
                    b.getBookId(), b.getTitle(), b.getTimesIssued()));
        }
        return sb.toString();
    }

    public String memberDirectoryReport() {
        StringBuilder sb = new StringBuilder("-- Registered Members --\n");
        for (Member m : members.getAllMembers()) {
            sb.append("  ").append(m).append("\n");
        }
        return sb.toString();
    }
}
