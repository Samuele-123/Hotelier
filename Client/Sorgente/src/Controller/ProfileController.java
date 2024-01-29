package Controller;

import Dati.Utente;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

import Connection.Client;

public class ProfileController extends Controller{
    @FXML
    Label username, nome, cognome, email, telefono, badge;

    Client client = Client.getInstance();

    public void initData(){
        Utente utente = Client.getLoggedUser();
        username.setText(utente.getUsername());
        nome.setText(utente.getName());
        cognome.setText(utente.getSurname());
        email.setText(utente.getEmail());
        telefono.setText(utente.getPhone());
        badge.setText(utente.getBadge());
    }
}
