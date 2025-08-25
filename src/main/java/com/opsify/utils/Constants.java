package com.opsify.utils;

import java.util.List;

/**
 * Common constants used across the application and tests.
 */
public final class Constants {
    private Constants() {}

    // Application
    public static final String APP_TITLE = "Opsify";
    public static final String LOGO = "/icons/new_logo.png";

    // Resources
    public static final String FXML_AUDIO_CONVERTER_FXML = "/fxml/audio-converter.fxml";
    public static final String FXML_IMAGE_RENAMER_FXML = "/fxml/image-renamer.fxml";
    public static final String FXML_PDF_JOINER_FXML = "/fxml/pdf-joiner.fxml";
    public static final String FXML_HOME_FXML = "/fxml/home.fxml";
    public static final String CSS_MAIN = "/css/style.css";

    // UI messages
    public static final String MSG_SELECT_INPUT_OUTPUT_FORMAT = "Please select input, output directory, and target format.";
    public static final String LOG_STARTING = "Starting conversion...";
    public static final String LOG_DONE_PREFIX = "Done: ";
    public static final String LOG_ERROR_PREFIX = "Error: ";
    public static final String LOG_FINISHED = "Conversion finished.";
    public static final String LOG_ERROR_GENERIC_PREFIX = "ERROR: ";

    // Supported formats in the combo box
    public static final List<String> SUPPORTED_FORMATS = List.of("mp3","wav","ogg","m4a","flac","aac");

    // Image Renamer constants
    public static final String[] IMAGE_SCHEMAS = {
            "yyyy_MM_dd_HH-mm-ss",  // Default format moved to first position
            "yyyy-MM-dd_HH-mm-ss",
            "yyyyMMdd_HHmmss",
            "dd-MM-yyyy_HH-mm-ss",
            "MMddyyyy_HHmmss"
    };
    public static final String MSG_SELECT_INPUT_OUTPUT_SCHEMA = "Please select input, output and naming schema";
    public static final String LOG_STARTING_RENAME = "Starting media file renaming...";
    public static final String LOG_FINISHED_RENAME = "Media file renaming completed!";
    public static final String LOG_RENAME_DONE_PREFIX = "Renamed: ";
    public static final String LOG_RENAME_ERROR_PREFIX = "Error renaming: ";
}
