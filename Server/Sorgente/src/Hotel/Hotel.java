package Hotel;

import SerializerManager.SerializableEntity;

import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Date;

import java.text.ParseException;
import java.text.SimpleDateFormat;


/**
 * Classe che rappresenta la struttura di un hotel
 * @param name nome dell'hotel
 * @param description descrizione dell'hotel
 * @param city città dell'hotel
 * @param phone numero di telefono dell'hotel
 * @param services servizi offerti dall'hotel
 * 
 * La classe mette a dissposizione i metodi per visualizzare i campi dell'hotel e i ratings
 * e per aggiungere una review all'hotel aggiornando i ratings.
 */
public class Hotel extends SerializableEntity implements Comparable<Hotel>{
    private String name;
    private String description;
    private String city;
    private String phone;
    private ArrayList<String> services;
    private float rate;
    private Ratings ratings = new Ratings(0, 0, 0, 0);
    private List<Review> reviews = new ArrayList<Review>();
    private float rank = 0;


    public Hotel(String name, String description, String city, String phone, String services){
        this.name = name;
        this.description = description;
        this.city = city;
        this.phone = phone;
        this.services = new ArrayList<String>(Arrays.asList(services.split(", ")));
        this.reviews = new ArrayList<Review>();
    }

    public String getId(){
        return this.name.concat(this.city);
    }

    public String getName(){
        return this.name;
    }

    public String getDescription(){
        return this.description;
    }

    public String getCity(){
        return this.city;
    }

    public String getPhone(){
        return this.phone;
    }

    public String getServices(){
        return String.join(", ", this.services);
    }

    public float getGlobalRate(){
        return this.rate;
    }

    public float getRateCleaning(){
        return ratings.cleaning;
    }

    public float getRatePosition(){
        return ratings.position;
    }

    public float getRateServices(){
        return ratings.services;
    }

    public float getRateQuality(){
        return ratings.quality;
    }

    public List<Review> getReviews(){
        return this.reviews;
    }

    public float getRank(){
        return this.rank;
    }

    public void setName(String name){
        this.name = name;
    }

    public void setDescription(String description){
        this.description = description;
    }

    public void setCity(String city){
        this.city = city;
    }

    public void setPhone(String phone){
        this.phone = phone;
    }

    public void setServices(String services){
        this.services = new ArrayList<String>(Arrays.asList(services.split(", ")));
    }

    public void setRank (float rank){
        this.rank = rank;
    }

    public int compareTo(Hotel hotel){
        return Float.compare(this.rank, hotel.getRank());
    }

    /**
     * Calcola il peso di una review in base alla data di pubblicazione
     * @param review
     * @return
     * @throws DateFormatException
     */
    private Float computeWeight(Review review) throws Exception{
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date currentDate = new Date();
        
        try {
            Date dataRecensione = dateFormat.parse(review.getDate());
            // Calcola la differenza in giorni tra la data corrente e la data della review
            long daysDifference = (currentDate.getTime() - dataRecensione.getTime()) / (1000 * 60 * 60 * 24) + 1;
            // Calcola il peso effettivo, più la review è recente più il peso è alto
            return 1.0f / (1 + daysDifference);
        } catch (Exception e) {
            if(e instanceof ParseException) throw new DateFormatException("Il formato della data della review "+review.getHotel()+review.getUserId()+" non è corretto "+e.getMessage());
            else throw new RuntimeException("Errore generico nel calcolo del peso della review "+review.getHotel()+review.getUserId()+" "+e.getMessage());
        }
    }

    private Float[] reviewWeight(List<Review> reviews) throws Exception{
        Float[] weight = new Float[reviews.size()];

        for (int i = 0; i < reviews.size(); i++) {
            weight[i] = computeWeight(reviews.get(i));
        }

        return weight;
    }

    /**
     * Aggiorna i ratings dell'hotel in base alle reviews, in particolare ad ogni review viene assegnato un peso
     * in base alla data di pubblicazione, il rating viene calcolato come la media pesata dei ratings delle reviews
     * @throws Exception
     */
    private void updateRatings()throws Exception{
        float rate = 0;
        float cleaning = 0;
        float position = 0;
        float services = 0;
        float quality = 0;
        Float[] weights = reviewWeight(this.reviews);
        
        for(Review review : this.reviews){
            float weight = weights[reviews.indexOf(review)];


            rate += review.getGlobalRate()*weight;
            cleaning += review.getRateCleaning()*weight;
            position += review.getRatePosition()*weight;
            services += review.getRateServices()*weight;
            quality += review.getRateQuality()*weight;
        }

        Float sumWeight = Arrays.stream(weights).reduce(0.0f, Float::sum);


        this.rate = Math.round(rate/sumWeight * 10.0f) / 10.0f;

        cleaning = Math.round(cleaning/sumWeight * 10.0f) / 10.0f;
        position = Math.round(position/sumWeight * 10.0f) / 10.0f;
        services = Math.round(services/sumWeight * 10.0f) / 10.0f;
        quality = Math.round(quality/sumWeight * 10.0f) / 10.0f;

        this.ratings = new Ratings(cleaning, position, services, quality);
    }

    public void addReview(Review review) throws RuntimeException{
        try{
            this.reviews.add(review);
            this.updateRatings();
        }catch(Exception e){
            throw new RuntimeException("Errore nell'aggiornamento dei ratings "+e.getMessage());
        }
    }

}