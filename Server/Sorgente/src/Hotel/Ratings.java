package Hotel;

/**
 * Classe che rappresenta i ratings di un hotel
 * @param cleaning rating della pulizia
 * @param position rating della posizione
 * @param services rating dei servizi
 * @param quality rating del prezzo
 */
public class Ratings {
    float cleaning;
    float position;
    float services;
    float quality;

    public Ratings(float rateCleaning, float ratePosition, float rateServices, float rateQuality){
        this.cleaning = rateCleaning;
        this.position = ratePosition;
        this.services = rateServices;
        this.quality = rateQuality;
    }
}