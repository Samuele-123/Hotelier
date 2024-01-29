package Controller;

import java.util.List;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.layout.HBox;

public class NotificationController extends Controller{
    @FXML
    VBox container;

    public void initData(List<String> notification){
        try{
            if(notification.size() == 0) {
                HBox hbox = createHBox("Non ci sono nuove notifiche");
                container.getChildren().add(hbox);
            }

            while(notification.size() != 0) {
            String str = notification.remove(notification.size()-1);
            HBox hbox = createHBox(str);
            container.getChildren().add(hbox);
        }
        }catch(Exception e){
            showAlert("Errore", "Errore nel recuperare le notifiche: "+e.getMessage());
        }
    }

    private HBox createHBox(String labelText) {
        HBox hbox = new HBox();
        hbox.setAlignment(Pos.CENTER);

        Label label = new Label(labelText);
        label.setFont(new Font(15));
        label.wrapTextProperty().setValue(true);
        hbox.getChildren().add(label);

        return hbox;
    }
}
