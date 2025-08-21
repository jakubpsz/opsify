package com.opsify.utils;

import javafx.scene.control.*;
import javafx.scene.text.Font;
import lombok.extern.slf4j.Slf4j;

/**
 * Utility class for handling font loading and application in JavaFX
 */
@Slf4j
public class FontUtils {

    /**
     * Loads Nunito font and applies it to UI components
     */
    public static void loadAndApplyNunitoFont(Label titleLabel, Button convertButton,
                                              TextField inputField, TextField outputField,
                                              ComboBox<String> formatCombo, TextArea logArea) {
        try {
            // Try to load Nunito font from resources
            Font nunitoRegular = Font.loadFont(FontUtils.class.getResourceAsStream("/fonts/Nunito-Regular.ttf"), 12);
            Font nunitoBold = Font.loadFont(FontUtils.class.getResourceAsStream("/fonts/Nunito-Bold.ttf"), 12);

            if (nunitoRegular != null && nunitoBold != null) {
                log.info("Nunito font loaded successfully");

                // Apply fonts to specific elements
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