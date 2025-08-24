package com.opsify.image.renamer.service;

import com.drew.imaging.ImageMetadataReader;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.Tag;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.sax.BodyContentHandler;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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

    private boolean isImageFile(Path path) {
        String fileName = path.getFileName().toString().toLowerCase();
        return IMAGE_EXTENSIONS.stream().anyMatch(fileName::endsWith);
    }

    private boolean isVideoFile(Path path) {
        String fileName = path.getFileName().toString().toLowerCase();
        return VIDEO_EXTENSIONS.stream().anyMatch(fileName::endsWith);
    }

    private void processMediaFile(Path mediaFile, Path outputRoot, String schema,
                                  boolean groupByYear, boolean groupByMonth, boolean groupByDay) throws Exception {
        // Get file modification time
        Map<String, String> metadata = null;
        if (isImageFile(mediaFile)) {
            metadata = extractImageMetadata(mediaFile);
        } else if (isVideoFile(mediaFile)) {
            metadata = extractVideoMetadata(mediaFile);
        }
        String dateStr = metadata.get("Date/Time Original"); // most common for images
        if (dateStr == null) dateStr = metadata.get("date"); // fallback for videos
        if (dateStr == null) dateStr = metadata.get("dcterms:created"); // some containers

        Date date;

        if (dateStr != null) {
            try {
                if (dateStr.contains("T") && dateStr.endsWith("Z")) {
                    // ISO 8601 style → Instant.parse
                    Instant instant = Instant.parse(dateStr);
                    date = Date.from(instant);
                } else {
                    // EXIF style → yyyy:MM:dd HH:mm:ss
                    SimpleDateFormat exifFormat = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss");
                    date = exifFormat.parse(dateStr);
                }
            } catch (ParseException | IllegalArgumentException e) {
                // Fallback to file attributes if parsing fails
                BasicFileAttributes attrs = Files.readAttributes(mediaFile, BasicFileAttributes.class);
                date = new Date(attrs.lastModifiedTime().toMillis());
            }
        } else {
            // No metadata date → fallback to filesystem modified time
            BasicFileAttributes attrs = Files.readAttributes(mediaFile, BasicFileAttributes.class);
            date = new Date(attrs.lastModifiedTime().toMillis());
        }

        // Format for filename
        SimpleDateFormat format = new SimpleDateFormat(schema);
        String newName = format.format(date);
        System.out.println("New filename date part: " + newName);

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

        // Copy the file with metadata preservation
        Files.copy(mediaFile, outputFile, StandardCopyOption.COPY_ATTRIBUTES, StandardCopyOption.REPLACE_EXISTING);
//        ObjectMapper mapper = new ObjectMapper();
//        Path jsonFile = outputFile.resolve(mediaFile.getFileName().toString() + ".json");
//        mapper.writerWithDefaultPrettyPrinter().writeValue(jsonFile.toFile(), metadata);
    }

    public static Map<String, String> extractImageMetadata(Path path) throws Exception {
        Map<String, String> map = new LinkedHashMap<>();
        try (InputStream is = Files.newInputStream(path)) {
            Metadata metadata = ImageMetadataReader.readMetadata(is);
            for (Directory dir : metadata.getDirectories()) {
                for (Tag tag : dir.getTags()) {
                    map.put(tag.getTagName(), tag.getDescription());
                }
            }
        }
        return map;
    }

    // Extract metadata for videos
    public static Map<String, String> extractVideoMetadata(Path path) throws Exception {
        Map<String, String> map = new LinkedHashMap<>();
        try (InputStream is = Files.newInputStream(path)) {
            org.apache.tika.metadata.Metadata metadata = new org.apache.tika.metadata.Metadata();
            AutoDetectParser parser = new AutoDetectParser();
            parser.parse(is, new BodyContentHandler(), metadata, new ParseContext());

            for (String name : metadata.names()) {
                map.put(name, metadata.get(name));
            }
        }
        return map;
    }

    private Path buildOutputDirectory(Path outputRoot, Date date,
                                      boolean groupByYear, boolean groupByMonth, boolean groupByDay) {
        SimpleDateFormat yearFormat = new SimpleDateFormat("yyyy");
        SimpleDateFormat monthFormat = new SimpleDateFormat("yyyy-MM"); // Updated to yyyy-MM format
        SimpleDateFormat dayFormat = new SimpleDateFormat("dd");

        Path outputDir = outputRoot;
        if (groupByYear) outputDir = outputDir.resolve(yearFormat.format(date));
        if (groupByMonth) outputDir = outputDir.resolve(monthFormat.format(date));
        if (groupByDay) outputDir = outputDir.resolve(dayFormat.format(date));

        return outputDir;
    }
}