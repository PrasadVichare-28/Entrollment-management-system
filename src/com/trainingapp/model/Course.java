package com.trainingapp.model;

public class Course {
    private String courseId;
    private String title;
    private String instructorName;
    private String datetime;  // keep string for simplicity
    private String location;
    private int capacity;

    public Course(String courseId, String title, String instructorName, String datetime, String location, int capacity) {
        this.courseId = courseId;
        this.title = title;
        this.instructorName = instructorName;
        this.datetime = datetime;
        this.location = location;
        this.capacity = capacity;
    }

    public String getCourseId() { return courseId; }
    public String getTitle() { return title; }
    public String getInstructorName() { return instructorName; }
    public String getDatetime() { return datetime; }
    public String getLocation() { return location; }
    public int getCapacity() { return capacity; }

    public void setTitle(String title) { this.title = title; }
    public void setInstructorName(String instructorName) { this.instructorName = instructorName; }
    public void setDatetime(String datetime) { this.datetime = datetime; }
    public void setLocation(String location) { this.location = location; }
    public void setCapacity(int capacity) { this.capacity = capacity; }
}
