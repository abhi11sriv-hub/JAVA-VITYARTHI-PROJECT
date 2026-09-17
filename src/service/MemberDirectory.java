package service;

import model.Faculty;
import model.Member;
import model.Student;
import util.FileHandler;
import util.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Module 1: User (Member) Management.
 * Handles registration, lookup, and removal of members, and persists
 * them to data/members.txt. Demonstrates polymorphism: callers work
 * only with the abstract Member type, while Student/Faculty apply
 * their own quota and fine rules underneath.
 */
public class MemberDirectory {

    private static final String DATA_FILE = "data/members.txt";
    private final Map<String, Member> members = new ConcurrentHashMap<>();

    public MemberDirectory() {
        load();
    }

    public void registerMember(Member member) {
        members.put(member.getMemberId(), member);
        Logger.log("Member registered: " + member.getMemberId() + " (" + member.getRole() + ")");
        save();
    }

    public Member getMember(String memberId) {
        return members.get(memberId);
    }

    public boolean removeMember(String memberId) {
        Member removed = members.remove(memberId);
        if (removed != null) {
            Logger.log("Member removed: " + memberId);
            save();
            return true;
        }
        return false;
    }

    public Collection<Member> getAllMembers() {
        return members.values();
    }

    // ---------- persistence ----------
    // Format: memberId|role|name|issuedBookIds(comma-separated)

    private void load() {
        List<String> lines = FileHandler.readLines(DATA_FILE);
        for (String line : lines) {
            String[] parts = line.split("\\|", -1);
            if (parts.length < 3) continue;
            String id = parts[0];
            String role = parts[1];
            String name = parts[2];
            Member member = "FACULTY".equals(role) ? new Faculty(id, name) : new Student(id, name);
            if (parts.length >= 4 && !parts[3].isEmpty()) {
                for (String bookId : parts[3].split(",")) {
                    member.addIssuedBook(bookId);
                }
            }
            members.put(id, member);
        }
    }

    public synchronized void save() {
        List<String> lines = new ArrayList<>();
        for (Member m : members.values()) {
            lines.add(String.join("|",
                    m.getMemberId(),
                    m.getRole(),
                    m.getName(),
                    String.join(",", m.getIssuedBookIds())));
        }
        FileHandler.writeLines(DATA_FILE, lines);
    }
}
