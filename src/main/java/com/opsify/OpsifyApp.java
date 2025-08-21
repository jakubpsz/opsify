package com.opsify;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import com.opsify.constants.Constants;

import java.util.Objects;

/**
 * JavaFX entry point for the Opsify desktop application.
 */
public class OpsifyApp extends Application {


    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(Constants.FXML_MAIN));
        Scene scene = new Scene(loader.load(), 900, 600);
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource(Constants.CSS_MAIN)).toExternalForm());
        stage.setTitle(Constants.APP_TITLE);
        Image image = new Image(Objects.requireNonNull(getClass().getResourceAsStream(Constants.LOGO)));
        stage.getIcons().add(image);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
