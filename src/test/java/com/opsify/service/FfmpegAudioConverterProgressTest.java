package com.opsify.service;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import javax.sound.sampled.*;
import java.io.*;
import java.nio.file.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

class FfmpegAudioConverterProgressTest {

    @Test
    @Disabled
    void singleFileConversion_reportsProgressAndCreatesOutput() throws Exception {
        Path input = Files.createTempFile("in-silence", ".wav");
        createSilentWav(input, 0.5, 22050);
        Path outDir = Files.createTempDirectory("out-silence");

        AtomicInteger started = new AtomicInteger();
        AtomicInteger done = new AtomicInteger();

        new FfmpegAudioConverter().convert(input, outDir, "wav", new ConversionListener() {
            @Override public void onStart(int total) { started.set(total); }
            @Override public void onFileDone(Path in, Path out, int d, int t) { done.set(d); }
        });

        assertThat(started.get()).isEqualTo(1);
        assertThat(done.get()).isEqualTo(1);
        Path expected = outDir.resolve(input.getFileName().toString());
        assertThat(Files.exists(expected) || Files.exists(outDir.resolve(input.getFileName().toString().replace(".wav"," (1).wav")))).isTrue();

        deleteRecursively(input.getParent());
        deleteRecursively(outDir);
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
