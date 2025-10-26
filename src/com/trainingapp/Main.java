package com.trainingapp;

import com.trainingapp.util.ViewUtil;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        Scene scene = ViewUtil.loadScene("/com/trainingapp/view/login.fxml", 420, 520);
        stage.setTitle("Training Enrollment Manager");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
    }
    public static void main(String[] args) { launch(args); }
}
