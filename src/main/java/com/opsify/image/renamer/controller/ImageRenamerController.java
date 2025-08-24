package com.opsify.image.renamer.controller;

import com.opsify.constants.Constants;
import com.opsify.image.renamer.service.ImageRenamerService;
import com.opsify.image.renamer.service.ImageRenamerServiceImpl;
import com.opsify.image.renamer.service.RenamerListener;
import com.opsify.utils.FontUtils;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
public class ImageRenamerController {

    private static final int INITIAL_PROGRESS = 0;
    @FXML
    protected TextField inputField;
    @FXML
    protected TextField outputField;
    @FXML
    protected ComboBox<String> schemaCombo;
    @FXML
    protected CheckBox groupByYear;
    @FXML
    protected CheckBox groupByMonth;
    @FXML
    protected CheckBox groupByDay;
    @FXML
    protected Button renameButton;
    @FXML
    protected ProgressBar progressBar;
    @FXML
    protected TextArea logArea;
    @FXML
    protected Label titleLabel;

    private ExecutorService exec;
    private final ImageRenamerService renamer;

    public ImageRenamerController() {
        this.renamer = new ImageRenamerServiceImpl();
    }

    @FXML
    public void goToHome() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(Constants.FXML_HOME_FXML));
            Stage stage = (Stage) renameButton.getScene().getWindow();
            Scene scene = new Scene(loader.load(), 900, 600);
            scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource(Constants.CSS_MAIN)).toExternalForm());
            stage.setScene(scene);
            stage.setTitle("Opsify Tools");
        } catch (IOException e) {
            log.error("Error navigating to home", e);
        }
    }

    @FXML
    public void initialize() {
        schemaCombo.setItems(FXCollections.observableArrayList(Constants.IMAGE_SCHEMAS));
        progressBar.setProgress(INITIAL_PROGRESS);
        FontUtils.loadAndApplyNunitoFont(
                titleLabel, renameButton, inputField, outputField, schemaCombo, logArea
        );
    }

    @FXML
    public void chooseInputFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Media Files",
                        "*.jpg", "*.jpeg", "*.png", "*.gif", "*.bmp", "*.tiff", "*.tif", "*.webp",
                        "*.mp4", "*.avi", "*.mov", "*.wmv", "*.flv", "*.mkv", "*.webm", "*.m4v", "*.mpg", "*.mpeg")
        );
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
    public void rename() {
        logArea.clear();
        String in = inputField.getText();
        String out = outputField.getText();
        String schema = schemaCombo.getValue();

        if (in == null || in.isBlank() || out == null || out.isBlank() || schema == null || schema.isBlank()) {
            alertWrongData();
            return;
        }

        boolean groupYear = groupByYear.isSelected();
        boolean groupMonth = groupByMonth.isSelected();
        boolean groupDay = groupByDay.isSelected();

        progressBar.setProgress(ProgressIndicator.INDETERMINATE_PROGRESS);
        renameButton.setDisable(true);
        exec = Executors.newSingleThreadExecutor();
        exec.submit(() -> runRenaming(in, out, schema, groupYear, groupMonth, groupDay));
    }

    private void runRenaming(String in, String out, String schema, boolean groupYear, boolean groupMonth, boolean groupDay) {
        try {
            appendLog(Constants.LOG_STARTING_RENAME + "\n");
            renamer.renameImages(in, out, schema, groupYear, groupMonth, groupDay, getListener());
            appendLog(Constants.LOG_FINISHED_RENAME + "\n");
        } catch (Exception e) {
            log.error("Renaming error", e);
            appendLog(Constants.LOG_ERROR_GENERIC_PREFIX + e.getMessage() + "\n");
        } finally {
            Platform.runLater(() -> renameButton.setDisable(false));
            exec.shutdown();
        }
    }

    private RenamerListener getListener() {
        return new RenamerListener() {
            int total = 0;

            @Override
            public void onStart(int t) {
                total = t;
                updateProgress(0, total);
            }

            @Override
            public void onFileDone(String input, String output, int done, int t) {
                appendLog(Constants.LOG_RENAME_DONE_PREFIX + input + " -> " + output + "\n");
                updateProgress(done, total);
            }

            @Override
            public void onError(String input, Exception e, int done, int t) {
                appendLog(Constants.LOG_RENAME_ERROR_PREFIX + input + " :: " + e.getMessage() + "\n");
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
        new Alert(Alert.AlertType.WARNING, Constants.MSG_SELECT_INPUT_OUTPUT_SCHEMA, ButtonType.OK).showAndWait();
    }
}