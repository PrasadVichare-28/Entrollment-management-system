package com.trainingapp;

import com.trainingapp.util.ViewUtil;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        // Load login page with dynamic scene
        Scene scene = ViewUtil.loadScene("/com/trainingapp/view/login.fxml", 1020, 720);

        stage.setTitle("Training Enrollment Manager");
        stage.setScene(scene);

        // Make the window adapt to the user's screen size
        var bounds = Screen.getPrimary().getVisualBounds();
        stage.setX(bounds.getMinX());
        stage.setY(bounds.getMinY());
        stage.setWidth(bounds.getWidth());
        stage.setHeight(bounds.getHeight());

        // Allow resizing and scaling for all responsive FXMLs
        stage.setResizable(true);
        stage.setMaximized(true); // keep windowed full-screen

        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
