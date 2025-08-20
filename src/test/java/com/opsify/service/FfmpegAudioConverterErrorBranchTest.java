package com.opsify.service;

import org.junit.jupiter.api.Test;

import javax.sound.sampled.*;
import java.io.*;
import java.nio.file.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

class FfmpegAudioConverterErrorBranchTest {

    @Test
    void directoryConversion_invokesOnErrorForInvalidAudio() throws Exception {
        Path in = Files.createTempDirectory("opsify-err");
        Path out = Files.createTempDirectory("opsify-err-out");
        // valid
        Path wav = in.resolve("ok.wav");
        createSilentWav(wav, 0.3, 22050);
        // invalid masquerading as audio
        Path bad = in.resolve("bad.wav");
        Files.writeString(bad, "not audio");

        AtomicInteger errors = new AtomicInteger(0);
        AtomicInteger done = new AtomicInteger(0);

        new FfmpegAudioConverter().convert(in, out, "wav", new ConversionListener() {
            @Override public void onFileDone(Path input, Path output, int d, int t) { done.incrementAndGet(); }
            @Override public void onError(Path input, Exception e, int d, int t) { errors.incrementAndGet(); }
        });

        assertThat(done.get()).as("At least one valid file should be converted").isGreaterThanOrEqualTo(1);
        assertThat(errors.get()).as("Invalid audio should trigger onError").isGreaterThanOrEqualTo(1);

        deleteRecursively(in);
        deleteRecursively(out);
    }

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
