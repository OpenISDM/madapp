package com.mad.openisdm.madnew;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.ResourceProxy;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.util.ResourceProxyImpl;
import org.osmdroid.views.MapController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.ItemizedOverlay;
import org.osmdroid.views.overlay.OverlayItem;

import java.lang.reflect.Array;
import java.util.ArrayList;


public class MapFragment extends Fragment implements LocationListener {
    public static final int SHOW_POLICE = 0;
    public static final int SHOW_HOSPITAL = 1;
    public static final String SHOW_ITEM_KEY = "item";

    private static final String CURRENT_MAP_CENTER_LATITUDE_KEY = "locationlat";
    private static final String CURRENT_MAP_CENTER_LONGITUDE_KEY = "locationlong";
    private static final String CURRENT_ZOOM_LEVEL = "level";
    private MapController mapController;
    private Location currentLocation;
    private MapView map;
    private int zoomLevel = 9;
    private GeoPoint mapCenter = new GeoPoint(25.02, 121.08);
    private ItemizedIconOverlay<OverlayItem> myLocationOverlay;
    private ResourceProxy resourceProxy;
    private ArrayList<OverlayItem> items;


    private ArrayList<OverlayItem> getOverlayFromJSON(String fileName) throws JSONException{
        AssetLoader loader = new AssetLoader(getActivity().getApplicationContext());
        String jsonStr = loader.loadJSONFromAsset("TsunamiShelter.json");

        ArrayList<OverlayItem> result = new ArrayList<OverlayItem>();

        JSONObject json = new JSONObject(jsonStr);
        JSONArray array = json.getJSONArray("rows");
        for (int i = 0; i<array.length(); i++){
            Double longitude = array.getJSONObject(i).getDouble("lng");
            Double latitude = array.getJSONObject(i).getDouble("lat");
            GeoPoint p = new GeoPoint(latitude, longitude);
            result.add(new OverlayItem("Title", "Description", p));
            Log.i("Location", latitude + ", " + longitude);
        }
        return result;
    }

    public static MapFragment newInstance(int showItemID) {
        Bundle args = new Bundle();
        args.putInt(SHOW_ITEM_KEY, showItemID);
        MapFragment fragment = new MapFragment();
        fragment.setArguments(args);
        return fragment;

    }

    public void display(int showItemID){
        String msg;
        switch (showItemID){
            case SHOW_POLICE:
                msg = "Police";
                break;
            case SHOW_HOSPITAL:
                msg = "Hospital";
                break;
            default:
                msg = "Default message";
                break;
        }
        Log.i("ITEM ID", msg);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int showItemID = getArguments().getInt(SHOW_ITEM_KEY);
        display(showItemID);
        setRetainInstance(true);



        resourceProxy = new ResourceProxyImpl(getActivity().getApplicationContext());
        Drawable marker = getResources().getDrawable(R.drawable.marker50);
        try{
            myLocationOverlay = new ItemizedIconOverlay<OverlayItem>(getOverlayFromJSON(null), marker,
                    new ItemizedIconOverlay.OnItemGestureListener<OverlayItem>(){
                        @Override
                        public boolean onItemSingleTapUp(final int index, final OverlayItem item) {
                            return true;
                        }

                        @Override
                        public boolean onItemLongPress(final int index, final OverlayItem item) {

                            return false;
                        }
                    }, resourceProxy);
        }catch(JSONException e){
            e.printStackTrace();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_map, container, false);
        map = (MapView)root.findViewById(R.id.mapview);
        mapController = (MapController)map.getController();
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setMultiTouchControls(true);
        map.getOverlays().add(myLocationOverlay);
        Log.i("Zoom level", "onCreateView: " + zoomLevel + "");
        Log.i("Zoom level", "onCreateView: Latitude" + mapCenter.getLatitude());
        Log.i("Zoom level", "onCreateView: Longitude" + mapCenter.getLongitude());
        mapController.setZoom(zoomLevel);
        mapController.setCenter(mapCenter);
        return root;
    }

    @Override
    public void onSaveInstanceState(Bundle saveInstanceState){
        /*saveInstanceState.putSerializable(CURRENT_MAP_CENTER_LATITUDE_KEY, map.getMapCenter().getLatitude());
        saveInstanceState.putSerializable(CURRENT_MAP_CENTER_LONGITUDE_KEY, map.getMapCenter().getLongitude());
        saveInstanceState.putSerializable(CURRENT_ZOOM_LEVEL, map.getZoomLevel());*/
    }

    @Override
    public void onLocationChanged(Location location) {

        String msg = "New Latitude: " + location.getLatitude()
                + "New Longitude: " + location.getLongitude();

        Toast.makeText(getActivity().getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
        currentLocation = location;
    }

    @Override
    public void onProviderDisabled(String provider) {

       // Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
       // startActivity(intent);
        Toast.makeText(getActivity().getApplicationContext(), "Gps is turned off!! ",
                Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onProviderEnabled(String provider) {

        Toast.makeText(getActivity().getApplicationContext(), "Gps is turned on!! ",
                Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onPause(){
        zoomLevel = map.getZoomLevel();
        Log.i("Zoom level", "On pause(): zoom level" + map.getZoomLevel());
        Log.i("Zoom level", "On pause():Latitude" + map.getMapCenter().getLatitude());
        Log.i("Zoom level", "On pause():Longitude" + map.getMapCenter().getLongitude());
        mapCenter = (GeoPoint)map.getMapCenter();
        super.onPause();
    }
}
