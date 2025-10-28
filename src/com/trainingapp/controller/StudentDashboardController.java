package com.trainingapp.controller;

<<<<<<< HEAD
import com.trainingapp.model.Course;
import com.trainingapp.model.Enrollment;
import com.trainingapp.model.User;
import com.trainingapp.service.TrainingService;
import com.trainingapp.util.Session;
import com.trainingapp.util.ViewUtil;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class StudentDashboardController {

    @FXML private Label lblUser;

    // Available Courses Table
    @FXML private TableView<Course> tblCourses;
    @FXML private TableColumn<Course, String> colCid, colTitle, colInstr, colDate, colLoc, colCap, colEnrolled;

    // Student’s Enrollments Table
    @FXML private TableView<Enrollment> tblMyEnrollments;
    @FXML private TableColumn<Enrollment, String> colCourse, colTitle2, colDate2, colLocation, colStatus;

    private final TrainingService svc = TrainingService.get();

    @FXML
    private void initialize() {
        User u = Session.getCurrentUser();
        if (u != null)
            lblUser.setText("Welcome, " + u.getUsername());

        // ===== Available Courses Table =====
        colCid.setCellValueFactory(e -> new javafx.beans.property.SimpleStringProperty(e.getValue().getCourseId()));
        colTitle.setCellValueFactory(e -> new javafx.beans.property.SimpleStringProperty(e.getValue().getTitle()));
        colInstr.setCellValueFactory(e -> new javafx.beans.property.SimpleStringProperty(e.getValue().getInstructorName()));
        colDate.setCellValueFactory(e -> new javafx.beans.property.SimpleStringProperty(e.getValue().getDatetime()));
        colLoc.setCellValueFactory(e -> new javafx.beans.property.SimpleStringProperty(e.getValue().getLocation()));
        colCap.setCellValueFactory(e -> new javafx.beans.property.SimpleStringProperty(String.valueOf(e.getValue().getCapacity())));
        colEnrolled.setCellValueFactory(e -> new javafx.beans.property.SimpleStringProperty(
                String.valueOf(svc.getEnrolledCount(e.getValue()))
        ));

        tblCourses.setItems(FXCollections.observableArrayList(svc.getCourses()));

        // ===== My Enrollments Table =====
        colCourse.setCellValueFactory(e -> new javafx.beans.property.SimpleStringProperty(e.getValue().getCourse().getCourseId()));
        colTitle2.setCellValueFactory(e -> new javafx.beans.property.SimpleStringProperty(e.getValue().getCourse().getTitle()));
        colDate2.setCellValueFactory(e -> new javafx.beans.property.SimpleStringProperty(e.getValue().getCourse().getDatetime()));
        colLocation.setCellValueFactory(e -> new javafx.beans.property.SimpleStringProperty(e.getValue().getCourse().getLocation()));
        colStatus.setCellValueFactory(e -> new javafx.beans.property.SimpleStringProperty(e.getValue().getStatus().name()));

        refresh();
    }

    private void refresh() {
        User u = Session.getCurrentUser();
        if (u == null || u.getParticipantId() == null) {
            tblMyEnrollments.setItems(FXCollections.emptyObservableList());
            return;
        }

        tblMyEnrollments.setItems(FXCollections.observableArrayList(
                svc.getEnrollmentsForParticipant(u.getParticipantId())));
        tblMyEnrollments.refresh();

        tblCourses.setItems(FXCollections.observableArrayList(svc.getCourses()));
        tblCourses.refresh();
    }

    @FXML
    private void handleEnroll(ActionEvent event) {
        Course selectedCourse = tblCourses.getSelectionModel().getSelectedItem();
        if (selectedCourse == null) {
            new Alert(Alert.AlertType.WARNING, "Please select a course to enroll.", ButtonType.OK).showAndWait();
            return;
        }

        try {
            User u = Session.getCurrentUser();
            if (u == null || u.getParticipantId() == null) {
                new Alert(Alert.AlertType.ERROR, "No participant linked to this user.", ButtonType.OK).showAndWait();
                return;
            }

            svc.enroll(selectedCourse.getCourseId(), u.getParticipantId());
            new Alert(Alert.AlertType.INFORMATION, "Enrolled successfully!", ButtonType.OK).showAndWait();
            refresh();

        } catch (Exception ex) {
            new Alert(Alert.AlertType.WARNING, ex.getMessage(), ButtonType.OK).showAndWait();
        }
    }

    @FXML
    private void handleLogout() {
        try {
            Session.clear();
            Stage stage = (Stage) tblMyEnrollments.getScene().getWindow();
            Scene scene = ViewUtil.loadScene("/com/trainingapp/view/login.fxml", 420, 520);
            stage.setScene(scene);
            stage.setResizable(false);
            stage.setTitle("Training Enrollment Manager");
        } catch (Exception ex) {
            new Alert(Alert.AlertType.ERROR, "Failed to logout.", ButtonType.OK).showAndWait();
        }
    }
=======
import com.trainingapp.persistence.CsvStore;
import com.trainingapp.service.Session;
import com.trainingapp.model.User;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Student dashboard controller.
 *
 * - Loads courses from data/courses.csv into the left table.
 *   Expected columns in courses.csv (after header):
 *   [0]=courseId, [1]=title, [2]=instructor, [3]=dateTime, [4]=location
 *
 * - Loads this student's enrollments (data/enrollments.csv) into the right table.
 *
 * - Lets student enroll in a selected course.
 *
 * - Handles logout.
 */
public class StudentDashboardController {

    // These fx:ids MUST match what's in student_dashboard.fxml
    @FXML private TableView<CourseRow> availableCoursesTable;
    @FXML private TableView<CourseRow> myEnrollmentsTable;
    @FXML private Label welcomeLabel;
    @FXML private Button logoutButton;

    private final CsvStore csv = CsvStore.getInstance();

    // backing lists for the tables
    private final ObservableList<CourseRow> allCourses = FXCollections.observableArrayList();
    private final ObservableList<CourseRow> myCourses  = FXCollections.observableArrayList();

    // -------------------------------------------------
    // initialize() is called automatically after FXML loads
    // -------------------------------------------------
    @FXML
    private void initialize() {
        // Make sure table columns know what data to show
        setupTableColumns();

        // Show "Welcome, <email>"
        User me = Session.getCurrentUser();
        if (me != null && welcomeLabel != null) {
            welcomeLabel.setText("Welcome, " + me.getUsername());
        }

        // Load data into both tables
        refreshAvailableCourses();
        refreshMyEnrollments();
    }

    // -------------------------------------------------
    // Enroll button handler (onAction="#handleEnroll")
    // -------------------------------------------------
    @FXML
    private void handleEnroll() {
        User me = Session.getCurrentUser();
        if (me == null) {
            showError("Error", "You are not logged in.");
            return;
        }
        String pid = me.getParticipantId();
        if (pid == null || pid.isBlank()) {
            // This was the old blocker you saw ("No participant linked...")
            showError("Error", "No participant linked to this user.");
            return;
        }

        CourseRow selected = availableCoursesTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Error", "Please select a course to enroll.");
            return;
        }

        // Add (participantId, courseId) to enrollments.csv
        csv.addEnrollment(pid, selected.courseId());

        // Reload "My Enrollments"
        refreshMyEnrollments();

        // Let the student know
        showInfo(
            "Enrollment Complete",
            "You are enrolled in " + selected.courseId() + " • " + selected.title()
        );
    }

    // -------------------------------------------------
    // Refresh button handler (onAction="#handleRefresh")
    // -------------------------------------------------
    @FXML
    private void handleRefresh() {
        refreshAvailableCourses();
        refreshMyEnrollments();
    }

    // -------------------------------------------------
    // Logout button handler (onAction="#handleLogout")
    // -------------------------------------------------
    @FXML
    private void handleLogout() {
        // Optional confirm dialog
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setHeaderText("Log out?");
        confirm.setContentText("Are you sure you want to log out?");
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() != ButtonType.OK) {
            return;
        }

        // Clear current user from session
        Session.setCurrentUser(null);

        // Navigate back to login.fxml
        try {
            // safest way to get the Stage in JavaFX controller code
            Stage stage = (Stage) logoutButton.getScene().getWindow();

            Scene loginScene = com.trainingapp.util.ViewUtil.loadScene(
                "/com/trainingapp/view/login.fxml",
                1020,
                720
            );

            stage.setTitle("Training Enrollment Manager");
            stage.setScene(loginScene);

            var bounds = Screen.getPrimary().getVisualBounds();
            stage.setX(bounds.getMinX());
            stage.setY(bounds.getMinY());
            stage.setWidth(bounds.getWidth());
            stage.setHeight(bounds.getHeight());
            stage.setResizable(true);
            stage.setMaximized(true);

            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showError("Navigation Error", "Failed to return to login screen.");
        }
    }

    // -------------------------------------------------
    // Data loading helpers
    // -------------------------------------------------

    /** Fill left table with all rows from courses.csv */
    private void refreshAvailableCourses() {
        List<String[]> rows = csv.loadCoursesRaw();

        List<CourseRow> mapped = rows.stream()
            .map(this::toCourseRow)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());

        allCourses.setAll(mapped);
        availableCoursesTable.setItems(allCourses);
    }

    /** Fill right table with only the courses this participant is enrolled in */
    private void refreshMyEnrollments() {
        User me = Session.getCurrentUser();
        if (me == null || me.getParticipantId() == null) {
            myCourses.clear();
            myEnrollmentsTable.setItems(myCourses);
            return;
        }

        String pid = me.getParticipantId();
        Set<String> enrolledIds = csv.getEnrolledCourseIds(pid);

        List<CourseRow> mine = allCourses.stream()
            .filter(c -> enrolledIds.contains(c.courseId()))
            .collect(Collectors.toList());

        myCourses.setAll(mine);
        myEnrollmentsTable.setItems(myCourses);
    }

    // -------------------------------------------------
    // Table column binding
    // -------------------------------------------------
    @SuppressWarnings("unchecked")
    private void setupTableColumns() {
        // LEFT TABLE (Available Courses)
        // Expected columns in the FXML in this order:
        // Course ID | Title | Instructor | Date/Time | Location
        if (availableCoursesTable != null && availableCoursesTable.getColumns().size() >= 5) {
            var c0 = (TableColumn<CourseRow,String>) availableCoursesTable.getColumns().get(0);
            var c1 = (TableColumn<CourseRow,String>) availableCoursesTable.getColumns().get(1);
            var c2 = (TableColumn<CourseRow,String>) availableCoursesTable.getColumns().get(2);
            var c3 = (TableColumn<CourseRow,String>) availableCoursesTable.getColumns().get(3);
            var c4 = (TableColumn<CourseRow,String>) availableCoursesTable.getColumns().get(4);

            c0.setCellValueFactory(d -> new ReadOnlyStringWrapper(d.getValue().courseId()));
            c1.setCellValueFactory(d -> new ReadOnlyStringWrapper(d.getValue().title()));
            c2.setCellValueFactory(d -> new ReadOnlyStringWrapper(d.getValue().instructor()));
            c3.setCellValueFactory(d -> new ReadOnlyStringWrapper(d.getValue().dateTime()));
            c4.setCellValueFactory(d -> new ReadOnlyStringWrapper(d.getValue().location()));
        }

        // RIGHT TABLE (My Enrollments)
        // Expected columns in the FXML in this order:
        // Course ID | Title | Date/Time | Location
        if (myEnrollmentsTable != null && myEnrollmentsTable.getColumns().size() >= 4) {
            var m0 = (TableColumn<CourseRow,String>) myEnrollmentsTable.getColumns().get(0);
            var m1 = (TableColumn<CourseRow,String>) myEnrollmentsTable.getColumns().get(1);
            var m2 = (TableColumn<CourseRow,String>) myEnrollmentsTable.getColumns().get(2);
            var m3 = (TableColumn<CourseRow,String>) myEnrollmentsTable.getColumns().get(3);

            m0.setCellValueFactory(d -> new ReadOnlyStringWrapper(d.getValue().courseId()));
            m1.setCellValueFactory(d -> new ReadOnlyStringWrapper(d.getValue().title()));
            m2.setCellValueFactory(d -> new ReadOnlyStringWrapper(d.getValue().dateTime()));
            m3.setCellValueFactory(d -> new ReadOnlyStringWrapper(d.getValue().location()));
        }
    }

    // -------------------------------------------------
    // Small utility / view model
    // -------------------------------------------------
    private CourseRow toCourseRow(String[] arr) {
        // defend against short or malformed rows in courses.csv
        if (arr.length < 1) return null;
        String id   = safeIdx(arr, 0);
        String ttl  = safeIdx(arr, 1);
        String inst = safeIdx(arr, 2);
        String dt   = safeIdx(arr, 3);
        String loc  = safeIdx(arr, 4);
        return new CourseRow(id, ttl, inst, dt, loc);
    }

    private String safeIdx(String[] a, int i) {
        return (i < a.length && a[i] != null) ? a[i] : "";
    }

    // Row object shown in the tables
    public static class CourseRow {
        private final String courseId;
        private final String title;
        private final String instructor;
        private final String dateTime;
        private final String location;

        public CourseRow(String courseId, String title, String instructor, String dateTime, String location) {
            this.courseId   = courseId;
            this.title      = title;
            this.instructor = instructor;
            this.dateTime   = dateTime;
            this.location   = location;
        }

        public String courseId()   { return courseId; }
        public String title()      { return title; }
        public String instructor() { return instructor; }
        public String dateTime()   { return dateTime; }
        public String location()   { return location; }
    }

    // -------------------------------------------------
    // Alert helpers
    // -------------------------------------------------
    private void showError(String header, String text) {
        Alert a = new Alert(Alert.AlertType.ERROR, text, ButtonType.OK);
        a.setHeaderText(header);
        a.showAndWait();
    }

    private void showInfo(String header, String text) {
        Alert a = new Alert(Alert.AlertType.INFORMATION, text, ButtonType.OK);
        a.setHeaderText(header);
        a.showAndWait();
    }
>>>>>>> master
}
