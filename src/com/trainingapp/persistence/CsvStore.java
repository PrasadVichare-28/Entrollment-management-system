package com.trainingapp.persistence;

import com.trainingapp.model.Role;
import com.trainingapp.model.User;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * CSV persistence:
 *  - users.csv (registered users)
 *  - participants.csv (students)
 *  - courses.csv (available courses)
 *  - enrollments.csv (participantId,courseId)
 */
public class CsvStore {

    private static final String USERS_PATH = "data/users.csv";
    private static final String PARTICIPANTS_PATH = "data/participants.csv";
    private static final String COURSES_PATH = "data/courses.csv";
    private static final String ENROLLMENTS_PATH = "data/enrollments.csv";

    private static final CsvStore INSTANCE = new CsvStore();
    public static CsvStore getInstance() { return INSTANCE; }

    private CsvStore() {}

    /* =========================================================
       USERS
     ========================================================= */

    public List<User> loadUsers() {
        List<User> out = new ArrayList<>();
        Path p = Paths.get(USERS_PATH);
        if (!Files.exists(p)) return out;

        try (BufferedReader br = Files.newBufferedReader(p)) {
            String line;
            boolean first = true;
            while ((line = br.readLine()) != null) {
                if (first) {
                    first = false;
                    if (line.toLowerCase().startsWith("username,")) continue; // header
                }
                if (line.isBlank()) continue;
                String[] parts = line.split(",", -1); // keep empties
                if (parts.length < 4) continue;

                String username      = parts[0];
                String password      = parts[1];
                String roleName      = parts[2];
                String participantId = parts[3].isEmpty() ? null : parts[3];

                Role role = Role.valueOf(roleName);
                User u = (participantId == null)
                        ? new User(username, password, role)
                        : new User(username, password, role, participantId);
                out.add(u);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return out;
    }

    public void saveUsers(List<User> users) {
        Path p = Paths.get(USERS_PATH);
        try { Files.createDirectories(p.getParent()); } catch (IOException ignored) {}

        try (BufferedWriter bw = Files.newBufferedWriter(p)) {
            bw.write("username,password,role,participantId");
            bw.newLine();
            for (User u : users) {
                bw.write(
                    safe(u.getUsername()) + "," +
                    safe(u.getPassword()) + "," +
                    u.getRole().name()    + "," +
                    safe(u.getParticipantId() == null ? "" : u.getParticipantId())
                );
                bw.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /* =========================================================
       PARTICIPANTS
     ========================================================= */

    public String addNewParticipant(String fullName, String emailOrUsername) {
        Path p = Paths.get(PARTICIPANTS_PATH);
        List<String> lines = new ArrayList<>();

        if (Files.exists(p)) {
            try { lines = Files.readAllLines(p); } catch (IOException e) { e.printStackTrace(); }
        }
        if (lines.isEmpty()) lines.add("participantId,name,email");

        int max = 0;
        for (int i = 1; i < lines.size(); i++) {
            String[] parts = lines.get(i).split(",", -1);
            if (parts.length == 0) continue;
            String pid = parts[0].trim();
            if (!pid.startsWith("P-")) continue;
            try { max = Math.max(max, Integer.parseInt(pid.substring(2))); }
            catch (NumberFormatException ignored) {}
        }
        String newPid = String.format("P-%03d", max + 1);

        lines.add(newPid + "," + safe(fullName) + "," + safe(emailOrUsername));
        try {
            Files.createDirectories(p.getParent());
            Files.write(p, lines);
        } catch (IOException e) { e.printStackTrace(); }

        return newPid;
    }

    /* =========================================================
       COURSES / ENROLLMENTS
     ========================================================= */

    /** Returns raw rows from courses.csv (without header). */
    public List<String[]> loadCoursesRaw() {
        return loadCsvNoHeader(COURSES_PATH);
    }

    /** Returns raw rows from enrollments.csv (without header). */
    public List<String[]> loadEnrollmentsRaw() {
        ensureEnrollmentsFile();
        return loadCsvNoHeader(ENROLLMENTS_PATH);
    }

    /**
     * Adds (participantId, courseId) if it doesn't already exist.
     * Creates enrollments.csv with header if missing.
     */
    public void addEnrollment(String participantId, String courseId) {
        ensureEnrollmentsFile();

        Path p = Paths.get(ENROLLMENTS_PATH);
        try {
            List<String> lines = Files.readAllLines(p);
            // check duplicate
            String candidate = participantId + "," + courseId;
            for (int i = 1; i < lines.size(); i++) {
                if (lines.get(i).trim().equalsIgnoreCase(candidate)) return; // already enrolled
            }
            lines.add(candidate);
            Files.write(p, lines);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /** Convenience: get the set of courseIds the participant is enrolled in. */
    public Set<String> getEnrolledCourseIds(String participantId) {
        return loadEnrollmentsRaw().stream()
                .filter(arr -> arr.length >= 2 && participantId.equalsIgnoreCase(arr[0]))
                .map(arr -> arr[1])
                .collect(Collectors.toSet());
    }

    /* ========================= helpers ========================= */

    private List<String[]> loadCsvNoHeader(String path) {
        List<String[]> out = new ArrayList<>();
        Path p = Paths.get(path);
        if (!Files.exists(p)) return out;
        try (BufferedReader br = Files.newBufferedReader(p)) {
            String line;
            boolean first = true;
            while ((line = br.readLine()) != null) {
                if (first) { first = false; continue; } // skip header
                if (line.isBlank()) continue;
                out.add(line.split(",", -1));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return out;
    }

    private void ensureEnrollmentsFile() {
        Path p = Paths.get(ENROLLMENTS_PATH);
        if (!Files.exists(p)) {
            try {
                Files.createDirectories(p.getParent());
                Files.write(p, List.of("participantId,courseId"));
            } catch (IOException e) { e.printStackTrace(); }
        }
    }

    private String safe(String s) {
        return (s == null) ? "" : s.replace(",", " ");
    }
}
