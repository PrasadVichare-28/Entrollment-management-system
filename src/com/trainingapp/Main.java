package com.trainingapp;

import com.trainingapp.util.ViewUtil;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        // Give the scene a reasonable base size; we'll maximize the window next
        Scene scene = ViewUtil.loadScene("/com/trainingapp/view/login.fxml", 1280, 800);

        stage.setTitle("Training Enrollment Manager");
        stage.setScene(scene);

        // ✨ Make the window fill the screen (keeps title bar and OS controls)
        stage.setMaximized(true);

        // If you prefer true fullscreen without window chrome, use this instead:
        // stage.setFullScreen(true);

        // Allow resizing so it looks good on all screens
        stage.setResizable(true);

        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
