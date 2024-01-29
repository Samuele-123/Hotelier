package Controller;
import Connection.Client;
import Connection.MessageProtocol;
import Connection.MulticastThread;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import java.net.URL;
import java.util.List;
import java.util.Arrays;
import java.util.ResourceBundle;


import Dati.Hotel;
import com.google.gson.Gson;
import java.lang.reflect.Type;
import com.google.gson.reflect.TypeToken;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.shape.Circle;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.event.EventHandler;


public class HomeController extends Controller implements Initializable{
    Client client = Client.getInstance();
    @FXML
    TextField searchbar;
    @FXML
    Label nome3, città3, descrizione3, rank3, nome2, città2, descrizione2, rank2, nome1, città1, descrizione1, rank1;
    @FXML
    private Circle ledNotification;

    private void updateHotels(){
        client.send("getTopThree,0,client,no", null);

        MessageProtocol message = client.receive();
        if(message.getCode() == 201){
            Gson gson = new Gson();
            Type type = new TypeToken<List<Hotel>>() {}.getType();
            List<Hotel> hotelsList = gson.fromJson(message.getData(), type);

            Hotel hotel = hotelsList.get(0);
            Hotel hotel2 = hotelsList.get(1);
            Hotel hotel3 = hotelsList.get(2);

            nome1.setText("1°  "+hotel.getName());
            città1.setText(hotel.getCity());
            descrizione1.setText(hotel.getDescription());
            rank1.setText("Rank: "+Float.toString(hotel.getRank()));

            nome2.setText("2°  "+hotel2.getName());
            città2.setText(hotel2.getCity());
            descrizione2.setText(hotel2.getDescription());
            rank2.setText("Rank: "+Float.toString(hotel2.getRank()));

            nome3.setText("3°  "+hotel3.getName());
            città3.setText(hotel3.getCity());
            descrizione3.setText(hotel3.getDescription());
            rank3.setText("Rank: "+Float.toString(hotel3.getRank()));
        }else{
            showAlert("Errore", message.getData().toString());
        }
    }

    private void periodicUpdateHotels(int seconds){
        Duration duration = Duration.seconds(seconds);
        Timeline timeline = new Timeline(new KeyFrame(duration, new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
                updateHotels();
            }
        }));

        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    private void periodicUpdateNotifications(int seconds){
        Duration duration = Duration.seconds(seconds);
        Timeline timeline = new Timeline(new KeyFrame(duration, new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
                if(MulticastThread.getMessages().size() != 0) ledNotification.setVisible(true); 
                else ledNotification.setVisible(false);
            }
        }));

        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    public void initialize(URL location, ResourceBundle resources){
        periodicUpdateHotels(30);
        periodicUpdateNotifications(30);
        updateHotels();
    }

    public void login(ActionEvent event) throws Exception{
        if(client.isLogged()){
            switchToSceneAndLoad("/layoutFXML/profileView.fxml");
        }else{
            showPopUp("Login", "/layoutFXML/login.fxml");
        }
    }

    protected void switchToSceneAndLoad(String scenePath){
        try{
            FXMLLoader loader = new FXMLLoader(getClass().getResource(scenePath));
            Parent root = loader.load();
            
            ProfileController controller = loader.getController();
            controller.initData();

            Scene newScene = new Scene(root);
            currentStage.setScene(newScene);
            currentStage.show();
        }catch(Exception e){
           e.printStackTrace();
        }
    }

    private void searchAll(String città){
        try{
            String[] citiesSet = Client.configReader.getProperty("citiesSet").split(",");
            if(!Arrays.asList(citiesSet).stream().anyMatch(e -> e.equals(città))) throw new RuntimeException("Città non valida");

            client.send("searchAll,0,client,no", client.createJsonData("city", città));
            MessageProtocol message = client.receive();
            if(message.getCode() == 201){
                Gson gson = new Gson();
                Type type = new TypeToken<List<Hotel>>() {}.getType();
                List<Hotel> hotelsList = gson.fromJson(message.getData(), type);

                switchToSceneAndLoad("/layoutFXML/displayHotels.fxml", hotelsList);
            }
        }catch(Exception e){
            showAlert("Errore", "Errore nella ricerca per città "+e.getMessage());
        }
    }

    private void search(String name, String città){
        Hotel hotel = getHotel(name, città);

        if(hotel != null){
            try{
                switchToSceneAndLoad("/layoutFXML/hotelView.fxml", hotel);
            }catch(Exception e){
               e.printStackTrace();
            }
        }
    }

    public void searchHotel(){
        String search = searchbar.getText();
        String[] searchArray = search.split(", ");
        if(searchArray.length == 2){
            search(searchArray[0], searchArray[1]);
        }else if(searchArray.length == 1 && !searchArray[0].contains(",")){
            searchAll(searchArray[0]);
        }else{
            showAlert("Errore", "La ricerca è possibile solo per 'Nome, Città' oppure per Città");
        }
    }

    public void loadNotification(){
    try{
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/layoutFXML/notification.fxml"));
            Parent root = loader.load();
            
            NotificationController controller = loader.getController();
            controller.initData(MulticastThread.getMessages());

            Scene newScene = new Scene(root);
            Stage popupStage = new Stage();
            popupStage.initModality(Modality.APPLICATION_MODAL);
            popupStage.setTitle("Notifiche");
            popupStage.setScene(newScene);
            Controller.setCurrentPopUp(popupStage);
            ledNotification.setVisible(false);
            popupStage.showAndWait();
        }catch(Exception e){
           e.printStackTrace();
        }
    }

    protected void switchToSceneAndLoad(String scenePath, Hotel hotel){
        try{
            FXMLLoader loader = new FXMLLoader(getClass().getResource(scenePath));
            Parent root = loader.load();
            
            HotelController controller = loader.getController();
            controller.initData(hotel);

            Scene newScene = new Scene(root);
            currentStage.setScene(newScene);
            currentStage.show();
        }catch(Exception e){
           e.printStackTrace();
        }
    }

    protected void switchToSceneAndLoad(String scenePath, List<Hotel> hotel){
        try{
            FXMLLoader loader = new FXMLLoader(getClass().getResource(scenePath));
            Parent root = loader.load();
            
            DisplayHotelController controller = loader.getController();
            controller.initData(hotel, 0);

            Scene newScene = new Scene(root);
            currentStage.setScene(newScene);
            currentStage.show();
        }catch(Exception e){
           e.printStackTrace();
        }
    }
}
