package com.mywatermark;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class MainViewController {

    @FXML
    private ListView<File> imageListView;

    @FXML
    private ImageView imagePreviewView;

    private final ObservableList<File> imageFiles = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        imageListView.setItems(imageFiles);

        imageListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                try {
                    Image image = new Image(newValue.toURI().toString());
                    imagePreviewView.setImage(image);
                } catch (Exception e) {
                    e.printStackTrace();
                    // Handle image loading error
                }
            }
        });
    }

    @FXML
    private void handleImportImages() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Import Images");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.bmp")
        );
        List<File> selectedFiles = fileChooser.showOpenMultipleDialog(null);

        if (selectedFiles != null) {
            imageFiles.addAll(selectedFiles);
        }
    }

    @FXML
    private void handleImportFolder() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Import Folder");
        File selectedDirectory = directoryChooser.showDialog(null);

        if (selectedDirectory != null) {
            File[] files = selectedDirectory.listFiles((dir, name) ->
                    name.toLowerCase().endsWith(".png") ||
                    name.toLowerCase().endsWith(".jpg") ||
                    name.toLowerCase().endsWith(".jpeg") ||
                    name.toLowerCase().endsWith(".bmp"));

            if (files != null) {
                imageFiles.addAll(Arrays.asList(files));
            }
        }
    }
}
