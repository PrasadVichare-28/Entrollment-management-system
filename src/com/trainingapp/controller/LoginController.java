package com.trainingapp.controller;

import com.trainingapp.model.Role;
import com.trainingapp.model.User;
import com.trainingapp.service.AuthService;
import com.trainingapp.util.Session;
import com.trainingapp.util.ViewUtil;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class LoginController {
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label message;

    @FXML
    private void handleLogin(ActionEvent e) {
        String u = usernameField.getText().trim();
        String p = passwordField.getText().trim();
        User user = AuthService.authenticate(u, p);
        if (user == null) {
            message.setText("Invalid username or password.");
            return;
        }
        Session.setCurrentUser(user);
        try {
            Stage stage = (Stage) usernameField.getScene().getWindow();
            Scene scene;
            if (user.getRole() == Role.ADMIN) {
                scene = ViewUtil.loadScene("/com/trainingapp/view/admin_dashboard.fxml", 1060, 640);
            } else if (user.getRole() == Role.INSTRUCTOR) {
                scene = ViewUtil.loadScene("/com/trainingapp/view/instructor_dashboard.fxml", 1060, 640);
            } else {
                scene = ViewUtil.loadScene("/com/trainingapp/view/student_dashboard.fxml", 1000, 620);
            }
            stage.setScene(scene);
            stage.setResizable(true);
            stage.setTitle("Dashboard - " + user.getRole().name());
        } catch (Exception ex) {
            ex.printStackTrace();
            message.setText("Failed to load dashboard.");
        }
    }
}
