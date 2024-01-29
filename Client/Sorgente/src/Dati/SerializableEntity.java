package Dati;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

public abstract class SerializableEntity{
    abstract protected String getId();

    /**
     * Serializza l'oggetto generico in una rappresentazione JSON.
     * 
     * @param object Oggetto da serializzare
     * @return Rappresentazione JSON dell'oggetto
     */
    public synchronized JsonElement serialize(){
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        String objectJson = gson.toJson(this);

        JsonElement jsonElement = JsonParser.parseString(objectJson);

        return jsonElement;
    }
}