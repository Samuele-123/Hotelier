package SerializerManager;

/**
 * Classe che gestisce l'eccezione del serializzatore.
 */
public class SerializerException extends Exception{
    public SerializerException(String message){
        super(message);
    }
}