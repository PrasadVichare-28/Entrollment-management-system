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
 * Controller for register.fxml.
 *
 * Flow:
 *  1. Validate inputs
 *  2. Create new participant + user via AuthService.registerStudent(fullName, username, pw)
 *  3. Store that new user in Session
 *  4. Send them to Login screen (clean handoff)
 */
public class RegisterController {

    @FXML
    private TextField fullNameField; // fx:id="fullNameField"

    @FXML
    private TextField emailField;    // fx:id="emailField"

    @FXML
    private PasswordField passwordField; // fx:id="passwordField"

    @FXML
    private PasswordField confirmPasswordField; // fx:id="confirmPasswordField"

    @FXML
    private Label errorLabel; // fx:id="errorLabel"

    private final AuthService authService = AuthService.getInstance();

    @FXML
    private void handleRegister() {
        String fullName         = fullNameField.getText().trim();
        String usernameOrEmail  = emailField.getText().trim();
        String pw               = passwordField.getText();
        String pw2              = confirmPasswordField.getText();

        // required fields
        if (fullName.isEmpty() ||
            usernameOrEmail.isEmpty() ||
            pw.isEmpty() ||
            pw2.isEmpty()) {

            setError("All fields are required.");
            return;
        }

        // passwords match
        if (!pw.equals(pw2)) {
            setError("Passwords do not match.");
            return;
        }

        // username is free
        if (authService.userExists(usernameOrEmail)) {
            setError("An account with that username already exists.");
            return;
        }

        // create new student account (also writes participant + user CSV)
        User newUser = authService.registerStudent(fullName, usernameOrEmail, pw);

        if (newUser == null) {
            setError("Could not create account. Please try again.");
            return;
        }

        // keep track of who just registered (nice for future use)
        Session.setCurrentUser(newUser);

        // go to login (safer than trying to jump straight into dashboard mid-flow)
        goToLogin();
    }

    @FXML
    private void goToLogin() {
        try {
            Stage stage = (Stage) fullNameField.getScene().getWindow();
            Scene loginScene = ViewUtil.loadScene(
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
            setError("Registered, but failed to open login screen.");
        }
    }

    // we keep this around in case later you want immediate dashboard entry.
    @SuppressWarnings("unused")
    private void openStudentDashboardDirect() {
        try {
            Stage stage = (Stage) fullNameField.getScene().getWindow();
            Scene studentScene = ViewUtil.loadScene(
                    "/com/trainingapp/view/student_dashboard.fxml",
                    1020,
                    720
            );

            stage.setTitle("Student Dashboard");
            stage.setScene(studentScene);

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
            setError("Registered, but failed to open dashboard.");
        }
    }

    private void setError(String text) {
        if (errorLabel != null) {
            errorLabel.setText(text);
        } else {
            System.err.println(text);
        }
    }
}
