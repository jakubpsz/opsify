package com.opsify.features.audio.converter.util;

import com.opsify.features.audio.converter.utils.PathAudioUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatCode;

class PathAudioUtilTest {

    @Test
    void testExt_WithExtension() {
        Path path = Path.of("audiofile.mp3");
        String result = PathAudioUtil.ext(path);
        assertThat(result).isEqualTo("mp3");
    }

    @Test
    void testExt_WithMultipleDots() {
        Path path = Path.of("audio.file.with.dots.flac");
        String result = PathAudioUtil.ext(path);
        assertThat(result).isEqualTo("flac");
    }

    @Test
    void testExt_WithUpperCaseExtension() {
        Path path = Path.of("audiofile.MP3");
        String result = PathAudioUtil.ext(path);
        assertThat(result).isEqualTo("mp3");
    }

    @Test
    void testExt_NoExtension() {
        Path path = Path.of("audiofile");
        String result = PathAudioUtil.ext(path);
        assertThat(result).isEmpty();
    }

    @Test
    void testExt_OnlyExtension() {
        Path path = Path.of(".mp3");
        String result = PathAudioUtil.ext(path);
        assertThat(result).isEqualTo("mp3");
    }

    @Test
    void testIsAudio_SupportedFormats() {
        assertThat(PathAudioUtil.isAudio(Path.of("file.mp3"))).isTrue();
        assertThat(PathAudioUtil.isAudio(Path.of("file.wav"))).isTrue();
        assertThat(PathAudioUtil.isAudio(Path.of("file.aac"))).isTrue();
        assertThat(PathAudioUtil.isAudio(Path.of("file.flac"))).isTrue();
        assertThat(PathAudioUtil.isAudio(Path.of("file.ogg"))).isTrue();
        assertThat(PathAudioUtil.isAudio(Path.of("file.m4a"))).isTrue();
        assertThat(PathAudioUtil.isAudio(Path.of("file.opus"))).isTrue();
    }

    @Test
    void testIsAudio_UnsupportedFormats() {
        assertThat(PathAudioUtil.isAudio(Path.of("file.txt"))).isFalse();
        assertThat(PathAudioUtil.isAudio(Path.of("file.pdf"))).isFalse();
        assertThat(PathAudioUtil.isAudio(Path.of("file.jpg"))).isFalse();
        assertThat(PathAudioUtil.isAudio(Path.of("file"))).isFalse();
        assertThat(PathAudioUtil.isAudio(Path.of("file.unknown"))).isFalse();
    }

    @Test
    void testIsAudio_CaseInsensitive() {
        assertThat(PathAudioUtil.isAudio(Path.of("file.MP3"))).isTrue();
        assertThat(PathAudioUtil.isAudio(Path.of("file.FLAC"))).isTrue();
        assertThat(PathAudioUtil.isAudio(Path.of("file.WAV"))).isTrue();
    }

    @Test
    void testEnsureDir_CreatesDirectory(@TempDir Path tempDir) throws IOException {
        Path newDir = tempDir.resolve("new-directory");
        PathAudioUtil.ensureDir(newDir);
        assertThat(Files.exists(newDir)).isTrue();
        assertThat(Files.isDirectory(newDir)).isTrue();
    }

    @Test
    void testEnsureDir_AlreadyExists(@TempDir Path tempDir) throws IOException {
        PathAudioUtil.ensureDir(tempDir);
        assertThat(Files.exists(tempDir)).isTrue();
    }

    @Test
    void testEnsureParent_CreatesParentDirectory(@TempDir Path tempDir) throws IOException {
        Path filePath = tempDir.resolve("subdir").resolve("file.txt");
        PathAudioUtil.ensureParent(filePath);

        Path parentDir = filePath.getParent();
        assertThat(Files.exists(parentDir)).isTrue();
        assertThat(Files.isDirectory(parentDir)).isTrue();
    }

    @Test
    void testEnsureParent_NoParent(@TempDir Path tempDir) throws IOException {
        Path filePath = tempDir.resolve("file.txt"); // No subdirectory
        PathAudioUtil.ensureParent(filePath);
    }

    @Test
    void testMapToOutput_SingleFileInput() {
        Path inputRoot = Path.of("/input/song.mp3");
        Path outputRoot = Path.of("/output");
        String newExt = "flac";

        Path result = PathAudioUtil.mapToOutput(inputRoot, inputRoot, outputRoot, newExt);
        assertThat(result).isEqualTo(Path.of("/output/song.flac"));
    }

    @Test
    void testMapToOutput_SingleFileInputNoExtension() {
        Path inputRoot = Path.of("/input/song");
        Path outputRoot = Path.of("/output");
        String newExt = "mp3";

        Path result = PathAudioUtil.mapToOutput(inputRoot, inputRoot, outputRoot, newExt);
        assertThat(result).isEqualTo(Path.of("/output/song.mp3"));
    }

    @Test
    void testMapToOutput_DirectoryInputFlatFile() {
        Path inputRoot = Path.of("/input");
        Path inputFile = Path.of("/input/song.wav");
        Path outputRoot = Path.of("/output");
        String newExt = "mp3";

        Path result = PathAudioUtil.mapToOutput(inputRoot, inputFile, outputRoot, newExt);
        assertThat(result).isEqualTo(Path.of("/output/song.mp3"));
    }

    @Test
    void testMapToOutput_DirectoryInputNestedFile() {
        Path inputRoot = Path.of("/input");
        Path inputFile = Path.of("/input/artists/album/song.flac");
        Path outputRoot = Path.of("/output");
        String newExt = "aac";

        Path result = PathAudioUtil.mapToOutput(inputRoot, inputFile, outputRoot, newExt);
        assertThat(result).isEqualTo(Path.of("/output/artists/album/song.aac"));
    }

    @Test
    void testMapToOutput_DirectoryInputFileInSubdirNoExtension() {
        Path inputRoot = Path.of("/input");
        Path inputFile = Path.of("/input/artists/song");
        Path outputRoot = Path.of("/output");
        String newExt = "wav";

        Path result = PathAudioUtil.mapToOutput(inputRoot, inputFile, outputRoot, newExt);
        assertThat(result).isEqualTo(Path.of("/output/artists/song.wav"));
    }

    @Test
    void testMapToOutput_DirectoryInputRootLevelNoExtension() {
        Path inputRoot = Path.of("/input");
        Path inputFile = Path.of("/input/song");
        Path outputRoot = Path.of("/output");
        String newExt = "ogg";

        Path result = PathAudioUtil.mapToOutput(inputRoot, inputFile, outputRoot, newExt);
        assertThat(result).isEqualTo(Path.of("/output/song.ogg"));
    }

    @Test
    void testUnique_FileDoesNotExist() throws IOException {
        Path desired = Path.of("/nonexistent/file.mp3");
        Path result = PathAudioUtil.unique(desired);
        assertThat(result).isEqualTo(desired);
    }

    @Test
    void testUnique_FileExistsGeneratesVariant(@TempDir Path tempDir) throws IOException {
        Path existingFile = tempDir.resolve("song.mp3");
        Files.createFile(existingFile);

        Path result = PathAudioUtil.unique(existingFile);
        assertThat(result).isEqualTo(tempDir.resolve("song (1).mp3"));
        assertThat(result).isNotEqualTo(existingFile);
    }

    @Test
    void testUnique_MultipleFilesExistGeneratesSequentialVariants(@TempDir Path tempDir) throws IOException {
        Path baseFile = tempDir.resolve("audio.wav");
        Files.createFile(baseFile);
        Files.createFile(tempDir.resolve("audio (1).wav"));
        Files.createFile(tempDir.resolve("audio (2).wav"));

        Path result = PathAudioUtil.unique(baseFile);
        assertThat(result).isEqualTo(tempDir.resolve("audio (3).wav"));
    }

    @Test
    void testUnique_FileNoExtensionExists(@TempDir Path tempDir) throws IOException {
        Path existingFile = tempDir.resolve("audio");
        Files.createFile(existingFile);

        Path result = PathAudioUtil.unique(existingFile);
        assertThat(result).isEqualTo(tempDir.resolve("audio (1)"));
    }

    @Test
    void testUnique_FileWithMultipleDots(@TempDir Path tempDir) throws IOException {
        Path existingFile = tempDir.resolve("audio.file.mp3");
        Files.createFile(existingFile);

        Path result = PathAudioUtil.unique(existingFile);
        assertThat(result).isEqualTo(tempDir.resolve("audio.file (1).mp3"));
    }

    @Test
    void testUnique_FileInSubdirectory(@TempDir Path tempDir) throws IOException {
        Path subDir = tempDir.resolve("subdir");
        Files.createDirectory(subDir);
        Path existingFile = subDir.resolve("song.flac");
        Files.createFile(existingFile);

        Path result = PathAudioUtil.unique(existingFile);
        assertThat(result).isEqualTo(subDir.resolve("song (1).flac"));
    }

    @Test
    void testConstructor_Private() throws Exception {
        var constructor = PathAudioUtil.class.getDeclaredConstructor();
        assertThat(constructor.isAccessible()).isFalse();

        constructor.setAccessible(true);

        assertThatCode(constructor::newInstance).doesNotThrowAnyException();
    }

    @Test
    void testUnique_FileWithOnlySpacesInName(@TempDir Path tempDir) throws IOException {
        Path existingFile = tempDir.resolve("   .txt");
        Files.createFile(existingFile);

        Path result = PathAudioUtil.unique(existingFile);
        assertThat(result).isEqualTo(tempDir.resolve("    (1).txt"));
    }

    @Test
    void testUnique_FileWithSpecialCharacters(@TempDir Path tempDir) throws IOException {
        Path existingFile = tempDir.resolve("file@name#.mp3");
        Files.createFile(existingFile);

        Path result = PathAudioUtil.unique(existingFile);
        assertThat(result).isEqualTo(tempDir.resolve("file@name# (1).mp3"));
    }

    // ---- Fixed cross-platform versions ----

    @Test
    void testMapToOutput_SingleFileInputWithBackslash() {
        Path inputRoot = Path.of("C:", "input", "song.mp3");
        Path outputRoot = Path.of("C:", "output");
        String newExt = "flac";

        Path result = PathAudioUtil.mapToOutput(inputRoot, inputRoot, outputRoot, newExt);

        Path expected = Path.of("C:", "output", "song.flac");
        assertThat(result).isEqualTo(expected);
    }

    @Test
    void testMapToOutput_SingleFileInputWithMixedSlashes() {
        Path inputRoot = Path.of("C:", "input", "song.mp3");
        Path outputRoot = Path.of("C:", "output");
        String newExt = "flac";

        Path result = PathAudioUtil.mapToOutput(inputRoot, inputRoot, outputRoot, newExt);

        Path expected = Path.of("C:", "output", "song.flac");
        assertThat(result).isEqualTo(expected);
    }

    @Test
    void testMapToOutput_SingleFileInputWithTrailingBackslash() {
        Path inputRoot = Path.of("C:", "input", "song.mp3").resolve("");
        Path outputRoot = Path.of("C:", "output");
        String newExt = "flac";

        Path result = PathAudioUtil.mapToOutput(inputRoot, inputRoot, outputRoot, newExt);

        Path expected = Path.of("C:", "output", "song.flac");
        assertThat(result).isEqualTo(expected);
    }

    @Test
    void testMapToOutput_DirectoryInputWithTrailingSlash() {
        Path inputRoot = Path.of("/input").resolve("");
        Path inputFile = Path.of("/input/song.wav");
        Path outputRoot = Path.of("/output");
        String newExt = "mp3";

        Path result = PathAudioUtil.mapToOutput(inputRoot, inputFile, outputRoot, newExt);
        assertThat(result).isEqualTo(Path.of("/output/song.mp3"));
    }

    @Test
    void testMapToOutput_RelativePaths() {
        Path inputRoot = Path.of("input");
        Path inputFile = Path.of("input/song.wav");
        Path outputRoot = Path.of("output");
        String newExt = "mp3";

        Path result = PathAudioUtil.mapToOutput(inputRoot, inputFile, outputRoot, newExt);
        assertThat(result).isEqualTo(Path.of("output/song.mp3"));
    }

    @Test
    void testUnique_FileWithExistingHighNumber(@TempDir Path tempDir) throws IOException {
        Path baseFile = tempDir.resolve("test.txt");
        Files.createFile(baseFile);
        Files.createFile(tempDir.resolve("test (100).txt"));

        Path result = PathAudioUtil.unique(baseFile);
        assertThat(result).isEqualTo(tempDir.resolve("test (1).txt"));
    }

    @Test
    void testUnique_FileWithGapsInNumbering(@TempDir Path tempDir) throws IOException {
        Path baseFile = tempDir.resolve("test.txt");
        Files.createFile(baseFile);
        Files.createFile(tempDir.resolve("test (1).txt"));
        Files.createFile(tempDir.resolve("test (3).txt"));

        Path result = PathAudioUtil.unique(baseFile);
        assertThat(result).isEqualTo(tempDir.resolve("test (2).txt"));
    }
}
