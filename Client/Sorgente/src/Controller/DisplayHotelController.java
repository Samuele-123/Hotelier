package Controller;

import java.util.List;
import Dati.Hotel;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;

public class DisplayHotelController extends Controller{
    private List<Hotel> hotels;
    private int index;

    @FXML
    Label nomeCittà, nome1, rank1, telefono1, nome2, rank2, telefono2, nome3, rank3, telefono3;

    @FXML
    Rectangle rect1, rect2, rect3, rect4, rect5;

    @FXML
    Text text1, text2, text3;

    @FXML
    Button nextPage, prevPage;

    public void initData(List<Hotel> hotelList, int idx){
        this.hotels = hotelList;
        this.index = idx;

        if(index == 0) prevPage.setDisable(true);
        else prevPage.setDisable(false);

        if(index+2 >= hotels.size() || index+3 >= hotels.size()) nextPage.setDisable(true);
        else nextPage.setDisable(false);

        try{
            Hotel hotel1 = hotels.get(index);
            Hotel hotel2 = index+2 >= hotels.size() ? null : hotels.get(index+1);
            Hotel hotel3 = index+3 >= hotels.size() ? null : hotels.get(index+2);

            nomeCittà.setText("Hotel a "+hotel1.getCity());
            nome1.setText((index+1)+"°  "+hotel1.getName());
            rank1.setText("Rank: "+String.format("%.1f", hotel1.getRank()));
            telefono1.setText("Tel: "+hotel1.getPhone());

            if(hotel2 != null){
                nome2.setText((index+2)+"°  "+hotel2.getName());
                rank2.setText("Rank: "+String.format("%.1f", hotel2.getRank()));
                telefono2.setText("Tel: "+hotel2.getPhone());
            }else hide(2);

            if(hotel3 != null){
                nome3.setText((index+3)+"°  "+hotel3.getName());
                rank3.setText("Rank: "+String.format("%.1f", hotel3.getRank()));
                telefono3.setText("Tel: "+hotel3.getPhone());
            }else hide(3);

            text1.setOnMouseEntered(e -> rect1.setStroke(Color.BLACK));
            text1.setOnMouseExited(e -> rect1.setStroke(Color.TRANSPARENT));
            text2.setOnMouseEntered(e -> rect3.setStroke(Color.BLACK));
            text2.setOnMouseExited(e -> rect3.setStroke(Color.TRANSPARENT));
            text3.setOnMouseEntered(e -> rect5.setStroke(Color.BLACK));
            text3.setOnMouseExited(e -> rect5.setStroke(Color.TRANSPARENT));
            text1.setOnMouseClicked(e -> switchToSceneAndLoad("/layoutFXML/hotelView.fxml", hotel1));
            text2.setOnMouseClicked(e -> switchToSceneAndLoad("/layoutFXML/hotelView.fxml", hotel2));
            text3.setOnMouseClicked(e -> switchToSceneAndLoad("/layoutFXML/hotelView.fxml", hotel3));

        }catch(Exception e){
            if(e instanceof IndexOutOfBoundsException) showAlert("Errore nella ricerca", "Nessun hotel trovato");
            e.printStackTrace();
        }
    }

    private void hide(int section){
        if(section == 2){
            rect2.setVisible(false);
            rect3.setVisible(false);
            text2.setVisible(false);
            nome2.setVisible(false);
            rank2.setVisible(false);
            telefono2.setVisible(false);
        }else if(section == 3){
            rect4.setVisible(false);
            rect5.setVisible(false);
            text3.setVisible(false);
            nome3.setVisible(false);
            rank3.setVisible(false);
            telefono3.setVisible(false);
        }
    }

    private void switchToSceneAndLoad(String scenePath, List<Hotel> hotel, int idx){
        try{
            FXMLLoader loader = new FXMLLoader(getClass().getResource(scenePath));
            Parent root = loader.load();
            
            DisplayHotelController controller = loader.getController();
            controller.initData(hotel, idx);

            Scene newScene = new Scene(root);
            currentStage.setScene(newScene);
            currentStage.show();
        }catch(Exception e){
           e.printStackTrace();
        }
    }

    public void nextPage(){
        switchToSceneAndLoad("/layoutFXML/displayHotels.fxml", hotels, index+=3);
    }

    public void prevPage(){
        switchToSceneAndLoad("/layoutFXML/displayHotels.fxml", hotels, index-=3);
    }

    private void switchToSceneAndLoad(String scenePath, Hotel hotel){
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
}
