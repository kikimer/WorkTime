import db.DClient;
import db.DWork;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import javafx.util.converter.BigDecimalStringConverter;
import javafx.util.converter.LocalTimeStringConverter;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;


public class WorkStage extends Stage{
    WorkStage(DWork dWork){
        this.dWork = dWork;
        //КОНВЕРТОРЫ
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        timeConverter = new LocalTimeStringConverter(timeFormatter, timeFormatter);
        bigDecimalStringConverter = new BigDecimalStringConverter();

        //ДАТА
        Label lWorkDate = new Label("Дата:");
        lWorkDate.setMinSize(40,30);
        GridPane.setConstraints(lWorkDate,0,0);

        workDate = new DatePicker();
        GridPane.setConstraints(workDate,1,0);
        workDate.setMaxWidth(120);

        //КЛИЕНТ
        Label lClient = new Label("Клиент:");
        lClient.setMinSize(50,30);
        GridPane.setConstraints(lClient,2,0);

        client = new ComboBox<>(FXCollections.observableList(DClient.getAll()));
        GridPane.setConstraints(client,3,0);
        client.setPrefWidth(300);

        //НАЧАЛО
        Label lStartTime = new Label("С:");
        lStartTime.setMinSize(10,30);
        GridPane.setConstraints(lStartTime,4,0);

        startTime = new TextField();
        startTime.setMaxWidth(60);
        startTime.setTextFormatter( new TextFormatter<>(timeConverter, LocalTime.of(0,0), InputFormats::formatTime));
        startTime.setOnAction(event -> calculateAmount());
        GridPane.setConstraints(startTime,5,0);

        //КОНЕЦ
        Label lEndTime = new Label("по");
        lEndTime.setMinSize(10,30);
        GridPane.setConstraints(lEndTime,6,0);

        endTime = new TextField();
        endTime.setMaxWidth(60);
        endTime.setTextFormatter(new TextFormatter<>(timeConverter, LocalTime.of(0,0) , InputFormats::formatTime));
        endTime.setOnAction(event -> calculateAmount());
        GridPane.setConstraints(endTime,7,0);

        //ЧАСЫ
        Label lAmount = new Label("Всего:");
        lAmount.setMinSize(30,30);
        GridPane.setConstraints(lAmount,8,0);

        amount = new TextField();
        amount.setMaxWidth(60);
        amount.setTextFormatter(new TextFormatter<>(bigDecimalStringConverter, BigDecimal.ZERO , InputFormats::formatDigit));
        GridPane.setConstraints(amount,9,0);

        //ОПИСАНИЕ
        Label lDescription = new Label("Описание:");
        lDescription.setMinSize(30,30);
        GridPane.setConstraints(lDescription,0,1);

        description = new TextArea();

        //OK
        Button bOk = new Button("OK");
        bOk.setDefaultButton(true);
        bOk.setOnAction(event -> {
            this.formToObject();
            dWork.write();
            this.close();
        });

        //Close
        Button bClose = new Button("Отмена");
        bClose.setOnAction(event -> {
            this.close();
        });
        HBox hBox =  new HBox(bOk, bClose);
        hBox.setAlignment(Pos.CENTER_RIGHT);
        hBox.setSpacing(5);
        GridPane gridPane = new GridPane();
        gridPane.setHgap(5);
        gridPane.getChildren().addAll(lWorkDate, workDate, lClient, client, lStartTime, startTime, lEndTime, endTime, lAmount, amount, lDescription);
        //gridPane.setGridLinesVisible(true);

        BorderPane root = new BorderPane();
        root.setTop(gridPane);
        root.setCenter(description);
        root.setBottom(hBox);
        description.prefWidthProperty().bind(this.widthProperty());
        description.prefHeightProperty().bind(this.heightProperty());

        this.setHeight(300);
        this.setTitle("Выполненная работа");
        this.setScene(new Scene(root));

        if(dWork.getID()!=0)objectToForm();
        else {
            workDate.setValue(LocalDate.now());
        }
    }

    private void calculateAmount(){
        Duration duration = Duration.between(timeConverter.fromString(startTime.getText()), timeConverter.fromString(endTime.getText()));

        //разница округляются по получаса в большую сторону
        long durMinutes = duration.toMinutes();
        if(durMinutes%30>0) durMinutes += 30-durMinutes%30;
        String sResult = Double.toString(durMinutes/60.0);
        Platform.runLater(() -> amount.setText(sResult));

    }

    private void objectToForm(){
        workDate.setValue(dWork.getWorkDate());
        client.getSelectionModel().select(dWork.getClient());
        startTime.setText(timeConverter.toString(dWork.getStartTime()));
        endTime.setText(timeConverter.toString(dWork.getEndTime()));
        description.setText(dWork.getDescription());
        amount.setText(bigDecimalStringConverter.toString(dWork.getAmount()));
    }

    private void formToObject(){
        dWork.setWorkDate(workDate.getValue());
        dWork.setClient(client.getSelectionModel().getSelectedItem());
        dWork.setStartTime(timeConverter.fromString(startTime.getText()));
        dWork.setEndTime(timeConverter.fromString(endTime.getText()));
        dWork.setAmount(bigDecimalStringConverter.fromString(amount.getText()));
        dWork.setDescription(description.getText());
    }

    private DWork dWork;

    private DatePicker workDate;
    private ComboBox<DClient> client;
    private TextField startTime;
    private TextField endTime;
    private TextArea description;
    private TextField amount;
    private StringConverter<LocalTime> timeConverter;
    private BigDecimalStringConverter bigDecimalStringConverter;
}
