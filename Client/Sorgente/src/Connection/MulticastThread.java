package Connection;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedList;
import java.util.Properties;

/**
 * Classe che rappresenta il thread per la ricezione dei messaggi multicast
 * multicastAddress corrisponde all'indirizzo multicast
 * multicastPort corrisponde alla porta multicast
 * messages corrisponde alla lista dei messaggi ricevuti
 * 
 * Il thread si mette in ascolto di messaggi multicast e li inserisce nella lista dei messaggi ricevuti
 */
public class MulticastThread extends Thread{
    private String fileConfigPath = "src\\client.config";
    public static MulticastThread instance = new MulticastThread();
    private String multicastAddress;
    private int multicastPort;
    private static LinkedList<String> messages = new LinkedList<String>();

    public MulticastThread(){
        Properties configReader = new Properties();
        try (FileInputStream fis = new FileInputStream(fileConfigPath)) {
            configReader.load(fis);
            
            this.multicastAddress = configReader.getProperty("multicastAddress");
            this.multicastPort = Integer.parseInt(configReader.getProperty("multicastPort"));
        }catch(Exception e){
            if(e instanceof FileNotFoundException) System.err.println("Errore nell'apertura del file config "+e.getMessage());
            if(e instanceof IOException) System.err.println("Errore nella creazione del MulticastThread "+e.getMessage());
            System.exit(1);
        }
    }

    /**
     * Metodo run del thread
     * Si mette in ascolto di messaggi multicast e li inserisce nella lista dei messaggi ricevuti aggiungendo anche la data e l'ora
     */

    @Override @SuppressWarnings("deprecation")
    public void run(){
        try(MulticastSocket multicastSocket = new MulticastSocket(multicastPort);){

            InetAddress group = InetAddress.getByName(multicastAddress);
            multicastSocket.joinGroup(group);

            byte[] buffer = new byte[2048];
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

            while (true) {
                multicastSocket.receive(packet);
                String message = new String(packet.getData(), 0, packet.getLength());
                LocalDateTime currentDateTime = LocalDateTime.now();
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("[HH:mm dd/MM/yyyy]");
                String formatted = currentDateTime.format(formatter);
                messages.add("["+formatted+"]   "+message);
            }
        }catch(Exception e){
            System.err.println("Errore nella creazione del socket multicast "+e.getMessage());
            System.exit(1);
        }
    }

    public static LinkedList<String> getMessages(){
        return messages;
    }

    public static MulticastThread getInstance(){
        return instance;
    }
}
