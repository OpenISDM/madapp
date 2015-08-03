package com.mad.openisdm.madnew.app;

import android.app.Activity;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class JSONBroadcastReceiverManager {
    private Context context;
    private ArrayList<JSONReceiver> listOfReceiver;
    private JSONBroadcastReceiver jsonBroadcastReceiver;

    public JSONBroadcastReceiverManager(Context context){
        super();
        jsonBroadcastReceiver = new JSONBroadcastReceiver();
        this.context = context;
        listOfReceiver = new ArrayList<JSONReceiver>();
    }

    public void registerJSONBroadcastReceiver(){
        IntentFilter filter = new IntentFilter(JSONBroadcastReceiver.RECEIVE_JSON_ACTION);
        filter.addCategory(Intent.CATEGORY_DEFAULT);
        context.registerReceiver(jsonBroadcastReceiver, filter);
    }

    public void unregisterJSONBroadcastReceiver(){
        context.unregisterReceiver(jsonBroadcastReceiver);
    }

    public void addReceiver(JSONReceiver receiver){
        listOfReceiver.add(receiver);
    }


    public class JSONBroadcastReceiver extends BroadcastReceiver {
        public static final String RECEIVE_JSON_ACTION = "com.mad.openisdm.madnew.JSONReceive";

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.e("???---","Received Intent");
            JSONObject jsonObject = null;
            String jsonText = intent.getStringExtra(FetchJSONIntentService.JSON_OBJECT_KEY);
            boolean exception = intent.getBooleanExtra(FetchJSONIntentService.EXCEPTION_KEY, false);
            if (exception){
                Toast.makeText(context, "IO ERROR", Toast.LENGTH_SHORT).show();
                Log.i("IOTAG", "IO ERROR");
            }else{
                try {
                    jsonObject = new JSONObject(JSONDataHolder.jsonStr);
                }catch (JSONException e){
                    Toast.makeText(context, "PARSE ERROR", Toast.LENGTH_SHORT).show();
                    Log.i("JSONTAG", "PARSE ERROR-CHECK JSON SYNTAX");
                }
                Log.i("JSONTAG", ""+(jsonObject == null));
                if (jsonObject != null) {
                    try{
                        for (JSONReceiver receiver : listOfReceiver){
                            //Log.e("Receiver.activity==nul?", "" + (((ShelterListFragment)receiver).activity == null));
                            //((ShelterListFragment)receiver).activity = (Activity) context;
                            receiver.handleJSON(jsonObject);
                        }
                    }catch (JSONException e){
                        Toast.makeText(context, "GEOJSON FORMAT ERROR", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
    }
}






