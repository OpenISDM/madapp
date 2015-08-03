/*This fragment presents a basic list layout that holds shelter information
*
* Note: this fragment by itself will not display any shelter information
* User must first register broadcast receiver using JSONBroadcastReceiverManager, then choose the
* shelter to display using ShelterSourceSelector.
* */

package com.mad.openisdm.madnew.app;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.mad.openisdm.madnew.R;
import com.mad.openisdm.madnew.main.MainActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;


public class ShelterListFragment extends Fragment implements JSONReceiver {
    public static final int SHOW_TAIPEI = 0;
    public static final int SHOW_HSINCHU = 1;
    public static final int SHOW_NEW_TAIPEI = 2;
    private static final int DEFAULT_SHOW_ITEM = SHOW_TAIPEI;

    private ListView list;
    public static String jsonStr ="";
    private JSONReceiver jsonReceiver;
    private ArrayAdapter<String> adapter;
    public Activity activity;

    public void equalsOrNot(MainActivity a){
        Log.e("List---", this + "Constructor-this == list?" + equals(a.listFragment));
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        Log.e("List---", this +  "onCreate");
        super.onCreate(savedInstanceState);
        ArrayList<String> array = new ArrayList<String>();
        adapter = new ArrayAdapter<String>(getActivity(), R.layout.property_list_item, array);
        MainActivity a = (MainActivity)(getActivity());
        Log.e("List---:", this + "onCreate-this == list?" + this.equals(a.listFragment));
    }

    @Override
    public void onStart(){
        Log.e("List---", this + "onStart");
        super.onStart();
        /*IntentFilter filter1 = new IntentFilter(JSONReceiver.RECEIVE_JSON_ACTION);
        filter1.addCategory(Intent.CATEGORY_DEFAULT);
        jsonReceiver = new JSONReceiver();
        getActivity().registerReceiver(jsonReceiver, filter1);*/
    }

/*    public void fetchShelterAndDisplay(int showItemID){
        String url;
        switch (showItemID){
            case SHOW_TAIPEI:
                url="http://140.109.17.112:5000/datasets/taipei";
                break;
            case SHOW_HSINCHU:
                url ="http://140.109.17.112:5000/datasets/hsinchu";
                break;
            default:
                url = "http://140.109.17.112:5000/datasets/newtaipei";
                break;
        }

        Intent serviceIntent = new Intent(getActivity(), FetchJSONIntentService.class);
        Log.i("URLTAG", url);
        serviceIntent.putExtra(FetchJSONIntentService.URL_KEY, url);
        getActivity().startService(serviceIntent);
    }*/

    public void updateUIWithJSON(JSONObject root) throws JSONException{
        ArrayList<String> array = new ArrayList<String>();
        JSONArray features = root.getJSONArray("features");
        for (int i = 0; i<features.length(); i++){
            Iterator<String> keys = features.getJSONObject(i).getJSONObject("properties").keys();
            String entry= "";
            while (keys.hasNext()){
                String key = keys.next();
                String value = features.getJSONObject(i).getJSONObject("properties").getString(key);
                entry += (key + ":" + value + "\n");
            }
            array.add(entry);
            //array.add(features.getJSONObject(i).getJSONObject("properties").getString("Park_Name"));
        }

        //Log.e("update UI", "activity == null?" + (activity == null));
        adapter = new ArrayAdapter<String>(activity, android.R.layout.simple_list_item_1, array);
       // Log.e("List == null?", ""+(list == null));
        list.setAdapter(adapter);
    }

    @Override
    public void onAttach(Activity activity) {
        Log.e("List---", this + "onAttach");
        super.onAttach(activity);
        this.activity = activity;
        //Log.e("onAttach", "activity == null?" + (this.activity == null));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.e("List---", this+ "onCreateView");
        View root = inflater.inflate(R.layout.fragment_shelter_list, container, false);
        list = (ListView)(root.findViewById(R.id.fragment_shelter_listView));
        return root;
    }

    @Override
    public void handleJSON(JSONObject jsonObject) throws JSONException {
        updateUIWithJSON(jsonObject);
    }

    @Override
    public void onPause() {
        Log.e("List---", this +  "onPause");
        super.onPause();
    }

    @Override
    public void onStop() {
        Log.e("List---", this +"onStop");
        super.onStop();
    }

    @Override
    public void onDestroyView() {
        Log.e("List---", this +"DestroyView");
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        Log.e("List---", this +"onDestroy");
        super.onDestroy();
    }

    @Override
    public void onDetach() {
        Log.e("List---",this + "onDetach");
        super.onDetach();
    }

    @Override
    public void onResume() {
        Log.e("List---", this +"onResume");
        super.onResume();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        Log.e("List---", this +"onActivity created");
        super.onActivityCreated(savedInstanceState);
    }

/*    @Override
    public void onSaveInstanceState(Bundle outState) {
        Log.e("List---", "onSavedInstance");
        super.onSaveInstanceState(outState);
    }*/
/*public class JSONReceiver extends BroadcastReceiver {
        public static final String RECEIVE_JSON_ACTION = "com.mad.openisdm.madnew.JSONReceive";

        @Override
        public void onReceive(Context context, Intent intent) {
            JSONObject jsonObject = null;
            String jsonText = intent.getStringExtra(FetchJSONIntentService.JSON_OBJECT_KEY);
            boolean exception = intent.getBooleanExtra(FetchJSONIntentService.EXCEPTION_KEY, false);
            if (exception){
                Toast.makeText(context, "ShelterFrag-IO ERROR", Toast.LENGTH_SHORT).show();
                Log.i("IOTAG", "IO ERROR");
            }else{
                try {
                    jsonObject = new JSONObject(jsonStr);
                }catch (JSONException e){
                    Toast.makeText(context, "PARSE ERROR", Toast.LENGTH_SHORT).show();
                    Log.i("JSONTAG", "PARSE ERROR-CHECK JSON SYNTAX");
                }
                Log.i("JSONTAG", ""+(jsonObject == null));
                if (jsonObject != null) {
                    try {
                        updateUIWithJSON(jsonObject);
                    } catch (JSONException e) {
                        Log.i("JSONTAG", "Dataset doesn't follow GEOJSON format");
                        Toast.makeText(context, "GEOJSON format error", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
    }*/

}
