package com.mad.openisdm.madnew.model;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.mad.openisdm.madnew.Config;
import com.mad.openisdm.madnew.service.FetchJSONIntentService;

import java.util.HashMap;

/**
 * Created by Wilbur on 2015/7/29.
 */
public class ShelterSourceSelector {

    private int shelterID;
    private Context context;
    public ShelterSourceSelector(Context context){
        this.context = context;
    }

    public ShelterSourceSelector selectShelterSource(int shelterID){
        this.shelterID = shelterID;
        return this;
    }

    public void fetchFromSource(){
        String url = new ShelterID().getIDMapping().get(shelterID);
        Intent serviceIntent = new Intent(context, FetchJSONIntentService.class);
        serviceIntent.putExtra(FetchJSONIntentService.URL_KEY, url);
        context.startService(serviceIntent);
    }

    public final class ShelterID{
        public static final int SHOW_TAIPEI = 0;
        public static final int SHOW_HSINCHU = 1;
        public static final int SHOW_NEW_TAIPEI = 2;

        public HashMap<Integer, String> getIDMapping(){
            HashMap<Integer, String> idMapping= new HashMap<Integer, String>();
            idMapping.put(SHOW_TAIPEI, "http://140.109.17.112:5000/datasets/taipei");
            idMapping.put(SHOW_HSINCHU, "http://140.109.17.112:5000/datasets/hsinchu");
            idMapping.put(SHOW_NEW_TAIPEI, "http://140.109.17.112:5000/datasets/newtaipei");
            return idMapping;
        }
    }
}
