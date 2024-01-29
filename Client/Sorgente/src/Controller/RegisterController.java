package Controller;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import Connection.Client;
import Connection.MessageProtocol;
import Dati.Utente;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;

public class RegisterController extends Controller{
    @FXML
    TextField username;
    @FXML
    TextField email;
    @FXML
    TextField password;
    @FXML
    TextField confirmPassword;
    @FXML
    TextField nome;
    @FXML
    TextField cognome;
    @FXML
    TextField telefono;
    @FXML
    DatePicker dataNascita;

    Client client = Client.getInstance();

    private boolean checkEmail(String email) {
        // Definisci la regex per verificare il formato dell'email
        String regexEmail = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
        
        // Crea un oggetto Pattern dalla regex
        Pattern pattern = Pattern.compile(regexEmail);
        
        // Crea un oggetto Matcher per confrontare l'email con la regex
        Matcher matcher = pattern.matcher(email);
        
        // Restituisci true se c'è una corrispondenza completa
        return matcher.matches();
    }

    public void register(ActionEvent event) throws Exception{
        try{
            String username, password, confirmPassword, email, nome, cognome, telefono, dataNascita;

            if((username = this.username.getText()) == null) showAlert("Errore", "Username non valido");
            if((password = this.password.getText()) == null) showAlert("Errore", "Password non valida");
            if((confirmPassword = this.confirmPassword.getText()) == null) showAlert("Errore", "Password non valida");
            if((email = this.email.getText()) == null) showAlert("Errore", "Email non valida");
            if((nome = this.nome.getText()) == null) showAlert("Errore", "Nome non valido");
            if((cognome = this.cognome.getText()) == null) showAlert("Errore", "Cognome non valido");
            if((telefono = this.telefono.getText()) == null) showAlert("Errore", "Numero di telefono non valido");
            if((dataNascita = this.dataNascita.getValue().toString()) == null) showAlert("Errore", "Data di nascita non valida");

            password = hashPassword(password);
            confirmPassword = hashPassword(confirmPassword);

            if(!checkEmail(email)){
                showAlert("Errore", "Email non valida");
                return;
            }

            if(!password.equals(confirmPassword)){
                showAlert("Errore", "Le password non coincidono");
                return;
            }

            if(telefono.length() < 8 || telefono.length() > 15){
                showAlert("Errore", "Numero di telefono non valido");
                return;
            }

            Utente user = new Utente(username, email, password, nome, cognome, telefono, dataNascita);

            client.send("register,0,client,no", user.serialize());
            MessageProtocol message = client.receive();

            if(message.getCode() == 200){
                this.backHome();
            }else{
                showAlert("Errore", message.getData().toString());
            }
        }catch(Exception e){
            if(e instanceof RuntimeException) showAlert("Errore", "Controllare i dati inseriti");
            else showAlert("Errore", "Errore nella comunicazione con il server ");
        }
    }
}
