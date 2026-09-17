package service;

import exception.BookNotAvailableException;
import exception.InvalidMemberException;
import model.Book;
import model.Member;
import util.FileHandler;
import util.Logger;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Module 3: Issue / Return processing.
 *
 * Multiple members can request the same book at (almost) the same time.
 * Requests are submitted to a bounded thread pool (ExecutorService) so
 * the system stays responsive under load (Performance, Resource
 * efficiency) while a per-book ReentrantLock guarantees that only one
 * thread can ever move a given book from AVAILABLE to ISSUED
 * (Reliability, avoids double-issuing the same physical book).
 */
public class IssueManager {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final String TRANSACTIONS_FILE = "data/transactions.txt";
    private static final int LOAN_PERIOD_DAYS = 14;

    private final BookCatalog catalog;
    private final MemberDirectory members;
    private final ExecutorService pool = Executors.newFixedThreadPool(4);
    private final Map<String, ReentrantLock> bookLocks = new ConcurrentHashMap<>();

    public IssueManager(BookCatalog catalog, MemberDirectory members) {
        this.catalog = catalog;
        this.members = members;
    }

    private ReentrantLock lockFor(String bookId) {
        return bookLocks.computeIfAbsent(bookId, id -> new ReentrantLock());
    }

    /**
     * Submits an issue request to the thread pool and blocks until it
     * completes, so the console UI still gets an immediate result while
     * the underlying operation is safely serialized per book.
     */
    public void issueBook(String memberId, String bookId)
            throws BookNotAvailableException, InvalidMemberException {

        Member member = members.getMember(memberId);
        if (member == null) {
            throw new InvalidMemberException("No member with ID " + memberId);
        }
        if (member.getIssuedBookIds().size() >= member.getMaxBooksAllowed()) {
            throw new InvalidMemberException(
                    member.getName() + " has reached the borrowing limit of "
                            + member.getMaxBooksAllowed() + " books");
        }

        Book book = catalog.getBook(bookId);
        if (book == null) {
            throw new BookNotAvailableException("No book with ID " + bookId);
        }

        ReentrantLock lock = lockFor(bookId);
        Runnable task = () -> {
            lock.lock();
            try {
                if (!book.isAvailable()) {
                    Logger.log("FAILED issue attempt: " + bookId + " already issued (requested by " + memberId + ")");
                    return;
                }
                String today = LocalDate.now().format(DATE_FMT);
                book.markIssued();
                book.assignTo(memberId, today);
                member.addIssuedBook(bookId);
                catalog.save();
                members.save();
                FileHandler.appendLine(TRANSACTIONS_FILE,
                        String.join("|", "ISSUE", bookId, memberId, today));
                Logger.log("Issued " + bookId + " to " + memberId + " on " + today);
            } finally {
                lock.unlock();
            }
        };

        runAndWait(task);

        // Re-check outcome after the synchronized task ran: if this call lost
        // the race to another thread, book.getIssuedTo() will not be us.
        if (!memberId.equals(book.getIssuedTo())) {
            throw new BookNotAvailableException(
                    "Book " + bookId + " was already issued to another member");
        }
    }

    public double returnBook(String memberId, String bookId) throws InvalidMemberException {
        Member member = members.getMember(memberId);
        if (member == null) {
            throw new InvalidMemberException("No member with ID " + memberId);
        }
        Book book = catalog.getBook(bookId);
        if (book == null || book.isAvailable()) {
            return 0.0;
        }

        ReentrantLock lock = lockFor(bookId);
        final double[] fine = {0.0};
        Runnable task = () -> {
            lock.lock();
            try {
                LocalDate issueDate = LocalDate.parse(book.getIssueDate(), DATE_FMT);
                long daysHeld = java.time.temporal.ChronoUnit.DAYS.between(issueDate, LocalDate.now());
                long overdue = Math.max(0, daysHeld - LOAN_PERIOD_DAYS);
                fine[0] = FineCalculator.calculate(member, overdue);

                book.markReturned();
                member.removeIssuedBook(bookId);
                catalog.save();
                members.save();
                FileHandler.appendLine(TRANSACTIONS_FILE,
                        String.join("|", "RETURN", bookId, memberId, LocalDate.now().format(DATE_FMT),
                                String.valueOf(fine[0])));
                Logger.log("Returned " + bookId + " by " + memberId + " (fine: Rs." + fine[0] + ")");
            } finally {
                lock.unlock();
            }
        };

        runAndWait(task);
        return fine[0];
    }

    private void runAndWait(Runnable task) {
        try {
            pool.submit(task).get();
        } catch (Exception e) {
            Logger.log("ERROR in issue/return task: " + e.getMessage());
        }
    }

    /** Gracefully shuts down the worker thread pool on application exit. */
    public void shutdown() {
        pool.shutdown();
        try {
            if (!pool.awaitTermination(3, TimeUnit.SECONDS)) {
                pool.shutdownNow();
            }
        } catch (InterruptedException e) {
            pool.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
