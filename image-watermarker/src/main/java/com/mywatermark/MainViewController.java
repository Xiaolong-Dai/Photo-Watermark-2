package com.mywatermark;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ListView;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;

import javax.imageio.ImageIO;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class MainViewController {

    @FXML
    private ListView<File> imageListView;
    @FXML
    private ImageView imagePreviewView;
    @FXML
    private TextField watermarkTextField;
    @FXML
    private ColorPicker colorPicker;
    @FXML
    private Slider opacitySlider;

    private final ObservableList<File> imageFiles = FXCollections.observableArrayList();
    private File currentImageFile;

    @FXML
    public void initialize() {
        imageListView.setItems(imageFiles);

        imageListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                currentImageFile = newValue;
                updatePreview();
            }
        });

        // Set default color
        colorPicker.setValue(Color.WHITE);

        // Add listeners to update preview automatically
        watermarkTextField.textProperty().addListener((obs, oldText, newText) -> updatePreview());
        colorPicker.valueProperty().addListener((obs, oldColor, newColor) -> updatePreview());
        opacitySlider.valueProperty().addListener((obs, oldVal, newVal) -> updatePreview());
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
                    name.toLowerCase().endsWith(".png") || name.toLowerCase().endsWith(".jpg") ||
                    name.toLowerCase().endsWith(".jpeg") || name.toLowerCase().endsWith(".bmp"));

            if (files != null) {
                imageFiles.addAll(Arrays.asList(files));
            }
        }
    }

    @FXML
    private void handlePreviewWatermark() {
        updatePreview();
    }

    private void updatePreview() {
        if (currentImageFile == null) {
            return;
        }

        try {
            BufferedImage originalImage = ImageIO.read(currentImageFile);
            BufferedImage watermarkedImage = new BufferedImage(originalImage.getWidth(), originalImage.getHeight(), BufferedImage.TYPE_INT_ARGB);

            Graphics2D g2d = watermarkedImage.createGraphics();
            g2d.drawImage(originalImage, 0, 0, null);

            // Watermark properties
            String text = watermarkTextField.getText();
            if (text != null && !text.isEmpty()) {
                Color fxColor = colorPicker.getValue();
                java.awt.Color awtColor = new java.awt.Color((float) fxColor.getRed(), (float) fxColor.getGreen(), (float) fxColor.getBlue(), (float) opacitySlider.getValue());
                g2d.setColor(awtColor);
                g2d.setFont(new Font("Arial", Font.BOLD, 48));
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Simple positioning for now (center)
                FontMetrics fm = g2d.getFontMetrics();
                int x = (watermarkedImage.getWidth() - fm.stringWidth(text)) / 2;
                int y = (watermarkedImage.getHeight() - fm.getHeight()) / 2 + fm.getAscent();
                g2d.drawString(text, x, y);
            }

            g2d.dispose();

            Image fxImage = SwingFXUtils.toFXImage(watermarkedImage, null);
            imagePreviewView.setImage(fxImage);

        } catch (IOException e) {
            e.printStackTrace();
            // Handle error
        }
    }
}
