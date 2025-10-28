package com.trainingapp.model;

public class Enrollment {
    private Course course;
    private Participant participant;
    private CompletionStatus status;

    // Existing constructor (defaulting status)
    public Enrollment(Course course, Participant participant) {
        this.course = course;
        this.participant = participant;
        this.status = CompletionStatus.IN_PROGRESS; // Default
    }

    // ✅ New overloaded constructor (for explicit status)
    public Enrollment(Course course, Participant participant, CompletionStatus status) {
        this.course = course;
        this.participant = participant;
        this.status = status;
    }

    // Getters and setters
    public Course getCourse() { return course; }
    public Participant getParticipant() { return participant; }
    public CompletionStatus getStatus() { return status; }
    public void setStatus(CompletionStatus status) { this.status = status; }
}
