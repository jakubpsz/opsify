package com.opsify.util;

import com.opsify.audio.converter.util.PathAudioUtil;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class PathAudioUtilTest {

    @Test
    void ext_isAudio() {
        assertThat(PathAudioUtil.ext(Path.of("song.MP3"))).isEqualTo("mp3");
        assertThat(PathAudioUtil.isAudio(Path.of("a.wav"))).isTrue();
        assertThat(PathAudioUtil.isAudio(Path.of("a.txt"))).isFalse();
    }

    @Test
    @Disabled
    void ensureParent_map_unique() throws IOException {
        Path root = Files.createTempDirectory("root");
        Path outRoot = Files.createTempDirectory("out");
        Path file = root.resolve("c/d/track.wav");
        PathAudioUtil.ensureParent(file);
        Files.writeString(file, "x");

        Path mapped = PathAudioUtil.mapToOutput(root, file, outRoot, "mp3");
        assertThat(mapped.toString()).endsWith("c\\d\\track.mp3");

        Path u1 = PathAudioUtil.unique(mapped);
        assertThat(u1).isEqualTo(mapped);
        Files.createDirectories(mapped.getParent());
        Files.createFile(mapped);
        Path u2 = PathAudioUtil.unique(mapped);
        assertThat(u2).isNotEqualTo(mapped);
    }
}
