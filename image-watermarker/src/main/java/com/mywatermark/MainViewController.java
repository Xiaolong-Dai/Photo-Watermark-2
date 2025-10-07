package com.mywatermark;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.AlphaComposite;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

public class MainViewController {

    private static final Logger logger = Logger.getLogger(MainViewController.class.getName());

    private enum WatermarkMode { TEXT, IMAGE }

    //<editor-fold desc="FXML Fields">
    @FXML private ListView<File> imageListView;
    @FXML private ImageView imagePreviewView;
    @FXML private Pane previewPane; // Container for preview to support dragging
    @FXML private TextField watermarkTextField;
    @FXML private ColorPicker colorPicker;
    @FXML private Slider opacitySlider;
    @FXML private Slider rotationSlider;
    @FXML private TextField xField;
    @FXML private TextField yField;
    @FXML private TextField outputDirField;
    @FXML private ComboBox<String> namingConventionBox;
    @FXML private TextField prefixSuffixField;
    @FXML private ComboBox<String> formatBox;
    @FXML private Slider qualitySlider;
    @FXML private Label qualityLabel;
    @FXML private Button exportButton;
    @FXML private ToggleButton textModeButton;
    @FXML private ToggleButton imageModeButton;
    @FXML private TitledPane textWatermarkPane;
    @FXML private TitledPane imageWatermarkPane;
    @FXML private TextField imageWatermarkField;
    @FXML private Slider imageOpacitySlider;
    @FXML private Slider imageScaleSlider;
    @FXML private Menu myTemplatesMenu;
    @FXML private ProgressBar exportProgressBar;
    @FXML private Label exportProgressLabel;
    @FXML private ComboBox<String> fontComboBox;
    @FXML private Slider fontSizeSlider;
    @FXML private CheckBox boldCheckBox;
    @FXML private CheckBox italicCheckBox;
    //</editor-fold>

    private final ObservableList<File> imageFiles = FXCollections.observableArrayList();
    private File currentImageFile;
    private File outputDirectory;
    private File imageWatermarkFile;

    private WatermarkMode watermarkMode = WatermarkMode.TEXT;
    private int watermarkX = 0;
    private int watermarkY = 0;
    private double watermarkRotation = 0;
    private final Path templatesDir = Paths.get(System.getProperty("user.home"), ".photo-watermark-templates");

    @FXML
    public void initialize() {
        logger.info("Application initializing...");
        imageListView.setItems(imageFiles);
        imageListView.getSelectionModel().selectedItemProperty().addListener((obs, ov, nv) -> {
            if (nv != null) {
                currentImageFile = nv;
                updatePreview();
            }
        });

        // Mode Toggle
        ToggleGroup modeGroup = new ToggleGroup();
        textModeButton.setToggleGroup(modeGroup);
        imageModeButton.setToggleGroup(modeGroup);
        textWatermarkPane.visibleProperty().bind(textModeButton.selectedProperty());
        imageWatermarkPane.visibleProperty().bind(imageModeButton.selectedProperty());
        modeGroup.selectedToggleProperty().addListener((obs, oldToggle, newToggle) -> {
            if (newToggle == textModeButton) {
                watermarkMode = WatermarkMode.TEXT;
            } else {
                watermarkMode = WatermarkMode.IMAGE;
            }
            updatePreview();
        });

        colorPicker.setValue(Color.WHITE);

        // Listeners
        watermarkTextField.textProperty().addListener(obs -> updatePreview());
        colorPicker.valueProperty().addListener(obs -> updatePreview());
        opacitySlider.valueProperty().addListener(obs -> updatePreview());
        imageOpacitySlider.valueProperty().addListener(obs -> updatePreview());
        imageScaleSlider.valueProperty().addListener(obs -> updatePreview());
        rotationSlider.valueProperty().addListener((obs, ov, nv) -> { watermarkRotation = nv.doubleValue(); updatePreview(); });
        xField.textProperty().addListener((obs, ov, nv) -> { try { watermarkX = Integer.parseInt(nv); updatePreview(); } catch (NumberFormatException e) { 
            showErrorAlert("Invalid Input", "Please enter a valid number for the X coordinate.");
            logger.warning("Invalid X coordinate input: " + nv);
        } });
        yField.textProperty().addListener((obs, ov, nv) -> { try { watermarkY = Integer.parseInt(nv); updatePreview(); } catch (NumberFormatException e) { 
            showErrorAlert("Invalid Input", "Please enter a valid number for the Y coordinate.");
            logger.warning("Invalid Y coordinate input: " + nv);
        } });

        // Export setup
        namingConventionBox.setItems(FXCollections.observableArrayList("Original", "Prefix", "Suffix"));
        namingConventionBox.setValue("Original");
        formatBox.setItems(FXCollections.observableArrayList("PNG", "JPEG"));
        formatBox.setValue("PNG");
        qualitySlider.visibleProperty().bind(formatBox.valueProperty().isEqualTo("JPEG"));
        qualityLabel.visibleProperty().bind(formatBox.valueProperty().isEqualTo("JPEG"));

        // Add drag functionality to preview pane
        addDragFunctionality();

        // Initialize font selection
        initializeFontSelection();

        loadTemplatesMenu();
    }

    private void initializeFontSelection() {
        // Get all available fonts and populate the combo box
        java.awt.GraphicsEnvironment ge = java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment();
        String[] fontNames = ge.getAvailableFontFamilyNames();
        fontComboBox.getItems().addAll(fontNames);
        fontComboBox.setValue("Arial");

        // Set up font size slider
        if (fontSizeSlider != null) {
            fontSizeSlider.setMin(8);
            fontSizeSlider.setMax(120);
            fontSizeSlider.setValue(48);
        }

        // Add listeners for font properties
        if (fontComboBox != null) {
            fontComboBox.valueProperty().addListener(obs -> updatePreview());
        }
        if (fontSizeSlider != null) {
            fontSizeSlider.valueProperty().addListener(obs -> updatePreview());
        }
        if (boldCheckBox != null) {
            boldCheckBox.selectedProperty().addListener(obs -> updatePreview());
        }
        if (italicCheckBox != null) {
            italicCheckBox.selectedProperty().addListener(obs -> updatePreview());
        }
    }

    private void addDragFunctionality() {
        previewPane.setOnMousePressed(event -> {
            if (currentImageFile != null) {
                // Capture the starting position of the mouse
                lastMouseX = event.getX();
                lastMouseY = event.getY();
            }
        });

        previewPane.setOnMouseDragged(event -> {
            if (currentImageFile != null) {
                // Calculate the movement delta relative to the preview pane
                double deltaX = event.getX() - lastMouseX;
                double deltaY = event.getY() - lastMouseY;

                // Update watermark position based on the delta
                watermarkX += (int) deltaX;
                watermarkY += (int) deltaY;

                // Apply boundary checks to keep watermark within image bounds
                applyBoundaryChecks();

                // Update fields and preview
                updatePositionFields();
                updatePreview();

                // Update the last mouse position
                lastMouseX = event.getX();
                lastMouseY = event.getY();
            }
        });
    }

    // Apply boundary checks to keep watermark within image bounds
    private void applyBoundaryChecks() {
        try {
            if (currentImageFile != null) {
                BufferedImage image = ImageIO.read(currentImageFile);
                if (image != null) {
                    int itemWidth = 0;
                    int itemHeight = 0;

                    if (watermarkMode == WatermarkMode.TEXT) {
                        FontMetrics fm = getFontMetrics();
                        String text = watermarkTextField.getText();
                        if (text != null) {
                            itemWidth = fm.stringWidth(text);
                            itemHeight = fm.getHeight();
                        }
                    } else if (imageWatermarkFile != null) {
                        BufferedImage watermark = ImageIO.read(imageWatermarkFile);
                        if (watermark != null) {
                            itemWidth = (int) (watermark.getWidth() * imageScaleSlider.getValue());
                            itemHeight = (int) (watermark.getHeight() * imageScaleSlider.getValue());
                        }
                    }

                    // Keep watermark within image bounds
                    if (watermarkX < 0) watermarkX = 0;
                    if (watermarkY < 0) watermarkY = 0;
                    if (watermarkX + itemWidth > image.getWidth()) {
                        watermarkX = Math.max(0, image.getWidth() - itemWidth);
                    }
                    if (watermarkY + itemHeight > image.getHeight()) {
                        watermarkY = Math.max(0, image.getHeight() - itemHeight);
                    }
                }
            }
        } catch (IOException e) {
            logger.log(Level.WARNING, "Could not apply boundary checks due to IO error", e);
        }
    }

    // Variables to store the last mouse position for dragging
    private double lastMouseX = 0;
    private double lastMouseY = 0;

    //<editor-fold desc="File Import">
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
            logger.info("Imported " + selectedFiles.size() + " image(s).");
        }
    }

    @FXML
    private void handleImportFolder() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Import Folder");
        File selectedDirectory = directoryChooser.showDialog(null);
        if (selectedDirectory != null) {
            logger.info("Importing from folder: " + selectedDirectory.getAbsolutePath());
            try {
                File[] files = selectedDirectory.listFiles((dir, name) ->
                        name.toLowerCase().endsWith(".png") || name.toLowerCase().endsWith(".jpg") ||
                        name.toLowerCase().endsWith(".jpeg") || name.toLowerCase().endsWith(".bmp"));
                if (files != null) {
                    imageFiles.addAll(Arrays.asList(files));
                    logger.info("Found " + files.length + " image(s) in folder.");
                } else {
                    showErrorAlert("Import Error", "Could not list files in the selected directory. Check folder permissions.");
                    logger.warning("Could not list files in directory: " + selectedDirectory.getAbsolutePath());
                }
            } catch (SecurityException e) {
                showErrorAlert("Import Error", "Could not access the selected directory due to security restrictions.");
                logger.log(Level.SEVERE, "Security exception when accessing directory: " + selectedDirectory.getAbsolutePath(), e);
            }
        }
    }
    //</editor-fold>

    //<editor-fold desc="Watermark & Preview">
    // Debounce mechanism for preview updates to improve performance
    private volatile boolean isPreviewUpdating = false;
    private volatile long lastPreviewUpdateTime = 0;
    private static final long PREVIEW_UPDATE_DEBOUNCE_MS = 100; // 100ms debounce time
    
    private void updatePreview() {
        if (currentImageFile == null) return;
        
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastPreviewUpdateTime < PREVIEW_UPDATE_DEBOUNCE_MS) {
            // Schedule update for later if we're updating too frequently
            javafx.application.Platform.runLater(() -> {
                // Double-check that enough time has passed before actually updating
                if (System.currentTimeMillis() - lastPreviewUpdateTime >= PREVIEW_UPDATE_DEBOUNCE_MS && !isPreviewUpdating) {
                    performPreviewUpdate();
                }
            });
            return;
        }
        
        if (!isPreviewUpdating) {
            performPreviewUpdate();
        }
    }
    
    private void performPreviewUpdate() {
        if (isPreviewUpdating) return;
        isPreviewUpdating = true;
        lastPreviewUpdateTime = System.currentTimeMillis();
        
        // Use a separate thread for preview updates to prevent UI freezing
        new Thread(() -> {
            try {
                BufferedImage originalImage = ImageIO.read(currentImageFile);
                if (originalImage == null) {
                    showErrorAlert("Image Load Error", "Could not read the image file: " + currentImageFile.getName() + ". The file may be corrupt or in an unsupported format.");
                    logger.warning("ImageIO.read returned null for: " + currentImageFile.getAbsolutePath());
                    javafx.application.Platform.runLater(() -> isPreviewUpdating = false);
                    return;
                }
                BufferedImage watermarkedImage = addWatermark(originalImage);
                
                // Run UI update on JavaFX Application Thread
                javafx.application.Platform.runLater(() -> {
                    // Ensure we're not passing a null image to the ImageView
                    if (watermarkedImage != null) {
                        Image fxImage = SwingFXUtils.toFXImage(watermarkedImage, null);
                        if (fxImage != null) {
                            imagePreviewView.setImage(fxImage);
                        } else {
                            logger.warning("SwingFXUtils.toFXImage returned null for: " + currentImageFile.getName());
                        }
                    } else {
                        logger.warning("Watermarked image is null for: " + currentImageFile.getName());
                    }
                    isPreviewUpdating = false;
                });
            } catch (OutOfMemoryError e) {
                logger.log(Level.SEVERE, "Out of memory error during preview: " + currentImageFile.getAbsolutePath(), e);
                javafx.application.Platform.runLater(() -> {
                    showErrorAlert("Memory Error", "The image file is too large to process. Please try a smaller image.");
                    isPreviewUpdating = false;
                });
            } catch (IOException e) {
                logger.log(Level.SEVERE, "IOException in updatePreview for: " + currentImageFile.getAbsolutePath(), e);
                javafx.application.Platform.runLater(() -> {
                    showErrorAlert("Image Load Error", "An I/O error occurred while reading the file: " + currentImageFile.getName());
                    isPreviewUpdating = false;
                });
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Unexpected error in updatePreview for: " + currentImageFile.getAbsolutePath(), e);
                javafx.application.Platform.runLater(() -> {
                    showErrorAlert("Error", "An unexpected error occurred: " + e.getMessage());
                    isPreviewUpdating = false;
                });
            }
        }).start();
    }

    private void showErrorAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private BufferedImage addWatermark(BufferedImage originalImage) {
        BufferedImage watermarkedImage = new BufferedImage(originalImage.getWidth(), originalImage.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = watermarkedImage.createGraphics();
        g2d.drawImage(originalImage, 0, 0, null);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (watermarkMode == WatermarkMode.TEXT) {
            addTextWatermark(g2d);
        } else if (watermarkMode == WatermarkMode.IMAGE && imageWatermarkFile != null) {
            addImageWatermark(g2d);
        }

        g2d.dispose();
        return watermarkedImage;
    }

    private void addTextWatermark(Graphics2D g2d) {
        String text = watermarkTextField.getText();
        if (text == null || text.isEmpty()) return;

        Color fxColor = colorPicker.getValue();
        java.awt.Color awtColor = new java.awt.Color((float) fxColor.getRed(), (float) fxColor.getGreen(), (float) fxColor.getBlue(), (float) opacitySlider.getValue());
        g2d.setColor(awtColor);
        
        // Use a dynamic font based on user selection if available, otherwise default
        String fontFamily = "Arial";
        int fontSize = 48;
        int fontStyle = Font.BOLD;
        
        // Check if font is selected and available
        if (fontComboBox != null && fontComboBox.getValue() != null) {
            fontFamily = fontComboBox.getValue();
        }
        if (fontSizeSlider != null) {
            fontSize = (int) fontSizeSlider.getValue();
        }
        if (boldCheckBox != null && boldCheckBox.isSelected()) {
            fontStyle = Font.BOLD;
        } else {
            fontStyle = Font.PLAIN;
        }
        if (italicCheckBox != null && italicCheckBox.isSelected()) {
            fontStyle |= Font.ITALIC;
        }
        
        g2d.setFont(new Font(fontFamily, fontStyle, fontSize));

        FontMetrics fm = g2d.getFontMetrics();
        int textWidth = fm.stringWidth(text);

        AffineTransform originalTransform = g2d.getTransform();
        g2d.rotate(Math.toRadians(watermarkRotation), watermarkX + textWidth / 2.0, watermarkY);
        g2d.drawString(text, watermarkX, watermarkY + fm.getAscent());
        g2d.setTransform(originalTransform);
    }

    private void addImageWatermark(Graphics2D g2d) {
        try {
            BufferedImage watermark = ImageIO.read(imageWatermarkFile);
            if (watermark == null) {
                showErrorAlert("Watermark Load Error", "Could not read the watermark image file: " + imageWatermarkFile.getName() + ". It may be corrupt or unsupported.");
                logger.warning("ImageIO.read returned null for watermark file: " + imageWatermarkFile.getAbsolutePath());
                return;
            }
            double scale = imageScaleSlider.getValue();
            int width = (int) (watermark.getWidth() * scale);
            int height = (int) (watermark.getHeight() * scale);

            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float) imageOpacitySlider.getValue()));

            AffineTransform originalTransform = g2d.getTransform();
            g2d.rotate(Math.toRadians(watermarkRotation), watermarkX + width / 2.0, watermarkY + height / 2.0);
            g2d.drawImage(watermark, watermarkX, watermarkY, width, height, null);
            g2d.setTransform(originalTransform);

        } catch (IOException e) {
            showErrorAlert("Watermark Load Error", "An I/O error occurred while loading the watermark image.");
            logger.log(Level.SEVERE, "IOException in addImageWatermark for: " + imageWatermarkFile.getAbsolutePath(), e);
        }
    }

    @FXML
    private void handleSelectImageWatermark() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Image Watermark");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg"));
        File selectedFile = fileChooser.showOpenDialog(null);
        if (selectedFile != null) {
            imageWatermarkFile = selectedFile;
            imageWatermarkField.setText(selectedFile.getName());
            updatePreview();
        }
    }
    //</editor-fold>

    //<editor-fold desc="Layout & Positioning">
    private void updatePositionFields() {
        xField.setText(String.valueOf(watermarkX));
        yField.setText(String.valueOf(watermarkY));
    }

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
            if (image == null) {
                showErrorAlert("Image Load Error", "Could not read the image file to calculate position.");
                logger.warning("setPosition could not read image file: " + currentImageFile.getAbsolutePath());
                return;
            }
            int itemWidth = 0;
            int itemHeight = 0;

            if (watermarkMode == WatermarkMode.TEXT) {
                FontMetrics fm = getFontMetrics();
                itemWidth = fm.stringWidth(watermarkTextField.getText());
                itemHeight = fm.getHeight();
            } else if (imageWatermarkFile != null) {
                BufferedImage watermark = ImageIO.read(imageWatermarkFile);
                if (watermark == null) {
                    showErrorAlert("Watermark Load Error", "Could not read the watermark image file to calculate position.");
                    logger.warning("setPosition could not read watermark file: " + imageWatermarkFile.getAbsolutePath());
                    return;
                }
                itemWidth = (int) (watermark.getWidth() * imageScaleSlider.getValue());
                itemHeight = (int) (watermark.getHeight() * imageScaleSlider.getValue());
            }

            if (hPos == 0) watermarkX = 10;
            else if (hPos == 1) watermarkX = (image.getWidth() - itemWidth) / 2;
            else watermarkX = image.getWidth() - itemWidth - 10;

            if (vPos == 0) watermarkY = 0;
            else if (vPos == 1) watermarkY = (image.getHeight() - itemHeight) / 2;
            else watermarkY = image.getHeight() - itemHeight - 10;

            updatePositionFields();
            updatePreview();
        } catch (IOException e) {
            showErrorAlert("Image Processing Error", "An error occurred while setting watermark position.");
            logger.log(Level.SEVERE, "IOException in setPosition", e);
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
    //</editor-fold>

    //<editor-fold desc="Export">
    @FXML
    private void handleSelectOutputDir() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select Output Directory");
        File selectedDirectory = directoryChooser.showDialog(null);
        if (selectedDirectory != null) {
            outputDirectory = selectedDirectory;
            outputDirField.setText(outputDirectory.getAbsolutePath());
        }
    }

    @FXML
    private void handleExport() {
        if (imageFiles.isEmpty() || outputDirectory == null) {
            new Alert(Alert.AlertType.WARNING, "Please import images and select an output directory first.").showAndWait();
            return;
        }

        for (File file : imageFiles) {
            if (outputDirectory.equals(file.getParentFile())) {
                showErrorAlert("Invalid Directory", "Output directory cannot be the same as the source directory to prevent overwriting original files.");
                return;
            }
        }

        ExportTask exportTask = new ExportTask(imageFiles, outputDirectory);

        exportProgressBar.visibleProperty().bind(exportTask.runningProperty());
        exportProgressLabel.visibleProperty().bind(exportTask.runningProperty());
        exportProgressBar.progressProperty().bind(exportTask.progressProperty());
        exportProgressLabel.textProperty().bind(exportTask.messageProperty());
        exportButton.disableProperty().bind(exportTask.runningProperty());

        // Change cursor to wait
        Scene scene = exportButton.getScene();
        exportTask.runningProperty().addListener((obs, wasRunning, isRunning) -> {
            scene.setCursor(isRunning ? Cursor.WAIT : Cursor.DEFAULT);
        });

        exportTask.setOnSucceeded(e -> {
            new Alert(Alert.AlertType.INFORMATION, exportTask.getValue()).showAndWait();
        });

        exportTask.setOnFailed(e -> {
            showErrorAlert("Export Failed", "An unexpected error occurred during the export process.");
        });

        new Thread(exportTask).start();
    }

    private String getOutputFileName(String originalName, String format) {
        String nameWithoutExt = originalName.substring(0, originalName.lastIndexOf('.'));
        String ext = format.toLowerCase();
        String namingConvention = namingConventionBox.getValue();
        String prefixSuffix = prefixSuffixField.getText();

        return switch (namingConvention) {
            case "Prefix" -> prefixSuffix + nameWithoutExt + "." + ext;
            case "Suffix" -> nameWithoutExt + prefixSuffix + "." + ext;
            default -> originalName.substring(0, originalName.lastIndexOf('.')) + "." + ext; // Original
        };
    }

    private boolean saveAsJPEG(BufferedImage image, File file) {
        ImageWriter writer = null;
        try {
            Iterator<ImageWriter> iter = ImageIO.getImageWritersByFormatName("jpeg");
            if (iter.hasNext()) {
                writer = iter.next();
            } else {
                showErrorAlert("Export Error", "No JPEG writer found on this system.");
                logger.severe("No JPEG writer found.");
                return false;
            }

            ImageWriteParam param = writer.getDefaultWriteParam();
            param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            param.setCompressionQuality((float) (qualitySlider.getValue() / 100.0));

            try (ImageOutputStream out = ImageIO.createImageOutputStream(file)) {
                if (out == null) {
                    showErrorAlert("Export Error", "Could not create output stream for file: " + file.getName());
                    logger.severe("ImageIO.createImageOutputStream returned null for: " + file.getAbsolutePath());
                    return false;
                }
                writer.setOutput(out);
                writer.write(null, new IIOImage(image, null, null), param);
            }
            return true;
        } catch (IOException e) {
            showErrorAlert("Export Error", "An error occurred while saving JPEG file " + file.getName() + ": " + e.getMessage());
            logger.log(Level.SEVERE, "IOException in saveAsJPEG for: " + file.getAbsolutePath(), e);
            return false;
        } finally {
            if (writer != null) {
                writer.dispose();
            }
        }
    }
    //</editor-fold>

    //<editor-fold desc="Export Task">
    private class ExportTask extends javafx.concurrent.Task<String> {
        private final List<File> files;
        private final File outputDir;

        public ExportTask(List<File> files, File outputDir) {
            this.files = files;
            this.outputDir = outputDir;
        }

        @Override
        protected String call() throws Exception {
            logger.info("Export task started for " + files.size() + " file(s).");
            int successCount = 0;
            int failCount = 0;
            int total = files.size();

            for (int i = 0; i < total; i++) {
                File file = files.get(i);
                updateProgress(i, total);
                updateMessage(String.format("Processing %d of %d: %s", i + 1, total, file.getName()));

                try {
                    BufferedImage originalImage = ImageIO.read(file);
                    if (originalImage == null) {
                        logger.warning("Skipping file (could not read): " + file.getAbsolutePath());
                        failCount++;
                        continue;
                    }

                    BufferedImage watermarkedImage = addWatermark(originalImage);
                    String format = formatBox.getValue();
                    File outputFile = new File(outputDir, getOutputFileName(file.getName(), format));

                    boolean success;
                    if (format.equals("JPEG")) {
                        success = saveAsJPEG(watermarkedImage, outputFile);
                    } else {
                        success = ImageIO.write(watermarkedImage, "png", outputFile);
                    }

                    if (success) {
                        successCount++;
                    } else {
                        logger.severe("Failed to write output file: " + outputFile.getAbsolutePath());
                        failCount++;
                    }
                } catch (IOException e) {
                    logger.log(Level.SEVERE, "IOException during export for file: " + file.getAbsolutePath(), e);
                    failCount++;
                }
            }

            updateProgress(total, total);
            updateMessage("Finishing up...");
            String resultMessage = String.format("Export complete!\n\nSuccessful: %d\nFailed: %d", successCount, failCount);
            logger.info("Export task finished. " + resultMessage.replace("\n", " "));
            return resultMessage;
        }
    }
    //</editor-fold>

    //<editor-fold desc="Template Management">
    @FXML
    private void handleSaveTemplate() {
        TextInputDialog dialog = new TextInputDialog("My-Template");
        dialog.setTitle("Save Template");
        dialog.setHeaderText("Enter a name for your template:");
        dialog.showAndWait().ifPresent(name -> {
            if (name.isEmpty() || !name.matches("[a-zA-Z0-9_-]+")) {
                showErrorAlert("Invalid Name", "Template name can only contain letters, numbers, hyphens, and underscores.");
                return;
            }

            WatermarkSettings settings = new WatermarkSettings();
            // Populate settings from UI controls
            settings.text = watermarkTextField.getText();
            settings.color = colorPicker.getValue().toString();
            settings.opacity = opacitySlider.getValue();
            settings.imageWatermarkPath = (imageWatermarkFile != null) ? imageWatermarkFile.getAbsolutePath() : null;
            settings.imageOpacity = imageOpacitySlider.getValue();
            settings.imageScale = imageScaleSlider.getValue();
            settings.x = watermarkX;
            settings.y = watermarkY;
            settings.rotation = watermarkRotation;

            try {
                if (!Files.exists(templatesDir)) {
                    Files.createDirectories(templatesDir);
                }
                Gson gson = new GsonBuilder().setPrettyPrinting().create();
                File templateFile = templatesDir.resolve(name + ".json").toFile();
                try (FileWriter writer = new FileWriter(templateFile)) {
                    gson.toJson(settings, writer);
                }
                logger.info("Template saved: " + templateFile.getAbsolutePath());
                loadTemplatesMenu(); // Refresh menu
                new Alert(Alert.AlertType.INFORMATION, "Template '" + name + "' saved successfully.").showAndWait();
            } catch (IOException e) {
                showErrorAlert("Save Error", "Could not save template '" + name + "': " + e.getMessage());
                logger.log(Level.SEVERE, "Error saving template: " + name, e);
            }
        });
    }

    @FXML
    private void handleLoadTemplate() {
        try {
            if (!Files.exists(templatesDir)) {
                Files.createDirectories(templatesDir);
            }
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Load Template");
            fileChooser.setInitialDirectory(templatesDir.toFile());
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON files (*.json)", "*.json"));
            File file = fileChooser.showOpenDialog(null);
            if (file != null) {
                loadTemplateFromFile(file);
            }
        } catch (IOException e) {
            showErrorAlert("Load Error", "Could not access the templates directory: " + e.getMessage());
            logger.log(Level.SEVERE, "Could not access templates directory: " + templatesDir, e);
        }
    }

    private void loadTemplateFromFile(File file) {
        logger.info("Loading template from: " + file.getAbsolutePath());
        try (FileReader reader = new FileReader(file)) {
            Gson gson = new Gson();
            WatermarkSettings settings = gson.fromJson(reader, WatermarkSettings.class);
            if (settings == null) {
                showErrorAlert("Load Error", "The template file is empty or corrupt: " + file.getName());
                logger.warning("Template file is empty or corrupt: " + file.getAbsolutePath());
                return;
            }
            applySettings(settings);
        } catch (Exception e) {
            showErrorAlert("Load Error", "Failed to load or apply template '" + file.getName() + "'. The file may be corrupt or incompatible. Error: " + e.getMessage());
            logger.log(Level.SEVERE, "Failed to load or apply template: " + file.getAbsolutePath(), e);
        }
    }

    private void applySettings(WatermarkSettings settings) {
        watermarkTextField.setText(settings.text);
        colorPicker.setValue(Color.valueOf(settings.color));
        opacitySlider.setValue(settings.opacity);
        if (settings.imageWatermarkPath != null) {
            imageWatermarkFile = new File(settings.imageWatermarkPath);
            imageWatermarkField.setText(imageWatermarkFile.getName());
        }
        imageOpacitySlider.setValue(settings.imageOpacity);
        imageScaleSlider.setValue(settings.imageScale);
        watermarkX = settings.x;
        watermarkY = settings.y;
        watermarkRotation = settings.rotation;
        rotationSlider.setValue(watermarkRotation);
        updatePositionFields();
        updatePreview();
        logger.info("Applied settings from template.");
    }

    private void loadTemplatesMenu() {
        myTemplatesMenu.getItems().clear();
        if (!Files.exists(templatesDir) || !Files.isDirectory(templatesDir)) {
            // Don't show an error if the directory just doesn't exist yet.
            return;
        }
        try (Stream<Path> files = Files.list(templatesDir)) {
            files.filter(f -> f.toString().endsWith(".json")).forEach(f -> {
                String name = f.getFileName().toString().replace(".json", "");
                MenuItem item = new MenuItem(name);
                item.setOnAction(e -> loadTemplateFromFile(f.toFile()));
                myTemplatesMenu.getItems().add(item);
            });
        } catch (IOException e) {
            showErrorAlert("Menu Error", "Could not load templates for the menu: " + e.getMessage());
            logger.log(Level.WARNING, "Could not load templates for menu.", e);
        }
    }

    public static class WatermarkSettings {
        public String text;
        public String color;
        public double opacity;
        public String imageWatermarkPath;
        public double imageOpacity;
        public double imageScale;
        public int x;
        public int y;
        public double rotation;
    }
    //</editor-fold>
}
