package com.trainingapp.service;

import com.trainingapp.model.User;

/**
 * Holds the currently logged-in user in memory.
 * Controllers can call Session.getCurrentUser() to know who is active.
 */
public class Session {

    private static User currentUser;

    public static void setCurrentUser(User user) {
        currentUser = user;
    }

    public static User getCurrentUser() {
        return currentUser;
    }

    public static String getParticipantIdOrNull() {
        return currentUser == null ? null : currentUser.getParticipantId();
    }
}
