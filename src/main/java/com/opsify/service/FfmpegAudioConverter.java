/**
 * Audio conversion service that uses FFmpeg (via JavaCV) to convert audio files from any supported format
 * to a requested target format while preserving original file names and directory structure.
 *
 * This class contains only instance methods; helpers are extracted to PathAudioUtil for reuse and testability.
 */
package com.opsify.service;

import com.opsify.util.PathAudioUtil;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.Frame;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static com.opsify.util.PathAudioUtil.*;
import static org.bytedeco.ffmpeg.global.avcodec.*;

@Slf4j
public class FfmpegAudioConverter {

    /** Convert an input path into the output directory. If input is a file converts one, if a directory converts recursively. */
    public void convert(@NonNull Path input, @NonNull Path outputDir, @NonNull String targetExt) throws IOException {
        convert(input, outputDir, targetExt, null);
    }

    /** Convert with optional progress listener. */
    public void convert(@NonNull Path input, @NonNull Path outputDir, @NonNull String targetExt, ConversionListener listener) throws IOException {
        log.info("Starting conversion: input={}, outputDir={}, targetExt={}", input, outputDir, targetExt);
        ensureDir(outputDir);
        if (Files.isRegularFile(input)) {
            if (listener != null) listener.onStart(1);
            Path out = mapToOutput(input.getParent() == null ? Path.of("") : input.getParent(), input, outputDir, targetExt);
            out = unique(out);
            ensureParent(out);
            this.transcodeAudio(input, out, targetExt);
            log.info("Converted file: {} -> {}", input, out);
            if (listener != null) listener.onFileDone(input, out, 1, 1);
            return;
        }
        if (Files.isDirectory(input)) {
            List<Path> files = new ArrayList<>();
            try (Stream<Path> s = Files.walk(input)) {
                s.filter(Files::isRegularFile).filter(PathAudioUtil::isAudio).forEach(files::add);
            }
            int total = files.size();
            if (listener != null) listener.onStart(total);
            int done = 0;
            for (Path p : files) {
                try {
                    Path out = mapToOutput(input, p, outputDir, targetExt);
                    out = unique(out);
                    ensureParent(out);
                    this.transcodeAudio(p, out, targetExt);
                    done++;
                    log.info("Converted file: {} -> {}", p, out);
                    if (listener != null) listener.onFileDone(p, out, done, total);
                } catch (Exception e) {
                    log.error("Failed to convert {}: {}", p, e.getMessage());
                    if (listener != null) listener.onError(p, e, done, total);
                }
            }
            log.info("Finished directory conversion for {} ({} of {} done)", input, done, total);
            return;
        }
        throw new IOException("Input does not exist: " + input);
    }

    /** Transcode audio using JavaCV (bundled FFmpeg), no external ffmpeg binary required. */
    protected void transcodeAudio(Path input, Path output, String targetExt) throws IOException {
        // Fast path for same extension: validate audio stream, then copy without re-encode.
        if (ext(input).equalsIgnoreCase(targetExt)) {
            FFmpegFrameGrabber g = new FFmpegFrameGrabber(input.toFile());
            try {
                g.start();
                boolean valid = false;
                for (int i=0; i<5; i++) {
                    Frame f = g.grabSamples();
                    if (f != null && f.samples != null && f.samples.length > 0) { valid = true; break; }
                }
                if (!valid) throw new IOException("No audio stream found in " + input);
            } catch (org.bytedeco.javacv.FrameGrabber.Exception e) {
                throw new IOException("JavaCV transcode failed: " + e.getMessage(), e);
            } finally {
                try { g.stop(); } catch (Exception ignored) {}
                try { g.release(); } catch (Exception ignored) {}
            }
            Files.copy(input, output, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            return;
        }
        log.debug("transcoding (JavaCV): {} -> {}", input, output);
        FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(input.toFile());
        FFmpegFrameRecorder recorder = null;
        try {
            grabber.start();
            int sampleRate = grabber.getSampleRate() > 0 ? grabber.getSampleRate() : 44100;
            int channels = grabber.getAudioChannels() > 0 ? grabber.getAudioChannels() : 2;

            recorder = new FFmpegFrameRecorder(output.toFile(), channels);
            recorder.setFormat(targetExt.toLowerCase());
            switch (targetExt.toLowerCase()) {
                case "mp3": recorder.setAudioCodec(AV_CODEC_ID_MP3); break;
                case "aac":
                case "m4a": recorder.setAudioCodec(AV_CODEC_ID_AAC); break;
                case "ogg":
                case "oga": recorder.setAudioCodec(AV_CODEC_ID_VORBIS); break;
                case "opus": recorder.setAudioCodec(AV_CODEC_ID_OPUS); break;
                case "flac": recorder.setAudioCodec(AV_CODEC_ID_FLAC); break;
                case "wav": recorder.setAudioCodec(AV_CODEC_ID_PCM_S16LE); break;
                default: /* allow FFmpeg default */ break;
            }
            recorder.setSampleRate(sampleRate);
            recorder.setAudioChannels(channels);
            recorder.setAudioBitrate(192000);
            recorder.start();

            Frame frame;
            boolean hadSamples = false;
            while ((frame = grabber.grabSamples()) != null) {
                hadSamples = true;
                recorder.recordSamples(frame.sampleRate, frame.audioChannels, frame.samples);
            }
            if (!hadSamples) throw new IOException("No audio stream found in " + input);
        } catch (org.bytedeco.javacv.FrameGrabber.Exception | org.bytedeco.javacv.FrameRecorder.Exception e) {
            throw new IOException("JavaCV transcode failed: " + e.getMessage(), e);
        } finally {
            try { if (recorder != null) recorder.stop(); } catch (Exception ignored) {}
            try { if (recorder != null) recorder.release(); } catch (Exception ignored) {}
            try { grabber.stop(); } catch (Exception ignored) {}
            try { grabber.release(); } catch (Exception ignored) {}
        }
    }
}
