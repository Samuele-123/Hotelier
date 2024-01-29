package SerializerManager;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

import java.lang.reflect.*;

import com.google.gson.reflect.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import Hotel.Hotel;
import Utente.Utente;


/*
 * Questa classe rappresenta un serializzatore generico per oggetti che estendono SerializableEntity.
 * è una classe generica che può essere utilizzata per serializzare e deserializzare oggetti di tipo Utente e Hotel.
 * I metodi di questa classe sono sincronizzati per evitare problemi di concorrenza durante la lettura e la scrittura del file JSON.
 */
public class GenericSerializer<T extends SerializableEntity>{
    private Path path;
    private Map<String, JsonElement> map;
    private String type;

    /**
     * Costruttore della classe GenericSerializer.
     * 
     * @param path Percorso del file JSON
     * @param type Tipo di oggetto da gestire ("Utente", "Hotel")
     * @throws SerializerException Lanciata in caso di errore durante l'inizializzazione
     */
    public GenericSerializer(String path, String type) throws SerializerException{
        this.path = Path.of(path);
        // Verifica del tipo dell'oggetto che stiamo gestendo
        if(!type.equals("Utente") && !type.equals("Hotel")) throw new SerializerException("Tipo non valido:");
        else this.type = type;

        try{
            // Inizializzazione della mappa dal file JSON
            this.map = getJsonMap();
        }catch(Exception e){
            if(e instanceof IOException) throw new SerializerException("Errore nell'apertura del file JSON: "+e.getMessage());
            else if(e instanceof IllegalArgumentException) throw new SerializerException("Errore riscontrato nella formattazione del file JSON: "+e.getMessage());
        }
    }

    /**
     * Ottiene un array JSON dal file, verificandone la correttezza del formato.
     * 
     * @return Array JSON ottenuto dal file
     * @throws IOException Lanciata in caso di errore durante la lettura del file
     * @throws IllegalArgumentException Lanciata se il contenuto del file non rappresenta un JsonArray
     */
    private synchronized JsonArray getJsonArray() throws IOException, IllegalArgumentException{
        JsonArray arr = null;

        // Legge il contenuto del file JSON come stringa
        String json = Files.readString(this.path);
        // Parsa la stringa JSON in un elemento JsonElement
        JsonElement jsonElement = JsonParser.parseString(json);

        if (jsonElement.isJsonArray()) {
            arr = jsonElement.getAsJsonArray();
        } else {
            throw new IllegalArgumentException("La stringa JSON non rappresenta un JsonArray:");
        }

        return arr;
    }


    /**
     * Ottiene una mappa che associa gli ID degli oggetti alle loro rappresentazioni JSON.
     * 
     * @return Mappa degli oggetti presenti nel file JSON
     * @throws IOException Lanciata in caso di errore durante la lettura del file
     * @throws IllegalArgumentException Lanciata se il contenuto del file non rappresenta un JsonArray
     */
    private synchronized Map<String, JsonElement> getJsonMap() throws IOException, IllegalArgumentException{
        // Crea una mappa vuota per associare gli ID alle rappresentazioni JSON
        Map<String, JsonElement> map = new HashMap<String, JsonElement>();

        // Ottiene l'array JSON utilizzando il metodo getJsonArray()
        JsonArray arr = this.getJsonArray();

        // Itera sull'array JSON e popola la mappa con gli ID come chiavi e le rappresentazioni JSON come valori
        for(int i = 0; i < arr.size(); i++){
            if(type.equals("Utente")) map.put(arr.get(i).getAsJsonObject().get("username").getAsString(), arr.get(i));
            if(type.equals("Hotel")){
                String customId = arr.get(i).getAsJsonObject().get("name").getAsString().concat(arr.get(i).getAsJsonObject().get("city").getAsString());
                map.put(customId, arr.get(i));
            }
        }

        return map;
    }


    /**
     * Deserializza la rappresentazione JSON degli oggetti nella lista.
     * 
     * @return Lista degli oggetti deserializzati
     * @throws SerializerException Lanciata in caso di errore durante la deserializzazione
     */
    public synchronized List<T> deserialize() throws SerializerException{
        List<T> objectList = null;

        try{
            map = getJsonMap();
            Gson gson = new Gson();
            Type listType = null;
            if(type.equals("Utente")) listType = new TypeToken<List<Utente>>(){}.getType();
            else if(type.equals("Hotel")) listType = new TypeToken<List<Hotel>>(){}.getType();
            objectList = gson.fromJson(map.values().toString(), listType);
        }catch(Exception e){
            if(e instanceof JsonSyntaxException) throw new SerializerException("Errore nella deserializzazione da Json ad istanze: "+e.getMessage());
        }

        return objectList;
    }


    /**
     * Deserializza la rappresentazione JSON di un oggetto specificato dall'ID.
     * 
     * @param id ID dell'oggetto da deserializzare
     * @return Oggetto deserializzato
     * @throws SerializerException Lanciata in caso di errore durante la deserializzazione
     */
    public synchronized T deserialize(String id) throws SerializerException{
        T object = null;

        try{
            map = getJsonMap();
            Gson gson = new Gson();
            Type objectType = null;
            
            if(type.equals("Utente")) objectType = new TypeToken<Utente>(){}.getType();
            else if(type.equals("Hotel")) objectType = new TypeToken<Hotel>(){}.getType();
            object = gson.fromJson(map.get(id), objectType);

            if(object == null) throw new SerializerException("Id non valido:");
        }catch(Exception e){
            if(e instanceof JsonSyntaxException) throw new SerializerException("Errore nella deserializzazione da Json ad istanza: "+e.getMessage());
            else throw new SerializerException("Errore generico nella deserializzazione: "+e.getMessage());
        }

        return object;
    }

    /**
     * Scrive la rappresentazione JSON della mappa degli oggetti nel file specificato.
     * 
     * @throws IOException Lanciata in caso di errore durante la scrittura del file
     */
    private synchronized void writeJson() throws IOException{
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        String objectJson = gson.toJson(map.values());

        Files.writeString(this.path, objectJson);
    }

    /**
     * Aggiunge un oggetto alla mappa e aggiorna il file JSON.
     * 
     * @param object Oggetto da aggiungere
     * @throws SerializerException Lanciata in caso di errore durante l'inserimento
     */
    public synchronized void add(T object) throws SerializerException{
        JsonElement objectJson = object.serialize();

        try{
            if(map.containsKey(object.getId())) throw new SerializerException("Id già presente:");
            map.put(object.getId(), objectJson);
            this.writeJson();
        }catch(Exception e){
            if(e instanceof IOException) throw new SerializerException("Errore nell'apertura o nella scrittura del file JSON durante l'inserimento: "+e.getMessage());
            else if(e instanceof IllegalArgumentException) throw new SerializerException("Errore riscontrato nella formattazione del file JSON durante l'inserimento: "+e.getMessage());
            else throw new SerializerException("Errore generico durante l'inserimento: "+e.getMessage());
        }

    }

    /**
     * Elimina un oggetto dalla mappa e aggiorna il file JSON.
     * 
     * @param id ID dell'oggetto da eliminare
     * @throws SerializerException Lanciata in caso di ID non valido o errori durante l'eliminazione
     */
    public synchronized void delete(String id) throws SerializerException{
        // Verifica se la mappa contiene l'ID specificato se l'ID non è presente, solleva un'eccezione
        if(!map.containsKey(id)) throw new SerializerException("Id non valido:");

        try{
            map.remove(id);
            this.writeJson();
        }catch(Exception e){
            if(e instanceof IOException) throw new SerializerException("Errore nell'apertura o nella scrittura del file JSON durante l'eliminazione: "+e.getMessage());
            else throw new SerializerException("Errore generico durante l'eliminazione: "+e.getMessage());
        }
    }

    /**
     * Aggiorna un oggetto nella mappa, eliminando quello esistente e aggiungendo il nuovo oggetto.
     * Infine, aggiorna il file JSON con la mappa modificata.
     * 
     * @param object Oggetto da aggiornare
     * @throws SerializerException Lanciata in caso di ID non valido o errori durante l'aggiornamento
     */
    public synchronized void update(T object) throws SerializerException{
        if(!map.containsKey(object.getId())) throw new SerializerException("Id non valido:");
        try{
            this.delete(object.getId());
            this.add(object);
            this.writeJson();
        }catch(Exception e){
            if(e instanceof IOException) throw new SerializerException("Errore nell'apertura o nella scrittura del file JSON durante l'aggiornamento: "+e.getMessage());
            else throw new SerializerException(e.getMessage());
        }
    }
}