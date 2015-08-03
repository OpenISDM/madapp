package com.mad.openisdm.madnew.app;

import android.app.IntentService;
import android.content.Intent;


public class FetchJSONIntentService extends IntentService {
    public final static String URL_KEY = "URL_KEY";
    public final static String JSON_OBJECT_KEY = "JSONOBJECT_KEY";
    public final static String EXCEPTION_KEY = "EXCEPTION_KEY";

    public FetchJSONIntentService() {
        super("FetchJSONIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String url = intent.getStringExtra(URL_KEY);
        String jsonText = null;
        boolean exception = false;
        try{
            jsonText = JsonReader.readJsonFromUrl(url);
        }catch (Exception e){
            exception = true;
        }
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(JSONBroadcastReceiverManager.JSONBroadcastReceiver.RECEIVE_JSON_ACTION);

        /*Directly modifying static variable is a bad design.
        * It would be safer and more secure to pass jsonText as intent extras, but in this case, it is possible that jsonText exceeds the 1MB maximum size limit
        * for intent extras. Thus the less favourable method. More thinking required*/
        JSONDataHolder.jsonStr = jsonText;
        //broadcastIntent.putExtra(JSON_OBJECT_KEY, jsonText);
        broadcastIntent.putExtra(EXCEPTION_KEY, exception);
        broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
        sendBroadcast(broadcastIntent);
    }
}
