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
import java.awt.geom.AffineTransform;
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
    @FXML
    private Slider rotationSlider;
    @FXML
    private TextField xField;
    @FXML
    private TextField yField;

    private final ObservableList<File> imageFiles = FXCollections.observableArrayList();
    private File currentImageFile;

    private int watermarkX = 0;
    private int watermarkY = 0;
    private double watermarkRotation = 0;

    @FXML
    public void initialize() {
        imageListView.setItems(imageFiles);

        imageListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                currentImageFile = newValue;
                updatePreview();
            }
        });

        colorPicker.setValue(Color.WHITE);

        watermarkTextField.textProperty().addListener((obs, oldText, newText) -> updatePreview());
        colorPicker.valueProperty().addListener((obs, oldColor, newColor) -> updatePreview());
        opacitySlider.valueProperty().addListener((obs, oldVal, newVal) -> updatePreview());
        rotationSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            watermarkRotation = newVal.doubleValue();
            updatePreview();
        });
        xField.textProperty().addListener((obs, oldText, newText) -> {
            try {
                watermarkX = Integer.parseInt(newText);
                updatePreview();
            } catch (NumberFormatException e) {
                // Ignore invalid input
            }
        });
        yField.textProperty().addListener((obs, oldText, newText) -> {
            try {
                watermarkY = Integer.parseInt(newText);
                updatePreview();
            } catch (NumberFormatException e) {
                // Ignore invalid input
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
                    name.toLowerCase().endsWith(".png") || name.toLowerCase().endsWith(".jpg") ||
                    name.toLowerCase().endsWith(".jpeg") || name.toLowerCase().endsWith(".bmp"));

            if (files != null) {
                imageFiles.addAll(Arrays.asList(files));
            }
        }
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

            String text = watermarkTextField.getText();
            if (text != null && !text.isEmpty()) {
                Color fxColor = colorPicker.getValue();
                java.awt.Color awtColor = new java.awt.Color((float) fxColor.getRed(), (float) fxColor.getGreen(), (float) fxColor.getBlue(), (float) opacitySlider.getValue());
                g2d.setColor(awtColor);
                g2d.setFont(new Font("Arial", Font.BOLD, 48));
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                FontMetrics fm = g2d.getFontMetrics();
                int textWidth = fm.stringWidth(text);

                // Save the original transform
                AffineTransform originalTransform = g2d.getTransform();

                // Rotate the graphics context
                g2d.rotate(Math.toRadians(watermarkRotation), watermarkX + textWidth / 2.0, watermarkY);

                g2d.drawString(text, watermarkX, watermarkY + fm.getAscent());

                // Restore the original transform
                g2d.setTransform(originalTransform);
            }

            g2d.dispose();

            Image fxImage = SwingFXUtils.toFXImage(watermarkedImage, null);
            imagePreviewView.setImage(fxImage);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void updatePositionFields() {
        xField.setText(String.valueOf(watermarkX));
        yField.setText(String.valueOf(watermarkY));
    }

    // --- Position Handlers ---
    @FXML private void handlePositionTopLeft() { setPosition(0, 0); }
    @FXML private void handlePositionTopCenter() { setPosition(1, 0); }
    @FXML private void handlePositionTopRight() { setPosition(2, 0); }
    @FXML private void handlePositionCenterLeft() { setPosition(0, 1); }
    @FXML private void handlePositionCenter() { setPosition(1, 1); }
    @FXML private void handlePositionCenterRight() { setPosition(2, 1); }
    @FXML private void handlePositionBottomLeft() { setPosition(0, 2); }
    @FXML private void handlePositionBottomCenter() { setPosition(1, 2); }
    @FXML private void handlePositionBottomRight() { setPosition(2, 2); }

    private void setPosition(int hPos, int vPos) { // 0=left/top, 1=center, 2=right/bottom
        if (currentImageFile == null) return;

        try {
            BufferedImage image = ImageIO.read(currentImageFile);
            FontMetrics fm = getFontMetrics();
            int textWidth = fm.stringWidth(watermarkTextField.getText());

            // Horizontal
            if (hPos == 0) watermarkX = 10; // margin
            else if (hPos == 1) watermarkX = (image.getWidth() - textWidth) / 2;
            else watermarkX = image.getWidth() - textWidth - 10;

            // Vertical (y is the baseline)
            if (vPos == 0) watermarkY = 0;
            else if (vPos == 1) watermarkY = (image.getHeight() - fm.getHeight()) / 2;
            else watermarkY = image.getHeight() - fm.getHeight() - 10;

            updatePositionFields();
            updatePreview();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private FontMetrics getFontMetrics() {
        BufferedImage tempImg = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = tempImg.createGraphics();
        g2d.setFont(new Font("Arial", Font.BOLD, 48));
        FontMetrics fm = g2d.getFontMetrics();
        g2d.dispose();
        return fm;
    }
}
