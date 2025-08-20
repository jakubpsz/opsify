package com.opsify.app;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import com.opsify.util.Constants;

/**
 * JavaFX entry point for the Audio Converter desktop application.
 */
public class AudioConverterApp extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(Constants.FXML_MAIN));
        Scene scene = new Scene(loader.load(), 900, 600);
        scene.getStylesheets().add(getClass().getResource(Constants.CSS_MAIN).toExternalForm());
        stage.setTitle(Constants.APP_TITLE);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
