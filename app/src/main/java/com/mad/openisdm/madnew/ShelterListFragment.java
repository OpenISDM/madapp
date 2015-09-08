/**This fragment presents a basic list layout that holds shelter information
*
* Note: this fragment by itself will not display any shelter information
* User must first create an instance of ShelterManager and call ShelterManager.connect(), then choose the
* shelter to display using ShelterSourceSelector.
*
* To update the map with a list of shelters, call the method updateUIWithShelters.
*
* */

package com.mad.openisdm.madnew;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.mad.openisdm.madnew.model.Shelter;

import java.util.ArrayList;


public class ShelterListFragment extends Fragment{
    private ListView list;
    private ArrayAdapter<String> adapter;


    @Override
    public void onCreate(Bundle savedInstanceState){
        Log.e("List---", this +  "onCreate");
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onStart() {
        Log.e("List---", this + "onStart");
        super.onStart();
    }

    /*This method takes a list of shelters, and display them on the list layout*/
    public void updateUIWithShelters(ArrayList<Shelter> shelters){
        ArrayList<Shelter> sortedShelters = ShelterListFragment.selectionSort(shelters);
        ArrayList<String> array = new ArrayList<String>();
        for (Shelter shelter : sortedShelters){
            String properties = "";
            for (String key :shelter.getProperties().keySet()){
                properties += (key + ":" + shelter.getProperties().get(key) + "\n");
            }
            array.add(properties);
        }
        adapter = new ArrayAdapter<String>(getActivity(), R.layout.shelter_list_item, array);
        list.setAdapter(adapter);
    }

    @Override
    public void onAttach(Activity activity) {
        Log.e("List---", this + "onAttach");
        super.onAttach(activity);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.e("List---", this+ "onCreateView");
        View root = inflater.inflate(R.layout.fragment_shelter_list, container, false);
        list = (ListView)(root.findViewById(R.id.fragment_shelter_listView));
        return root;
    }

    public void setAndUpdateShelters(ArrayList<Shelter> shelters) {
        updateUIWithShelters(shelters);
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
    public void onSaveInstanceState(Bundle outState){
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        Log.e("List---", this +"onActivity created");
        super.onActivityCreated(savedInstanceState);
    }


    /*Sort a list of shelter based on its distance attribute, from lowest to highest*/
    private static ArrayList<Shelter> selectionSort(ArrayList<Shelter> shelters) {
        for (int i = 0; i < shelters.size() - 1; i++)
        {
            for (int j = i + 1; j < shelters.size(); j++)
            {
                if (shelters.get(i).distance> shelters.get(j).distance) {

                    Shelter temp = shelters.get(j);
                    shelters.set(j, shelters.get(i));
                    shelters.set(i, temp);
                }
            }
        }
        return shelters;
    }
}
