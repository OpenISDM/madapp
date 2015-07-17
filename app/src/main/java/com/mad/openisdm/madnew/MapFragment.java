package com.mad.openisdm.madnew;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import junit.framework.Test;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.ResourceProxy;
import org.osmdroid.bonuspack.clustering.RadiusMarkerClusterer;
import org.osmdroid.bonuspack.kml.KmlDocument;
import org.osmdroid.bonuspack.overlays.FolderOverlay;
import org.osmdroid.bonuspack.overlays.InfoWindow;
import org.osmdroid.bonuspack.overlays.MapEventsOverlay;
import org.osmdroid.bonuspack.overlays.MapEventsReceiver;
import org.osmdroid.bonuspack.overlays.Marker;
import org.osmdroid.bonuspack.overlays.MarkerInfoWindow;
import org.osmdroid.bonuspack.overlays.Polyline;
import org.osmdroid.bonuspack.routing.OSRMRoadManager;
import org.osmdroid.bonuspack.routing.Road;
import org.osmdroid.bonuspack.routing.RoadManager;
import org.osmdroid.bonuspack.routing.RoadNode;
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


public class MapFragment extends Fragment implements LocationListener, MapEventsReceiver {
    public static final int SHOW_POLICE = 0;
    public static final int SHOW_HOSPITAL = 1;
    public static final String SHOW_ITEM_KEY = "item";

    private static final GeoPoint SOMEWHERE_IN_GERMANY = new GeoPoint(35.5069039, 139.680770);

    private static final String MAP_CENTER_LATITUDE_KEY = "map_center_lat";
    private static final String MAP_CENTER_LONGITUDE_KEY = "map_center_long";
    private static final String USER_LOCATION_LATITUDE_KEY = "usr_location_lat";
    private static final String USER_LOCATION_LONGITUDE_KEY = "usr_location_long";
    private static final String CURRENT_ZOOM_LEVEL_KEY = "level";

    private static final  GeoPoint DEFAULT_MAP_CENTER = SOMEWHERE_IN_GERMANY;
    private static final GeoPoint DEFAULT_USER_LOCATION = SOMEWHERE_IN_GERMANY;
    private static final int DEFAULT_ZOOM_LEVEL = 9;

    private MapController mapController;
    private Marker userLocationMarker;
    private MapView map;
    private Road currentRoad;
    private int zoomLevel;
    private GeoPoint mapCenter;
    private GeoPoint userLocation;
    private Marker pinPointMarker;
    private ItemizedIconOverlay<OverlayItem> myLocationOverlay;
    private RadiusMarkerClusterer clusterer;

    private JSONObject dataset;




 /*   private ArrayList<Marker> getOverlayFromJSON(String fileName) throws JSONException{
        AssetLoader loader = new AssetLoader(getActivity().getApplicationContext());

        ArrayList<Marker> result = new ArrayList<Marker>();

       *//* String jsonStr = loader.loadJSONFromAsset("TsunamiShelter.json");
        JSONObject json = new JSONObject(jsonStr);
        JSONArray array = json.getJSONArray("rows");
        for (int i = 0; i<array.length(); i++){
            Double longitude = array.getJSONObject(i).getDouble("lng");
            Double latitude = array.getJSONObject(i).getDouble("lat");
            String name = array.getJSONObject(i).getString("Name");

            Marker newMarker = new Marker(map);
            newMarker.setPosition(new GeoPoint(latitude, longitude));
            newMarker.setInfoWindow(new NavigateInfoWindow(map, newMarker.getPosition()));
            newMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
            newMarker.setTitle(name);
            result.add(newMarker);

        }*//*
        JSONObject json = null;
        try{
            Log.i("abcdefg", "before reading");
            Toast.makeText(this.getActivity(), "Before reading", Toast.LENGTH_SHORT).show();
            json = JsonReader.readJsonFromUrl("http://140.109.17.112:5000/taipei");
            Log.i("abcdefg", "after reading");
            Toast.makeText(this.getActivity(), "After reading", Toast.LENGTH_SHORT).show();
        }catch(Exception e){
            Log.i("abcdefg Exception", "" +e.getMessage());
        }

        JSONArray array = json.getJSONArray("features");
        for (int i =0; i<array.length(); i++){
            Double latitude = array.getJSONObject(i).getJSONObject("geometry").getJSONArray("coordinates").getDouble(0);
            Double longitude = array.getJSONObject(i).getJSONObject("geometry").getJSONArray("coordinates").getDouble(1);

            String name = array.getJSONObject(i).getJSONObject("properties").getString("Park_Name");

            Marker newMarker = new Marker(map);
            newMarker.setPosition(new GeoPoint(latitude, longitude));
            newMarker.setInfoWindow(new NavigateInfoWindow(map, newMarker.getPosition()));
            newMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
            newMarker.setTitle(name);
            result.add(newMarker);
        }

        return result;
    }*/

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

        pinPointMarker = null;
        if (savedInstanceState == null){
            userLocation = DEFAULT_USER_LOCATION;
            mapCenter = DEFAULT_MAP_CENTER;
            zoomLevel = DEFAULT_ZOOM_LEVEL;
        }else{
            userLocation = new GeoPoint(savedInstanceState.getDouble(USER_LOCATION_LATITUDE_KEY),
                    savedInstanceState.getDouble(USER_LOCATION_LONGITUDE_KEY));
            mapCenter = new GeoPoint(savedInstanceState.getDouble(MAP_CENTER_LATITUDE_KEY),
                    savedInstanceState.getDouble(MAP_CENTER_LONGITUDE_KEY));
            zoomLevel = savedInstanceState.getInt(CURRENT_ZOOM_LEVEL_KEY);
        }


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_map, container, false);
        map = (MapView)root.findViewById(R.id.mapview);
        mapController = (MapController)map.getController();
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setMultiTouchControls(true);
        map.setBuiltInZoomControls(true);

        mapController.setZoom(zoomLevel);
        mapController.setCenter(mapCenter);


        MapEventsOverlay mapEventsOverlay = new MapEventsOverlay(this.getActivity().getApplicationContext(), this);
        map.getOverlays().add(0, mapEventsOverlay);


        userLocationMarker = new Marker(map);
        userLocationMarker.setPosition(userLocation);
        userLocationMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        userLocationMarker.setInfoWindow(new SetLocationInfoWindow(map, userLocationMarker));
        map.getOverlays().add(userLocationMarker);
        getDatasetSync("http://140.109.17.112:5000/datasets/taipei");


        /*clusterer = new RadiusMarkerClusterer(this.getActivity().getApplicationContext());
        Drawable clusterIconD = getResources().getDrawable(R.drawable.marker_cluster);
        Bitmap clusterIcon = ((BitmapDrawable)clusterIconD).getBitmap();
        clusterer.setIcon(clusterIcon);
        clusterer.setRadius(70);
        try{
            ArrayList<Marker> shelterMarkers = getOverlayFromJSON(null);
            for (Marker marker:shelterMarkers){
                clusterer.add(marker);
            }
        }catch(JSONException e){
            Log.i("Error", "Some error");
        }

        map.getOverlays().add(clusterer);
        map.invalidate();*/

        return root;
    }

    public void updateUIWithRoad(Road road){


        Drawable nodeIcon = getResources().getDrawable(R.drawable.marker_node);
        Log.i("Null", String.valueOf(road == null));
        for (int i=0; i<road.mNodes.size(); i++){
            RoadNode node = road.mNodes.get(i);
            Marker nodeMarker = new Marker(map);
            nodeMarker.setPosition(node.mLocation);
            nodeMarker.setIcon(nodeIcon);
            nodeMarker.setTitle("Step " + i);
            nodeMarker.setSnippet(node.mInstructions);
            nodeMarker.setSubDescription(Road.getLengthDurationText(node.mLength, node.mDuration));

            map.getOverlays().add(nodeMarker);
        }
        map.invalidate();

        Polyline roadOverlay = RoadManager.buildRoadOverlay(road, getActivity().getApplicationContext());

        map.getOverlays().add(roadOverlay);
        map.invalidate();
    }

    private class UpdateRoadTask extends AsyncTask<Object, Void, Road> {
        protected Road doInBackground(Object ... params){
            ArrayList<GeoPoint> waypoints = (ArrayList<GeoPoint>)params[0];
            RoadManager roadManager = null;
            roadManager = new OSRMRoadManager();
            return roadManager.getRoad(waypoints);
        }

        protected void onPostExecute(Road result){
            currentRoad = result;
            if (currentRoad.mStatus != Road.STATUS_OK){
                Log.i("Status", "Not ok");
            }
            updateUIWithRoad(result);
        }

    }

    public void getRoadAsync(GeoPoint startPoint, GeoPoint endPoint){
        ArrayList<GeoPoint> waypoints = new ArrayList<GeoPoint>();
        waypoints.add(startPoint);
        waypoints.add(endPoint);
        new UpdateRoadTask().execute(waypoints);
    }



    private class UpdateDatasetTask extends AsyncTask<Object, Void, JSONObject>{
        protected JSONObject doInBackground(Object ... params){
            JSONObject updatedDataset = null;
            try{
                updatedDataset = JsonReader.readJsonFromUrl((String)params[0]);
            }catch (Exception e){
                Log.i("Read JSON Exception", "Some error");
            }
            return updatedDataset;
        }

        protected void onPostExecute(JSONObject result){
            dataset = result;
            try{
                updateUIWithDataset(result);
            }catch(JSONException e){
                Log.i("update UI", "Some error");
            }
        }
    }

    public void getDatasetSync(String url){
        new UpdateDatasetTask().execute(url);
    }

    public void updateUIWithDataset(JSONObject dataset) throws JSONException{
        clusterer = new RadiusMarkerClusterer(this.getActivity().getApplicationContext());
        Log.i("dataset is null", String.valueOf(dataset == null));
        JSONArray array = dataset.getJSONArray("features");
        for (int i =0; i<array.length(); i++){
            Double latitude = array.getJSONObject(i).getJSONObject("geometry").getJSONArray("coordinates").getDouble(1);
            Double longitude = array.getJSONObject(i).getJSONObject("geometry").getJSONArray("coordinates").getDouble(0);

            String name = array.getJSONObject(i).getJSONObject("properties").getString("Park_Name");

            Marker newMarker = new Marker(map);
            newMarker.setPosition(new GeoPoint(latitude, longitude));
            newMarker.setInfoWindow(new NavigateInfoWindow(map, newMarker.getPosition()));
            newMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
            newMarker.setTitle(name);
            clusterer.add(newMarker);
        }

        Drawable clusterIconD = getResources().getDrawable(R.drawable.marker_cluster);
        Bitmap clusterIcon = ((BitmapDrawable)clusterIconD).getBitmap();
        clusterer.setIcon(clusterIcon);
        clusterer.setRadius(70);

        map.getOverlays().add(clusterer);
        map.invalidate();

    }

    private class NavigateInfoWindow extends MarkerInfoWindow {
        public NavigateInfoWindow(MapView mapView, final GeoPoint markerLocation) {
            super(R.layout.bonuspack_bubble, mapView);
            Button btn = (Button)(mView.findViewById(R.id.bubble_btn));
            btn.setText("Navigate");
            btn.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    Toast.makeText(view.getContext(), "navigating...", Toast.LENGTH_SHORT).show();
                    getRoadAsync(userLocation, markerLocation);
                }
            });
        }

        @Override public void onOpen(Object item){
            super.onOpen(item);
            mView.findViewById(R.id.bubble_btn).setVisibility(View.VISIBLE);
            InfoWindow.closeAllInfoWindowsOn(map);
        }
    }

    //marker past into constuctor must have position set first.
    private class SetLocationInfoWindow extends MarkerInfoWindow {
        public SetLocationInfoWindow(MapView mapView, final Marker marker) {
            super(R.layout.bonuspack_bubble, mapView);
            Button btn = (Button)(mView.findViewById(R.id.bubble_btn));
            btn.setText("Set location");
            btn.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {

                    Toast.makeText(view.getContext(), "setting location...", Toast.LENGTH_SHORT).show();
                    marker.getInfoWindow().close();
                    userLocation = marker.getPosition();
                    Marker oldUserLocationMarker = userLocationMarker;
                    map.getOverlays().remove(map.getOverlays().indexOf(oldUserLocationMarker));
                    map.getOverlays().remove(map.getOverlays().indexOf(marker));
                    userLocationMarker = new Marker(map);
                    userLocationMarker.setPosition(userLocation);
                    userLocationMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                    map.getOverlays().add(userLocationMarker);
                    pinPointMarker = null;
                    map.invalidate();


                }
            });
        }

        @Override public void onOpen(Object item){
            super.onOpen(item);
            mView.findViewById(R.id.bubble_btn).setVisibility(View.VISIBLE);
            InfoWindow.closeAllInfoWindowsOn(map);
        }
    }



    @Override
    public void onSaveInstanceState(Bundle saveInstanceState){
        saveInstanceState.putDouble(MAP_CENTER_LATITUDE_KEY, map.getMapCenter().getLatitude());
        saveInstanceState.putDouble(MAP_CENTER_LONGITUDE_KEY, map.getMapCenter().getLongitude());
        saveInstanceState.putInt(CURRENT_ZOOM_LEVEL_KEY, map.getZoomLevel());
        saveInstanceState.putDouble(USER_LOCATION_LATITUDE_KEY, userLocation.getLatitude());
        saveInstanceState.putDouble(USER_LOCATION_LONGITUDE_KEY, userLocation.getLongitude());

    }

    @Override
    public void onLocationChanged(Location location) {

        String msg = "New Latitude: " + location.getLatitude()
                + "New Longitude: " + location.getLongitude();

        Toast.makeText(getActivity().getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
        //userLocation = new GeoPoint(location.getLatitude(), location.getLongitude());
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
    public boolean singleTapConfirmedHelper(GeoPoint p) {
        Toast.makeText(this.getActivity().getApplicationContext(), "Tap on ("+p.getLatitude()+","+p.getLongitude()+")", Toast.LENGTH_SHORT).show();
        InfoWindow.closeAllInfoWindowsOn(map);
        return true;
    }

    @Override public boolean longPressHelper(GeoPoint p) {
        if (pinPointMarker != null){
            map.getOverlays().remove(map.getOverlays().indexOf(pinPointMarker));
        }
        pinPointMarker = new Marker(map);
        pinPointMarker.setPosition(p);
        pinPointMarker.setInfoWindow(new SetLocationInfoWindow(map, pinPointMarker));
        pinPointMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);

        map.getOverlays().add(pinPointMarker);
        map.invalidate();
        return true;
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
