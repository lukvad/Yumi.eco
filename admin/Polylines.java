package com.lukvad.admin;
import com.google.firebase.database.Exclude;

import java.util.HashMap;
import java.util.Map;

public class Polylines {

    public String latitude;
    public String longitude;

    public Map<String, Boolean> stars = new HashMap<>();

    public Polylines(){

    }
    public Polylines(String latitude, String longitude){

        this.latitude = latitude;
        this.longitude = longitude;
    }
 @Exclude
    public Map<String, Object> toMap() {
     HashMap<String, Object> result = new HashMap<>();
     result.put("latitude", latitude);
     result.put("longitude", longitude);


     return result;
 }
}

