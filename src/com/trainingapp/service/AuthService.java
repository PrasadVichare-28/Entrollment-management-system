package com.trainingapp.service;

import com.trainingapp.model.Role;
import com.trainingapp.model.User;

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
}
