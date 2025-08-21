package com.opsify.service;

import com.opsify.audio.converter.service.ConversionListener;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;

public class ConversionListenerTest {

    @Test
    void defaultMethodsAreNoopAndCanBeOverridden() {
        AtomicBoolean started = new AtomicBoolean(false);
        AtomicBoolean done = new AtomicBoolean(false);
        AtomicBoolean errored = new AtomicBoolean(false);
        ConversionListener l = new ConversionListener() {
            @Override public void onStart(int total) { started.set(true); }
            @Override public void onFileDone(Path input, Path output, int d, int t) { done.set(true); }
            @Override public void onError(Path input, Exception e, int d, int t) { errored.set(true); }
        };
        l.onStart(0);
        l.onFileDone(Path.of("a"), Path.of("b"), 0, 0);
        l.onError(Path.of("a"), new RuntimeException("x"), 0, 0);
        assertThat(started.get()).isTrue();
        assertThat(done.get()).isTrue();
        assertThat(errored.get()).isTrue();
    }
}
