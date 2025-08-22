package com.opsify.audio.converter.controller;

import com.opsify.audio.converter.service.AudioConverterService;
import com.opsify.audio.converter.service.ConversionListener;
import com.opsify.utils.Constants;
import com.opsify.utils.FontUtils;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import lombok.extern.slf4j.Slf4j;

import java.nio.file.Path;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
public class AudioConverterController {

    private static final int INITIAL_PROGRESS_OF_CONVERSION = 0;
    @FXML
    protected TextField inputField;
    @FXML
    protected TextField outputField;
    @FXML
    protected ComboBox<String> formatCombo;
    @FXML
    protected Button convertButton;
    @FXML
    protected ProgressBar progressBar;
    @FXML
    protected TextArea logArea;
    @FXML
    protected Label titleLabel;

    private ExecutorService exec;
    private final AudioConverterService converter;

    public AudioConverterController () {
        this.converter = new AudioConverterService();
    }

    @FXML
    public void initialize() {
        formatCombo.setItems(FXCollections.observableArrayList(Constants.SUPPORTED_FORMATS));
        progressBar.setProgress(INITIAL_PROGRESS_OF_CONVERSION);
        FontUtils.loadAndApplyNunitoFont(
                titleLabel, convertButton, inputField, outputField, formatCombo, logArea
        );
    }

    @FXML
    public void chooseInputFile() {
        var fileChooser = new FileChooser();
        var file = fileChooser.showOpenDialog(inputField.getScene().getWindow());
        if (file != null) inputField.setText(file.getAbsolutePath());
    }

    @FXML
    public void chooseInputDir() {
        var directoryChooser = new DirectoryChooser();
        var directory = directoryChooser.showDialog(inputField.getScene().getWindow());
        if (directory != null) inputField.setText(directory.getAbsolutePath());
    }

    @FXML
    public void chooseOutputDir() {
        var directoryChooser = new DirectoryChooser();
        var directory = directoryChooser.showDialog(outputField.getScene().getWindow());
        if (directory != null) outputField.setText(directory.getAbsolutePath());
    }

    @FXML
    public void convert() {
        logArea.clear();
        String in = inputField.getText();
        String out = outputField.getText();
        String fmt = formatCombo.getValue();
        if (in == null || in.isBlank() || out == null || out.isBlank() || fmt == null || fmt.isBlank()) {
            alertWrongData();
            return;
        }
        progressBar.setProgress(ProgressIndicator.INDETERMINATE_PROGRESS);
        convertButton.setDisable(true);
        exec = Executors.newSingleThreadExecutor();
        exec.submit(() -> runConversion(in, out, fmt));
    }

    private void runConversion(String in, String out, String fmt) {
        try {
            appendLog(Constants.LOG_STARTING + "\n");
            converter.convert(Path.of(in), Path.of(out), fmt, getListener());
            appendLog(Constants.LOG_FINISHED + "\n");
        } catch (Exception e) {
            log.error("Conversion error", e);
            appendLog(Constants.LOG_ERROR_GENERIC_PREFIX + e.getMessage() + "\n");
        } finally {
            Platform.runLater(() -> convertButton.setDisable(false));
            exec.shutdown();
        }
    }

    private ConversionListener getListener() {
        return new ConversionListener() {
            int total = 0;

            @Override
            public void onStart(int t) {
                total = t;
                updateProgress(0, total);
            }

            @Override
            public void onFileDone(Path input, Path output, int done, int t) {
                appendLog(Constants.LOG_DONE_PREFIX + input + " -> " + output + "\n");
                updateProgress(done, total);
            }

            @Override
            public void onError(Path input, Exception e, int done, int t) {
                appendLog(Constants.LOG_ERROR_PREFIX + input + " :: " + e.getMessage() + "\n");
                updateProgress(done, total);
            }

            void updateProgress(int done, int total) {
                Platform.runLater(() -> progressBar.setProgress(total == 0 ? 0 : (double) done / total));
            }
        };
    }

    private void appendLog(String text) {
        Platform.runLater(() -> logArea.appendText(text));
    }

    private void alertWrongData() {
        new Alert(Alert.AlertType.WARNING, Constants.MSG_SELECT_INPUT_OUTPUT_FORMAT, ButtonType.OK).showAndWait();
    }
}