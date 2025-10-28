package com.trainingapp.controller;

import com.trainingapp.model.User;
import com.trainingapp.service.AuthService;
import com.trainingapp.service.Session;
import com.trainingapp.util.ViewUtil;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Screen;
import javafx.stage.Stage;

/**
 * Students log in with their EMAIL + password.
 */
public class LoginController {

    @FXML
    private TextField emailField;     // fx:id="emailField" in login.fxml

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label message;

    private final AuthService authService = AuthService.getInstance();

    @FXML
    private void handleLogin() {
        String email = emailField.getText().trim();
        String password = passwordField.getText();

        if (email.isEmpty() || password.isEmpty()) {
            setError("Please enter email and password.");
            return;
        }

        User user = authService.authenticate(email, password);
        if (user == null) {
            setError("Invalid email or password.");
            return;
        }

        // ✅ Save the logged-in user globally for the rest of the app
        Session.setCurrentUser(user);

        // DEBUG: print to console so we can confirm we have a participantId here
        System.out.println("[LoginController] Logged in as " + user.getUsername()
                + " role=" + user.getRole()
                + " pid=" + user.getParticipantId());

        // Route by role
        switch (user.getRole()) {
            case ADMIN -> openFullScreen("/com/trainingapp/view/admin_dashboard.fxml", "Admin Dashboard");
            case INSTRUCTOR -> openFullScreen("/com/trainingapp/view/instructor_dashboard.fxml", "Instructor Dashboard");
            case STUDENT -> openFullScreen("/com/trainingapp/view/student_dashboard.fxml", "Student Dashboard");
            default -> setError("Unknown role.");
        }
    }

    @FXML
    private void goToRegister() {
        try {
            Stage stage = (Stage) emailField.getScene().getWindow();
            Scene registerScene = ViewUtil.loadScene("/com/trainingapp/view/register.fxml", 400, 450);
            stage.setTitle("Create Account");
            stage.setScene(registerScene);
            stage.setResizable(false);
            stage.centerOnScreen();
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            setError("Failed to open registration screen.");
        }
    }

    private void openFullScreen(String fxmlPath, String title) {
        try {
            Stage stage = (Stage) emailField.getScene().getWindow();
            Scene scene = ViewUtil.loadScene(fxmlPath, 1020, 720);

            stage.setTitle(title);
            stage.setScene(scene);

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
            setError("Failed to load dashboard.");
        }
    }

    private void setError(String text) {
        if (message != null) {
            message.setText(text);
        } else {
            System.err.println(text);
        }
    }
}
