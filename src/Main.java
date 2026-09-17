import exception.BookNotAvailableException;
import exception.InvalidMemberException;
import model.Book;
import model.Faculty;
import model.Member;
import model.Student;
import service.BookCatalog;
import service.IssueManager;
import service.MemberDirectory;
import service.ReportGenerator;
import util.Logger;

import java.util.Scanner;

/**
 * Console entry point wiring the three functional modules together:
 *   1. Member management   (MemberDirectory)
 *   2. Book catalog CRUD   (BookCatalog)
 *   3. Issue/Return + reporting (IssueManager, ReportGenerator)
 */
public class Main {

    private static final BookCatalog catalog = new BookCatalog();
    private static final MemberDirectory members = new MemberDirectory();
    private static final IssueManager issueManager = new IssueManager(catalog, members);
    private static final ReportGenerator reports = new ReportGenerator(catalog, members);
    private static final Scanner sc = new Scanner(System.in);

    public static void main(String[] args) {
        seedSampleDataIfEmpty();
        Logger.log("Library Management System started");
        boolean running = true;
        while (running) {
            printMenu();
            String choice = sc.nextLine().trim();
            try {
                switch (choice) {
                    case "1" -> addBook();
                    case "2" -> updateBook();
                    case "3" -> removeBook();
                    case "4" -> listBooks();
                    case "5" -> searchBooks();
                    case "6" -> registerMember();
                    case "7" -> listMembers();
                    case "8" -> issueBook();
                    case "9" -> returnBook();
                    case "10" -> System.out.println(reports.generateReport());
                    case "0" -> running = false;
                    default -> System.out.println("Invalid option, try again.");
                }
            } catch (InvalidMemberException | BookNotAvailableException e) {
                System.out.println("Error: " + e.getMessage());
            } catch (Exception e) {
                System.out.println("Unexpected error: " + e.getMessage());
                Logger.log("UNEXPECTED ERROR: " + e);
            }
        }
        issueManager.shutdown();
        Logger.log("Library Management System shut down");
        System.out.println("Goodbye!");
    }

    private static void printMenu() {
        System.out.println("""

                ==== LIBRARY MANAGEMENT SYSTEM ====
                 1. Add book
                 2. Update book
                 3. Remove book
                 4. List all books
                 5. Search books by title
                 6. Register member
                 7. List all members
                 8. Issue a book
                 9. Return a book
                10. View reports
                 0. Exit
                Choose an option:""");
    }

    private static void addBook() {
        System.out.print("Book ID: ");
        String id = sc.nextLine().trim();
        System.out.print("Title: ");
        String title = sc.nextLine().trim();
        System.out.print("Author: ");
        String author = sc.nextLine().trim();
        catalog.addBook(new Book(id, title, author));
        System.out.println("Book added.");
    }

    private static void updateBook() {
        System.out.print("Book ID to update: ");
        String id = sc.nextLine().trim();
        System.out.print("New title: ");
        String title = sc.nextLine().trim();
        System.out.print("New author: ");
        String author = sc.nextLine().trim();
        System.out.println(catalog.updateBook(id, title, author) ? "Updated." : "Book not found.");
    }

    private static void removeBook() {
        System.out.print("Book ID to remove: ");
        String id = sc.nextLine().trim();
        System.out.println(catalog.removeBook(id) ? "Removed." : "Book not found.");
    }

    private static void listBooks() {
        catalog.getAllBooks().forEach(System.out::println);
    }

    private static void searchBooks() {
        System.out.print("Keyword: ");
        String kw = sc.nextLine().trim();
        catalog.searchByTitle(kw).forEach(System.out::println);
    }

    private static void registerMember() {
        System.out.print("Member ID: ");
        String id = sc.nextLine().trim();
        System.out.print("Name: ");
        String name = sc.nextLine().trim();
        System.out.print("Role (STUDENT/FACULTY): ");
        String role = sc.nextLine().trim().toUpperCase();
        Member member = role.equals("FACULTY") ? new Faculty(id, name) : new Student(id, name);
        members.registerMember(member);
        System.out.println("Member registered.");
    }

    private static void listMembers() {
        members.getAllMembers().forEach(System.out::println);
    }

    private static void issueBook() throws InvalidMemberException, BookNotAvailableException {
        System.out.print("Member ID: ");
        String memberId = sc.nextLine().trim();
        System.out.print("Book ID: ");
        String bookId = sc.nextLine().trim();
        issueManager.issueBook(memberId, bookId);
        System.out.println("Book issued successfully.");
    }

    private static void returnBook() throws InvalidMemberException {
        System.out.print("Member ID: ");
        String memberId = sc.nextLine().trim();
        System.out.print("Book ID: ");
        String bookId = sc.nextLine().trim();
        double fine = issueManager.returnBook(memberId, bookId);
        System.out.println(fine > 0
                ? String.format("Returned. Overdue fine: Rs. %.2f", fine)
                : "Returned. No fine due.");
    }

    /** Seeds a few sample rows on first run so graders can explore immediately. */
    private static void seedSampleDataIfEmpty() {
        if (catalog.getAllBooks().isEmpty()) {
            catalog.addBook(new Book("B001", "Clean Code", "Robert C. Martin"));
            catalog.addBook(new Book("B002", "Operating System Concepts", "Silberschatz"));
            catalog.addBook(new Book("B003", "Effective Java", "Joshua Bloch"));
        }
        if (members.getAllMembers().isEmpty()) {
            members.registerMember(new Student("S001", "Abhi"));
            members.registerMember(new Faculty("F001", "Dr. Rao"));
        }
    }
}
