package Dati;

public class Review extends SerializableEntity{
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

    public String getId(){
        return this.referredHotel.concat(this.userId);
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