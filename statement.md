# Problem Statement

College and departmental libraries that rely on manual registers for
issuing and returning books face slow lookups, frequent double-issuing of
the same physical copy when two requests arrive close together, no
automatic overdue-fine tracking, and no easy way to see which books are
most in demand. A small, self-contained system is needed that manages
members and the book catalog, safely handles concurrent issue/return
requests, computes fines automatically, and reports usage.

## Scope of the Project

- Single-library, single-branch scope (not a multi-branch/consortium system).
- Console-based operator interface (librarian-operated, not a public
  self-service portal).
- File-based persistence (`data/*.txt`), sufficient for a single
  library's dataset; not intended to replace a full RDBMS deployment.
- Concurrency handling covers simultaneous issue/return requests for the
  same book; it does not cover distributed/multi-node deployment.

## Target Users

- **Librarian / Admin** — operates the console application to manage the
  catalog and members, and to issue/return books on behalf of patrons.
- **Students** — a member type with a 3-book borrowing limit and a
  Rs. 5/day overdue fine.
- **Faculty** — a member type with a 6-book borrowing limit and a
  Rs. 2/day overdue fine (reflecting longer-term research use).

## High-Level Features

1. **Member Management** — register, list, and remove Student and
   Faculty members; each type enforces its own borrowing quota and fine
   rate through polymorphism.
2. **Book Catalog Management** — add, update, remove, list, and search
   books (full CRUD).
3. **Issue / Return & Reporting** — thread-safe book issuing and
   returning with automatic fine calculation, plus summary, overdue, and
   popularity reports.
