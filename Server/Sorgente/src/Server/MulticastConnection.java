package Server;

import java.io.FileInputStream;

import java.net.MulticastSocket;
import java.net.InetAddress;
import java.net.DatagramPacket;
 
import java.util.List;
import java.util.HashMap;
import java.util.Properties;

import Hotel.Hotel;
import SerializerManager.GenericSerializer;

/**
 * Classe che gestisce la connessione UDP per l'aggiornamento di nuovi top hotel per città.
 * Il thread si occupa di inviare un messaggio multicast contenente il nuovo top hotel per una città.
 */
public class MulticastConnection implements Runnable{
    private static HashMap<String, Hotel> topHotels;
    private GenericSerializer<Hotel> hotelSerializer;
    private String multicastAddress;
    private int multicastPort;


    /**
     * Costruttore della classe MulticastConnection, si occupa di inizializzare il thread
     * crea un oggetto GenericSerializer per la gestione dei file JSON e inizializza gli attributi
     * multicastAddress e multicastPort per la connessione UDP.
     * 
     * @param fileConfigPath Percorso del file di configurazione
     */
    public MulticastConnection(String fileConfigPath){
        try (FileInputStream fis = new FileInputStream(fileConfigPath)) {
            Properties configReader = new Properties();
            configReader.load(fis);
            this.hotelSerializer = new GenericSerializer<Hotel>(configReader.getProperty("jsonHotelPath"), "Hotel");
            this.multicastAddress = configReader.getProperty("multicastAddress");
            this.multicastPort = Integer.parseInt(configReader.getProperty("multicastPort"));
        } catch (Exception e) {
            System.err.println("Errore nell'avvio del thread UDP: "+e.getMessage());
            System.exit(1);
        }
    }
    
    /**
     * Metodo che si occupa di inviare un messaggio multicast contenente il nuovo top hotel per una città.
     * 
     * @param messageBytes Array di byte contenente il messaggio da inviare
     * @param group Indirizzo multicast
     * @param multicastSocket Socket multicast
     */
    private void sendMessage(byte[] messageBytes, InetAddress group, MulticastSocket multicastSocket){
        try{
            DatagramPacket packet = new DatagramPacket(messageBytes, messageBytes.length, group, multicastPort);
            multicastSocket.send(packet);
        }catch(Exception e){
            System.err.println("Errore nell'invio del messaggio: "+e.getMessage());
            System.exit(1);
        }
    }

    /**
     * Metodo che si occupa di inviare un messaggio multicast contenente il nuovo top hotel per una città.
     * Il metodo ottiene il gruppo multicast e procede a calcolare i nuovi top hotel per ogni città.
     * Al primo avvio del thread, il metodo ottiene i top hotel e li salva in una HashMap.
     * Successivamente, ad ogni esecuzione del thread, il metodo ottiene i nuovi top hotel e li confronta con quelli salvati in precedenza.
     * Se il nuovo top hotel è diverso da quello salvato in precedenza, il metodo procede a inviare un messaggio multicast contenente il nuovo top hotel.
     */
    @Override
    public void run(){
        try{
            InetAddress group = InetAddress.getByName(multicastAddress);
            MulticastSocket multicastSocket = new MulticastSocket(multicastPort);

            if(topHotels == null) topHotels = getTopHotels();
            else{
                HashMap<String, Hotel> newTopHotels = getTopHotels();

                for(String k : newTopHotels.keySet()){
                    Hotel nuovo = newTopHotels.get(k);
                    Hotel old = topHotels.get(k);

                    if(old != null){
                        if(!old.getName().equals(nuovo.getName())){
                            topHotels.put(k, nuovo);
                            String message = "Il nuovo miglior hotel di "+k+" è "+nuovo.getName()+" con un rank di "+nuovo.getRank()+".";
                            byte[] messageBytes = message.getBytes();
                            sendMessage(messageBytes, group, multicastSocket);
                        }
                    }else if(nuovo != null){
                        topHotels.put(k, nuovo);
                        String message = "Il miglior hotel di "+k+" è "+nuovo.getName()+" con un rank di "+nuovo.getRank()+".";
                        byte[] messageBytes = message.getBytes();
                        sendMessage(messageBytes, group, multicastSocket);
                    }
                }
            }
        
        }catch(Exception e){
            System.err.println("Errore nel thread UDP: "+e.getMessage());
            System.exit(1);
        }
    }

    /**
     * Metodo che si occupa di ottenere i top hotel per ogni città.
     * Il metodo ottiene la lista degli hotel e la ordina in base al rank filtrandoli poi per città.
     * 
     * @return HashMap contenente i top hotel per ogni città
     */
    private HashMap<String, Hotel> getTopHotels(){
        List<Hotel> hotels = null;
        HashMap<String, Hotel> topHotels = new HashMap<String, Hotel>();

        try{
            hotels = this.hotelSerializer.deserialize();
            hotels.sort((Hotel h1, Hotel h2) -> h2.compareTo(h1));
            
            for(Hotel h : hotels){
                if(!topHotels.containsKey(h.getCity())) topHotels.put(h.getCity(), h);
            }

        }catch(Exception e){
            System.err.println("Errore nel thread UDP nella ricerca dei top hotel: "+e.getMessage());
            System.exit(1);
        }

        return topHotels;
    }
}
