package com.mad.openisdm.madnew.service;

import android.app.IntentService;
import android.content.Intent;

import com.mad.openisdm.madnew.model.DataHolder;
import com.mad.openisdm.madnew.util.JsonReader;
import com.mad.openisdm.madnew.manager.ShelterManager;

/**
 * This class is an intent service(a worker thread) that downloads JSON file (in plain text format) and store it
 * in DataHolder.jsonStr.
 *
 * The context(activity) that starts this intent service should register a broadcast receiver in order to receive the result
 * */
public class FetchJSONIntentService extends IntentService {
    public final static String URL_KEY = "URL_KEY";
    public final static String JSON_OBJECT_KEY = "JSONOBJECT_KEY";
    public final static String EXCEPTION_KEY = "EXCEPTION_KEY";

    public FetchJSONIntentService() {
        super("FetchJSONIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        /*Get the url of the JSON file*/
        String url = intent.getStringExtra(URL_KEY);
        /*The result(JSON file in plain text format)*/
        String jsonText = null;
        /*An boolean indicator, if this is true, an IOException has occured while downloading the JSON file(probably because there is no internet access), otherwise this will be false*/
        boolean exception = false;
        try{
            jsonText = JsonReader.readJsonFromUrl(url);
        }catch (Exception e){
            exception = true;
        }

        /*Send an implicit Intent once this intent service ends.
        * The context(activity) that are registered to listen for this implicit intent will receive the intent, and retrieve the result of this worker thread*/
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(ShelterManager.JSONBroadcastReceiver.RECEIVE_JSON_ACTION);

        /*Directly modifying static variable is a bad design.
        * It would be safer and more secure to pass jsonText as intent extras, but in this case, it is possible that jsonText exceeds the 1MB maximum size limit
        * for intent extras. Thus the less favourable method. More thinking required*/
        DataHolder.jsonStr = jsonText;
        //broadcastIntent.putExtra(JSON_OBJECT_KEY, jsonText);
        broadcastIntent.putExtra(EXCEPTION_KEY, exception);
        broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
        sendBroadcast(broadcastIntent);
    }
}
