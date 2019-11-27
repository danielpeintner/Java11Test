package com.example;

import com.example.jaxb.Project;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.stage.Stage;

import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.controlsfx.control.HyperlinkLabel;
import org.controlsfx.control.table.TableFilter;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFCreationHelper;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.awt.*;
import java.io.*;

public class HelloFX extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        // Top
        String javaVersion = System.getProperty("java.version");
        String javafxVersion = System.getProperty("javafx.version");
        Label l = new Label("Hello, JavaFX " + javafxVersion + ", running on Java " + javaVersion + ".");
        // Center
        TabPane tabPane = new TabPane();
        tabPane.getTabs().add(getTabControlsFX());
        tabPane.getTabs().add(getTabJAXB());
        tabPane.getTabs().add(getApachePOI());
        // TODOs: XMLUnit 2.6, Iconli, Jasperreports, Math EvalEx, icu4j, Prefereneces, Access database/jackcess
        // setup
        BorderPane bp = new BorderPane();
        bp.setTop(l);
        bp.setCenter(tabPane);

        Scene scene = new Scene(bp, 640, 480);
        stage.setScene(scene);
        stage.show();
    }


    Tab getApachePOI() {
        Tab t = new Tab("Apache POI");
        FlowPane flow = new FlowPane(Orientation.VERTICAL);
        t.setContent(flow);

        Button bXSLX = new Button("Create XSLX");
        flow.getChildren().add(bXSLX);

        bXSLX.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent e) {
                try {
                    XSSFWorkbook workbook = new XSSFWorkbook();
                    XSSFCreationHelper createHelper = workbook.getCreationHelper();
                    XSSFSheet sheet = workbook.createSheet("Test");

                    File f = File.createTempFile("test", ".xlsx");
                    workbook.write(new FileOutputStream(f));
                    workbook.close();
                    openFile(f);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });

        Button bDOCX = new Button("Create DOCX");
        flow.getChildren().add(bDOCX);

        bDOCX.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent e) {
                try {
                    XWPFDocument document = new XWPFDocument();
                    XWPFParagraph paragraph = document.createParagraph();
                    XWPFRun run = paragraph.createRun();
                    run.setText("Testikone");

                    File f = File.createTempFile("test", ".docx");
                    document.write(new FileOutputStream(f));
                    openFile(f);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });

        return t;
    }

    private static void openFile(File f) {
        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.OPEN)) {
                new Thread(() -> {
                    try {
                        Desktop.getDesktop().open(f);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }).start();
            } else {
                throw new Exception("Error opening file " + f);
            }
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Information Dialog");
            alert.setContentText("Error opening: " + f);
            alert.showAndWait();
        }
    }

    Tab getTabJAXB() throws JAXBException {
        Tab t = new Tab("JAXB");
        FlowPane flow = new FlowPane(Orientation.VERTICAL);
        t.setContent(flow);

        // marshall
        {
            Project p = new Project();
            p.setInformation("foo");

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            marshal(p, baos);

            flow.getChildren().add(new Label("Marshall: " + new String(baos.toByteArray())));
        }

        // unmarshall
        {
            String s = "<project xmlns='http://www.example.com/jaxb'><information>XXX</information></project>";
            InputStream is = new ByteArrayInputStream(s.getBytes());
            Project p = unmarshal(is);

            flow.getChildren().add(new Label("Unmarshall: " + p.toString()));
        }

        return t;
    }

    static Class<Project> CLASS = Project.class;

    public static void marshal(Project proj,
                               OutputStream outputStream) throws JAXBException {
        String packageName = CLASS.getPackage().getName();
        JAXBContext jc = JAXBContext.newInstance(packageName);
        Marshaller m = jc.createMarshaller();
        // m.setProperty("jaxb.formatted.output", Boolean.TRUE);
        m.marshal(proj, outputStream);
    }

    public static Project unmarshal(InputStream inputStream)
            throws JAXBException {
        String packageName = CLASS.getPackage().getName();
        JAXBContext jc = JAXBContext.newInstance(packageName);
        Unmarshaller u = jc.createUnmarshaller();
        Object o = u.unmarshal(inputStream);
        if(o instanceof Project) {
            return (Project)o;
        } else {
            throw new JAXBException("No object of class " + CLASS);
        }
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