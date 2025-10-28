package com.trainingapp.controller;

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
}
