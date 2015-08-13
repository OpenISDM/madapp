package com.mad.openisdm.madnew.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * This is a helper class for activities that will be hosting MapFragment and/or ShelterListFragment
 * This class is meant to be use along with Shelter.OnShelterReceiveListener, and ShelterSourceSelector.
 * The procedure is as follows: In the hosting activity, you must call ShelterManager.connect before you
 * select a source with ShelterSourceSelector. When ShelterSourceSelector has finish fetching data, the
 * callback method in Shelter.OnShelterReceiveListener will then be called. See MainActivity for example.
 * */
public class ShelterManager {
    private Shelter.OnShelterReceiveListener listener;
    private Context context;
    private JSONBroadcastReceiver jsonBroadcastReceiver;

    /**Constructor takes a Context parameter(your hosting activity), and an implementation of OnShelterReceiveListener*/
    public ShelterManager(Context context, Shelter.OnShelterReceiveListener listener){
        super();
        jsonBroadcastReceiver = new JSONBroadcastReceiver();
        this.listener = listener;
        this.context = context;
    }

    /**Set up a BroadcastReceiver for the hosting activity*/
    public void connect(){
        IntentFilter filter = new IntentFilter(JSONBroadcastReceiver.RECEIVE_JSON_ACTION);
        filter.addCategory(Intent.CATEGORY_DEFAULT);
        context.registerReceiver(jsonBroadcastReceiver, filter);
    }

    public void disconnect(){
        context.unregisterReceiver(jsonBroadcastReceiver);
    }


    /**A broadcast receiver, the onReceive method is called when the FetchJsonIntentService worker thread ends*/
    public class JSONBroadcastReceiver extends BroadcastReceiver {
        public static final String RECEIVE_JSON_ACTION = "com.mad.openisdm.madnew.JSONReceive";

        @Override
        /**This method is called when FetchJSONIntentService ends.
         * The Intent parameter is the implicit intent that FetchJSONIntentService fires when it finishes
         * The result in that implicit intent is processed here*/
        public void onReceive(Context context, Intent intent) {
            //Log.e("???---","Activity received Intent");

            /*The JSON object that will be built based on the JSON file read from FetchJSONIntentService*/
            JSONObject jsonObject = null;

            /*Check to see if there is an exception(If there is, it is most likely due to network issue)*/
            boolean exception = intent.getBooleanExtra(FetchJSONIntentService.EXCEPTION_KEY, false);
            if (exception){
                Toast.makeText(context, "IO ERROR", Toast.LENGTH_SHORT).show();
                Log.i("IOTAG", "IO ERROR");
            }else{
                try {
                    jsonObject = new JSONObject(DataHolder.jsonStr);
                }catch (JSONException e){
                    /**/
                    Toast.makeText(context, "PARSE ERROR", Toast.LENGTH_SHORT).show();
                    Log.i("JSONTAG", "PARSE ERROR-CHECK JSON SYNTAX");
                }
                if (jsonObject != null) {
                    try {
                        /*Make a list of Shelters from the JSONObject, and pass the result to the listener's callback method*/
                        ArrayList<Shelter> shelters = Shelter.parseFromRoot(jsonObject);
                        listener.onShelterReceive(shelters);
                    }
                    catch (JSONException e){
                        Toast.makeText(context, "GEOJSON FORMAT ERROR", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
    }
}






