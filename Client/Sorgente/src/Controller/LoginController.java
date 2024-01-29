package Controller;
import com.google.gson.Gson;

import Connection.Client;
import Connection.MessageProtocol;
import Dati.Utente;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class LoginController extends Controller{
    @FXML
    private TextField username;
    @FXML
    private PasswordField password;
    Client client = Client.getInstance();

    public void register(ActionEvent event) throws Exception{
        Controller.currentPopUp.close();
        switchToScene("/layoutFXML/registrazione.fxml");
    }

    public void sendLogin(ActionEvent event) throws Exception{
        try{
            client.send("login,0,client,no", client.createJsonData("username,password", username.getText()+","+hashPassword(password.getText())));
            MessageProtocol message = client.receive();
            if(message.getCode() == 201){
                client.login();
                Utente utente = new Gson().fromJson(message.getData(), Utente.class);
                Client.setLoggedUser(utente);

                currentPopUp.close();
                // switchToSceneAndLoad("/layoutFXML/profileView.fxml");
            }else{
                showAlert("Errore", message.getData().toString());
            }
        }catch(Exception e){
            if(e instanceof RuntimeException) showAlert("Errore", "Controllare i dati inseriti");
            else showAlert("Errore", "Errore nella comunicazione con il server\n"+e.getMessage());
        }
    }

    protected void switchToSceneAndLoad(String scenePath) throws Exception{
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
}
