
package com.opsify.features.pdf.joiner.view;

import com.opsify.features.pdf.joiner.service.PdfJoinerService;
import com.opsify.features.pdf.joiner.service.PdfJoinerServiceImpl;
import com.opsify.utils.Constants;
import com.opsify.utils.FontUtils;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
public class PdfJoinerController {

    @FXML
    protected TextField outputDirField;

    @FXML
    protected TextField outputFileNameField;

    @FXML
    protected ListView<String> filesListView;

    @FXML
    protected Button joinButton;

    @FXML
    protected ProgressBar progressBar;

    @FXML
    protected TextArea logArea;

    @FXML
    protected Label titleLabel;

    private final ObservableList<String> pdfFiles;
    private ExecutorService exec;
    private final PdfJoinerService pdfJoiner;

    public PdfJoinerController() {
        this.pdfJoiner = new PdfJoinerServiceImpl();
        this.pdfFiles = FXCollections.observableArrayList();
    }

    @FXML
    public void goToHome() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(Constants.FXML_HOME_FXML));
            Stage stage = (Stage) joinButton.getScene().getWindow();
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
        filesListView.setItems(pdfFiles);
        progressBar.setProgress(0);
        FontUtils.loadAndApplyNunitoFont(
                titleLabel, joinButton, outputDirField, outputFileNameField, filesListView, logArea
        );

        filesListView.setOnDragOver((DragEvent event) -> {
            if (event.getGestureSource() != filesListView &&
                    event.getDragboard().hasFiles()) {
                event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
            }
            event.consume();
        });

        // When file is dropped
        filesListView.setOnDragDropped((DragEvent event) -> {
            Dragboard db = event.getDragboard();
            boolean success = false;
            if (db.hasFiles()) {
                success = true;
                List<File> files = db.getFiles();
                for (File file : files) {
                    pdfFiles.add(file.getAbsolutePath());
                }
                setPlaceHolders();
            }
            event.setDropCompleted(success);
            event.consume();
        });
    }

    @FXML
    public void addPdfFiles() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("PDF Files", "*.pdf")
        );
        var files = fileChooser.showOpenMultipleDialog(outputDirField.getScene().getWindow());
        if (files != null && !files.isEmpty()) {
            for (File file : files) {
                pdfFiles.add(file.getAbsolutePath());
            }
            setPlaceHolders();
        }
    }

    @FXML
    public void removeSelectedFile() {
        int selectedIndex = filesListView.getSelectionModel().getSelectedIndex();
        if (selectedIndex >= 0) {
            pdfFiles.remove(selectedIndex);
        }
    }

    @FXML
    public void moveFileUp() {
        int selectedIndex = filesListView.getSelectionModel().getSelectedIndex();
        if (selectedIndex > 0) {
            String item = pdfFiles.remove(selectedIndex);
            pdfFiles.add(selectedIndex - 1, item);
            filesListView.getSelectionModel().select(selectedIndex - 1);
        }
    }

    @FXML
    public void moveFileDown() {
        int selectedIndex = filesListView.getSelectionModel().getSelectedIndex();
        if (selectedIndex >= 0 && selectedIndex < pdfFiles.size() - 1) {
            String item = pdfFiles.remove(selectedIndex);
            pdfFiles.add(selectedIndex + 1, item);
            filesListView.getSelectionModel().select(selectedIndex + 1);
        }
    }

    @FXML
    public void chooseOutputDir() {
        var directoryChooser = new DirectoryChooser();
        var directory = directoryChooser.showDialog(outputDirField.getScene().getWindow());
        if (directory != null) {
            outputDirField.setText(directory.getAbsolutePath());
        }
    }

    @FXML
    public void joinPdfs() {
        logArea.clear();

        if (pdfFiles.isEmpty()) {
            alertError("Please add at least one PDF file to join.");
            return;
        }

        String outputDir = outputDirField.getText();
        String outputFileName = outputFileNameField.getText();

        if (outputDir == null || outputDir.isBlank()) {
            alertError("Please select an output directory.");
            return;
        }

        if (outputFileName == null || outputFileName.isBlank()) {
            alertError("Please specify an output file name.");
            return;
        }

        if (!outputFileName.toLowerCase().endsWith(".pdf")) {
            outputFileName += ".pdf";
            outputFileNameField.setText(outputFileName);
        }

        progressBar.setProgress(ProgressBar.INDETERMINATE_PROGRESS);
        joinButton.setDisable(true);
        exec = Executors.newSingleThreadExecutor();
        String finalOutputFileName = outputFileName;
        exec.submit(() -> runJoining(outputDir, finalOutputFileName));
    }

    private void runJoining(String outputDir, String outputFileName) {
        try {
            appendLog("Starting PDF joining process...\n");
            Path outputPath = pdfJoiner.joinPdfs(pdfFiles, outputDir, outputFileName);
            appendLog("Successfully joined PDFs to: " + outputPath + "\n");
        } catch (Exception e) {
            log.error("PDF joining error", e);
            appendLog("Error: " + e.getMessage() + "\n");
        } finally {
            Platform.runLater(() -> {
                joinButton.setDisable(false);
                progressBar.setProgress(1);
            });
            exec.shutdown();
        }
    }

    private void appendLog(String text) {
        Platform.runLater(() -> logArea.appendText(text));
    }

    private void alertError(String message) {
        Platform.runLater(() ->
                new Alert(Alert.AlertType.ERROR, message, ButtonType.OK).showAndWait()
        );
    }

    private void setPlaceHolders() {
        if (outputFileNameField.getText().isEmpty() && !pdfFiles.isEmpty()) {
            String firstFilePath = pdfFiles.getFirst();
            String firstName = Paths.get(firstFilePath).getFileName().toString();
            String baseName = firstName.substring(0, firstName.lastIndexOf('.'));
            outputFileNameField.setText(baseName + "_merged.pdf");
        }
        if (outputDirField.getText().isEmpty() && !pdfFiles.isEmpty()) {
            String first = pdfFiles.getFirst();
            String dirName = Paths.get(first).getParent().toString();
            outputDirField.setText(dirName);
        }
    }
}