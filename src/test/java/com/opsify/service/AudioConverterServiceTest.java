package com.opsify.service;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static com.opsify.audio.converter.util.PathAudioUtil.*;

class AudioConverterServiceTest {

    @Test
    void mapToOutput_preservesStructure() {
        Path root = Path.of("/a/b");
        Path file = Path.of("/a/b/c/d/track.wav");
        Path outRoot = Path.of("/x/y");
        Path out = mapToOutput(root, file, outRoot, "mp3");
        Path expected = outRoot.resolve(Path.of("c","d","track.mp3"));
        assertThat(out.normalize()).isEqualTo(expected.normalize());
    }

    @Test
    void unique_appendsCounterWhenExists() throws IOException {
        Path dir = Files.createTempDirectory("opsify-uni");
        Path desired = dir.resolve("song.mp3");
        Files.createFile(desired);
        Path unique = unique(desired);
        assertThat(unique.getFileName().toString()).matches("^song \\([0-9]+\\)\\.mp3$");
    }

    @Test
    void extAndIsAudio() throws IOException {
        assertThat(ext(Path.of("track.MP3"))).isEqualTo("mp3");
        assertThat(isAudio(Path.of("file.wav"))).isTrue();
        assertThat(isAudio(Path.of("file.txt"))).isFalse();
    }
}
