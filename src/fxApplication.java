import db.DBaseElement;
import db.DClient;
import db.DWork;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.LinkedList;

public class fxApplication extends Application{
    public static void main(String[] args){

        try{
            DBaseElement.openConnection();
        }catch (SQLException e){
            e.printStackTrace();
            return;
        }

        launch(args);

        try{
            DBaseElement.closeConnection();
        }catch (SQLException e){
            e.printStackTrace();
        }


    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        readWorks();
        readClients();

        BorderPane root = new BorderPane();
        root.setCenter(getWorksPane());
        TitledPane clients = new TitledPane("Клиенты", getCientsPane());
        TitledPane consolidateReport = new TitledPane("Сводный отчет",getConsolidatedReportPanel());
        //TitledPane clientReport = new TitledPane("Отчет по клиенту",getClientReportPanel());

        Accordion accordion = new Accordion(clients,consolidateReport);
        root.setRight(accordion);

        Scene scene = new Scene(root);
        primaryStage.setTitle("Учет отработанных часов");
        primaryStage.setScene(scene);
        primaryStage.setOnCloseRequest(event -> {Platform.exit();});
        primaryStage.show();
    }

    private Pane getWorksPane(){

        //TABLE STRUCTURE
        TableView<DWork> tableWorks = new TableView<>(works);
        TableColumn<DWork, LocalDate> colWorkDate = new TableColumn<>("Дата");
        colWorkDate.setCellValueFactory(new PropertyValueFactory<>("WorkDate"));
        colWorkDate.setCellFactory(LocalDateTableCell.getCellFactory());
        colWorkDate.setPrefWidth(100);

        TableColumn<DWork, DClient> colClient = new TableColumn<>("Клиент");
        colClient.setCellValueFactory(new PropertyValueFactory<>("client"));
        colClient.setPrefWidth(200);

        TableColumn<DWork, DClient> colDescription = new TableColumn<>("Описание");
        colDescription.setCellValueFactory(new PropertyValueFactory<>("description"));
        colDescription.setPrefWidth(400);

        TableColumn<DWork, LocalTime> colStartTime = new TableColumn<>("Начало");
        colStartTime.setCellValueFactory(new PropertyValueFactory<>("startTime"));
        colStartTime.setPrefWidth(70);

        TableColumn<DWork, LocalTime> colEndTime = new TableColumn<>("Конец");
        colEndTime.setCellValueFactory(new PropertyValueFactory<>("EndTime"));
        colEndTime.setPrefWidth(70);

        TableColumn<DWork, BigDecimal> colAmount = new TableColumn<>("Часы");
        colAmount.setCellValueFactory(new PropertyValueFactory<>("amount"));
        colAmount.setPrefWidth(50);

        tableWorks.setOnMouseClicked(event -> {
            if(event.getClickCount() < 2) return;
            if(event.getButton()!= MouseButton.PRIMARY) return;
            DWork selDWork = tableWorks.getSelectionModel().getSelectedItem();
            if(selDWork != null) {
                new WorkStage(selDWork).showAndWait();
                if(selDWork.getID()!=0) readWorks();
            }
        });
        tableWorks.setOnKeyPressed(event -> {
            if(event.getCode() == KeyCode.ENTER) {
                DWork selDWork = tableWorks.getSelectionModel().getSelectedItem();
                if(selDWork != null)
                    new WorkStage(selDWork).show();
            }
            else if(event.getCode() == KeyCode.INSERT){
                DWork newWork =new DWork(0);
                new WorkStage(newWork).showAndWait();
                if(newWork.getID()!=0)readWorks();
            }
            else if(event.getCode() == KeyCode.DELETE) {
                DWork selDWork = tableWorks.getSelectionModel().getSelectedItem();
                if (selDWork != null)
                    removeDElement(tableWorks.getSelectionModel().getSelectedItem(), works);
            }
        });

        tableWorks.getColumns().addAll(colWorkDate, colClient, colDescription, colStartTime, colEndTime, colAmount);

        //TOOLBOX
        Button bAdd = new Button("Создать");
        bAdd.setOnAction(event -> {
            DWork newWork =new DWork(0);
            new WorkStage(newWork).showAndWait();
            if(newWork.getID()!=0)readWorks();
        });
        Button bDel = new Button("Удалить");
        bDel.setOnAction(event ->{
            DWork selDWork = tableWorks.getSelectionModel().getSelectedItem();
            if(selDWork!=null)
            removeDElement(selDWork,  works);
        });
        ToolBar toolBar = new ToolBar(bAdd, bDel);

        BorderPane result = new BorderPane();

        result.setTop(toolBar);
        result.setCenter(tableWorks);
        return result;
    }

    private Pane getCientsPane(){
        //TABLE
        TableView<DClient> tableClients = new TableView<>(clients);
        TableColumn<DClient, String> colName = new TableColumn<>("Наименование");
        colName.setPrefWidth(200);
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        tableClients.getColumns().add(colName);
        tableClients.setOnKeyPressed(event -> {
        if(event.getCode() == KeyCode.ENTER) {
            DClient selClient =tableClients.getSelectionModel().getSelectedItem();
            if(selClient!=null)
                new ClientStage(selClient).show();
        }
        else if(event.getCode() == KeyCode.INSERT) {
            DClient newClient = new DClient(0);
            new ClientStage(newClient).showAndWait();
            if(newClient.getID()!=0) readClients();
        }
        else if(event.getCode() == KeyCode.DELETE) {
            DClient selClient = tableClients.getSelectionModel().getSelectedItem();
            if (selClient != null)
                removeDElement(selClient, clients);
        }
    });
        tableClients.setOnMouseClicked(event -> {
            if(event.getClickCount()<2)return;
            DClient selClient =tableClients.getSelectionModel().getSelectedItem();
            if(selClient!=null)
            new ClientStage(selClient).show();
        });

        //TOOLBAR
        Button bAdd = new Button("Создать");
        bAdd.setOnAction(event -> {
            DClient newClient = new DClient(0);
            new ClientStage(newClient).showAndWait();
            if(newClient.getID()!=0) readClients();
        });
        Button bDel = new Button("Удалить");
        bDel.setOnAction(event ->{
            DClient selClient =tableClients.getSelectionModel().getSelectedItem();
            if(selClient!=null)
            removeDElement(selClient, clients);});

        ToolBar toolBar = new ToolBar(bAdd, bDel);

        BorderPane result = new BorderPane();
        result.setTop(toolBar);
        result.setCenter(tableClients);
        return result;
    }

    private Pane getConsolidatedReportPanel(){
        GridPane gridPane =  new GridPane();
        gridPane.setVgap(5);
        gridPane.setHgap(5);

        gridPane.add(new Label("Период с "),0,0);
        gridPane.add(new Label("по "),2,0);

        DatePicker firstDate = new DatePicker();
        firstDate.setMaxWidth(120);
        gridPane.add(firstDate,1,0);

        DatePicker secondDate = new DatePicker();
        secondDate.setMaxWidth(120);
        gridPane.add(secondDate,3,0);

        Button bMake = new Button("Сформировать");
        bMake.setOnAction(event -> Reports.consolidatedReport(firstDate.getValue(), secondDate.getValue()));
        gridPane.add(bMake, 0,2,4,1);

        return gridPane;

    }

    private Pane getClientReportPanel(){
        return new GridPane();
    }
    private void removeDElement(DBaseElement element, ObservableList<?> list){

        if(element.remove()) {
           // tooltipControl.setTooltip(new Tooltip("Запись была удалена"));
            list.remove(element);
        }
//        else
//            tooltipControl.setTooltip(new Tooltip("Запись невозможно удалить"));
//        new Thread(()->{
//            try {Thread.sleep(5000);}catch (InterruptedException e){}
//            Platform.runLater(()->tooltipControl.setTooltip(null));
//        }).start();

    }

    private void readWorks(){
        works.clear();
        works.addAll(DWork.getAll());
    }
    private void readClients(){
        clients.clear();
        clients.addAll(DClient.getAll());
    }

    private ObservableList<DWork> works = FXCollections.observableList(new LinkedList<>());
    private ObservableList<DClient> clients = FXCollections.observableList(new LinkedList<>());
}

