package com.trainingapp.service;

import com.trainingapp.model.Role;
import com.trainingapp.model.User;
<<<<<<< HEAD

import java.util.*;

public class AuthService {
    // demo in-memory users
    private static final Map<String, User> USERS = new HashMap<>();
    static {
        USERS.put("admin",     new User("admin", "admin123", Role.ADMIN));
        USERS.put("instructor",new User("instructor", "teach123", Role.INSTRUCTOR));
        // student user is linked to participant P-001 (so they only see their enrollments)
        USERS.put("student",   new User("student", "stud123", Role.STUDENT, "P-001"));
    }

    public static User authenticate(String username, String password) {
        User u = USERS.get(username);
        if (u != null && Objects.equals(u.getPassword(), password)) return u;
        return null;
    }
=======
import com.trainingapp.persistence.CsvStore;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Handles:
 *  - authenticate()
 *  - userExists()
 *  - registerStudent()
 *
 * registerStudent():
 *   1. creates a new participant row in participants.csv (P-501, P-502, ...)
 *   2. creates a new User with that participantId
 *   3. appends that User to users.csv
 */
public class AuthService {

    private static final AuthService INSTANCE = new AuthService();
    public static AuthService getInstance() { return INSTANCE; }

    private final CsvStore csvStore = CsvStore.getInstance();

    private AuthService() {}

    /**
     * Returns all known users:
     *  - built-in demo accounts
     *  - plus registered users from users.csv
     */
    private List<User> getAllUsers() {
        List<User> combined = new ArrayList<>();

        // built-in demo accounts
        combined.add(new User("admin", "admin123", Role.ADMIN));
        combined.add(new User("instructor", "teach123", Role.INSTRUCTOR));
        combined.add(new User("student", "stud123", Role.STUDENT, "P-001"));

        // registered users from disk
        combined.addAll(csvStore.loadUsers());

        return combined;
    }

    /**
     * Login: return matching user, or null.
     */
    public User authenticate(String username, String password) {
        for (User u : getAllUsers()) {
            if (u.getUsername().equalsIgnoreCase(username)
                    && Objects.equals(u.getPassword(), password)) {
                return u;
            }
        }
        return null;
    }

    /**
     * Does this username already exist?
     */
    public boolean userExists(String username) {
        for (User u : getAllUsers()) {
            if (u.getUsername().equalsIgnoreCase(username)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Create a brand new STUDENT user and persist them.
     *
     * Steps:
     *  1. Add participant row -> get new participantId like "P-503"
     *  2. Create User(username,password,STUDENT,thatId)
     *  3. Save that new user to users.csv
     *
     * @param fullName student full name (goes to participants.csv)
     * @param username login username / email
     * @param rawPassword plaintext password
     * @return the created User, or null if username already exists
     */
    public User registerStudent(String fullName, String username, String rawPassword) {
        // duplicate check
        if (userExists(username)) {
            return null;
        }

        // 1. create participant row and get its new ID
        String participantId = csvStore.addNewParticipant(fullName, username);

        // 2. build new user bound to that participant
        User newStudent = new User(username, rawPassword, Role.STUDENT, participantId);

        // 3. append to users.csv and save
        List<User> stored = csvStore.loadUsers();
        stored.add(newStudent);
        csvStore.saveUsers(stored);

        return newStudent;
    }
>>>>>>> master
}
