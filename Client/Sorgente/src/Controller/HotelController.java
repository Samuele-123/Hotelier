package Controller;
import Dati.Hotel;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.Parent;

import org.controlsfx.control.Rating;

import Connection.Client;

public class HotelController extends Controller{
    @FXML
    private Label nome, descrizione, città, telefono, servizi, rank;
    @FXML
    private Rating rate, ratePosizione, ratePulizia, rateServizi, ratePrezzo;
    private Hotel hotel;

    public void initData(Hotel data){
        this.hotel = data;

        nome.setText(hotel.getName());
        descrizione.setText(hotel.getDescription());
        città.setText(hotel.getCity());
        telefono.setText(hotel.getPhone());
        servizi.setText(hotel.getServices());
        rank.setText(String.format("%.1f", hotel.getRank()));

        rate.setRating(hotel.getGlobalRate());
        ratePosizione.setRating(hotel.getRatePosition());
        ratePulizia.setRating(hotel.getRateCleaning());
        rateServizi.setRating(hotel.getRateServices());
        ratePrezzo.setRating(hotel.getRateQuality());

        rate.setDisable(true);
        ratePosizione.setDisable(true);
        ratePulizia.setDisable(true);
        rateServizi.setDisable(true);
        ratePrezzo.setDisable(true);
    }

    protected void showPopUp(String title, String scenePath) throws Exception{
        FXMLLoader loader = new FXMLLoader(getClass().getResource(scenePath));
        Parent root = loader.load();
        Scene scene = new Scene(root);

        RatingController controller = loader.getController();
        controller.init(hotel);

        Stage popupStage = new Stage();
        popupStage.initModality(Modality.APPLICATION_MODAL);
        popupStage.setTitle(title);
        popupStage.setScene(scene);
        Controller.setCurrentPopUp(popupStage);
        popupStage.showAndWait();
    }

    protected void switchToSceneAndLoad(String scenePath, Hotel hotel) throws Exception{
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

    public void recensisci(){
        try{
            if(Client.getLoggedUser() != null){
                showPopUp("Lascia una recensione", "/layoutFXML/rating.fxml");
                backHome();
            }
            else showAlert("Errore", "Devi essere loggato per poter lasciare una recensione");
        }catch(Exception e){
            e.printStackTrace();
        }
    }
}
