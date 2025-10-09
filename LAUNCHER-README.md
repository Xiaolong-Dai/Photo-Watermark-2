# Photo Watermark Tool - Launcher

## Overview

This project includes a Windows batch file (`launcher.bat`) to easily run the Photo Watermark Tool application.

## Prerequisites

- Java 17 or higher must be installed on your system
- Java must be available in your system PATH

## How to Use

1. Double-click the `launcher.bat` file to run the application
2. The Photo Watermark Tool window should appear shortly
3. You can also run the batch file from the command line:
   ```cmd
   launcher.bat
   ```

## How It Works

The launcher script:

1. Checks if Java is available in your PATH
2. Looks for the required JAR file (`image-watermarker/target/image-watermarker-1.0-SNAPSHOT.jar`)
3. Locates the JavaFX dependencies in the `lib` directory
4. Launches the application with the proper module path settings for JavaFX

## Troubleshooting

### If the application doesn't start:

1. Make sure Java 17+ is installed and available in your PATH:
   ```cmd
   java -version
   ```

2. Verify that the JAR file exists in the `image-watermarker\target` directory

3. If you get JavaFX module errors, ensure all required dependencies are in the `lib` directory

### Building the JAR:

If the JAR file doesn't exist, you need to build it first using Maven:

```cmd
cd image-watermarker
mvn clean package
```

This will create the JAR file in the `target` directory along with all required dependencies in the `target\lib` directory.