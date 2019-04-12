package com.example;

import com.example.jaxb.Project;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import org.controlsfx.control.HyperlinkLabel;

public class HelloFX extends Application {

    @Override
    public void start(Stage stage) {
        String javaVersion = System.getProperty("java.version");
        String javafxVersion = System.getProperty("javafx.version");
        Label l = new Label("Hello, JavaFX " + javafxVersion + ", running on Java " + javaVersion + ".");
        BorderPane bp = new BorderPane();
        bp.setTop(l);
        HyperlinkLabel hyperlinkLabel = new HyperlinkLabel("[Link]");
        hyperlinkLabel.setOnAction(event -> {
            Hyperlink link = (Hyperlink)event.getSource();
            final String str = link == null ? "" : link.getText();
            switch(str) {
                case "Link":
                    Project p = new Project();
                    p.setInformation("test");
                    System.out.println("Link clicked: " + p.toString());
                    break;
                default:
                    break;
            }
        });
        bp.setCenter(hyperlinkLabel);
        Scene scene = new Scene(bp, 640, 480);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }

}