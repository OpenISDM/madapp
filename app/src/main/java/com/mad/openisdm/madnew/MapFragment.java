package com.mad.openisdm.madnew;

import android.app.Activity;
import android.content.Context;
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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

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
import java.util.Iterator;


public class MapFragment extends Fragment implements // ConnectionCallbacks, OnConnectionFailedListener,
        LocationListener, MapEventsReceiver {
    public static final int SHOW_TAIPEI = 0;
    public static final int SHOW_HSINCHU = 1;
    public static final int SHOW_NEW_TAIPEI = 2;
    public static final String SHOW_ITEM_KEY = "item";

    private static final GeoPoint SOMEWHERE_IN_GERMANY = new GeoPoint(35.5069039, 139.680770);
    private static final GeoPoint SOMEWHERE_IN_TAIWAN = new GeoPoint(22.6369039, 120.260770);

    private static final String MAP_CENTER_LATITUDE_KEY = "map_center_lat";
    private static final String MAP_CENTER_LONGITUDE_KEY = "map_center_long";
    private static final String USER_LOCATION_LATITUDE_KEY = "usr_location_lat";
    private static final String USER_LOCATION_LONGITUDE_KEY = "usr_location_long";
    private static final String CURRENT_ZOOM_LEVEL_KEY = "level";

    private static final  GeoPoint DEFAULT_MAP_CENTER = SOMEWHERE_IN_TAIWAN;
    private static final GeoPoint DEFAULT_USER_LOCATION = SOMEWHERE_IN_TAIWAN;
    private static final int DEFAULT_ZOOM_LEVEL = 9;

    private MapController mapController;
    private Marker userLocationMarker;
    private MapView map;
    private Polyline currentRoadOverlay;
    private int zoomLevel;
    private GeoPoint mapCenter;
    private GeoPoint userLocation;
    private Marker pinPointMarker;
    private ItemizedIconOverlay<OverlayItem> myLocationOverlay;
    private RadiusMarkerClusterer clusterer;
    private int showItem;

    private GoogleApiClient googleApiClient;
    private Location lastLocation;
    private LocationRequest locationRequest;


    private JSONObject dataset;


/*
    protected synchronized void buildGoogleApiClient() {
        googleApiClient = new GoogleApiClient.Builder(getActivity())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    @Override
    public void onConnectionFailed(ConnectionResult result){

    }

    @Override
    public void onConnected(Bundle connectionHint) {
        lastLocation = LocationServices.FusedLocationApi.getLastLocation(
                googleApiClient);
        if (lastLocation != null) {

        }

        startLocationUpdates();
    }

    protected void createLocationRequest() {
        locationRequest = new LocationRequest();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    protected void startLocationUpdates() {
        LocationServices.FusedLocationApi.requestLocationUpdates(
                googleApiClient, locationRequest, this);
    }

    @Override
    public void onLocationChanged(Location location) {
        mCurrentLocation = location;
        mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
        updateUI();
    }*/

    public static MapFragment newInstance() {
        Bundle args = new Bundle();
        MapFragment fragment = new MapFragment();
        fragment.setArguments(args);
        return fragment;

    }

    public int automaticDisplay(){

        return 0;
    }

    public void display(int showItemID){
        showItem = showItemID;
        switch (showItemID){
            case SHOW_TAIPEI:
                getDatasetSync("http://140.109.17.112:5000/datasets/taipei");
                break;
            case SHOW_HSINCHU:
                getDatasetSync("http://140.109.17.112:5000/datasets/hsinchu");
                break;
            default:
                getDatasetSync("http://140.109.17.112:5000/datasets/newtaipei");
                break;
        }
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        clusterer = null;
        pinPointMarker = null;
        currentRoadOverlay = null;
        if (savedInstanceState == null){
            userLocation = DEFAULT_USER_LOCATION;
            mapCenter = DEFAULT_MAP_CENTER;
            zoomLevel = DEFAULT_ZOOM_LEVEL;
            showItem = 30;
        }else{
            userLocation = new GeoPoint(savedInstanceState.getDouble(USER_LOCATION_LATITUDE_KEY),
                    savedInstanceState.getDouble(USER_LOCATION_LONGITUDE_KEY));
            mapCenter = new GeoPoint(savedInstanceState.getDouble(MAP_CENTER_LATITUDE_KEY),
                    savedInstanceState.getDouble(MAP_CENTER_LONGITUDE_KEY));
            zoomLevel = savedInstanceState.getInt(CURRENT_ZOOM_LEVEL_KEY);
            showItem = savedInstanceState.getInt(SHOW_ITEM_KEY);
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

        display(showItem);

        return root;
    }

    public void updateUIWithRoad(Road road){
        /*Drawable nodeIcon = getResources().getDrawable(R.drawable.marker_node);
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
        map.invalidate();*/

        if (currentRoadOverlay != null){
            map.getOverlays().remove(map.getOverlays().indexOf(currentRoadOverlay));
            currentRoadOverlay = null;
        }

        currentRoadOverlay = RoadManager.buildRoadOverlay(road, getActivity().getApplicationContext());

        map.getOverlays().add(currentRoadOverlay);
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
            if (result.mStatus != Road.STATUS_OK){
                Log.i("ROADTAG", "Not ok");
                Toast.makeText(map.getContext(), "ROAD STATUS NOT OK", Toast.LENGTH_SHORT).show();
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
                Log.i("IOTAG", "IO ERROR");
            }
            return updatedDataset;
        }

        protected void onPostExecute(JSONObject result){
            dataset = result;
            if (dataset != null){
                try{
                    updateUIWithDataset(result);
                }catch(JSONException e){
                    Log.i("JSONTAG", "Dataset doesn't follow GEOJSON format");
                    Toast.makeText(map.getContext(), "GEOJSON format error", Toast.LENGTH_SHORT).show();
                }
            }else{
                Toast.makeText(map.getContext(), "DATASET IO ERROR", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void getDatasetSync(String url){
        new UpdateDatasetTask().execute(url);
    }

    public void updateUIWithDataset(JSONObject dataset) throws JSONException{
        if (clusterer != null){
            map.getOverlays().remove(map.getOverlays().indexOf(clusterer));
            clusterer = null;
        }
        clusterer = new RadiusMarkerClusterer(this.getActivity().getApplicationContext());
        JSONArray array = dataset.getJSONArray("features");
        for (int i =0; i<array.length(); i++){
            Double latitude = array.getJSONObject(i).getJSONObject("geometry").getJSONArray("coordinates").getDouble(1);
            Double longitude = array.getJSONObject(i).getJSONObject("geometry").getJSONArray("coordinates").getDouble(0);

            Marker newMarker = new Marker(map);
            newMarker.setPosition(new GeoPoint(latitude, longitude));
            NavigateInfoWindow infoWindow = new NavigateInfoWindow(map, newMarker);

            Iterator<String> keys = array.getJSONObject(i).getJSONObject("properties").keys();
            while (keys.hasNext()){
                String key = keys.next();
                String value = array.getJSONObject(i).getJSONObject("properties").getString(key);
                infoWindow.addProperty(key, value);
            }

            //String name = array.getJSONObject(i).getJSONObject("properties").getString("Park_Name");

            newMarker.setInfoWindow(infoWindow);
            newMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
            newMarker.setTitle("Title");
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
        ListView list;
        ArrayAdapter<String> adapter;
        public NavigateInfoWindow(MapView mapView, final Marker marker) {
            super(R.layout.bonuspack_bubble, mapView);

            Context context = mapView.getContext();
            String packageName = context.getPackageName();
            int listID = context.getResources().getIdentifier("id/bubble_list", (String)null, packageName);
            list = (ListView)this.mView.findViewById(listID);
            list.setVisibility(View.VISIBLE);

            ArrayList<String> array = new ArrayList<String>();
            adapter = new ArrayAdapter<String>(context, R.layout.property_list_item, array);
            list.setAdapter(adapter);


            Button btn = (Button)(mView.findViewById(R.id.bubble_btn));
            btn.setText("Navigate");
            btn.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    Toast.makeText(view.getContext(), "navigating...", Toast.LENGTH_SHORT).show();
                    getRoadAsync(userLocation, marker.getPosition());
                }
            });
        }

        @Override public void onOpen(Object item){
            super.onOpen(item);
            mView.findViewById(R.id.bubble_btn).setVisibility(View.VISIBLE);
            InfoWindow.closeAllInfoWindowsOn(map);
        }

        public void addProperty(String key, String value){
            adapter.add(key + ":" + value);
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
                    userLocation = marker.getPosition();
                    userLocationMarker.setPosition(userLocation);
                    map.getOverlays().remove(map.getOverlays().indexOf(marker));
                    pinPointMarker = null;
                    map.invalidate();


                }
            });

            closeAllInfoWindowsOn(map);
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
        saveInstanceState.putInt(SHOW_ITEM_KEY, showItem);
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
        if (pinPointMarker != null){
            map.getOverlays().remove(map.getOverlays().indexOf(pinPointMarker));
            pinPointMarker = null;
            map.invalidate();
        }
        return true;
    }

    @Override public boolean longPressHelper(GeoPoint p) {
        if (pinPointMarker != null){
            map.getOverlays().remove(map.getOverlays().indexOf(pinPointMarker));
            pinPointMarker = null;
        }

        if (currentRoadOverlay != null){
            map.getOverlays().remove(map.getOverlays().indexOf(currentRoadOverlay));
            currentRoadOverlay = null;
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
        mapCenter = (GeoPoint)map.getMapCenter();

        super.onPause();
    }
}
