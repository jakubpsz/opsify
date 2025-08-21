package com.opsify.service;

import com.opsify.audio.converter.service.AudioConverterService;
import com.opsify.audio.converter.service.ConversionListener;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import javax.sound.sampled.*;
import java.io.*;
import java.nio.file.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AudioConverterServiceMoreTest {

    @Test
    void convert_emptyDirectory_reportsZeroTotal() throws IOException {
        Path empty = Files.createTempDirectory("opsify-empty");
        Path out = Files.createTempDirectory("opsify-empty-out");
        AtomicInteger start = new AtomicInteger(0);
        new AudioConverterService().convert(empty, out, "wav", new ConversionListener() {
            @Override public void onStart(int total) { start.set(total); }
        });
        assertThat(start.get()).isEqualTo(0);
        deleteRecursively(empty);
        deleteRecursively(out);
    }

    @Test
    @Disabled
    void convert_skipsNonAudioFilesInDirectory() throws Exception {
        Path in = Files.createTempDirectory("opsify-skip");
        Path out = Files.createTempDirectory("opsify-skip-out");
        // non-audio
        Files.writeString(in.resolve("note.txt"), "hello");
        // audio
        Path wav = in.resolve("track.wav");
        createSilentWav(wav, 0.3, 22050);

        new AudioConverterService().convert(in, out, "wav");
        assertThat(Files.exists(out.resolve("track.wav"))).isTrue();
        assertThat(Files.exists(out.resolve("note.txt"))).as("non-audio should be skipped").isFalse();

        deleteRecursively(in);
        deleteRecursively(out);
    }

    @Test
    @Disabled
    void convert_missingInput_throws() {
        Path missing = Path.of("this/does/not/exist");
        Path out = missing.getParent() == null ? Path.of(".") : missing.getParent();
        assertThatThrownBy(() -> new AudioConverterService().convert(missing, out, "wav")).isInstanceOf(IOException.class);
    }

    // ---- helpers ----

    private static void createSilentWav(Path out, double seconds, int sampleRate) throws IOException, UnsupportedAudioFileException {
        int channels = 1;
        AudioFormat fmt = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, sampleRate, 16, channels, channels * 2, sampleRate, false);
        int frames = (int) (seconds * sampleRate);
        byte[] data = new byte[frames * channels * 2];
        try (ByteArrayInputStream bais = new ByteArrayInputStream(data);
             AudioInputStream ais = new AudioInputStream(bais, fmt, frames)) {
            Files.createDirectories(out.getParent() == null ? Path.of(".") : out.getParent());
            AudioSystem.write(ais, AudioFileFormat.Type.WAVE, out.toFile());
        }
    }

    private static void deleteRecursively(Path root) throws IOException {
        if (root == null || !Files.exists(root)) return;
        try (var s = Files.walk(root)) {
            s.sorted((a,b) -> b.getNameCount() - a.getNameCount()).forEach(p -> {
                try { Files.deleteIfExists(p); } catch (IOException ignored) {}
            });
        }
    }
}
