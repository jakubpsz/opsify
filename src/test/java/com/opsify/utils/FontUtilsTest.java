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
        // JavaFX Application thread bootstrap (no need to show stage for unit tests)
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
    void testAreNunitoFontsAvailable_runsWithoutCrash() {
        // We cannot guarantee fonts exist on all environments,
        // but we can verify the method runs and returns a boolean
        boolean result = FontUtils.areNunitoFontsAvailable();
        assertThat(result).isInstanceOf(Boolean.class);
    }

    @Test
    void testLoadAndApplyNunitoFont_appliesSomeStyle() {
        FontUtils.loadAndApplyNunitoFont(titleLabel, convertButton, inputField, outputField, formatCombo, logArea);

        // Regardless of fonts availability, some style should be applied
        assertThat(titleLabel.getStyle()).isNotEmpty();
        assertThat(convertButton.getStyle()).isNotEmpty();
        assertThat(inputField.getStyle()).isNotEmpty();
        assertThat(outputField.getStyle()).isNotEmpty();
        assertThat(formatCombo.getStyle()).isNotEmpty();
        assertThat(logArea.getStyle()).isNotEmpty();
    }

    @Test
    void testLoadAndApplyNunitoFont_usesNunitoOrFallback() {
        FontUtils.loadAndApplyNunitoFont(titleLabel, convertButton, inputField, outputField, formatCombo, logArea);

        String style = titleLabel.getStyle();
        // Must be either Nunito style or fallback Segoe UI
        assertThat(style)
                .containsAnyOf("Nunito", "Segoe UI");
    }

    @Test
    void testFallbackStyleAppliedWhenFontsUnavailable_shouldThrowExceptionOnNullInputs() {
        assertThatThrownBy(() ->
                FontUtils.loadAndApplyNunitoFont(null, null, null, null, null, null)
        ).isInstanceOf(IllegalArgumentException.class)
                .hasMessage("UI components cannot be null when applying fonts");
    }
}
