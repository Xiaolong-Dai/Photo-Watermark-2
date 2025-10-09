@echo off
REM Script to run the Photo Watermark Tool from the fat JAR
REM This script includes necessary JavaFX options to run the application

setlocal

REM Get the directory where this script is located
set SCRIPT_DIR=%~dp0

REM Set the path to the fat JAR file
set JAR_PATH=%SCRIPT_DIR%image-watermarker\target\image-watermarker-1.0-SNAPSHOT.jar

REM Check if required file exists
if not exist "%JAR_PATH%" (
    echo Error: JAR file not found at "%JAR_PATH%"
    echo Please make sure the JAR has been built with Maven using: mvn clean package
    pause
    exit /b 1
)

REM Check if Java is available
java -version >nul 2>&1
if errorlevel 1 (
    echo Error: Java is not found in your PATH.
    echo Please install Java 17 or higher and ensure it's in your PATH.
    pause
    exit /b 1
)

REM Try to run with JavaFX options (in case JavaFX isn't bundled properly in fat JAR)
echo Attempting to run Photo Watermark Application with JavaFX options...
echo.

REM Note: This will only work if you have JavaFX SDK separately installed
java --module-path "%SCRIPT_DIR%image-watermarker\target\lib" --add-modules javafx.controls,javafx.fxml,javafx.swing --add-opens javafx.controls/javafx.scene.control.skin=ALL-UNNAMED --add-opens javafx.graphics/javafx.scene=ALL-UNNAMED --add-opens javafx.base/com.sun.javafx.event=ALL-UNNAMED -cp "%JAR_PATH%" com.mywatermark.App

REM If the above fails, try running directly from fat JAR (this might work if JavaFX is properly embedded)
if errorlevel 1 (
    echo.
    echo JavaFX module path approach failed. Attempting fallback...
    echo.
    java -cp "%JAR_PATH%" com.mywatermark.App
)

if errorlevel 1 (
    echo.
    echo Application failed to start. You may need to ensure JavaFX libraries
    echo are available on your system or run using Maven instead:
    echo.
    echo cd image-watermarker
    echo mvn javafx:run
    echo.
    pause
)

endlocal