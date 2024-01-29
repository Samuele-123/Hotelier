import java.io.FileInputStream;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import Server.MulticastConnection;
import Server.RequestHandler;

import java.nio.channels.Selector;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Classe che si occupa di avviare il server e metterlo in ascolto su una porta
 * Usa un thread schedulato per l'invio di messaggi multicast
 */
public class ServerMain {
    private Properties configReader;
    private int port;
    private String fileConfigPath = "src\\server.config";

    public ServerMain(){
        configReader = new Properties();
    }

    /**
     * Metodo main che si occupa di avviare il server e metterlo in ascolto su una porta
     * Le informazioni di configurazione sono contenute nel file server.config e vengono lette da un oggetto Properties
     * Viene creato un thread schedulato per l'invio di messaggi multicast e viene istanziato un oggetto RequestHandler per la gestione delle richieste
     * 
     * @param args
     */
    public static void main(String[] args) {
        ServerMain server = new ServerMain();
        server.start();
    }

    private void start(){
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

        try(FileInputStream fis = new FileInputStream(fileConfigPath);
            ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
            Selector selector = Selector.open();){

            configReader.load(fis);
            port = Integer.parseInt(configReader.getProperty("port"));

            serverSocketChannel.socket().bind(new InetSocketAddress(port));
            serverSocketChannel.configureBlocking(false);
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

            System.out.println("Server avviato su porta " + port);

            scheduler.scheduleAtFixedRate(new MulticastConnection(fileConfigPath), 0L, Long.parseLong(configReader.getProperty("frequenza")), TimeUnit.SECONDS);

            RequestHandler requestHandler = new RequestHandler(fileConfigPath);
            requestHandler.start(serverSocketChannel, selector);

        }catch(Exception e){
            if(e instanceof FileNotFoundException) System.err.println("File di configurazione non trovato "+e.getMessage());
            else if(e instanceof NumberFormatException) System.err.println("Errore nel parsing del file di configurazione "+e.getMessage());
            else if(e instanceof IOException) System.err.println("Errore durante la connessione "+e.getMessage());
            else System.err.println("Errore generico "+e.getMessage());
        }finally{
            scheduler.shutdown();
            System.out.println("Server chiuso");
            System.exit(0);
        }
    }
}
