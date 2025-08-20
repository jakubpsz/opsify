package com.opsify.util;

import java.io.IOException;
import java.nio.file.*;
import java.util.Set;

/**
 * Utilities for audio path operations and filesystem helpers.
 * Small, documented static helpers kept in one place for reuse and testability.
 */
public final class PathAudioUtil {
    private PathAudioUtil() {}

    /** Supported audio extensions used for filtering in directory mode. */
    private static final Set<String> AUDIO_EXTS = Set.of(
            "wav","mp3","aac","ogg","flac","m4a","wma","aiff","aif","alac","opus","oga","mka","m4b","amr"
    );

    /** Return lowercase file extension (no dot) or empty string. */
    public static String ext(Path p) {
        String n = p.getFileName().toString();
        int i = n.lastIndexOf('.');
        return i >= 0 ? n.substring(i + 1).toLowerCase() : "";
    }

    /** True if the path looks like an audio file we should convert. */
    public static boolean isAudio(Path p) { return AUDIO_EXTS.contains(ext(p)); }

    /** Ensure directory exists. */
    public static void ensureDir(Path dir) throws IOException { Files.createDirectories(dir); }

    /** Ensure parent directory of a file path exists. */
    public static void ensureParent(Path file) throws IOException {
        Path parent = file.getParent();
        if (parent != null) Files.createDirectories(parent);
    }

    /** Map inputFile under inputRoot into outputRoot preserving structure and changing ext. */
    public static Path mapToOutput(Path inputRoot, Path inputFile, Path outputRoot, String newExt) {
        Path rel = inputRoot.toAbsolutePath().relativize(inputFile.toAbsolutePath());
        String name = rel.getFileName().toString();
        int dot = name.lastIndexOf('.');
        String base = dot > 0 ? name.substring(0, dot) : name;
        Path outRel = rel.getParent() == null ? Path.of(base + "." + newExt)
                : rel.getParent().resolve(base + "." + newExt);
        return outputRoot.resolve(outRel);
    }

    /** If desired exists, returns a non-conflicting variant "name (n).ext". */
    public static Path unique(Path desired) throws IOException {
        if (!Files.exists(desired)) return desired;
        Path parent = desired.getParent();
        String name = desired.getFileName().toString();
        int dot = name.lastIndexOf('.');
        String base = dot >= 0 ? name.substring(0, dot) : name;
        String ext = dot >= 0 ? name.substring(dot) : "";
        int n = 1;
        Path cand;
        do {
            cand = parent == null ? Path.of(base + " (" + n + ")" + ext) : parent.resolve(base + " (" + n + ")" + ext);
            n++;
        } while (Files.exists(cand));
        return cand;
    }
}
