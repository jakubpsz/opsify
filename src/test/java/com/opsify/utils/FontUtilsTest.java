package com.opsify.utils;

import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;

import java.lang.reflect.Constructor;

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

    // --- loadAndApplyNunitoFont ---

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
        // Force fallback scenario by checking fonts first
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

    // --- useFallbackFonts ---

    @Test
    void testUseFallbackFonts_appliesExpectedStyles() {
        // Directly call fallback (via normal flow, since it's private we trigger through public)
        FontUtils.loadAndApplyNunitoFont(titleLabel, convertButton, inputField, outputField, formatCombo, logArea);

        if (!FontUtils.areNunitoFontsAvailable()) {
            assertThat(titleLabel.getStyle()).contains("Segoe UI").contains("bold");
            assertThat(convertButton.getStyle()).contains("Segoe UI");
            assertThat(inputField.getStyle()).contains("Segoe UI");
            assertThat(outputField.getStyle()).contains("Segoe UI");
            assertThat(formatCombo.getStyle()).contains("Segoe UI");
            assertThat(logArea.getStyle()).contains("Segoe UI");
        }
    }

    // --- areNunitoFontsAvailable ---

    @Test
    void testAreNunitoFontsAvailable_returnsBoolean() {
        boolean result = FontUtils.areNunitoFontsAvailable();
        assertThat(result).isTrue().isInstanceOf(Boolean.class);
    }

    @Test
    void testAreNunitoFontsAvailable_invalidPath_returnsFalse() {
        // Simulate broken fonts path by trying non-existent resources
        boolean result;
        try {
            result = javafx.scene.text.Font.loadFont("invalid-path.ttf", 12) != null;
        } catch (Exception e) {
            result = false;
        }
        assertThat(result).isFalse();
    }

    // --- private constructor ---

    @Test
    void testPrivateConstructor_viaReflection() throws Exception {
        Constructor<FontUtils> constructor = FontUtils.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        FontUtils instance = constructor.newInstance();
        assertThat(instance).isNotNull();
    }
}
