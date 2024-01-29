package Hotel;

/**
 * Classe che gestisce l'eccezione di formato della data.
 */
public class DateFormatException extends Exception{
    public DateFormatException(String message){
        super(message);
    }
}
