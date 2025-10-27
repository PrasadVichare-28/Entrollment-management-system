package com.trainingapp.service;

import com.trainingapp.model.*;
import com.trainingapp.util.CsvUtil;
import java.util.*;
import java.util.stream.Collectors;

public class TrainingService {
    private static TrainingService instance;

    private final List<Course> courses = new ArrayList<>();
    private final List<Participant> participants = new ArrayList<>();
    private final List<Enrollment> enrollments = new ArrayList<>();

    private TrainingService() {
        loadData();
        if (courses.isEmpty() && participants.isEmpty()) {
            seedSampleData();
            saveAll();
        }
    }

    public static synchronized TrainingService get() {
        if (instance == null) instance = new TrainingService();
        return instance;
    }

    // ---------- Courses ----------
    public List<Course> getCourses() { return courses; }

    public void addCourse(String id, String title, String instructor, String dt, String loc, int cap) throws Exception {
        if (findCourse(id) != null) throw new Exception("Course ID exists.");
        if (cap <= 0) throw new Exception("Capacity must be > 0.");
        courses.add(new Course(id, title, instructor, dt, loc, cap));
        saveCourses();
    }

    public Course findCourse(String id) {
        return courses.stream()
                .filter(c -> c.getCourseId().equalsIgnoreCase(id))
                .findFirst().orElse(null);
    }

    public int getEnrolledCount(Course c) {
        return (int) enrollments.stream().filter(e -> e.getCourse().equals(c)).count();
    }

    // ---------- Participants ----------
    public List<Participant> getParticipants() { return participants; }

    public Participant findParticipant(String pid) {
        return participants.stream()
                .filter(p -> p.getParticipantId().equalsIgnoreCase(pid))
                .findFirst().orElse(null);
    }

    public void addParticipant(String id, String name, String dept, String email) throws Exception {
        if (findParticipant(id) != null) throw new Exception("Participant ID exists.");
        participants.add(new Participant(id, name, dept, email));
        saveParticipants();
    }

    // ---------- Enrollments ----------
    public List<Enrollment> getEnrollmentsForCourse(String courseId) {
        Course c = findCourse(courseId);
        if (c == null) return Collections.emptyList();
        return enrollments.stream()
                .filter(e -> e.getCourse().equals(c))
                .collect(Collectors.toList());
    }

    public List<Enrollment> getEnrollmentsForParticipant(String participantId) {
        return enrollments.stream()
                .filter(e -> e.getParticipant().getParticipantId().equalsIgnoreCase(participantId))
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
        saveEnrollments();
    }

    public void setStatus(String courseId, String participantId, CompletionStatus status) throws Exception {
        Course c = findCourse(courseId);
        Participant p = findParticipant(participantId);
        if (c == null || p == null) throw new Exception("Not found.");
        for (Enrollment e : enrollments) {
            if (e.getCourse().equals(c) && e.getParticipant().equals(p)) {
                e.setStatus(status);
                saveEnrollments();
                return;
            }
        }
        throw new Exception("Enrollment not found.");
    }

    // ---------- Persistence ----------
    private void saveAll() {
        saveCourses();
        saveParticipants();
        saveEnrollments();
    }

    private void saveCourses() {
        List<List<String>> data = courses.stream().map(c ->
                List.of(c.getCourseId(), c.getTitle(), c.getInstructorName(),
                        c.getDatetime(), c.getLocation(), String.valueOf(c.getCapacity()))
        ).collect(Collectors.toList());
        CsvUtil.write("courses.csv",
                List.of("id", "title", "instructor", "datetime", "location", "capacity"),
                data);
    }

    private void saveParticipants() {
        List<List<String>> data = participants.stream().map(p ->
                List.of(p.getParticipantId(), p.getName(), p.getDepartment(), p.getEmail())
        ).collect(Collectors.toList());
        CsvUtil.write("participants.csv",
                List.of("id", "name", "dept", "email"),
                data);
    }

    private void saveEnrollments() {
        List<List<String>> data = enrollments.stream().map(e ->
                List.of(e.getCourse().getCourseId(), e.getParticipant().getParticipantId(), e.getStatus().name())
        ).collect(Collectors.toList());
        CsvUtil.write("enrollments.csv",
                List.of("courseId", "participantId", "status"),
                data);
    }

    private void loadData() {
        loadCourses();
        loadParticipants();
        loadEnrollments();
    }

    private void loadCourses() {
        for (var row : CsvUtil.read("courses.csv")) {
            courses.add(new Course(
                    row.get("id"),
                    row.get("title"),
                    row.get("instructor"),
                    row.get("datetime"),
                    row.get("location"),
                    Integer.parseInt(row.get("capacity"))
            ));
        }
    }

    private void loadParticipants() {
        for (var row : CsvUtil.read("participants.csv")) {
            participants.add(new Participant(
                    row.get("id"),
                    row.get("name"),
                    row.get("dept"),
                    row.get("email")
            ));
        }
    }

    private void loadEnrollments() {
        for (var row : CsvUtil.read("enrollments.csv")) {
            Course c = findCourse(row.get("courseId"));
            Participant p = findParticipant(row.get("participantId"));
            if (c != null && p != null) {
                Enrollment e = new Enrollment(c, p);
                try {
                    e.setStatus(CompletionStatus.valueOf(row.get("status")));
                } catch (Exception ex) {
                    e.setStatus(CompletionStatus.IN_PROGRESS);
                }
                enrollments.add(e);
            }
        }
    }

    // ---------- Default seed ----------
    private void seedSampleData() {
        courses.add(new Course("C-101","Excel Basics","Dr. Patel","2025-11-02 10:00 AM","Room 204",3));
        courses.add(new Course("C-202","Leadership Bootcamp","Ms. Rivera","2025-11-05 2:00 PM","Conf B",5));

        participants.add(new Participant("P-001","Alice Johnson","Marketing","alice@org.com"));
        participants.add(new Participant("P-002","Brian Lee","Finance","brian@org.com"));
        participants.add(new Participant("P-003","Carla Singh","HR","carla@org.com"));

        enrollments.add(new Enrollment(courses.get(0), participants.get(0)));
    }
}
