@echo off
echo Building Photo Watermark Application...

REM Build the JAR file
mvn clean package -DskipTests

if %ERRORLEVEL% NEQ 0 (
    echo Maven build failed!
    pause
    exit /b 1
)

echo.
echo JAR file created successfully!

REM Check if jpackage is available
echo.
echo Checking for jpackage...

"%JAVA_HOME%\bin\jpackage" --version >nul 2>&1
if %ERRORLEVEL% EQU 0 (
    echo jpackage found. Creating Windows installer...
    
    REM Create Windows executable
    "%JAVA_HOME%\bin\jpackage" ^
        --name "Photo Watermark" ^
        --app-version 1.0.0 ^
        --vendor "My Watermark Inc." ^
        --description "A desktop application for adding watermarks to images." ^
        --copyright "Copyright (c) 2025 My Watermark Inc. All rights reserved." ^
        --dest target/dist ^
        --input target ^
        --main-jar image-watermarker-1.0-SNAPSHOT.jar ^
        --main-class com.mywatermark.App ^
        --type exe ^
        --win-dir-chooser ^
        --win-menu ^
        --win-shortcut
    
    if %ERRORLEVEL% EQU 0 (
        echo Windows installer created successfully in target/dist/
    ) else (
        echo jpackage execution failed. Make sure WiX Toolset is installed for Windows installer creation.
        echo The JAR file is available in target/ for manual execution.
    )
) else (
    echo jpackage not found. The JAR file is available in target/ for manual execution.
    echo To create a Windows installer, make sure you have JDK 14+ with jpackage included.
)

echo.
echo Build process completed.
pause