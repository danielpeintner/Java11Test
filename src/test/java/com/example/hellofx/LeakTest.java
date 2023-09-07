package com.example.hellofx;

import de.sandec.jmemorybuddy.JMemoryBuddy;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.MenuButton;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public class LeakTest {

    @Test @Disabled
    public void stageLeakWithMenuButton() throws Exception {
        JMemoryBuddy.memoryTest((checker) -> {
            CountDownLatch startupLatch = new CountDownLatch(1);
            CountDownLatch showingLatch = new CountDownLatch(1);
            AtomicReference<Stage> stage = new AtomicReference<>();

            Platform.startup(() -> {
                startupLatch.countDown();
            });
            try {
                startupLatch.await(15, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            Platform.runLater(() -> {
                stage.set(new Stage());
                BorderPane root = new BorderPane();

                ToolBar tb = new ToolBar();
                root.setTop(tb);
                tb.getItems().add(new Button("A"));
                tb.getItems().add(new Button("B"));
                tb.getItems().add(new Button("C"));
                // causes memory leak !!!
                tb.getItems().add(new MenuButton("MB"));

                stage.get().setScene(new Scene(root));
                stage.get().setOnShown(l -> {
                    Platform.runLater(() -> showingLatch.countDown());
                });
                stage.get().show();
            });

            try {
                showingLatch.await(15, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            Platform.runLater(() -> {
                stage.get().close();
            });

            checker.assertCollectable(stage.get());
        });
    }

}
