package model;

import interfaces.Issuable;

/**
 * Represents a single catalog entry. Implements Issuable so the
 * issuing subsystem can be extended to other item types later
 * without changing IssueManager's logic.
 */
public class Book implements Issuable {

    private final String bookId;
    private String title;
    private String author;
    private volatile boolean available;
    private String issuedTo;      // memberId, null if not issued
    private String issueDate;     // ISO date string, null if not issued
    private int timesIssued;      // for popularity analytics

    public Book(String bookId, String title, String author) {
        this.bookId = bookId;
        this.title = title;
        this.author = author;
        this.available = true;
        this.issuedTo = null;
        this.issueDate = null;
        this.timesIssued = 0;
    }

    public String getBookId() {
        return bookId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getIssuedTo() {
        return issuedTo;
    }

    public String getIssueDate() {
        return issueDate;
    }

    public int getTimesIssued() {
        return timesIssued;
    }

    @Override
    public boolean isAvailable() {
        return available;
    }

    @Override
    public void markIssued() {
        this.available = false;
        this.timesIssued++;
    }

    @Override
    public void markReturned() {
        this.available = true;
        this.issuedTo = null;
        this.issueDate = null;
    }

    public void assignTo(String memberId, String isoDate) {
        this.issuedTo = memberId;
        this.issueDate = isoDate;
    }

    @Override
    public String toString() {
        String status = available ? "AVAILABLE" : ("ISSUED to " + issuedTo + " on " + issueDate);
        return String.format("%s | %-30s | %-15s | %s", bookId, title, author, status);
    }
}
