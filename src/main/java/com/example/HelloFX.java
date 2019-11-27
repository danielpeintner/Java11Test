package com.example;

import com.example.jaxb.Project;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import org.controlsfx.control.HyperlinkLabel;
import org.controlsfx.control.table.TableFilter;

public class HelloFX extends Application {

    @Override
    public void start(Stage stage) {
        // Top
        String javaVersion = System.getProperty("java.version");
        String javafxVersion = System.getProperty("javafx.version");
        Label l = new Label("Hello, JavaFX " + javafxVersion + ", running on Java " + javaVersion + ".");
        // Center
        TabPane tabPane = new TabPane();
        tabPane.getTabs().add(getTabControlsFX());
        // setup
        BorderPane bp = new BorderPane();
        bp.setTop(l);
        bp.setCenter(tabPane);

        Scene scene = new Scene(bp, 640, 480);
        stage.setScene(scene);
        stage.show();
    }


    Tab getTabControlsFX() {
        Tab t = new Tab("ControlsFX");
        BorderPane bp = new BorderPane();
        t.setContent(bp);

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
        // Center
        bp.setCenter(getTableView());
        // Bottom
        bp.setBottom(hyperlinkLabel);

        return t;
    }

    TableView getTableView() {
        TableView tableView = new TableView();

        TableColumn<String, Person> column1 = new TableColumn<>("First Name");
        column1.setCellValueFactory(new PropertyValueFactory<>("firstName"));


        TableColumn<String, Person> column2 = new TableColumn<>("Last Name");
        column2.setCellValueFactory(new PropertyValueFactory<>("lastName"));


        tableView.getColumns().add(column1);
        tableView.getColumns().add(column2);

        tableView.getItems().add(new Person("John", "Doe"));
        tableView.getItems().add(new Person("Jane", "Deer"));

        // Issue Caused by: java.lang.IllegalArgumentException: Invalid URL: Invalid URL or resource not found
        // TableFilter<Person> tableFilter = TableFilter.forTableView(tableView).apply();

        return tableView;
    }

    public static void main(String[] args) {
        launch();
    }


    public static class Person {

        private String firstName = null;
        private String lastName = null;

        public Person() {
        }

        public Person(String firstName, String lastName) {
            this.firstName = firstName;
            this.lastName = lastName;
        }

        public String getFirstName() {
            return firstName;
        }

        public void setFirstName(String firstName) {
            this.firstName = firstName;
        }

        public String getLastName() {
            return lastName;
        }

        public void setLastName(String lastName) {
            this.lastName = lastName;
        }
    }
}