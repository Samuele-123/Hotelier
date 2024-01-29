package Hotel;

/**
 * Classe che rappresenta una review di un hotel
 * @param referredHotel hotel recensito
 * @param userId utente che ha scritto la review
 * @param globalRate rating globale
 * @param cleaning rating della pulizia
 * @param position rating della posizione
 * @param services rating dei servizi
 * @param quality rating del prezzo
 * @param date data della review
 * 
 * La classe mette a dissposizione i metodi per visualizzare i campi della review
 */
public class Review{
    private String referredHotel;
    private String userId;
    private float globalRate;
    private float cleaning;
    private float position;
    private float services;
    float quality;
    private String date;

    public Review(String hotelId, String userId, float rate, float rateCleaning, float ratePosition, float rateServices, float rateQuality, String date){
        this.referredHotel = hotelId;
        this.userId = userId;
        this.globalRate = rate;
        this.cleaning = rateCleaning;
        this.position = ratePosition;
        this.services = rateServices;
        this.quality = rateQuality;
        this.date = date;
    }

    public String getUserId(){
        return this.userId;
    }

    public String getHotel(){
        return this.referredHotel;
    }

    public float getGlobalRate(){
        return this.globalRate;
    }

    public float getRateCleaning(){
        return this.cleaning;
    }

    public float getRatePosition(){
        return this.position;
    }

    public float getRateServices(){
        return this.services;
    }

    public float getRateQuality(){
        return this.quality;
    }

    public String getDate(){
        return this.date;
    }
}