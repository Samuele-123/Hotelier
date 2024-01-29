package Dati;

public class Utente extends SerializableEntity{
    private String username;
    private String email;
    private String password;
    private String name;
    private String surname;
    private String phone;
    private String birthdate;
    private int reviewCounter = 0;
    private String badge = "Recensore";
    
    public Utente(String username, String email, String password, String name, String surname, String phone, String birthdate){
        this.username = username;
        this.email = email;
        this.password = password;
        this.name = name;
        this.surname = surname;
        this.phone = phone;
        this.birthdate = birthdate;
    }

    public String getId(){
        return this.username;
    }

    public String getUsername(){
        return this.username;
    }

    public String getEmail(){
        return this.email;
    }

    public String getPassword(){
        return this.password;
    }

    public String getName(){
        return this.name;
    }

    public String getSurname(){
        return this.surname;
    }

    public String getPhone(){
        return this.phone;
    }

    public String getBirthdate(){
        return this.birthdate;
    }

    public void setPassword(String password){
        this.password = password;
    }

    private void updateBadge(){
        if(this.reviewCounter>=300){
            this.badge = "Contributore Super";
        }
        else if(this.reviewCounter>=150){
            this.badge = " Contributore esperto";
        }
        else if(this.reviewCounter>=50){
            this.badge = "Contributore";
        }
        else if(this.reviewCounter>=25){
            this.badge = "Recensore esperto";
        }
    }

    public void review(int reviewCounter){
        this.reviewCounter+=1;
        this.updateBadge();
    }

    public String getBadge(){
        return this.badge;
    }
}
