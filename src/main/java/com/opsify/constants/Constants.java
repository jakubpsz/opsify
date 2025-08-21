package com.opsify.constants;

import java.util.List;

/**
 * Common constants used across the application and tests.
 */
public final class Constants {
    private Constants() {}

    // Application
    public static final String APP_TITLE = "Opsify";
    public static final String LOGO = "/logo/logo_no_name.png";

    // Resources
    public static final String FXML_MAIN = "/fxml/audio-converter.fxml";
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
}
