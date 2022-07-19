package com.lukvad.scooter;
import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.HashMap;
import java.util.Map;

import java.util.ArrayList;
import java.util.List;
public class ScooterInformation {

    public String battery;
    public String latitude;
    public String longitude;
    public String state;
    public String name;
    public String userKey;
    public Long start;
    public String engine;

    public Map<String, Boolean> stars = new HashMap<>();

    public ScooterInformation(){

    }
    public ScooterInformation(String battery, String latitude, String longitude, String state, String name, String userKey, Long start, String engine){
        this.battery = battery;
        this.latitude = latitude;
        this.longitude = longitude;
        this.state = state;
        this.name = name;
        this.userKey = userKey;
        this.start = start;
        this.engine = engine;
    }
 @Exclude
    public Map<String, Object> toMap() {
     HashMap<String, Object> result = new HashMap<>();
     result.put("battery", battery);
     result.put("latitude", latitude);
     result.put("longitude", longitude);
     result.put("state", state);
     result.put("name", name);
     result.put("userKey", userKey);
     result.put("start", start);
     result.put("engine", engine);

     return result;
 }
}

