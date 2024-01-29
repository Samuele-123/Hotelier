package Controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.controlsfx.control.Rating;

import Connection.Client;
import Connection.MessageProtocol;
import Dati.Hotel;
import Dati.Review;

public class RatingController extends Controller{
    @FXML
    Label nome;
    @FXML
    Rating rate, ratePosizione, ratePulizia, rateServizi, ratePrezzo;
    private Hotel hotel;

    public void init(Hotel data){
        this.hotel = data;
        nome.setText("Stai recensendo: "+hotel.getName());
    }

    public void sendReview(){
        try{
            Client client = Client.getInstance();
            LocalDate currentDate = LocalDate.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            String data = currentDate.format(formatter);

            Review review = new Review(hotel.getId(), Client.getLoggedUser().getId(), (float)rate.getRating(), (float)ratePulizia.getRating(), (float)ratePosizione.getRating(), (float)rateServizi.getRating(), (float)ratePrezzo.getRating(), data);
            client.send("addReview,0,client,yes", review.serialize());

            MessageProtocol message = client.receive();
            if(message.getCode() == 200){
                Controller.currentPopUp.close();
            }else{
                showAlert("Errore", message.getData().toString());
            }
        }catch(Exception e){
            showAlert("Errore", e.getMessage());
        }
    }
}