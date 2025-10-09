@echo off
REM Photo Watermark Application Launcher
REM This script launches the JavaFX application with proper module path settings

setlocal

REM Get the directory where this script is located
set SCRIPT_DIR=%~dp0

REM Set the path to the JAR file and the lib directory
set JAR_PATH=%SCRIPT_DIR%image-watermarker\target\image-watermarker-1.0-SNAPSHOT.jar
set LIB_PATH=%SCRIPT_DIR%image-watermarker\target\lib

REM Check if required files exist
if not exist "%JAR_PATH%" (
    echo Error: JAR file not found at "%JAR_PATH%"
    echo Please make sure the JAR has been built with Maven.
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

REM Launch the application with proper module path for JavaFX
echo Launching Photo Watermark Application...
echo.
echo Starting application - the main window should appear shortly.
echo If you see an error, please check that Java 17+ is installed.
echo.
java --module-path "%LIB_PATH%" --add-modules javafx.controls,javafx.fxml,javafx.swing --add-opens javafx.controls/javafx.scene.control.skin=ALL-UNNAMED --add-opens javafx.graphics/javafx.scene=ALL-UNNAMED --add-opens javafx.base/com.sun.javafx.event=ALL-UNNAMED -jar "%JAR_PATH%"

REM If the above fails, try with full classpath as fallback
if errorlevel 1 (
    echo.
    echo Launch failed. Attempting fallback with classpath...
    java -cp "%JAR_PATH%;%LIB_PATH%/*" com.mywatermark.App
)

echo.
echo Application closed. Press any key to exit.
pause >nul

endlocal