package com.trainingapp.model;

public class Participant {
    private String participantId;
    private String name;
    private String department;
    private String email;

    public Participant(String participantId, String name, String department, String email) {
        this.participantId = participantId;
        this.name = name;
        this.department = department;
        this.email = email;
    }

    public String getParticipantId() { return participantId; }
    public String getName() { return name; }
    public String getDepartment() { return department; }
    public String getEmail() { return email; }

    @Override public String toString() {
        return name + " (" + participantId + ")";
    }
}
