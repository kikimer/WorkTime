import db.DClient;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;



public class ClientStage extends Stage{
    ClientStage(DClient client){
        this.client = client;
        GridPane gridPane = new GridPane();
        gridPane.add(new Label("Наименование:"), 0,0);
        gridPane.add(new Label("Наименование полное:"), 0,1);

        name = new TextField();
        name.setPrefWidth(200);
        gridPane.add(name,1,0,1,1);

        fullName = new TextField();
        fullName.setPrefWidth(200);
        gridPane.add(fullName,1,1, 1,1);

        Button bOk = new Button("OK");
        bOk.setDefaultButton(true);
        bOk.setOnAction(event -> {
            formToObject();
            client.write();
            this.close();
        });

        Button bClose = new Button("Отмена");
        bClose.setOnAction(event -> this.close());
        HBox hBox =  new HBox(bOk, bClose);
        hBox.setAlignment(Pos.CENTER_RIGHT);
        hBox.setSpacing(5);
        gridPane.add(hBox,1,3);
        gridPane.setVgap(2);
        gridPane.setHgap(5);


        setScene(new Scene(gridPane));
        setTitle("Клиент");
        if(client.getID()!=0) objectToForm();
    }

    private void objectToForm(){
        name.setText(client.getName());
        fullName.setText(client.getFullName());
    }

    private void formToObject(){
        client.setName(name.getText());
        client.setFullName(fullName.getText());
    }
    private DClient client;
    private TextField name;
    private TextField fullName;
}
