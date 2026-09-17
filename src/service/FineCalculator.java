package service;

import model.Member;

/**
 * Pure, stateless fine calculation. Kept separate from IssueManager
 * so the fine policy can be unit-tested and changed independently
 * (Maintainability).
 */
public class FineCalculator {

    private FineCalculator() {
    }

    /** Fine = member-type rate x number of days overdue (0 if not overdue). */
    public static double calculate(Member member, long daysOverdue) {
        if (daysOverdue <= 0) {
            return 0.0;
        }
        return daysOverdue * member.getFinePerDay();
    }
}
