package Controller;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import com.google.gson.Gson;

import Connection.MessageProtocol;
import Dati.Hotel;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Modality;
import javafx.stage.Stage;

import Connection.Client;

public abstract class Controller {
    private Client client = Client.getInstance();
    protected static Stage currentStage;
    protected static Stage currentPopUp;
    protected static Scene mainScene;

    public static void setMainScene(Scene scene){
        mainScene = scene;
    }

    public static void setCurrentSatge(Stage stage){
        currentStage = stage;
    }

    public static void setCurrentPopUp(Stage stage){
        currentPopUp = stage;
    }
    
    protected void switchToScene(String scenePath) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource(scenePath));
        Scene scene = new Scene(root);
        currentStage.setScene(scene);
        Controller.setCurrentSatge(currentStage);
    }

    protected void showPopUp(String title, String scenePath) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource(scenePath));
        Scene scene = new Scene(root);

        Stage popupStage = new Stage();
        popupStage.initModality(Modality.APPLICATION_MODAL);
        popupStage.setTitle(title);
        popupStage.setScene(scene);
        Controller.setCurrentPopUp(popupStage);
        popupStage.showAndWait();
    }

    public void backHome() throws Exception{
        currentStage.setScene(mainScene);
        currentStage.show();
    }

    public void showAlert(String title, String message){
        Alert errorAlert = new Alert(AlertType.ERROR);
        String[] splitted = message.split(":");
        if(splitted.length > 1) message = splitted[splitted.length-2];
        errorAlert.setResizable(true);
        errorAlert.setHeaderText(title);
        errorAlert.setContentText(message);
        errorAlert.showAndWait();
    }

    protected String hashPassword(String psw) throws RuntimeException{
        String hashPassword = null;
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedHash = digest.digest(psw.getBytes(StandardCharsets.UTF_8));

            StringBuilder hash = new StringBuilder();
            for (byte b : encodedHash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hash.append('0');
                }
                hash.append(hex);
            }
            hashPassword = hash.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Errore nella codifica della password");
        }
        return hashPassword;
    }

    protected Hotel getHotel(String name, String city){
        Hotel hotel = null;
        try{
            client.send("search,0,client,no", client.createJsonData("name,city", name+","+city));
            MessageProtocol message = client.receive();

            if(message.getCode() == 201) hotel = new Gson().fromJson(message.getData(), Hotel.class);
            else{
                showAlert("Errore", message.getData().toString());
            }
        }catch(Exception e){
            showAlert("Errore", "Errore nella comunicazione con il server\n"+e.getMessage());
        }
        return hotel;
    }

    public void logout() throws Exception{

        client.send("logout,0,client,yes", Client.getLoggedUser().serialize());

        MessageProtocol message = client.receive();
        if(message.getCode() == 200){
            Client.setLoggedUser(null);
            client.logout();
            backHome();
        }else{
            showAlert("Errore", message.getData().toString());
        }
    }
}