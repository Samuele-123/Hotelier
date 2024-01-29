package MessageProtocol;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

/**
 * Classe che rappresenta il protocollo di comunicazione tra client e server
 * @param header corrisponde all'header del messaggio
 * @param message corrisponde al messaggio da inviare che deve essere in formato JSON
 * 
 * La classe mette a dissposizione i metodi per visualizzare i campi dell'header e del messaggio, inoltre ha 
 * due costruttori, uno che prende in input l'header e il messaggio e l'altro che prende in input il messaggio ricevuto come stringa
 * e lo divide in header e dati.
 * 
 * La classe mette a disposizioone i seguenti metodi:
 * - getOp() che restituisce l'operazione da eseguire
 * - getCode() che restituisce il codice dell'operazione
 * - getSender() che restituisce il mittente del messaggio
 * - getAuth() che restituisce l'autenticazione del mittente
 * - getHeader() che restituisce l'header del messaggio sotto forma di JsonObject
 * - getData() che restituisce i dati del messaggio sotto forma di JsonElement
 * - toString() che restituisce il messaggio sotto forma di stringa
 */
public class MessageProtocol {
    private String op;
    private int code;
    private String sender;
    private String auth;
    private JsonElement data;

    public MessageProtocol(String header, JsonElement message) {
        JsonObject object = this.parseHeader(header);
        this.op = object.get("op").getAsString();
        this.code = object.get("code").getAsInt();
        this.sender = object.get("sender").getAsString();
        this.auth = object.get("auth").getAsString();
        this.data = message;
    }

    public MessageProtocol(String message) throws RuntimeException{
        String[] parts = message.split(";-;");
        if(parts.length > 2) throw new ArrayIndexOutOfBoundsException("Messaggio non valido");

        try{
            JsonElement jsonElement = JsonParser.parseString(parts[0]);
            this.setHeader(jsonElement.getAsJsonObject());

            if(parts.length == 2) this.data = JsonParser.parseString(parts[1]);
        }catch(Exception e){
            if(e instanceof ArrayIndexOutOfBoundsException) throw new RuntimeException("Errore nella sintassi del messaggio "+e.getMessage());
            if(e instanceof JsonSyntaxException) throw new RuntimeException("Errore nel parsing del messaggio "+e.getMessage());
        }
    }

    private void setHeader(JsonObject header){
        this.op = header.get("op").getAsString();
        this.code = header.get("code").getAsInt();
        this.sender = header.get("sender").getAsString();
        this.auth = header.get("auth").getAsString();
    }

    /**
     * Metodo che prende in input la stringa che rappresenta l'header del messaggio
     * e procede a creare il JsonObject che lo rappresenta
     * @param msgHeader
     * @return JsonObject rappresentante l'header del messaggio
     * @throws RuntimeException
     */
    private JsonObject parseHeader(String msgHeader) throws RuntimeException{
        JsonObject object = new JsonObject();
        String[] parts = msgHeader.split(",");
        try{
            if(parts.length != 4) throw new ArrayIndexOutOfBoundsException("Messaggio non valido");
        }catch(Exception e){
            if(e instanceof ArrayIndexOutOfBoundsException) throw new RuntimeException("Errore nella sintassi del messaggio "+e.getMessage());
        }

        object.addProperty("op", parts[0]);
        object.addProperty("code", parts[1]);
        object.addProperty("sender", parts[2]);
        object.addProperty("auth", parts[3]);
        return object;
    }

    public String getOp() {
        return this.op;
    }

    public int getCode() {
        return this.code;
    }

    public String getSender() {
        return this.sender;
    }

    public String getAuth() {
        return this.auth;
    }

    public JsonObject getHeader(){
        JsonObject object = new JsonObject();
        object.addProperty("op", this.op);
        object.addProperty("code", this.code);
        object.addProperty("sender", this.sender);
        object.addProperty("auth", this.auth);
        return object;
    }

    public JsonElement getData() {
        return this.data;
    }

    public String toString(){
        if(this.data == null) return this.getHeader().toString();
        return this.getHeader().toString() +";-;"+ this.data.toString();
    }
}