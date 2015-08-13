package com.mad.openisdm.madnew.app;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.util.GeoPoint;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/**
 * A class that represents a Shelter.
 */
public class Shelter {
    public int distance;
    private GeoPoint position;
    private HashMap<String, String> properties;

    private Shelter(JSONObject shelterJSON) throws  JSONException{
        Double latitude = shelterJSON.getJSONObject("geometry").getJSONArray("coordinates").getDouble(1);
        Double longitude = shelterJSON.getJSONObject("geometry").getJSONArray("coordinates").getDouble(0);
        position = new GeoPoint(latitude, longitude);
        properties = new HashMap<String, String>();
        Iterator<String> keys = shelterJSON.getJSONObject("properties").keys();
        while (keys.hasNext()){
            String key = keys.next();
            String value = shelterJSON.getJSONObject("properties").getString(key);
            properties.put(key, value);
        }
    }

    public GeoPoint getPosition(){
        return position;
    }

    public HashMap<String, String> getProperties(){
        return properties;
    }

    /**Parse a JSONObject(A geojson file) from the root, and returns a list of shelters in that file
     * throws exceptions if the format of the JSONObject is not geojson*/
    public static ArrayList<Shelter> parseFromRoot(JSONObject root) throws JSONException{
        ArrayList<Shelter> result = new ArrayList<Shelter>();
        JSONArray features = root.getJSONArray("features");
        for (int i = 0 ; i < features.length(); i ++){
            Shelter shelter = new Shelter(features.getJSONObject(i));
            shelter.calculateDistance(DataHolder.userLocation);

            result.add(shelter);
        }
        return result;
    }

    public void calculateDistance(GeoPoint userLocation){
        this.distance = position.distanceTo(userLocation);
    }


    public interface OnShelterReceiveListener{
        /**A callback method when Shelters are received(after fetching from shelter source),
        * the list of Shelter received is passed as arguments*/
        public abstract void onShelterReceive(ArrayList<Shelter> shelters);
    }

}
