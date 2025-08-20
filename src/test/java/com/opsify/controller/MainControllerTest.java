package com.opsify.controller;

import com.opsify.service.ConversionListener;
import com.opsify.service.FfmpegAudioConverter;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MainControllerTest {

    @BeforeAll
    static void initFx() throws Exception {
        try {
            CountDownLatch latch = new CountDownLatch(1);
            Platform.startup(latch::countDown);
            latch.await(5, TimeUnit.SECONDS);
        } catch (IllegalStateException ignore) {}
    }

    @Mock FfmpegAudioConverter mockConverter;

    @Test
    void convert_callsServiceAndUpdatesProgressAndLog() throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/main.fxml"));
        CountDownLatch shown = new CountDownLatch(1);
        final Stage[] stageBox = new Stage[1];
        Platform.runLater(() -> {
            try {
                // Inject controller with constructor so dependency is mocked
                loader.setControllerFactory(c -> new MainController(mockConverter));
                Stage stage = new Stage();
                stage.setScene(new Scene(loader.load(), 800, 600));
                stage.show();
                stageBox[0] = stage;
            } catch (Exception e) { throw new RuntimeException(e); } finally { shown.countDown(); }
        });
        shown.await(5, TimeUnit.SECONDS);
        MainController controller = loader.getController();

        // Prepare to capture listener
        ArgumentCaptor<ConversionListener> captor = ArgumentCaptor.forClass(ConversionListener.class);

        Platform.runLater(() -> {
            controller.formatCombo.setValue("wav");
            controller.inputField.setText(Path.of("in.wav").toString());
            controller.outputField.setText(Path.of("out").toString());
            controller.convert();
        });

        // Wait for convert() to call service
        boolean captured = waitFor(() -> {
            try {
                verify(mockConverter, atLeastOnce()).convert(any(), any(), any(), captor.capture());
                return true;
            } catch (AssertionError | IOException e) { return false; }
        }, 30);
        assertThat(captured).isTrue();

        // Simulate progress callbacks
        ConversionListener listener = captor.getValue();
        listener.onStart(2);
        listener.onFileDone(Path.of("a.wav"), Path.of("a.wav"), 1, 2);
        listener.onError(Path.of("b.wav"), new RuntimeException("x"), 1, 2);

        // Check UI updates
        String[] logText = new String[1]; CountDownLatch got = new CountDownLatch(1);
        Platform.runLater(() -> { logText[0] = controller.logArea.getText(); got.countDown(); });
        assertThat(got.await(3, TimeUnit.SECONDS)).isTrue();
        assertThat(logText[0]).contains("Done:").contains("Error:");

        double[] prog = new double[1]; CountDownLatch gp = new CountDownLatch(1);
        Platform.runLater(() -> { prog[0] = controller.progressBar.getProgress(); gp.countDown(); });
        assertThat(gp.await(3, TimeUnit.SECONDS)).isTrue();
        assertThat(prog[0]).isEqualTo(0.5);

        // Close stage
        CountDownLatch closed = new CountDownLatch(1);
        Platform.runLater(() -> { stageBox[0].close(); closed.countDown(); });
        assertThat(closed.await(3, TimeUnit.SECONDS)).isTrue();
    }

    private static boolean waitFor(Check c, int tenths) throws InterruptedException {
        for (int i=0;i<tenths;i++) { if (c.ok()) return true; Thread.sleep(100); }
        return false;
    }
    private interface Check { boolean ok(); }
}
