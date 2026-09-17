import model.Book;
import model.Faculty;
import model.Student;
import service.BookCatalog;
import service.FineCalculator;
import service.IssueManager;
import service.MemberDirectory;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Lightweight test harness (plain assertions, no external test framework
 * required so it runs with only a JDK). Run with:
 *   javac -d out $(find src -name "*.java") test/LibraryTest.java
 *   java -cp out;test LibraryTest      (Windows)
 *   java -cp out:test LibraryTest      (Linux/Mac)
 *
 * Covers: CRUD correctness, fine calculation, and the key concurrency
 * guarantee that two threads racing to issue the same book never both
 * succeed.
 */
public class LibraryTest {

    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) throws InterruptedException {
        testBookCrud();
        testFineCalculation();
        testConcurrentIssueIsSafe();

        System.out.println("\nResults: " + passed + " passed, " + failed + " failed");
    }

    private static void check(String name, boolean condition) {
        if (condition) {
            passed++;
            System.out.println("PASS - " + name);
        } else {
            failed++;
            System.out.println("FAIL - " + name);
        }
    }

    private static void testBookCrud() {
        BookCatalog catalog = new BookCatalog();
        Book b = new Book("TEST001", "Test Driven Development", "Kent Beck");
        catalog.addBook(b);
        check("book is retrievable after add", catalog.getBook("TEST001") != null);

        catalog.updateBook("TEST001", "TDD By Example", "Kent Beck");
        check("book title updates", catalog.getBook("TEST001").getTitle().equals("TDD By Example"));

        catalog.removeBook("TEST001");
        check("book is gone after remove", catalog.getBook("TEST001") == null);
    }

    private static void testFineCalculation() {
        Student student = new Student("TESTS1", "Test Student");
        Faculty faculty = new Faculty("TESTF1", "Test Faculty");

        check("no fine when not overdue", FineCalculator.calculate(student, 0) == 0.0);
        check("student fine = days * 5", FineCalculator.calculate(student, 3) == 15.0);
        check("faculty fine = days * 2", FineCalculator.calculate(faculty, 3) == 6.0);
    }

    private static void testConcurrentIssueIsSafe() throws InterruptedException {
        BookCatalog catalog = new BookCatalog();
        MemberDirectory members = new MemberDirectory();
        catalog.addBook(new Book("TESTBOOK", "Concurrency Test Book", "Author X"));
        members.registerMember(new Student("TESTU1", "User One"));
        members.registerMember(new Student("TESTU2", "User Two"));

        IssueManager issueManager = new IssueManager(catalog, members);
        CountDownLatch latch = new CountDownLatch(2);
        ExecutorService requesters = Executors.newFixedThreadPool(2);
        final int[] successCount = {0};

        Runnable attempt1 = () -> {
            try {
                issueManager.issueBook("TESTU1", "TESTBOOK");
                synchronized (successCount) { successCount[0]++; }
            } catch (Exception ignored) {
            } finally {
                latch.countDown();
            }
        };
        Runnable attempt2 = () -> {
            try {
                issueManager.issueBook("TESTU2", "TESTBOOK");
                synchronized (successCount) { successCount[0]++; }
            } catch (Exception ignored) {
            } finally {
                latch.countDown();
            }
        };

        requesters.submit(attempt1);
        requesters.submit(attempt2);
        latch.await();
        requesters.shutdown();
        issueManager.shutdown();

        check("exactly one of two racing issue requests succeeds", successCount[0] == 1);

        // cleanup
        catalog.removeBook("TESTBOOK");
        members.removeMember("TESTU1");
        members.removeMember("TESTU2");
    }
}
