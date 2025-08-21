# Opsify

![Audio Converter Logo](docs/logo.png)

JavaFX desktop application for batch audio conversion using JavaCV (bundled FFmpeg). Includes progress reporting and logging.

Features
- Convert a single file or whole directories recursively
- Preserve original filenames and directory structure
- Choose target format (mp3, wav, ogg, m4a, flac, aac)
- Progress bar and log area in UI
- JavaCV embedded FFmpeg, no external binary required

Tech stack
- Java 21
- JavaFX 21 (controls, FXML)
- JavaCV 1.5.9
- SLF4J + Logback for logging
- JUnit 5 + AssertJ + Mockito for tests
- Maven

Project structure
- src/main/java/com/opsify/app/AudioConverterApp.java: JavaFX entry point
- src/main/java/com/opsify/controller/MainController.java: UI controller
- src/main/java/com/opsify/service/FfmpegAudioConverter.java: conversion service
- src/main/java/com/opsify/service/ConversionListener.java: progress listener interface
- src/main/java/com/opsify/util/PathAudioUtil.java: path utilities
- src/main/java/com/opsify/util/Constants.java: common constants
- src/main/resources/fxml/main.fxml: UI
- src/main/resources/css/style.css: styling

Build
- mvn -q -DskipTests package

Run
- mvn javafx:run

Test
- mvn -q test

Notes
- Ensure your system can initialize a JavaFX platform during tests; the tests bootstrap Platform.startup as needed.
- JaCoCo coverage rules are configured for util/service packages.
