package com.trainingapp.service;

import com.trainingapp.model.*;

import java.util.*;
import java.util.stream.Collectors;

public class TrainingService {
    private static TrainingService instance;

    private final List<Course> courses = new ArrayList<>();
    private final List<Participant> participants = new ArrayList<>();
    private final List<Enrollment> enrollments = new ArrayList<>();

    private TrainingService() {
        // seed sample data
        Course c1 = new Course("C-101","Excel Basics","Dr. Patel","2025-11-02 10:00 AM","Room 204",3);
        Course c2 = new Course("C-202","Leadership Bootcamp","Ms. Rivera","2025-11-05 2:00 PM","Conf B",5);
        courses.add(c1); courses.add(c2);

        Participant p1 = new Participant("P-001","Alice Johnson","Marketing","alice@org.com");
        Participant p2 = new Participant("P-002","Brian Lee","Finance","brian@org.com");
        Participant p3 = new Participant("P-003","Carla Singh","HR","carla@org.com");
        participants.addAll(Arrays.asList(p1,p2,p3));

        enrollments.add(new Enrollment(c1, p1));
    }

    public static synchronized TrainingService get() {
        if (instance == null) instance = new TrainingService();
        return instance;
    }

    // Courses
    public List<Course> getCourses() { return courses; }
    public void addCourse(String id, String title, String instructor, String dt, String loc, int cap) throws Exception {
        if (findCourse(id) != null) throw new Exception("Course ID exists.");
        if (cap <= 0) throw new Exception("Capacity must be > 0.");
        courses.add(new Course(id, title, instructor, dt, loc, cap));
    }
    public Course findCourse(String id) {
        for (Course c : courses) if (c.getCourseId().equalsIgnoreCase(id)) return c;
        return null;
    }
    public int getEnrolledCount(Course c) {
        int n = 0;
        for (Enrollment e : enrollments) if (e.getCourse().equals(c)) n++;
        return n;
    }

    // Participants
    public List<Participant> getParticipants() { return participants; }
    public Participant findParticipant(String pid) {
        for (Participant p : participants) if (p.getParticipantId().equalsIgnoreCase(pid)) return p;
        return null;
    }
    public void addParticipant(String id, String name, String dept, String email) throws Exception {
        if (findParticipant(id) != null) throw new Exception("Participant ID exists.");
        participants.add(new Participant(id, name, dept, email));
    }

    // Enrollments
    public List<Enrollment> getEnrollmentsForCourse(String courseId) {
        Course c = findCourse(courseId);
        if (c == null) return Collections.emptyList();
        return enrollments.stream().filter(e -> e.getCourse().equals(c)).collect(Collectors.toList());
    }

    public List<Enrollment> getEnrollmentsForParticipant(String participantId) {
        return enrollments.stream().filter(e -> e.getParticipant().getParticipantId().equalsIgnoreCase(participantId))
                .collect(Collectors.toList());
    }

    public void enroll(String courseId, String participantId) throws Exception {
        Course c = findCourse(courseId);
        Participant p = findParticipant(participantId);
        if (c == null || p == null) throw new Exception("Course or participant not found.");

        for (Enrollment e : enrollments) {
            if (e.getCourse().equals(c) && e.getParticipant().equals(p))
                throw new Exception("Duplicate enrollment.");
        }
        if (getEnrolledCount(c) >= c.getCapacity()) throw new Exception("Course is at capacity.");
        enrollments.add(new Enrollment(c, p));
    }

    public void setStatus(String courseId, String participantId, CompletionStatus status) throws Exception {
        Course c = findCourse(courseId);
        Participant p = findParticipant(participantId);
        if (c == null || p == null) throw new Exception("Not found.");
        for (Enrollment e : enrollments) {
            if (e.getCourse().equals(c) && e.getParticipant().equals(p)) {
                e.setStatus(status); return;
            }
        }
        throw new Exception("Enrollment not found.");
    }
}
