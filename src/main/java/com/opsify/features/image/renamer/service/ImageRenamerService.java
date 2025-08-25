package com.opsify.features.image.renamer.service;

public interface ImageRenamerService {
    void renameImages(String inputPath, String outputPath, String schema,
                      boolean groupByYear, boolean groupByMonth, boolean groupByDay,
                      RenamerListener listener);
}