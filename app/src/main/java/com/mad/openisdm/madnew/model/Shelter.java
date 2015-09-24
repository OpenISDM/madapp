package com.mad.openisdm.madnew.model;

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
    private int mDistance;
    private GeoPoint mPosition;
    private HashMap<String, String> mProperties;

    public GeoPoint getPosition(){
        return mPosition;
    }

    public HashMap<String, String> getProperties(){
        return mProperties;
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
        this.mDistance = mPosition.distanceTo(userLocation);
    }

    public int getDistance(){
        return mDistance;
    }

    private Shelter(JSONObject shelterJSON) throws  JSONException{

        Double latitude = shelterJSON.getJSONObject("geometry").getJSONArray("coordinates").getDouble(1);
        Double longitude = shelterJSON.getJSONObject("geometry").getJSONArray("coordinates").getDouble(0);

        mPosition = new GeoPoint(latitude, longitude);
        mProperties = new HashMap<String, String>();

        Iterator<String> keys = shelterJSON.getJSONObject("properties").keys();
        while (keys.hasNext()){
            String key = keys.next();
            String value = shelterJSON.getJSONObject("properties").getString(key);
            mProperties.put(key, value);
        }
    }

}
