package com.mywatermark;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

public class App extends Application {

    @Override
    public void start(Stage primaryStage) throws IOException {
        URL fxmlLocation = getClass().getResource("/com/mywatermark/main-view.fxml");
        if (fxmlLocation == null) {
            System.err.println("Cannot find FXML file. Please check the path.");
            return;
        }
        Parent root = FXMLLoader.load(fxmlLocation);
        Scene scene = new Scene(root, 1024, 768);
        URL cssLocation = getClass().getResource("/com/mywatermark/dark-theme.css");
        if (cssLocation != null) {
            scene.getStylesheets().add(cssLocation.toExternalForm());
        } else {
            System.err.println("Cannot find CSS file. Please check the path.");
        }
        primaryStage.setTitle("Photo Watermark Tool");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
