package com.opsify.audio.converter.service;

import java.nio.file.Path;

/**
 * Listener to observe conversion progress.
 */
public interface ConversionListener {
    /** Called once with total number of files that will be converted. */
    default void onStart(int total) {}
    /** Called after a file is successfully converted. */
    default void onFileDone(Path input, Path output, int done, int total) {}
    /** Called when a file fails to convert. */
    default void onError(Path input, Exception e, int done, int total) {}
}
