package Server;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.ByteBuffer;

import java.util.Iterator;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import Hotel.Hotel;
import Utente.Utente;

import MessageProtocol.MessageProtocol;

/*
 * Classe che gestisce le richieste dei client.
 * Il metodo start si occupa di gestire le richieste dei client, gestedndo le richieste di connessione e
 * facendo multiplexing sulle richieste dei client.
 * Se abbiamo una richiesta questa viene gestita dal metodo handle che si occupa di leggere il messaggio, e di eseguire
 * l'operazione richiesta dal client, restituendo sempre un messaggio di risposta.
 * In caso di chiusura da parte del client il metodo handle restituisce false e il metodo start si occupa di rimuovere
 * il client dal selettore.
 */
public class RequestHandler{
    private SocketChannel clientChannel;
    private Server server;
    private Gson gson;

    public RequestHandler(String fileConfigPath) throws ServerException{
            this.server = new Server(fileConfigPath);
            this.gson = new Gson();
    }

    /**
     * Metodo che si occupa di leggere il messaggio dal canale del client.
     * 
     * @param clientChannel Canale del client
     * @param buffer Buffer per la lettura del messaggio
     */
    private String read(SocketChannel clientChannel, ByteBuffer buffer) throws ServerException{
        String message = null;
        int bytesRead = 0;
        try{
            try{ bytesRead = clientChannel.read(buffer); }catch(IOException e){}

            if(bytesRead == -1){
                clientChannel.close();
            }

            if(bytesRead > 0){
                buffer.flip();
                byte[] requestData = new byte[buffer.remaining()];
                buffer.get(requestData);
                message = new String(requestData);
            }
        }catch(IOException e){
            throw new ServerException("Errore nella chiusura dal canale: "+e.getMessage());
        }
        return message;
    }

    /**
     * Metodo che si occupa di scrivere il messaggio sul canale del client.
     * Il protocollo utilizzato è il seguente:
     * Viene inviata prima la lunghezza del messaggio (4 byte) e poi il messaggio vero e proprio.
     * In questo modo il client sa sempre quanti byte deve leggere per ottenere il messaggio.
     * @param clientChannel
     * @param message
     * @throws ServerException
     */
    private void write(SocketChannel clientChannel, String message) throws ServerException{
        try{
            byte[] dataBytes = message.getBytes();
            int dataLength = dataBytes.length;
            ByteBuffer responseBuffer = ByteBuffer.allocate(4 + dataLength);

            responseBuffer.putInt(dataLength);
            responseBuffer.put(message.getBytes());
            responseBuffer.flip();
            
            clientChannel.write(responseBuffer);
        }catch(IOException e){
            throw new ServerException("Errore nella scrittura sul canale: "+e.getMessage());
        }
    }

    /**
     * Metodo che si occupa di creare un messaggio di errore in base all'eccezione lanciata.
     * @param e eccezione
     * @param errorCode codice di errore
     * @return messaggio di errore
     * @throws RuntimeException
     */
    private MessageProtocol messageError(Exception e, String errorCode) throws RuntimeException{
        JsonObject data = new JsonObject();
        data.addProperty("message", e.getMessage());
        
        MessageProtocol response = new MessageProtocol("ERR,"+errorCode+",server,2", data);
        return response;
    }

    /**
     * Metodo che si occupa di gestire la richiesta del client.
     * Il metodo si occupa di leggere il messaggio, di eseguire l'operazione richiesta dal client e di scrivere il messaggio
     * di risposta.
     * Le operazioni disponibili sono:
     * - register: registrazione di un nuovo utente
     * - login: login di un utente
     * - logout: logout di un utente
     * - search: ricerca di un hotel
     * - searchAll: ricerca di tutti gli hotel di una città
     * - showBadge: visualizzazione del badge di un utente
     * - addReview: aggiunta di una recensione ad un hotel
     * - getTopThree: restituzione dei top three hotel
     * 
     * Se l'operazione non è inclusa in quelle disponibili viene restituito un messaggio di errore.
     * 
     * Per ogni operazione viene restituito un messaggio di risposta appropriato, inoltre in caso di fallimento viene inviato un messaggio di errore.
     * @return true se la richiesta è stata gestita correttamente, false se il client ha chiuso la connessione
     * @throws RuntimeException
     */
    private Boolean handle(){
        ByteBuffer buffer = ByteBuffer.allocate(2048);

        try{
            String msgRead = this.read(clientChannel, buffer);
            if(msgRead == null) return false;

            MessageProtocol request = new MessageProtocol(msgRead);
            MessageProtocol response = null;

            switch(request.getOp()){
                case "register":
                try{
                    server.register(request.getData());
                    response = new MessageProtocol("OK,200,server,2", null);
                    this.write(clientChannel, response.toString());
                }catch(Exception e){
                    if(e instanceof ServerException) this.write(clientChannel, this.messageError(e, "403").toString());
                    else throw new RuntimeException("Errore nella registrazione dell'utente: "+e.getMessage());
                }
                break;

                case "login":
                try{
                    Utente user = server.login(request.getData());
                    if(user == null) throw new ServerException("Password errata");
                    response = new MessageProtocol("OK,201,server,2", user.serialize());
                    this.write(clientChannel, response.toString());
                }catch(Exception e){
                    JsonObject data = new JsonObject();
                    data.addProperty("message", e.getMessage());
                    response = new MessageProtocol("ERR,401,server,2", data);
                    if(e instanceof ServerException) this.write(clientChannel, this.messageError(e, "403").toString());
                    else throw new RuntimeException("Errore nel login dell'utente: "+e.getMessage());
                }
                break;

                case "logout":
                try{
                    if(!request.getAuth().equals("yes")) throw new ServerException("Utente non autenticato:");
                    server.logout(request.getData());
                    response = new MessageProtocol("OK,200,server,2", null);
                    this.write(clientChannel, response.toString());
                }catch(Exception e){
                    if(e instanceof ServerException) this.write(clientChannel, this.messageError(e, "401").toString());
                    else throw new RuntimeException("Errore nel logout dell'utente: "+e.getMessage());
                }
                break;

                case "search":
                try{
                    Hotel hotel = server.searchHotel(request.getData());
                    response = new MessageProtocol("OK,201,server,2", hotel.serialize());
                    this.write(clientChannel, response.toString());
                }catch(Exception e){
                    if(e instanceof ServerException) this.write(clientChannel, this.messageError(e, "404").toString());
                    else throw new RuntimeException("Errore nella ricerca dell'hotel: "+e.getMessage());
                }
                break;

                case "searchAll":
                try{
                    List<Hotel> hotels = server.searchAll(request.getData());
                    response = new MessageProtocol("OK,201,server,2", gson.toJsonTree(hotels));
                    this.write(clientChannel, response.toString());
                }catch(Exception e){
                    if(e instanceof ServerException) this.write(clientChannel, this.messageError(e, "404").toString());
                    else throw new RuntimeException("Errore nella ricerca degli hotel per città: "+e.getMessage());
                }
                break;

                case "showBadge":
                try{
                    String badge = server.viewBadge(request.getData());
                    JsonObject data = new JsonObject();
                    data.addProperty("badge", badge);
                    response = new MessageProtocol("OK,201,server,2", data);
                    this.write(clientChannel, response.toString());
                }catch(Exception e){
                    if(e instanceof ServerException) this.write(clientChannel, this.messageError(e, "500").toString());
                    else throw new RuntimeException("Errore nella visualizzazione del badge: "+e.getMessage());
                }
                break;

                case "addReview":
                try{
                    if(!request.getAuth().equals("yes")) throw new ServerException("Utente non autenticato");
                    server.addReview(request.getData());
                    response = new MessageProtocol("OK,200,server,2", null);
                    this.write(clientChannel, response.toString());
                }catch(Exception e){
                    if(e instanceof ServerException) this.write(clientChannel, this.messageError(e, "500").toString());
                    else throw new RuntimeException("Errore nell'aggiunta della recensione: "+e.getMessage());
                }
                break;

                case "getTopThree":
                try{
                    List<Hotel> hotels = server.getTopThree();
                    response = new MessageProtocol("OK,201,server,2", gson.toJsonTree(hotels));
                    this.write(clientChannel, response.toString());
                }catch(Exception e){
                    if(e instanceof ServerException) this.write(clientChannel, this.messageError(e, "500").toString());
                    else throw new RuntimeException("Errore nella restituzione dei top three hotel: "+e.getMessage());
                }
                break;

                default:
                JsonObject data = new JsonObject();
                data.addProperty("message", "Operazione non inclusa in quelle diponibili:");
                this.write(clientChannel, (new MessageProtocol("ERR,400,server,2", data)).toString());
            }
        }catch(Exception e){
            if(e instanceof ServerException) throw new RuntimeException("Errore nella gestione della richiesta (lettura o scrittura): "+e.getMessage());
            else if(e instanceof IllegalArgumentException) throw new RuntimeException("Errore nella creazione di messageProtocol: "+e.getMessage());
            else throw new RuntimeException("Errore generico nella gestione della richiesta: "+e.getMessage());
        }
        return true;
    }

    public void start(ServerSocketChannel serverSocketChannel, Selector selector)throws RuntimeException{
        try{
            while (true) {
                selector.select();

                Iterator<SelectionKey> keys = selector.selectedKeys().iterator();
                while (keys.hasNext()) {
                    SelectionKey selectedKey = keys.next();

                    if (selectedKey.isAcceptable()) {
                        SocketChannel clientChannel = serverSocketChannel.accept();

                        if(clientChannel != null){;
                            clientChannel.configureBlocking(false);
                            clientChannel.register(selector, SelectionKey.OP_READ);
                        }
                    } else if (selectedKey.isReadable()) {
                        this.clientChannel = (SocketChannel) selectedKey.channel();
                        if(this.handle() == false) selectedKey.cancel();
                    }
                    keys.remove();
                }
            }
        }catch(Exception e){
            throw new RuntimeException("Errore nella gestione della richiesta: "+e.getMessage());
        }
    }
}
