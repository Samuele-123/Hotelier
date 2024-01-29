import Connection.Client;
import Connection.MulticastThread;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.scene.Scene;
import javafx.application.Platform;
import javafx.event.EventHandler;

import Controller.Controller;

/**
 * Classe che contiene il main del client, avvia il client e il thread per la ricezione dei messaggi multicast
 * prosegue nel caricamento dell'interfaccia grafica e nella sua visualizzazione
 */
public class MainClient extends Application{
    Client client;
    public static void main(String[] args) throws Exception {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        Client client = Client.getInstance();
        client.connect();

        MulticastThread multicastReceiverThread = MulticastThread.getInstance();
        multicastReceiverThread.start();

        System.out.println("Client started");

        // //Load application
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/layoutFXML/home.fxml"));
        Parent root = loader.load();
        Scene mainScene = new Scene(root);

        Controller controller = loader.getController();
        
        primaryStage.setResizable(false);
        primaryStage.setTitle("Hotelier");
        primaryStage.setScene(mainScene);

        Controller.setMainScene(mainScene);
        Controller.setCurrentSatge(primaryStage);

        primaryStage.setOnHidden(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                multicastReceiverThread.interrupt();
                try{if(client.isLogged()) controller.logout();}catch(Exception e){controller.showAlert("Errore", e.getMessage());}
                Platform.exit();
                System.exit(0);
            }
        });

        primaryStage.show();
    }
}