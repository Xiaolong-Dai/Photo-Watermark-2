# Photo Watermark Tool

A Java desktop application for adding watermarks to images with a user-friendly interface. This tool allows users to add both text and image watermarks to their photos with customizable properties and batch processing capabilities.

## ✨ Features

- **Multiple Image Import**: Import single images, multiple images, or entire folders
- **Text Watermarks**: Add customizable text watermarks with options for font, color, size, and transparency
- **Image Watermarks**: Use images (like logos) as watermarks with transparency support
- **Flexible Positioning**: Place watermarks using nine-grid presets or drag them to any position
- **Real-time Preview**: See changes as you adjust watermark properties
- **Batch Processing**: Apply watermarks to multiple images at once
- **Export Options**: Save to different formats (JPEG/PNG) with custom naming rules
- **Template System**: Save and reload watermark configurations
- **Quality Control**: Adjust JPEG quality settings when exporting

## 🚀 Installation & Usage

### Prerequisites

- Java 17 or higher
- Maven 3.6.0 or higher

### Building the Application

1. Clone the repository:
   ```bash
   git clone <repository-url>
   cd image-watermarker
   ```

2. Build the project:
   ```bash
   mvn clean package
   ```

3. Run the application:
   ```bash
   java -jar target/image-watermarker-1.0-SNAPSHOT.jar
   ```

### Creating a Windows Executable

The application can be packaged as a Windows executable using jpackage:

```bash
mvn package
```

This will create an installer in the `target/dist` directory if you have the WiX toolset installed.

### Alternative: Running the JAR directly

If you don't have the WiX toolset installed or prefer to run the application directly:

```bash
java -jar target/image-watermarker-1.0-SNAPSHOT.jar
```

This will run the application from the JAR file without needing to create an installer.

## 🛠️ Development

### Project Structure

The application follows the MVC (Model-View-Controller) pattern using JavaFX:

- **Model**: Represents application data and business logic
- **View**: User interface defined in FXML files
- **Controller**: Handles user input and updates the model

### Modules

- **Image Import Module**: Handles loading single/multiple images and folders
- **Watermark Processing Module**: Core logic for applying text and image watermarks
- **Watermark Layout & Preview Module**: UI for controlling watermark appearance and real-time preview
- **Image Export Module**: Saves watermarked images with configurable options
- **Template Management Module**: Save and load watermark configurations

### Technologies Used

- **Java 17+**: Core language
- **JavaFX 21**: GUI framework
- **Maven**: Build automation
- **Gson**: JSON serialization for templates
- **JUnit 5**: Testing framework

## 📋 Development Roadmap

The project follows a structured 10-stage development process:

1. **Environment Setup**: JDK, Maven, and IDE configuration
2. **Project Initialization**: Maven project structure and architecture design
3. **Image Import Module**: Single/multiple image import with thumbnail display
4. **Text Watermark Core**: Text watermark functionality (content, font, color, transparency)
5. **Layout & Interaction**: Watermark positioning (nine-grid/drag), rotation, real-time preview
6. **Image Export**: Output directory, format selection, naming rules, quality controls
7. **Image Watermarks**: Support for logo/transparent PNG watermarks
8. **Template System**: Save/load watermark configurations as JSON
9. **Packaging**: Create Windows executable with jpackage
10. **Optimization**: Performance, error handling, UI improvements

## 📦 Download

You can download the latest Windows executable from the [Releases](https://github.com/Xiaolong-Dai/Photo-Watermark-2/releases) section of this repository.

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Make your changes
4. Commit your changes following the [Conventional Commits](https://www.conventionalcommits.org/) specification:
   ```bash
   git commit -m "feat(watermark): add text watermark transparency support"
   ```
5. Push to the branch (`git push origin feature/amazing-feature`)
6. Open a Pull Request

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 📞 Support

For support, please open an issue in the GitHub repository.