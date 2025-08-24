package com.opsify.image.renamer.service;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Slf4j
public class ImageRenamerServiceImpl implements ImageRenamerService {

    private static final List<String> IMAGE_EXTENSIONS = List.of(
            ".jpg", ".jpeg", ".png", ".gif", ".bmp", ".tiff", ".tif", ".webp"
    );

    private static final List<String> VIDEO_EXTENSIONS = List.of(
            ".mp4", ".avi", ".mov", ".wmv", ".flv", ".mkv", ".webm", ".m4v", ".mpg", ".mpeg"
    );

    @Override
    public void renameImages(String inputPath, String outputPath, String schema,
                             boolean groupByYear, boolean groupByMonth, boolean groupByDay,
                             RenamerListener listener) {
        try {
            Path input = Paths.get(inputPath);
            Path output = Paths.get(outputPath);

            List<Path> mediaFiles = collectMediaFiles(input);
            listener.onStart(mediaFiles.size());

            int processed = 0;
            for (Path mediaFile : mediaFiles) {
                try {
                    processMediaFile(mediaFile, output, schema, groupByYear, groupByMonth, groupByDay);
                    listener.onFileDone(mediaFile.toString(), "Success", ++processed, mediaFiles.size());
                } catch (Exception e) {
                    listener.onError(mediaFile.toString(), e, ++processed, mediaFiles.size());
                }
            }
        } catch (Exception e) {
            log.error("Error processing media files", e);
            throw new RuntimeException("Failed to rename media files", e);
        }
    }

    private List<Path> collectMediaFiles(Path input) throws IOException {
        List<Path> result = new ArrayList<>();
        if (Files.isRegularFile(input) && isMediaFile(input)) {
            result.add(input);
        } else if (Files.isDirectory(input)) {
            Files.walk(input)
                    .filter(Files::isRegularFile)
                    .filter(this::isMediaFile)
                    .forEach(result::add);
        }
        return result;
    }

    private boolean isMediaFile(Path path) {
        String fileName = path.getFileName().toString().toLowerCase();
        return IMAGE_EXTENSIONS.stream().anyMatch(fileName::endsWith) ||
                VIDEO_EXTENSIONS.stream().anyMatch(fileName::endsWith);
    }

    private void processMediaFile(Path mediaFile, Path outputRoot, String schema,
                                  boolean groupByYear, boolean groupByMonth, boolean groupByDay) throws IOException {
        // Get file modification time
        BasicFileAttributes attrs = Files.readAttributes(mediaFile, BasicFileAttributes.class);
        long modTime = attrs.lastModifiedTime().toMillis();

        // Generate new filename based on schema
        Date date = new Date(modTime);
        SimpleDateFormat format = new SimpleDateFormat(schema);
        String newName = format.format(date);

        // Get file extension
        String fileName = mediaFile.getFileName().toString();
        int dotIndex = fileName.lastIndexOf('.');
        String ext = dotIndex > 0 ? fileName.substring(dotIndex) : "";

        // Build output directory path based on grouping options
        Path outputDir = buildOutputDirectory(outputRoot, date, groupByYear, groupByMonth, groupByDay);
        Files.createDirectories(outputDir);

        // Create the output file path
        Path outputFile = outputDir.resolve(newName + ext);

        // Handle name conflicts
        int counter = 0;
        while (Files.exists(outputFile)) {
            outputFile = outputDir.resolve(newName + "_" + (counter++) + ext);
        }

        // Copy the file
        Files.copy(mediaFile, outputFile);
    }

    private Path buildOutputDirectory(Path outputRoot, Date date,
                                      boolean groupByYear, boolean groupByMonth, boolean groupByDay) {
        SimpleDateFormat yearFormat = new SimpleDateFormat("yyyy");
        SimpleDateFormat monthFormat = new SimpleDateFormat("MM");
        SimpleDateFormat dayFormat = new SimpleDateFormat("dd");

        Path outputDir = outputRoot;
        if (groupByYear) outputDir = outputDir.resolve(yearFormat.format(date));
        if (groupByMonth) outputDir = outputDir.resolve(monthFormat.format(date));
        if (groupByDay) outputDir = outputDir.resolve(dayFormat.format(date));

        return outputDir;
    }
}