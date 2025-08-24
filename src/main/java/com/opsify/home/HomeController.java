package com.opsify.home;

import com.opsify.OpsifyApp;
import com.opsify.constants.Constants;
import com.opsify.utils.FontUtils;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class HomeController {

    @FXML
    private Button audioConverterBtn;

    @FXML
    private Button imageRenamerBtn;

    @FXML
    private Button pdfJoinerBtn;

    @FXML
    private Label titleLabel;

    @FXML
    public void initialize() {
        FontUtils.loadAndApplyNunitoFont(
                titleLabel, audioConverterBtn, imageRenamerBtn, pdfJoinerBtn
        );
    }

    @FXML
    public void navigateToAudioConverter() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(Constants.FXML_AUDIO_CONVERTER_FXML));
            Stage stage = (Stage) audioConverterBtn.getScene().getWindow();
            Scene scene = new Scene(loader.load(), 900, 600);
            scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource(Constants.CSS_MAIN)).toExternalForm());
            stage.setScene(scene);
            stage.setTitle("Audio Converter - Opsify");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void navigateToPlaceholder() {
        // Placeholder for future implementations
        System.out.println("This feature is coming soon!");
    }

    @FXML
    public void navigateToImageRenamer() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(Constants.FXML_IMAGE_RENAMER_FXML));
            Stage stage = (Stage) imageRenamerBtn.getScene().getWindow();
            Scene scene = new Scene(loader.load(), 900, 600);
            scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource(Constants.CSS_MAIN)).toExternalForm());
            stage.setScene(scene);
            stage.setTitle("Media File Renamer - Opsify");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}