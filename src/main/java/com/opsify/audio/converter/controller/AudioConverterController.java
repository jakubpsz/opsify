package com.opsify.audio.converter.controller;

import com.opsify.audio.converter.service.AudioConverterService;
import com.opsify.audio.converter.service.ConversionListener;
import com.opsify.constants.Constants;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import lombok.extern.slf4j.Slf4j;

import java.nio.file.Path;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Main UI controller. Lets the user choose an input (file or directory), an output directory,
 * and a target format. Triggers ffmpeg-based conversion and streams logs to the UI.
 */
@Slf4j
public class AudioConverterController {

    @FXML
    protected TextField inputField;
    @FXML
    protected TextField outputField;
    @FXML
    protected ComboBox<String> formatCombo;
    @FXML private Button convertButton;
    @FXML
    protected ProgressBar progressBar;
    @FXML
    protected TextArea logArea;

    private final ExecutorService exec = Executors.newSingleThreadExecutor();
    private final AudioConverterService converter;

    // Default constructor for JavaFX
    public AudioConverterController() {
        this(new AudioConverterService());
    }

    // Constructor injection (used in tests)
    public AudioConverterController(AudioConverterService converter) {
        this.converter = converter;
    }

    @FXML
    public void initialize() {
        formatCombo.setItems(FXCollections.observableArrayList(Constants.SUPPORTED_FORMATS));
        progressBar.setProgress(0);
    }

    @FXML
    public void chooseInputFile() {
        var chooser = new FileChooser();
        var f = chooser.showOpenDialog(inputField.getScene().getWindow());
        if (f != null) inputField.setText(f.getAbsolutePath());
    }

    @FXML
    public void chooseInputDir() {
        var chooser = new DirectoryChooser();
        var d = chooser.showDialog(inputField.getScene().getWindow());
        if (d != null) inputField.setText(d.getAbsolutePath());
    }

    @FXML
    public void chooseOutputDir() {
        var chooser = new DirectoryChooser();
        var d = chooser.showDialog(outputField.getScene().getWindow());
        if (d != null) outputField.setText(d.getAbsolutePath());
    }

    @FXML
    public void convert() {
        logArea.clear();
        String in = inputField.getText();
        String out = outputField.getText();
        String fmt = formatCombo.getValue();
        if (in == null || in.isBlank() || out == null || out.isBlank() || fmt == null || fmt.isBlank()) {
            alert(Constants.MSG_SELECT_INPUT_OUTPUT_FORMAT);
            return;
        }
        progressBar.setProgress(ProgressIndicator.INDETERMINATE_PROGRESS);
        convertButton.setDisable(true);
        exec.submit(() -> {
            try {
                append(Constants.LOG_STARTING + "\n");
                converter.convert(Path.of(in), Path.of(out), fmt, new ConversionListener() {
                    int total = 0;
                    @Override public void onStart(int t) { total = t; updateProgress(0, total); }
                    @Override public void onFileDone(Path input, Path output, int done, int t) {
                        append(Constants.LOG_DONE_PREFIX + input + " -> " + output + "\n");
                        updateProgress(done, total);
                    }
                    @Override public void onError(Path input, Exception e, int done, int t) {
                        append(Constants.LOG_ERROR_PREFIX + input + " :: " + e.getMessage() + "\n");
                        updateProgress(done, total);
                    }
                    void updateProgress(int done, int total) {
                        Platform.runLater(() -> progressBar.setProgress(total == 0 ? 0 : (double) done / total));
                    }
                });
                append(Constants.LOG_FINISHED + "\n");
            } catch (Exception e) {
                log.error("Conversion error", e);
                append(Constants.LOG_ERROR_GENERIC_PREFIX + e.getMessage() + "\n");
            } finally {
                Platform.runLater(() -> convertButton.setDisable(false));
            }
        });
    }

    private void append(String text) {
        Platform.runLater(() -> logArea.appendText(text));
    }

    private void alert(String msg) { new Alert(Alert.AlertType.WARNING, msg, ButtonType.OK).showAndWait(); }
}
