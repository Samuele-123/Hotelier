package Server;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.LinkedList;
import java.util.List;
import java.util.Iterator;


import Hotel.Hotel;
import Hotel.Review;
import Hotel.DateFormatException;
import Utente.Utente;
import SerializerManager.GenericSerializer;
import SerializerManager.SerializerException;

/**
 * Classe che si occupa di offrire risposte alle richieste dei client, è il cuore del sistema.
 * Alcuni metodi di questa classe sono implementati ma non utilizzati in quasto sono riservati ad usi futuri del sistema.
 * 
 */
public class Server {
    private Properties configReader;
    private GenericSerializer<Hotel> hotelSerializer;
    private GenericSerializer<Utente> userSerializer;
    private Gson gson;
    private List<String> loggedUser;

    /**
     * Costruttore della classe Server, si occupa di inizializzare gli attributi della classe.
     * Inizializza un oggetto Properties per la lettura del file di configurazione, due oggetti GenericSerializer;
     * uno per la gestione degli hotel e uno per la gestione degli utenti.
     * 
     * @param fileConfigPath Percorso del file di configurazione
     * @throws ServerException Lanciata in caso di errore durante l'inizializzazione
     */
    public Server(String fileConfigPath) throws ServerException{
        this.configReader = new Properties();
        try (FileInputStream fis = new FileInputStream(fileConfigPath)) {
            configReader.load(fis);
            this.hotelSerializer = new GenericSerializer<Hotel>(configReader.getProperty("jsonHotelPath"), "Hotel");
            this.userSerializer = new GenericSerializer<Utente>(configReader.getProperty("jsonUtentiPath"), "Utente");
            this.gson = new Gson();
            loggedUser = new LinkedList<String>();
        }catch(Exception e){
            if(e instanceof FileNotFoundException)throw new ServerException("File di configurazione non trovato: "+e.getMessage());
            if(e instanceof IOException) throw new ServerException("Errore nella lettura del file di configurazione: "+e.getMessage());
            if(e instanceof SerializerException) throw new ServerException("Errore nell'istanziazione del serializzatore: "+e.getMessage());
            else throw new ServerException("Errore generico nell'istanziazione del server: "+e.getMessage());
        }
    }


    /**
     * Ottiene un oggetto Hotel dal file JSON, verificandone la correttezza del formato.
     * @param info
     * @return
     * @throws IllegalArgumentException
     * @throws ServerException
     */
    private Hotel parseToHotel(JsonElement info)throws IllegalArgumentException, ServerException{
        Type hotelType = new TypeToken<Hotel>(){}.getType();
        Hotel hotel = null;

        try{
            hotel = gson.fromJson(info, hotelType);
        }catch(Exception e){
            if(e instanceof JsonSyntaxException) throw new IllegalArgumentException("Errore nel parsing dell'hotel: "+e.getMessage());
            else throw new ServerException("Errore generico nel parseToHotel: "+e.getMessage());
        }

        return hotel;
    }


    /**
     * Verifica che la città inserita sia presente nell'elenco delle città valide
     * @param city
     * @return true se la città è presente nell'elenco delle città valide, false altrimenti
     */
    private Boolean checkCity(String city){
        return configReader.getProperty("citiesSet").contains(city);
    }


    /**
     * ATTUALMENTE NON SUPPORTATO DAL CLIENT
     * Aggiunge un nuovo hotel al file JSON, verificandone la correttezza del formato.
     * @param hotelInfo
     * @throws ServerException
     */
    public void newHotel(JsonElement hotelInfo) throws ServerException{
        try{
            Hotel hotel = this.parseToHotel(hotelInfo);
            if(!this.checkCity(hotel.getCity())) throw new ServerException("Città non valida:");
            this.hotelSerializer.add(hotel);
        }catch(Exception e){
            throw new ServerException("Errore nell'inserimento di un hotel: "+e.getMessage());
        }
    }


    /**
     * ATTUALMENTE NON SUPPORTATO DAL CLIENT
     * Modifica un hotel già presente nel file JSON, verificandone la correttezza del formato.
     * @param hotelInfo
     * @throws ServerException
     */
    public void modifyHotel(JsonElement hotelInfo) throws ServerException{
        try{
            Hotel hotel = this.parseToHotel(hotelInfo);
            if(!this.checkCity(hotel.getCity())) throw new ServerException("Città non valida:");
            this.hotelSerializer.update(hotel);
        }catch(Exception e){
            throw new ServerException("Errore nella modifica di un hotel: "+e.getMessage());
        }
    }


    /**
     * ATTUALMENTE NON SUPPORTATO DAL CLIENT
     * Cancella un hotel già presente nel file JSON, verificandone la correttezza del formato.
     * @param hotelID
     * @throws ServerException
     */
    public void deleteHotel(JsonElement hotelID) throws ServerException{
        try{
            JsonObject hotelObject = hotelID.getAsJsonObject();
            String hotelName = hotelObject.get("name").getAsString();
            String hotelCity = hotelObject.get("city").getAsString();

            this.hotelSerializer.delete(hotelName.concat(hotelCity));
        }catch(Exception e){
            throw new ServerException("Errore nella cancellazione di un hotel: "+e.getMessage());
        }
    }


    /**
     * Si occupa di cercare un hotel nel file JSON, e di restituirlo.
     * La ricerca viene effettuata per hotelID che si ottiene concatenando il nome dell'hotel con la città.
     * @param hotelID
     * @return Hotel
     * @throws ServerException
     */
    public Hotel searchHotel(JsonElement hotelID) throws ServerException{
        Hotel hotel = null;
        try{
            JsonObject hotelObject = hotelID.getAsJsonObject();
            String nomeHotel = hotelObject.get("name").getAsString();
            String città = hotelObject.get("city").getAsString();

            hotel = this.hotelSerializer.deserialize(nomeHotel.concat(città));
        }catch(Exception e){
            if(e instanceof SerializerException) throw new ServerException("Impossibile trovare l'hotel: "+e.getMessage());
            else throw new ServerException("Errore generico nella ricerca di un hotel: "+e.getMessage());
        }
        return hotel;
    }

    /**
     * Si occupa di effettuare una ricerca per città.
     * Procede a recuperare la lista degli hotel e la ordina in base al rank, filtrandoli poi per la città desiderata.
     * @param cittàInfo
     * @return List<Hotel>
     * @throws ServerException
     */
    public List<Hotel> searchAll(JsonElement cittàInfo) throws ServerException{
        List<Hotel> hotels = null;
        try{
            JsonObject cittàObject = cittàInfo.getAsJsonObject();
            String città = cittàObject.get("city").getAsString();
            if(!this.checkCity(città)) throw new IllegalArgumentException("Città non presente nell'elenco delle città valide:");
            hotels = ranking();
            hotels = hotels.stream().filter(hotel -> hotel.getCity().equals(città)).toList();
        }catch(Exception e){
            if(e instanceof IllegalArgumentException) throw new ServerException(e.getMessage());
            throw new ServerException("Errore generico nella ricerca degli hotel per città: "+e.getMessage());
        }
        
        return hotels;
    }


    /**
     * Esegue una verifica di correttezza del formato dell'email.
     * @param email
     * @return true se l'email è nel formato corretto, false altrimenti
     */
    private boolean checkEmail(String email) {
        String regexEmail = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
        Pattern pattern = Pattern.compile(regexEmail);
        Matcher matcher = pattern.matcher(email);
        
        return matcher.matches();
    }


    /**
     * Esegue una verifica di correttezza del formato della data.
     * @param date
     * @return true se la data è nel formato corretto, false altrimenti
     */
    private Boolean checkDate(String date){
            DateTimeFormatter dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE;
    
            try {
                LocalDate.parse(date, dateFormatter);
                return true;
            } catch (DateTimeParseException e) {
                return false;
            }
    }

    /**
     * Ottiene un oggetto Utente dal file JSON e lo restituisce, verificandone la correttezza del formato.
     * @param info
     * @return Utente
     * @throws IllegalArgumentException
     */
    private Utente parseToUser(JsonElement info)throws IllegalArgumentException{
        Type userType = new TypeToken<Utente>(){}.getType();
        Utente user = null;

        try{
            user = gson.fromJson(info, userType);
        }catch(Exception e){
            throw new IllegalArgumentException("Errore nel parsing dell'utente: "+e.getMessage());
        }
        if(!this.checkEmail(user.getEmail())) throw new IllegalArgumentException("Email non valida:");
        if(!this.checkDate(user.getBirthdate())) throw new IllegalArgumentException("Data non valida:");

        return user;
    }


    /**
     * Permette di inserire un nuovo utente nel file JSON, utilizza il metodo parseToUser per trasformare l'oggetto JSON in un oggetto Utente.
     * @param userInfo
     * @throws ServerException
     */
    public void register(JsonElement userInfo) throws ServerException{
        try{
            this.userSerializer.add(this.parseToUser(userInfo));
        }catch(Exception e){
            if(e instanceof IllegalArgumentException) throw new ServerException("Errore nel parsing di un utente: "+e.getMessage());
            if(e instanceof SerializerException) throw new ServerException("Errore nell'inserimento di un utente: "+e.getMessage());
            else throw new ServerException("Errore generico nella fase di registrazione: "+e.getMessage());
        }
    }

    /**
     * Permette di tenere traccia degli utenti attualmente loggati.
     * @param username
     * @throws ServerException
     */
    private void addLoggedUser(String username) throws ServerException{
        try{
            this.loggedUser.add(username);
        }catch(Exception e){
            throw new ServerException("Errore nell'aggiunta dell'utente alla lista degli utenti loggati: "+e.getMessage());
        }
    }


    /**
     * Permette di effettuare il login di un utente, utilizza il JsonElement ricevuto per ottenere verificare che l'utente esista e che la password sia corretta.
     * Dopodichè se l'utente non è già loggato da qualche altro dispositivo aggiunge l'utente alla lista degli utenti loggati.
     * @param userInfo
     * @return Utente
     * @throws ServerException
     */
    public Utente login(JsonElement userInfo) throws ServerException{
        Utente user = null;
        try{
            JsonObject userObject = userInfo.getAsJsonObject();
            String username = userObject.get("username").getAsString();
            String password = userObject.get("password").getAsString();

            user = this.userSerializer.deserialize(username);

            if(!user.getPassword().equals(password)) throw new ServerException("Password errata:");
            else if(this.loggedUser.contains(username)) throw new ServerException("Utente già loggato:");
            else this.addLoggedUser(username);
        }catch(Exception e){
            if(e instanceof SerializerException) throw new ServerException("L'utente non esiste: "+e.getMessage());
            else throw new ServerException("Errore generico nella fase di login: "+e.getMessage());
        }
        return user;
    }

    /**
     * Permette di effettuare il logout di un utente, utilizza il JsonElement ricevuto per ottenere l'username dell'utente.
     * Dopodichè se l'utente è loggato lo rimuove dalla lista degli utenti loggati.
     * @param userInfo
     * @throws ServerException
     */
    public void logout(JsonElement userInfo) throws ServerException{
        try{
            JsonObject userObject = userInfo.getAsJsonObject();
            String username = userObject.get("username").getAsString();

            this.loggedUser.remove(username);
        }catch(Exception e){
            e.printStackTrace();
            throw new ServerException("Errore nella fase di logout: "+e.getMessage());
        }
    }


    /**
     * Permette di ottenere il badge di un utente, utilizza il JsonElement ricevuto per ottenere l'username dell'utente.
     * @param userInfo
     * @throws ServerException
     */
    public String viewBadge(JsonElement userInfo) throws ServerException{
        Utente utente = null;
        try{
            JsonObject userObject = userInfo.getAsJsonObject();
            String username = userObject.get("username").getAsString();

            utente = this.userSerializer.deserialize(username);
        }catch(Exception e){
            throw new ServerException("Errore nell'ottenere il badge dell'utente: "+e.getMessage());
        }

        return utente.getBadge();
    }


    /**Metodo che trasforma un oggetto JSON in un oggetto Review, esegue una serie di controlli per verificare la correttezza del formato.
     * 
     * @param reviewInfo
     * @return Review
     * @throws ServerException
     */
    private Review parseToReview(JsonElement reviewInfo) throws ServerException {
        Type reviewType = new TypeToken<Review>(){}.getType();
        Review review = null;
        try{
            review = gson.fromJson(reviewInfo, reviewType);
            if(!this.checkDate(review.getDate())) throw new DateFormatException("Data non valida:");
            if((review.getGlobalRate() < 0 || review.getGlobalRate() > 5) || (review.getRateCleaning() < 0 || review.getRateCleaning() > 5) || (review.getRatePosition() < 0 || review.getRatePosition() > 5) || (review.getRateQuality() < 0 || review.getRateQuality() > 5) || (review.getRateCleaning() < 0 || review.getRateCleaning() > 5)) throw new IllegalArgumentException("Valore dei rating non valido");
        }catch(Exception e){
            if(e instanceof NumberFormatException) throw new ServerException("Errore nel parsing dei ratings: "+e.getMessage());
            if(e instanceof IllegalArgumentException) throw new ServerException("Errore nel parsing della review: "+e.getMessage());
            if(e instanceof DateFormatException) throw new ServerException("Errore nel parsing della data: "+e.getMessage());
            else throw new ServerException("Errore generico nel parseToReview: "+e.getMessage());
        }
        
        return review;
    }

    /**
     * Metodo che si occupa di cercare una review di un utente e rimuoverla dalla lista delle review dell'hotel per permetterne l'aggiornamento.
     * @param reviews
     * @param userID
     */
    private Boolean searchUserReview(List<Review> reviews, String userID){
        Iterator<Review> iterator = reviews.iterator();

        while (iterator.hasNext()) {
            Review review = iterator.next();
            if (review.getUserId().equals(userID)) {
                reviews.remove(review);
                return true;
            }
        }

        return false;
    }

    /**
     * Metodo che si occupa di aggiungere una review ad un hotel, aggiornando i ratings,il rank dell'hotel
     * ed il numero di recensioni dell'utente se è la prima volta che recensisce questo hotel.
     * @param reviewInfo
     * @throws ServerException
     */
    public void addReview(JsonElement reviewInfo) throws ServerException{
        try{
            Review review = this.parseToReview(reviewInfo);
            Hotel hotel = this.hotelSerializer.deserialize(review.getHotel());
            Utente user = this.userSerializer.deserialize(review.getUserId());

            if(!searchUserReview(hotel.getReviews(), review.getUserId())){
                user.review();
                this.userSerializer.update(user);
            }
            
            hotel.addReview(review);
            computeRank(hotel);

            this.hotelSerializer.update(hotel);
        }catch(Exception e){
            if(e instanceof ServerException) throw new ServerException("Errore nel parsing della recensione: "+e.getMessage());
            if(e instanceof DateFormatException) throw new ServerException("Errore nell'inserimento della review: "+e.getMessage());
            if(e instanceof SerializerException) throw new ServerException("Errore nell'aggiornamento dell'hotel: "+e.getMessage());
            else throw new ServerException("Errore generico nell'aggiunta di una review: "+e.getMessage());
        }
    }



    /**
     * Metodo che si occupa di calcolare il rank di un hotel.
     * Il rank viene calcolato come la media pesata tra il rank globale dell'hotel che conta di più e la media dei ratings delle categorie.
     * @param Hotel
     * @throws ServerException
     */
    public void computeRank(Hotel hotel)throws ServerException{
        try{
            Float categoryRank = (hotel.getRateCleaning() + hotel.getRatePosition() + hotel.getRateServices() + hotel.getRateQuality()) / 4;
            Float rank = Math.round( ((2 * hotel.getGlobalRate() + categoryRank) / 3) * 10.0f) / 10.0f;

            hotel.setRank(rank);
        }catch(Exception e){
            throw new ServerException("Errore nel calcolo del rank dell'hotel: "+e.getMessage());
        }
    }

    /**
     * Metodo che si occupa di ottenere la lista degli hotel e di ordinarla in base al rank.
     * @return List<Hotel>
     * @throws ServerException
     */
    private List<Hotel> ranking() throws ServerException{
        List<Hotel> hotels = null;
        try{
            hotels = this.hotelSerializer.deserialize();
            hotels.sort((Hotel h1, Hotel h2) -> h2.compareTo(h1));
        }catch(Exception e){
            throw new ServerException("Errore nell'ottenere la lista degli hotel: "+e.getMessage());
        }
        return hotels;
    }

    /**
     * Metodo che si occupa di ottenere i migliori 3 hotel in assoluto.
     * @return List<Hotel>
     * @throws ServerException
     */
    public List<Hotel> getTopThree() throws ServerException{
        List<Hotel> hotels = null;
        try{
            hotels = this.ranking();
        }catch(Exception e){
            throw new ServerException("Errore nel calcolo del rank degli hotel: "+e.getMessage());
        }
        return hotels.subList(0, 3);
    }
}