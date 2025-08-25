package com.opsify.features.audio.converter.service;

import lombok.NonNull;

import java.io.IOException;
import java.nio.file.Path;

public interface AudioConverterService {
    void convert(@NonNull Path input,
                 @NonNull Path outputDir,
                 @NonNull String targetExt,
                 ConversionListener listener) throws IOException;
}
