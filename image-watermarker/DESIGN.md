# Project Architecture and Design

This document outlines the architecture and design of the Photo Watermark Tool.

## 1. Architecture Overview

The application follows a Model-View-Controller (MVC) pattern, facilitated by the JavaFX framework.

- **Model:** Represents the application's data and business logic (e.g., image data, watermark settings, template information).
- **View:** The user interface, defined in FXML files (`.fxml`). It displays the data from the model and sends user actions to the controller.
- **Controller:** Handles user input from the View, updates the Model, and refreshes the View with new data.

## 2. Functional Modules

The project is divided into the following functional modules:

- **Image Import Module:** Responsible for loading single images, multiple images, or images from a folder. It will manage the list of images to be watermarked.

- **Watermark Processing Module:** Contains the core logic for applying watermarks. This includes both text and image watermarks. It will handle rendering the watermark onto the source image.

- **Watermark Layout and Preview Module:** Provides the UI for users to control the watermark's appearance and position. This includes setting text content, font, color, transparency, rotation, and position. It will also display a real-time preview of the watermarked image.

- **Image Export Module:** Manages the process of saving the final watermarked images. It will provide options for output directory, file format (JPEG/PNG), naming conventions, and quality settings.

- **Template Management Module:** Allows users to save and load watermark configurations as templates. This will involve serializing and deserializing watermark settings to and from a file (e.g., JSON).

- **Packaging Module:** Not a code module, but a process defined in the `pom.xml` and using tools like `jpackage` to create a distributable Windows application.

## 3. Main Application Flow

The user interacts with the application following this general workflow:

1.  **Import:** The user imports one or more images into the application.
2.  **Configure:** The user designs the watermark by:
    - Choosing between a text or image watermark.
    - Setting the content (text or image file).
    - Adjusting parameters like font, size, color, transparency, and rotation.
    - Positioning the watermark using presets (e.g., nine-square grid) or manual dragging.
3.  **Preview:** The application shows a real-time preview of the watermark applied to the currently selected image.
4.  **Export:** The user configures export settings (output folder, file format, etc.) and starts the batch process to apply the watermark to all imported images and save them to the specified location.
