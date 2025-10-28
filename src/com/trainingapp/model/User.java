package com.trainingapp.model;

public class User {
    private final String username;
    private final String password;
    private final Role role;
    // link to participant if role=STUDENT (optional)
    private final String participantId;

    public User(String username, String password, Role role) {
        this(username, password, role, null);
    }
    public User(String username, String password, Role role, String participantId) {
        this.username = username;
        this.password = password;
        this.role = role;
        this.participantId = participantId;
    }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public Role getRole() { return role; }
    public String getParticipantId() { return participantId; }
}
