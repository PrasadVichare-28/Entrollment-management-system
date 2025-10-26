package com.trainingapp.model;

public class Enrollment {
    private final Course course;
    private final Participant participant;
    private CompletionStatus status = CompletionStatus.IN_PROGRESS;

    public Enrollment(Course course, Participant participant) {
        this.course = course;
        this.participant = participant;
    }
    public Course getCourse() { return course; }
    public Participant getParticipant() { return participant; }
    public CompletionStatus getStatus() { return status; }
    public void setStatus(CompletionStatus status) { this.status = status; }
}
