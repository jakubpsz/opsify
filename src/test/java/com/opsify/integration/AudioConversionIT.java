package com.opsify.integration;

import com.opsify.audio.converter.service.AudioConverterService;
import org.junit.jupiter.api.Test;

import javax.sound.sampled.*;
import java.io.*;
import java.nio.file.*;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test using the JavaCV-based converter (no external ffmpeg required).
 * It converts all files recursively and preserves original basenames and directory structure.
 */
public class AudioConversionIT {

    @Test
    void convertsAllFilesRecursively_retainsNamesAndStructure() throws Exception {
        Path inputRoot = Files.createTempDirectory("in-audio");
        Path sub = Files.createDirectory(inputRoot.resolve("album"));
        Path outputRoot = Files.createTempDirectory("out-audio");

        // Create small silent wav files
        Path rootWav = inputRoot.resolve("track1.wav");
        Path subWav = sub.resolve("track2.wav");
        createSilentWav(rootWav, 0.5, 22050);
        createSilentWav(subWav, 0.5, 22050);

        // Convert entire directory to WAV (PCM), which should always be supported
        new AudioConverterService().convert(inputRoot, outputRoot, "wav");

        // Assertions: outputs exist and structure retained
        Path expectedRootOut = outputRoot.resolve("track1.wav");
        Path expectedSubOut = outputRoot.resolve("album").resolve("track2.wav");
        assertThat(Files.exists(expectedRootOut)).as("Root file converted with same base name").isTrue();
        assertThat(Files.exists(expectedSubOut)).as("Nested file converted with same base name and structure").isTrue();

        // Cleanup
        deleteRecursively(inputRoot);
        deleteRecursively(outputRoot);
    }

    /** Create a short silent WAV file (PCM 16-bit) for testing. */
    private static void createSilentWav(Path out, double seconds, int sampleRate) throws IOException, UnsupportedAudioFileException {
        int channels = 1;
        AudioFormat fmt = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, sampleRate, 16, channels, channels * 2, sampleRate, false);
        int frames = (int) (seconds * sampleRate);
        byte[] data = new byte[frames * channels * 2]; // zeroed => silence
        try (ByteArrayInputStream bais = new ByteArrayInputStream(data);
             AudioInputStream ais = new AudioInputStream(bais, fmt, frames)) {
            Files.createDirectories(out.getParent() == null ? Path.of(".") : out.getParent());
            AudioSystem.write(ais, AudioFileFormat.Type.WAVE, out.toFile());
        }
    }

    /** Recursively delete a directory. */
    private static void deleteRecursively(Path root) throws IOException {
        if (!Files.exists(root)) return;
        try (var s = Files.walk(root)) {
            s.sorted((a,b) -> b.getNameCount() - a.getNameCount()).forEach(p -> {
                try { Files.deleteIfExists(p); } catch (IOException ignored) {}
            });
        }
    }
}
