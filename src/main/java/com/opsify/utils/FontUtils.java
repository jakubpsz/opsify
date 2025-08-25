package com.opsify.utils;

import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.text.Font;
import lombok.extern.slf4j.Slf4j;

/**
 * Utility class for handling font loading and application in JavaFX
 */
@Slf4j
public class FontUtils {

    public static final String FONTS_NUNITO_REGULAR_TTF = "/fonts/Nunito-Regular.ttf";
    public static final String FONTS_NUNITO_BOLD_TTF = "/fonts/Nunito-Bold.ttf";

    public static void loadAndApplyNunitoFont(Label titleLabel, Button convertButton,
                                              TextField inputField, TextField outputField,
                                              ComboBox<String> formatCombo, TextArea logArea) {
        if (titleLabel == null || convertButton == null || inputField == null
                || outputField == null || formatCombo == null || logArea == null) {
            throw new IllegalArgumentException("UI components cannot be null when applying fonts");
        }

        try {
            Font nunitoRegular = Font.loadFont(FontUtils.class.getResourceAsStream(FONTS_NUNITO_REGULAR_TTF), 12);
            Font nunitoBold = Font.loadFont(FontUtils.class.getResourceAsStream(FONTS_NUNITO_BOLD_TTF), 12);

            if (nunitoRegular != null && nunitoBold != null) {
                log.info("Nunito font loaded successfully");
                applyNunitoStyles(titleLabel, convertButton, inputField, outputField, formatCombo, logArea);
            } else {
                log.warn("Nunito font could not be loaded, using system fallback fonts");
                useFallbackFonts(titleLabel, convertButton, inputField, outputField, formatCombo, logArea);
            }
        } catch (Exception e) {
            log.error("Error loading Nunito font: {}", e.getMessage());
            useFallbackFonts(titleLabel, convertButton, inputField, outputField, formatCombo, logArea);
        }
    }

    // Add this method to handle buttons in the home page
    public static void loadAndApplyNunitoFont(Label titleLabel, Button... buttons) {
        if (titleLabel == null || buttons == null) {
            throw new IllegalArgumentException("UI components cannot be null when applying fonts");
        }

        try {
            Font nunitoRegular = Font.loadFont(FontUtils.class.getResourceAsStream(FONTS_NUNITO_REGULAR_TTF), 12);
            Font nunitoBold = Font.loadFont(FontUtils.class.getResourceAsStream(FONTS_NUNITO_BOLD_TTF), 12);

            if (nunitoRegular != null && nunitoBold != null) {
                log.info("Nunito font loaded successfully");
                applyNunitoStyles(titleLabel, buttons);
            } else {
                log.warn("Nunito font could not be loaded, using system fallback fonts");
                useFallbackFonts(titleLabel, buttons);
            }
        } catch (Exception e) {
            log.error("Error loading Nunito font: {}", e.getMessage());
            useFallbackFonts(titleLabel, buttons);
        }
    }

    // New method for PDF joiner controller with ListView
    public static void loadAndApplyNunitoFont(Label titleLabel, Button joinButton,
                                              TextField outputDirField, TextField outputFileNameField,
                                              ListView<String> filesListView, TextArea logArea) {
        if (titleLabel == null || joinButton == null || outputDirField == null
                || outputFileNameField == null || filesListView == null || logArea == null) {
            throw new IllegalArgumentException("UI components cannot be null when applying fonts");
        }

        try {
            Font nunitoRegular = Font.loadFont(FontUtils.class.getResourceAsStream(FONTS_NUNITO_REGULAR_TTF), 12);
            Font nunitoBold = Font.loadFont(FontUtils.class.getResourceAsStream(FONTS_NUNITO_BOLD_TTF), 12);

            if (nunitoRegular != null && nunitoBold != null) {
                log.info("Nunito font loaded successfully");
                applyNunitoStyles(titleLabel, joinButton, outputDirField, outputFileNameField, filesListView, logArea);
            } else {
                log.warn("Nunito font could not be loaded, using system fallback fonts");
                useFallbackFonts(titleLabel, joinButton, outputDirField, outputFileNameField, filesListView, logArea);
            }
        } catch (Exception e) {
            log.error("Error loading Nunito font: {}", e.getMessage());
            useFallbackFonts(titleLabel, joinButton, outputDirField, outputFileNameField, filesListView, logArea);
        }
    }

    private static void applyNunitoStyles(Label titleLabel, Button... buttons) {
        titleLabel.setStyle("-fx-font-family: 'Nunito'; -fx-font-weight: bold; -fx-font-size: 24px;");
        for (Button button : buttons) {
            button.setStyle("-fx-font-family: 'Nunito'; -fx-font-weight: bold;");
        }
    }

    private static void useFallbackFonts(Label titleLabel, Button... buttons) {
        titleLabel.setStyle("-fx-font-family: 'Segoe UI', sans-serif; -fx-font-weight: bold; -fx-font-size: 24px;");
        for (Button button : buttons) {
            button.setStyle("-fx-font-family: 'Segoe UI', sans-serif; -fx-font-weight: bold;");
        }
    }


    /**
     * Applies Nunito font styles to UI components
     */
    private static void applyNunitoStyles(Label titleLabel, Button convertButton,
                                          TextField inputField, TextField outputField,
                                          ComboBox<String> formatCombo, TextArea logArea) {
        titleLabel.setStyle("-fx-font-family: 'Nunito'; -fx-font-weight: bold; -fx-font-size: 24px;");
        convertButton.setStyle("-fx-font-family: 'Nunito'; -fx-font-weight: bold;");

        // Apply to other text elements
        String nunitoStyle = "-fx-font-family: 'Nunito';";
        inputField.setStyle(nunitoStyle);
        outputField.setStyle(nunitoStyle);
        formatCombo.setStyle(nunitoStyle);
        logArea.setStyle(nunitoStyle);
    }

    /**
     * Applies Nunito font styles to PDF joiner components
     */
    private static void applyNunitoStyles(Label titleLabel, Button joinButton,
                                          TextField outputDirField, TextField outputFileNameField,
                                          ListView<String> filesListView, TextArea logArea) {
        titleLabel.setStyle("-fx-font-family: 'Nunito'; -fx-font-weight: bold; -fx-font-size: 24px;");
        joinButton.setStyle("-fx-font-family: 'Nunito'; -fx-font-weight: bold;");

        // Apply to other text elements
        String nunitoStyle = "-fx-font-family: 'Nunito';";
        outputDirField.setStyle(nunitoStyle);
        outputFileNameField.setStyle(nunitoStyle);
        filesListView.setStyle(nunitoStyle);
        logArea.setStyle(nunitoStyle);
    }

    /**
     * Applies fallback system fonts to UI components
     */
    private static void useFallbackFonts(Label titleLabel, Button convertButton,
                                         TextField inputField, TextField outputField,
                                         ComboBox<String> formatCombo, TextArea logArea) {
        String fallbackStyle = "-fx-font-family: 'Segoe UI', sans-serif;";
        titleLabel.setStyle("-fx-font-family: 'Segoe UI', sans-serif; -fx-font-weight: bold; -fx-font-size: 24px;");
        inputField.setStyle(fallbackStyle);
        outputField.setStyle(fallbackStyle);
        formatCombo.setStyle(fallbackStyle);
        convertButton.setStyle(fallbackStyle + "-fx-font-weight: bold;");
        logArea.setStyle(fallbackStyle);
    }

    /**
     * Applies fallback system fonts to PDF joiner components
     */
    private static void useFallbackFonts(Label titleLabel, Button joinButton,
                                         TextField outputDirField, TextField outputFileNameField,
                                         ListView<String> filesListView, TextArea logArea) {
        String fallbackStyle = "-fx-font-family: 'Segoe UI', sans-serif;";
        titleLabel.setStyle("-fx-font-family: 'Segoe UI', sans-serif; -fx-font-weight: bold; -fx-font-size: 24px;");
        outputDirField.setStyle(fallbackStyle);
        outputFileNameField.setStyle(fallbackStyle);
        filesListView.setStyle(fallbackStyle);
        joinButton.setStyle(fallbackStyle + "-fx-font-weight: bold;");
        logArea.setStyle(fallbackStyle);
    }

    /**
     * Loads fonts without applying them to check if they're available
     */
    public static boolean areNunitoFontsAvailable() {
        try {
            Font regular = Font.loadFont(FontUtils.class.getResourceAsStream("/fonts/Nunito-Regular.ttf"), 12);
            Font bold = Font.loadFont(FontUtils.class.getResourceAsStream("/fonts/Nunito-Bold.ttf"), 12);
            return regular != null && bold != null;
        } catch (Exception e) {
            log.warn("Nunito fonts not available: {}", e.getMessage());
            return false;
        }
    }
}