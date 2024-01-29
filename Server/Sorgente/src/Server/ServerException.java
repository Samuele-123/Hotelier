package Server;

/**
 * Classe che rappresenta un'eccezione generica del server
 */
public class ServerException extends Exception{
    public ServerException(String message){
        super(message);
    }
}
