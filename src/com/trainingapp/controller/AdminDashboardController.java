package com.trainingapp.controller;

import com.trainingapp.model.Course;
import com.trainingapp.model.Participant;
import com.trainingapp.model.Enrollment;
import com.trainingapp.model.CompletionStatus;
import com.trainingapp.service.TrainingService;
import com.trainingapp.util.Session;
import com.trainingapp.util.ViewUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class AdminDashboardController {
    @FXML private TableView<Course> tblCourses;
    @FXML private TableColumn<Course, String> colCid, colTitle, colInstr, colDate, colLoc;
    @FXML private TableColumn<Course, Number> colCap, colEnrolled;

    @FXML private TextField txtId, txtTitle, txtInstr, txtDate, txtLoc, txtCap;

    @FXML private TableView<Enrollment> tblEnrollments;
    @FXML private TableColumn<Enrollment, String> colPid, colPname, colDept, colEmail, colStatus;

    @FXML private ComboBox<Participant> cmbParticipant;
    @FXML private ComboBox<CompletionStatus> cmbStatus;

    private final TrainingService svc = TrainingService.get();

    @FXML
    private void initialize() {
        // Courses table bindings
        colCid.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getCourseId()));
        colTitle.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getTitle()));
        colInstr.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getInstructorName()));
        colDate.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getDatetime()));
        colLoc.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getLocation()));
        colCap.setCellValueFactory(c -> new javafx.beans.property.SimpleIntegerProperty(c.getValue().getCapacity()));
        colEnrolled.setCellValueFactory(c -> new javafx.beans.property.SimpleIntegerProperty(svc.getEnrolledCount(c.getValue())));

        refreshCourses();

        // Enrollments
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
        ObservableList<Course> data = FXCollections.observableArrayList(svc.getCourses());
        tblCourses.setItems(data);
        tblCourses.refresh();
        refreshEnrollments();
    }

    private void refreshEnrollments() {
        Course sel = tblCourses.getSelectionModel().getSelectedItem();
        if (sel == null) {
            tblEnrollments.setItems(FXCollections.emptyObservableList());
            return;
        }
        ObservableList<Enrollment> data = FXCollections.observableArrayList(svc.getEnrollmentsForCourse(sel.getCourseId()));
        tblEnrollments.setItems(data);
        tblEnrollments.refresh();
    }

    @FXML
    private void handleAddCourse() {
        try {
            String id = txtId.getText().trim();
            String title = txtTitle.getText().trim();
            String instr = txtInstr.getText().trim();
            String dt = txtDate.getText().trim();
            String loc = txtLoc.getText().trim();
            int cap = Integer.parseInt(txtCap.getText().trim());
            svc.addCourse(id, title, instr, dt, loc, cap);
            clearCourseForm();
            refreshCourses();
        } catch (Exception ex) {
            showError(ex.getMessage());
        }
    }

    @FXML
    private void handleEnroll() {
        Course sel = tblCourses.getSelectionModel().getSelectedItem();
        Participant part = cmbParticipant.getValue();
        if (sel == null || part == null) { showError("Select course and participant."); return; }
        try {
            svc.enroll(sel.getCourseId(), part.getParticipantId());
            refreshCourses();
        } catch (Exception ex) {
            showError(ex.getMessage());
        }
    }

    @FXML
    private void handleUpdateStatus() {
        Course sel = tblCourses.getSelectionModel().getSelectedItem();
        Enrollment en = tblEnrollments.getSelectionModel().getSelectedItem();
        CompletionStatus st = cmbStatus.getValue();
        if (sel == null || en == null || st == null) { showError("Select enrollment and status."); return; }
        try {
            svc.setStatus(sel.getCourseId(), en.getParticipant().getParticipantId(), st);
            refreshEnrollments();
        } catch (Exception ex) {
            showError(ex.getMessage());
        }
    }

    @FXML
    private void handleAddParticipant() {
        TextInputDialog d = new TextInputDialog("P-100, Jane Doe, Ops, jane@org.com");
        d.setHeaderText("Add Participant (CSV: id, name, dept, email)");
        d.showAndWait().ifPresent(csv -> {
            try {
                String[] parts = csv.split(",");
                if (parts.length < 4) throw new Exception("Expected 4 values.");
                svc.addParticipant(parts[0].trim(), parts[1].trim(), parts[2].trim(), parts[3].trim());
                cmbParticipant.setItems(FXCollections.observableArrayList(svc.getParticipants()));
            } catch (Exception ex) { showError(ex.getMessage()); }
        });
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
        } catch (Exception ex) {
            showError("Failed to logout.");
        }
    }

    private void clearCourseForm() {
        txtId.clear(); txtTitle.clear(); txtInstr.clear(); txtDate.clear(); txtLoc.clear(); txtCap.clear();
    }
    private void showError(String msg) {
        new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK).showAndWait();
    }
}
