package com.example;

import com.ibm.icu.text.RuleBasedNumberFormat;
import com.udojava.evalex.Expression;

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
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import org.xmlunit.builder.*;
import org.xmlunit.diff.*;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.awt.*;
import java.io.*;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Locale;

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
        tabPane.getTabs().add(getTabApachePOI());
        tabPane.getTabs().add(getTabXMLUnit());
        tabPane.getTabs().add(getTabMath());
        // TODOs: Iconli, Jasperreports, Prefereneces, Access database/jackcess
        // setup
        BorderPane bp = new BorderPane();
        bp.setTop(l);
        bp.setCenter(tabPane);

        Scene scene = new Scene(bp, 640, 480);
        stage.setScene(scene);
        stage.show();
    }

    Tab getTabMath() throws ParserConfigurationException, IOException, SAXException {
        Tab t = new Tab("Math");
        FlowPane flow = new FlowPane(Orientation.VERTICAL);
        t.setContent(flow);

        BigDecimal bd = null;
        // EvalEx
        {
            String exp = "10 + 2 * 10";
            Expression expression = new Expression(exp);
            expression.setPrecision(MathContext.DECIMAL64.getPrecision());
            bd = expression.eval();

            flow.getChildren().add(new Label(exp + " -> " + bd.toPlainString()));
        }

        // icu4j
        {
            RuleBasedNumberFormat rbnfDE = new RuleBasedNumberFormat(Locale.GERMAN, RuleBasedNumberFormat.SPELLOUT);
            RuleBasedNumberFormat rbnfIT = new RuleBasedNumberFormat(Locale.ITALIAN, RuleBasedNumberFormat.SPELLOUT);

            flow.getChildren().add(new Label("DE" + " -> " +rbnfDE.format(bd)));
            flow.getChildren().add(new Label("IT" + " -> " +rbnfIT.format(bd)));
        }

        return t;
    }


    Tab getTabXMLUnit() throws ParserConfigurationException, IOException, SAXException {
        Tab t = new Tab("XMLUnit");
        FlowPane flow = new FlowPane(Orientation.VERTICAL);
        t.setContent(flow);

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        DocumentBuilder dBuilder = dbf.newDocumentBuilder();

        {
            String sc = "<a></a>";
            String st = "<a></a>";
            Document docControl = dBuilder.parse(new ByteArrayInputStream(sc.getBytes()));
            Document docTest = dBuilder.parse(new ByteArrayInputStream(st.getBytes()));

            Diff diff = DiffBuilder.compare(docControl).withTest(docTest).ignoreComments().ignoreWhitespace().withComparisonController(ComparisonControllers.StopWhenDifferent).build();
            flow.getChildren().add(new Label(diff.hasDifferences() + ": " + sc+ " != " + st));
        }
        {
            String sc = "<a>X</a>";
            String st = "<a></a>";
            Document docControl = dBuilder.parse(new ByteArrayInputStream(sc.getBytes()));
            Document docTest = dBuilder.parse(new ByteArrayInputStream(st.getBytes()));

            Diff diff = DiffBuilder.compare(docControl).withTest(docTest).ignoreComments().ignoreWhitespace().withComparisonController(ComparisonControllers.StopWhenDifferent).build();
            flow.getChildren().add(new Label(diff.hasDifferences() + ": " + sc+ " != " + st));
        }

        return t;
    }

    Tab getTabApachePOI() {
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