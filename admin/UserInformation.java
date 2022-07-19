package com.lukvad.scooter;
import com.google.firebase.database.Exclude;

import java.util.HashMap;
import java.util.Map;

public class UserInformation {

    public String firstname;
    public String surname;
    public String alert;
    public String address;
    public String idno;
    public Double balance;
    public String scooterName;
    public String email;
    public  Double fIn;
    public Double charge;
    public UserInformation(){

    }

    public UserInformation(String firstname, String surname, String alert, String address, String idno, Double balance, String scooterName, String email, Double fIn, Double charge){
        this.firstname = firstname;
        this.surname = surname;
        this.alert = alert;
        this.address = address;
        this.idno = idno;
        this.balance = balance;
        this.scooterName = scooterName;
        this.email = email;
        this.fIn = fIn;
        this.charge = charge;


    }
 @Exclude
    public Map<String, Object> toMap() {
     HashMap<String, Object> result = new HashMap<>();
     result.put("firstname", firstname);
     result.put("surname", surname);
     result.put("alert", alert);
     result.put("address", address);
     result.put("idno", idno);
     result.put("balance", balance);
     result.put("scooterName", scooterName);
     result.put("email", email);
     result.put("*fIn&", fIn);
     result.put("charge", charge);

     return result;
 }
}

