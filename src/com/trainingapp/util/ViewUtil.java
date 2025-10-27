package com.trainingapp.util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import java.io.IOException;

public class ViewUtil {
    public static Scene loadScene(String fxmlPath, int width, int height) {
        try {
            FXMLLoader loader = new FXMLLoader(ViewUtil.class.getResource(fxmlPath));
            Parent root = loader.load();
            return new Scene(root, width, height);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
