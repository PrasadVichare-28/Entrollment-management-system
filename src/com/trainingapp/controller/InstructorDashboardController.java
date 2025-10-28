package com.trainingapp.controller;

import com.trainingapp.model.*;
import com.trainingapp.service.TrainingService;
import com.trainingapp.util.Session;
import com.trainingapp.util.ViewUtil;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class InstructorDashboardController {
    @FXML private TableView<Course> tblCourses;
    @FXML private TableColumn<Course, String> colCid, colTitle, colInstr, colDate, colLoc;
    @FXML private TableColumn<Course, Number> colCap, colEnrolled;

    @FXML private TableView<Enrollment> tblEnrollments;
    @FXML private TableColumn<Enrollment, String> colPid, colPname, colDept, colEmail, colStatus;

    @FXML private ComboBox<Participant> cmbParticipant;
    @FXML private ComboBox<CompletionStatus> cmbStatus;

    private final TrainingService svc = TrainingService.get();

    @FXML
    private void initialize() {
        colCid.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getCourseId()));
        colTitle.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getTitle()));
        colInstr.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getInstructorName()));
        colDate.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getDatetime()));
        colLoc.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getLocation()));
        colCap.setCellValueFactory(c -> new javafx.beans.property.SimpleIntegerProperty(c.getValue().getCapacity()));
        colEnrolled.setCellValueFactory(c -> new javafx.beans.property.SimpleIntegerProperty(svc.getEnrolledCount(c.getValue())));
        refreshCourses();

        colPid.setCellValueFactory(e -> new javafx.beans.property.SimpleStringProperty(e.getValue().getParticipant().getParticipantId()));
        colPname.setCellValueFactory(e -> new javafx.beans.property.SimpleStringProperty(e.getValue().getParticipant().getName()));
        colDept.setCellValueFactory(e -> new javafx.beans.property.SimpleStringProperty(e.getValue().getParticipant().getDepartment()));
        colEmail.setCellValueFactory(e -> new javafx.beans.property.SimpleStringProperty(e.getValue().getParticipant().getEmail()));
        colStatus.setCellValueFactory(e -> new javafx.beans.property.SimpleStringProperty(e.getValue().getStatus().name()));

        tblCourses.getSelectionModel().selectedItemProperty().addListener((obs, o, n) -> refreshEnrollments());

        cmbParticipant.setItems(FXCollections.observableArrayList(svc.getParticipants()));
        cmbStatus.setItems(FXCollections.observableArrayList(CompletionStatus.values()));
    }

    private void refreshCourses() {
        tblCourses.setItems(FXCollections.observableArrayList(svc.getCourses()));
        tblCourses.refresh();
        refreshEnrollments();
    }

    private void refreshEnrollments() {
        Course sel = tblCourses.getSelectionModel().getSelectedItem();
        if (sel == null) {
            tblEnrollments.setItems(FXCollections.emptyObservableList());
            return;
        }
        tblEnrollments.setItems(FXCollections.observableArrayList(svc.getEnrollmentsForCourse(sel.getCourseId())));
        tblEnrollments.refresh();
    }

    @FXML
    private void handleEnroll() {
        Course sel = tblCourses.getSelectionModel().getSelectedItem();
        Participant part = cmbParticipant.getValue();
        if (sel == null || part == null) { error("Select course and participant."); return; }
        try {
            svc.enroll(sel.getCourseId(), part.getParticipantId());
            refreshCourses();
        } catch (Exception ex) { error(ex.getMessage()); }
    }

    @FXML
    private void handleUpdateStatus() {
        Course sel = tblCourses.getSelectionModel().getSelectedItem();
        Enrollment en = tblEnrollments.getSelectionModel().getSelectedItem();
        CompletionStatus st = cmbStatus.getValue();
        if (sel == null || en == null || st == null) { error("Select enrollment and status."); return; }
        try {
            svc.setStatus(sel.getCourseId(), en.getParticipant().getParticipantId(), st);
            refreshEnrollments();
        } catch (Exception ex) { error(ex.getMessage()); }
    }

    @FXML
    private void handleLogout() {
        try {
            Session.clear();
            Stage stage = (Stage) tblCourses.getScene().getWindow();
            Scene scene = ViewUtil.loadScene("/com/trainingapp/view/login.fxml", 420, 520);
            stage.setScene(scene);
            stage.setResizable(false);
            stage.setTitle("Training Enrollment Manager");
        } catch (Exception ex) { error("Failed to logout."); }
    }

    private void error(String m) { new Alert(Alert.AlertType.ERROR, m, ButtonType.OK).showAndWait(); }
}
