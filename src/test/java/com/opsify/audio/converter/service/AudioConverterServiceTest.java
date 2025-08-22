package com.opsify.audio.converter.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class AudioConverterServiceTest {

    private AudioConverterService service;
    private ConversionListener listener;

    @BeforeEach
    void setUp() {
        service = spy(new AudioConverterService()); // spy to override transcodeAudio
        listener = mock(ConversionListener.class);
    }

    @Test
    void testConvert_singleFile(@TempDir Path tempDir) throws IOException {
        Path inputFile = tempDir.resolve("test.mp3");
        Files.writeString(inputFile, "dummy audio content");

        Path outputDir = tempDir.resolve("out");
        String targetExt = "mp3";

        doNothing().when(service).transcodeAudio(any(), any(), any());

        service.convert(inputFile, outputDir, targetExt, listener);

        verify(listener).onStart(1);
        ArgumentCaptor<Path> inputCaptor = ArgumentCaptor.forClass(Path.class);
        ArgumentCaptor<Path> outputCaptor = ArgumentCaptor.forClass(Path.class);
        verify(listener).onFileDone(inputCaptor.capture(), outputCaptor.capture(), eq(1), eq(1));

        assertThat(inputCaptor.getValue()).isEqualTo(inputFile);
        assertThat(outputCaptor.getValue().getFileName().toString()).endsWith(".mp3");

        verifyNoMoreInteractions(listener);
    }

    @Test
    void testConvert_directory(@TempDir Path tempDir) throws IOException {
        Path dir = tempDir.resolve("audio");
        Files.createDirectories(dir);
        Path file1 = dir.resolve("song1.wav");
        Path file2 = dir.resolve("song2.aac");
        Files.writeString(file1, "dummy content");
        Files.writeString(file2, "dummy content");

        Path outputDir = tempDir.resolve("out");
        String targetExt = "mp3";

        doNothing().when(service).transcodeAudio(any(), any(), any());

        service.convert(dir, outputDir, targetExt, listener);

        verify(listener).onStart(2);
        ArgumentCaptor<Path> inputCaptor = ArgumentCaptor.forClass(Path.class);
        ArgumentCaptor<Path> outputCaptor = ArgumentCaptor.forClass(Path.class);
        verify(listener, times(2)).onFileDone(inputCaptor.capture(), outputCaptor.capture(), anyInt(), eq(2));

        assertThat(inputCaptor.getAllValues()).containsExactlyInAnyOrder(file1, file2);
        assertThat(outputCaptor.getAllValues()).allSatisfy(p -> assertThat(p.getFileName().toString()).endsWith(".mp3"));
    }

    @Test
    void testConvert_noAudioFiles(@TempDir Path tempDir) throws IOException {
        Path emptyDir = tempDir.resolve("empty");
        Files.createDirectories(emptyDir);

        Path outputDir = tempDir.resolve("out");

        IOException ex = null;
        try {
            service.convert(emptyDir, outputDir, "mp3", listener);
        } catch (IOException e) {
            ex = e;
        }

        assertThat(ex).isNotNull();
        assertThat(ex.getMessage()).contains("No audio files found");
        verifyNoInteractions(listener);
    }

    @Test
    void testProcessFile_handlesException(@TempDir Path tempDir) throws IOException {
        Path inputFile = tempDir.resolve("bad.mp3");
        Files.writeString(inputFile, "dummy content");

        Path outputDir = tempDir.resolve("out");

        doThrow(new IOException("forced error")).when(service).transcodeAudio(any(), any(), any());

        service.convert(inputFile, outputDir, "mp3", listener);

        verify(listener).onStart(1);
        verify(listener).onError(eq(inputFile), any(IOException.class), eq(1), eq(1));
    }

    // ---------- Additional Tests ----------

    @Test
    void testConvert_nestedDirectories(@TempDir Path tempDir) throws IOException {
        Path nestedDir = tempDir.resolve("a/b/c");
        Files.createDirectories(nestedDir);
        Path file1 = nestedDir.resolve("song1.wav");
        Files.writeString(file1, "dummy content");

        Path outputDir = tempDir.resolve("out");
        doNothing().when(service).transcodeAudio(any(), any(), any());

        service.convert(tempDir, outputDir, "mp3", listener);

        verify(listener).onStart(1);
        verify(listener).onFileDone(any(), any(), eq(1), eq(1));
    }

    @Test
    void testConvert_skipsNonAudioFiles(@TempDir Path tempDir) throws IOException {
        Path file1 = tempDir.resolve("song.txt");
        Path file2 = tempDir.resolve("song.mp3");
        Files.writeString(file1, "dummy content");
        Files.writeString(file2, "dummy content");

        Path outputDir = tempDir.resolve("out");
        doNothing().when(service).transcodeAudio(any(), any(), any());

        service.convert(tempDir, outputDir, "wav", listener);

        verify(listener).onStart(1); // only one audio file
        verify(listener).onFileDone(eq(file2), any(), eq(1), eq(1));
    }

    @Test
    void testConvert_generatesUniqueOutput(@TempDir Path tempDir) throws IOException {
        Path file1 = tempDir.resolve("song.mp3");
        Files.writeString(file1, "dummy content");

        Path outputDir = tempDir.resolve("out");
        Files.createDirectories(outputDir);

        Path existingFile = outputDir.resolve("song.wav");
        Files.writeString(existingFile, "already exists");

        doNothing().when(service).transcodeAudio(any(), any(), any());

        service.convert(file1, outputDir, "wav", listener);

        verify(listener).onStart(1);
        ArgumentCaptor<Path> outputCaptor = ArgumentCaptor.forClass(Path.class);
        verify(listener).onFileDone(any(), outputCaptor.capture(), eq(1), eq(1));

        assertThat(outputCaptor.getValue().getFileName().toString()).matches("song \\(\\d+\\)\\.wav");
    }

    @Test
    void testConvert_listenerNullDoesNotFail(@TempDir Path tempDir) throws IOException {
        Path file1 = tempDir.resolve("song.mp3");
        Files.writeString(file1, "dummy content");

        Path outputDir = tempDir.resolve("out");

        doNothing().when(service).transcodeAudio(any(), any(), any());

        service.convert(file1, outputDir, "wav", null); // listener is null, should not fail
    }
}
