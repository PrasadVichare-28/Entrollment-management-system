package com.trainingapp.controller;

import com.trainingapp.model.Enrollment;
import com.trainingapp.model.User;
import com.trainingapp.service.TrainingService;
import com.trainingapp.util.Session;
import com.trainingapp.util.ViewUtil;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class StudentDashboardController {
    @FXML private Label lblUser;
    @FXML private TableView<Enrollment> tblMyEnrollments;
    @FXML private TableColumn<Enrollment, String> colCourse, colTitle, colDate, colLocation, colStatus;

    private final TrainingService svc = TrainingService.get();

    @FXML
    private void initialize() {
        User u = Session.getCurrentUser();
        lblUser.setText("Welcome, " + u.getUsername());

        colCourse.setCellValueFactory(e -> new javafx.beans.property.SimpleStringProperty(e.getValue().getCourse().getCourseId()));
        colTitle.setCellValueFactory(e -> new javafx.beans.property.SimpleStringProperty(e.getValue().getCourse().getTitle()));
        colDate.setCellValueFactory(e -> new javafx.beans.property.SimpleStringProperty(e.getValue().getCourse().getDatetime()));
        colLocation.setCellValueFactory(e -> new javafx.beans.property.SimpleStringProperty(e.getValue().getCourse().getLocation()));
        colStatus.setCellValueFactory(e -> new javafx.beans.property.SimpleStringProperty(e.getValue().getStatus().name()));

        refresh();
    }

    private void refresh() {
        User u = Session.getCurrentUser();
        // Use participantId from User (linked in AuthService)
        String pid = u.getParticipantId();  
        if (pid == null) {
            tblMyEnrollments.setItems(FXCollections.emptyObservableList());
            return;
        }
        tblMyEnrollments.setItems(FXCollections.observableArrayList(svc.getEnrollmentsForParticipant(pid)));
        tblMyEnrollments.refresh();
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
