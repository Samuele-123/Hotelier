package Connection;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import java.net.InetSocketAddress;

import java.util.Properties;

import Dati.Utente;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/*
 * Classe che utilizza il design pattern Singleton per la gestione del client
 * Offre i metodi per la connessione, la comunicazione e la disconnessione dal server
 */
public class Client {
    private static Utente loggedUser;
    private static Boolean logged = false;
    public static Properties configReader;
    private String fileConfigPath = "src\\client.config";
    private volatile static Client uniqueClient = new Client();
    private SocketChannel socketChannel;
    private String host;
    private int port;

    /**
     * Costruttore privato per la creazione del client
     * Legge i parametri di configurazione dal file config
     * Crea il socketChannel
     * 
     * Tutti i metodi sono sincronizzati per evitare problemi di concorrenza
     * @throws FileNotFoundException
     * @throws IOException
     */
    private Client() {
        configReader = new Properties();
        try (FileInputStream fis = new FileInputStream(fileConfigPath)) {
            configReader.load(fis);
            
            this.host = configReader.getProperty("hostname");
            this.port = Integer.parseInt(configReader.getProperty("port"));

            this.socketChannel = SocketChannel.open();
        }catch(Exception e){
            if(e instanceof FileNotFoundException) System.err.println("Errore nell'apertura del file config "+e.getMessage());
            if(e instanceof IOException) System.err.println("Errore nella creazione del client "+e.getMessage());
            else System.err.println("Errore generico "+e.getMessage());
            System.exit(1);
        }
    }

    public static synchronized Client getInstance(){
        return uniqueClient;
    }

    public synchronized Boolean isLogged(){
        return Client.logged;
    }

    public synchronized void login(){
        Client.logged = true;
    }

    public synchronized void logout(){
        Client.logged = false;
    }

    public static synchronized void setLoggedUser(Utente user){
        Client.loggedUser = user;
    }

    public static synchronized Utente getLoggedUser(){
        return Client.loggedUser;
    }

    /**
     * Metodo che si occupa di connettere il client al server
     * @throws IOException
     */
    public synchronized void connect(){
        try {
            socketChannel.connect(new InetSocketAddress(host, port));
        } catch (IOException e) {
            System.err.println("Errore nella connessione al server "+e.getMessage());
            System.exit(1);
        }
    }

    /**
     * Metodo che crea un JsonObject a partire da due stringhe
     * La prima stringa contiene le chiavi separate da virgole
     * La seconda stringa contiene i valori separati da virgole
     * @param keys
     * @param values
     * @return JsonObject
     * @throws RuntimeException
     */
    public synchronized JsonObject createJsonData(String keys, String values) throws RuntimeException{
        String[] keysArray = keys.split(",");
        String[] valuesArray = values.split(",");

        if(keysArray.length != valuesArray.length) throw new RuntimeException("Errore nella creazione del JsonObject");

        JsonObject object = new JsonObject();
        for(int i = 0; i < keysArray.length; i++){
            object.addProperty(keysArray[i], valuesArray[i]);
        }
        return object;
    }

    /**
     * Metodo che permette di inviare un messaggio al server
     * Il messaggio è un oggetto MessageProtocol che contiene un header e un JsonObject
     * 
     * @param header
     * @param data
     */
    public synchronized void send(String header, JsonElement data){
        try {
            MessageProtocol message = new MessageProtocol(header, data);
            ByteBuffer requestBuffer = ByteBuffer.wrap(message.toString().getBytes());
            socketChannel.write(requestBuffer);
        } catch (IOException e) {
            System.err.println("Errore nell'invio del messaggio "+e.getMessage());
            System.exit(1);
        }
    }

    /**
     * Metodo che permette di ricevere un messaggio dal server
     * Il messaggio è un oggetto MessageProtocol che contiene un header e un JsonObject
     * 
     * @return MessageProtocol response
     */
    public synchronized MessageProtocol receive(){
        MessageProtocol response = null;
        try{
            ByteBuffer lengthBuffer = ByteBuffer.allocate(4);
            int bytesRead = socketChannel.read(lengthBuffer);
            if(bytesRead == -1) throw new IOException("Il server ha chiuso il canale");

            lengthBuffer.flip();
            int dataLength = lengthBuffer.getInt();
            ByteBuffer responseBuffer = ByteBuffer.allocate(dataLength);
            bytesRead = socketChannel.read(responseBuffer);

            if (bytesRead > 0) {
                responseBuffer.flip();

                byte[] responseData = new byte[responseBuffer.remaining()];
                responseBuffer.get(responseData);
                response = new MessageProtocol(new String(responseData));
            }
        } catch (IOException e) {
            System.err.println("Errore nella ricezione del messaggio "+e.getMessage());
            System.exit(1);
        }
        return response;
    }

    /**
     * Metodo che permette di chiudere il socketChannel
     */
    public synchronized void disconnect(){
        try {
            socketChannel.close();
        } catch (IOException e) {
            System.err.println("Errore nella chiusura del client "+e.getMessage());
            System.exit(1);
        }
    }
}
