# Library Management System (Java)

A console-based, multithreaded Library Management System built to demonstrate
core object-oriented and concurrent programming concepts: inheritance,
polymorphism, abstraction, interfaces, exception handling, and thread-safe
multithreading, backed by simple file persistence.

## Overview

The system lets an operator register members (Students and Faculty),
maintain a book catalog, issue and return books, and view usage reports.
Concurrent issue/return requests (e.g. two members trying to borrow the
same book at once) are processed through a thread pool with per-book
locking, so exactly one request can ever succeed for a given copy.

## Features

- **Member management** — register Students and Faculty with role-specific
  borrowing quotas and fine rates (polymorphism via the abstract `Member`
  class).
- **Book catalog CRUD** — add, update, remove, list, and search books by
  title.
- **Issue / Return workflow** — thread-safe issuing backed by a fixed
  thread pool (`ExecutorService`) and per-book `ReentrantLock`s, so
  simultaneous requests for the same book never both succeed.
- **Fine calculation** — automatic overdue fine computation based on a
  14-day loan period and the member's role-specific daily rate.
- **Reporting & analytics** — summary report, overdue book list, and
  most-borrowed books.
- **File-based persistence** — books, members, and transactions are
  stored in plain pipe-delimited text files under `data/`.
- **Logging** — every significant action is timestamped and appended to
  `data/activity.log`.
- **Custom exceptions** — `BookNotAvailableException` and
  `InvalidMemberException` for clear, typed error handling.

## Technologies / Tools Used

- Java 21 (Standard Edition, no external libraries)
- `java.util.concurrent` (`ExecutorService`, `ReentrantLock`,
  `ConcurrentHashMap`) for thread safety
- Plain text file I/O for persistence
- Git for version control

## Project Structure

```
LibraryManagementSystem/
├── README.md
├── statement.md
├── src/
│   ├── Main.java                       # console UI, wires modules together
│   ├── model/
│   │   ├── Member.java                 # abstract base (inheritance/polymorphism)
│   │   ├── Student.java
│   │   ├── Faculty.java
│   │   └── Book.java                   # implements Issuable
│   ├── interfaces/
│   │   ├── Issuable.java
│   │   └── Reportable.java
│   ├── service/
│   │   ├── MemberDirectory.java        # Module 1: member management
│   │   ├── BookCatalog.java            # Module 2: book catalog CRUD
│   │   ├── IssueManager.java           # Module 3: multithreaded issue/return
│   │   ├── FineCalculator.java
│   │   └── ReportGenerator.java        # Module 3: reporting/analytics
│   ├── exception/
│   │   ├── BookNotAvailableException.java
│   │   └── InvalidMemberException.java
│   └── util/
│       ├── FileHandler.java
│       └── Logger.java
├── test/
│   └── LibraryTest.java                # plain-assertion tests, incl. concurrency test
├── data/                                # generated at runtime (books/members/log)
└── docs/diagrams/                       # architecture, UML, ER diagrams
```

## Steps to Install & Run

Requires JDK 17 or later.

```bash
# From the project root:
mkdir -p out data

# Compile
javac -d out $(find src -name "*.java")

# Run
java -cp out Main
```

On first run the system seeds three sample books and two sample members
(`S001` - Abhi, `F001` - Dr. Rao) so you can explore the menu immediately.

## Instructions for Testing

The `test/LibraryTest.java` harness uses plain assertions (no external
framework needed) and covers CRUD correctness, fine calculation, and the
key concurrency guarantee.

```bash
javac -cp out -d out test/LibraryTest.java
java -cp out LibraryTest
```

Expected output ends with `Results: 7 passed, 0 failed`.

## Screenshots

See `docs/diagrams/` for the system architecture, workflow, UML, and ER
diagrams referenced in the project report.
