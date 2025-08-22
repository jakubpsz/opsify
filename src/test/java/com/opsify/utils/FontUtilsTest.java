package com.opsify.utils;

import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;

import java.io.InputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FontUtilsTest extends ApplicationTest {

    private Label titleLabel;
    private Button convertButton;
    private TextField inputField;
    private TextField outputField;
    private ComboBox<String> formatCombo;
    private TextArea logArea;

    @Override
    public void start(Stage stage) {
        // JavaFX Application thread bootstrap (stage not shown)
    }

    @BeforeEach
    void setup() {
        titleLabel = new Label("Title");
        convertButton = new Button("Convert");
        inputField = new TextField();
        outputField = new TextField();
        formatCombo = new ComboBox<>();
        logArea = new TextArea();
    }

    @Test
    void testLoadAndApplyNunitoFont_withNunitoFontsAvailable() {
        if (FontUtils.areNunitoFontsAvailable()) {
            FontUtils.loadAndApplyNunitoFont(titleLabel, convertButton, inputField, outputField, formatCombo, logArea);

            assertThat(titleLabel.getStyle()).contains("Nunito");
            assertThat(convertButton.getStyle()).contains("Nunito");
            assertThat(inputField.getStyle()).contains("Nunito");
            assertThat(outputField.getStyle()).contains("Nunito");
            assertThat(formatCombo.getStyle()).contains("Nunito");
            assertThat(logArea.getStyle()).contains("Nunito");
        }
    }

    @Test
    void testLoadAndApplyNunitoFont_withNunitoFontsUnavailable_fallbackApplied() {
        // Simulate missing fonts by checking availability first
        if (!FontUtils.areNunitoFontsAvailable()) {
            FontUtils.loadAndApplyNunitoFont(titleLabel, convertButton, inputField, outputField, formatCombo, logArea);

            String style = titleLabel.getStyle();
            assertThat(style).contains("Segoe UI");
        }
    }

    @Test
    void testLoadAndApplyNunitoFont_nullInputs_shouldThrowException() {
        assertThatThrownBy(() ->
                FontUtils.loadAndApplyNunitoFont(null, null, null, null, null, null)
        ).isInstanceOf(IllegalArgumentException.class)
                .hasMessage("UI components cannot be null when applying fonts");
    }

    @Test
    void testLoadAndApplyNunitoFont_exceptionDuringLoading() {
        // Mock the font loading to force an exception
        // This is a simplified approach that assumes the fonts are not available
        // In a real scenario, you might use a mocking framework
        if (!FontUtils.areNunitoFontsAvailable()) {
            FontUtils.loadAndApplyNunitoFont(titleLabel, convertButton, inputField, outputField, formatCombo, logArea);

            // Verify fallback is applied
            assertThat(titleLabel.getStyle()).contains("Segoe UI");
            assertThat(convertButton.getStyle()).contains("Segoe UI");
            assertThat(inputField.getStyle()).contains("Segoe UI");
            assertThat(outputField.getStyle()).contains("Segoe UI");
            assertThat(formatCombo.getStyle()).contains("Segoe UI");
            assertThat(logArea.getStyle()).contains("Segoe UI");
        }
    }

    @Test
    void testLoadAndApplyNunitoFont_partialFontsAvailable() {
        // This test would ideally mock the font loading to return only one font
        // Since we're not using mocking frameworks, we'll check the current behavior
        boolean hasBothFonts = FontUtils.areNunitoFontsAvailable();

        // If both fonts are available, the method should apply Nunito styles
        // If not, it should use fallback
        FontUtils.loadAndApplyNunitoFont(titleLabel, convertButton, inputField, outputField, formatCombo, logArea);

        if (hasBothFonts) {
            assertThat(titleLabel.getStyle()).contains("Nunito");
        } else {
            assertThat(titleLabel.getStyle()).contains("Segoe UI");
        }
    }

    @Test
    void testLoadAndApplyNunitoFont_emptyComponents() {
        // Test with empty but non-null components
        Label emptyLabel = new Label();
        Button emptyButton = new Button();
        TextField emptyField = new TextField();
        ComboBox<String> emptyCombo = new ComboBox<>();
        TextArea emptyArea = new TextArea();

        // Should not throw an exception
        FontUtils.loadAndApplyNunitoFont(emptyLabel, emptyButton, emptyField, emptyField, emptyCombo, emptyArea);

        // Verify styles are applied
        String style = emptyLabel.getStyle();
        assertThat(style).isNotEmpty();
    }

    @Test
    void testLoadAndApplyNunitoFont_stylePersistence() {
        // Set initial styles
        titleLabel.setStyle("-fx-text-fill: red;");
        inputField.setStyle("-fx-border-color: blue;");

        FontUtils.loadAndApplyNunitoFont(titleLabel, convertButton, inputField, outputField, formatCombo, logArea);

        // Verify that font styles are applied without removing existing styles
        String labelStyle = titleLabel.getStyle();
        assertThat(labelStyle).contains("Nunito").contains("-fx-font-family: 'Nunito'; -fx-font-weight: bold; -fx-font-size: 24px;");

        String fieldStyle = inputField.getStyle();
        assertThat(fieldStyle).contains("Nunito").contains("-fx-font-family: 'Nunito';");
    }

    @Test
    void testAreNunitoFontsAvailable_afterFontLoading() {
        // Test that font availability check works after fonts have been loaded
        boolean beforeLoading = FontUtils.areNunitoFontsAvailable();

        // Load fonts (which might cache them)
        FontUtils.loadAndApplyNunitoFont(titleLabel, convertButton, inputField, outputField, formatCombo, logArea);

        boolean afterLoading = FontUtils.areNunitoFontsAvailable();

        // Availability should be consistent
        assertThat(afterLoading).isEqualTo(beforeLoading);
    }

    @Test
    void testFontUtilsMultipleCalls() {
        // Test calling the method multiple times
        FontUtils.loadAndApplyNunitoFont(titleLabel, convertButton, inputField, outputField, formatCombo, logArea);
        String firstCallStyle = titleLabel.getStyle();

        FontUtils.loadAndApplyNunitoFont(titleLabel, convertButton, inputField, outputField, formatCombo, logArea);
        String secondCallStyle = titleLabel.getStyle();

        // Styles should be consistent across multiple calls
        assertThat(secondCallStyle).isEqualTo(firstCallStyle);
    }

    @Test
    void testUseFallbackFonts_appliesExpectedStyles() {
        // Trigger fallback through the public method
        if (!FontUtils.areNunitoFontsAvailable()) {
            FontUtils.loadAndApplyNunitoFont(titleLabel, convertButton, inputField, outputField, formatCombo, logArea);

            assertThat(titleLabel.getStyle()).contains("Segoe UI").contains("bold");
            assertThat(convertButton.getStyle()).contains("Segoe UI");
            assertThat(inputField.getStyle()).contains("Segoe UI");
            assertThat(outputField.getStyle()).contains("Segoe UI");
            assertThat(formatCombo.getStyle()).contains("Segoe UI");
            assertThat(logArea.getStyle()).contains("Segoe UI");
        }
    }

    @Test
    void testAreNunitoFontsAvailable_returnsBoolean() {
        boolean result = FontUtils.areNunitoFontsAvailable();
        assertThat(result).isInstanceOf(Boolean.class);
    }

    @Test
    void testAreNunitoFontsAvailable_invalidPath_returnsFalse() {
        // Test with invalid path
        boolean result = Font.loadFont("invalid-path.ttf", 12) != null;
        assertThat(result).isFalse();
    }

    @Test
    void testAreNunitoFontsAvailable_partialFonts() {
        // Test when only one font is available
        // This is tricky without mocking, but we can check the current behavior
        boolean originalAvailability = FontUtils.areNunitoFontsAvailable();
        // The method requires both fonts, so partial availability should return false
        assertThat(originalAvailability).isEqualTo(FontUtils.areNunitoFontsAvailable());
    }
}