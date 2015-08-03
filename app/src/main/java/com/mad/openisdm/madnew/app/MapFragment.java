/*This fragment handles a map UI.
* The map UI is implemented using OSMDroid's map view. ODMDroid's MapView class permits offline-viewing, nevertheless, other
* functionality such as routing(navigation) requires wireless access.
*
* This fragment  fetches GEOJSON files containing shelter information from a server and displays them on a map. It also provides
 * other basic map functionalities.
*
* When this fragment first creates, the map uses the GPS system to locate the current user location, and display such on the map.
* During first launch, when user location is undetectable(GPS is weak when used indoor), the default location is determined by the constant DEFAULT_USER_LOCATION, which
* is currently set to (somewhere in) Taipei. After launching, user location is updated periodically(when user location is detectable).
*
* GEOJSON files are also fetched from server when first launched, developers can choose which GEOJSON files to fetch by passing in
* corresponding constant parameters into the method fetchShelterAndDisplay(String parameter). This method downloads GEOJSON file from a server.
* parse it, and display them on the map. During the execution of the fragment, only one GEOJSON file can be shown on the map.
* In otherwords, if some GEOJSON data are already presented on the UI, calling fetchShelterAndDisplay() with a different parameter
* will fetch the requested data from the server, and replace the previously fetched data. The current default parameter passed into fetchShelterAndDisplay()
  * is SHOW_TAIPEI, as a result, when first launched,  shelter information in Taipei is loaded onto the map. Default display item can be changed
  * by modifying the constant, DEFAULT_SHOW_ITEM
* */


package com.mad.openisdm.madnew.app;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.mad.openisdm.madnew.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.bonuspack.clustering.RadiusMarkerClusterer;
import org.osmdroid.bonuspack.overlays.InfoWindow;
import org.osmdroid.bonuspack.overlays.MapEventsOverlay;
import org.osmdroid.bonuspack.overlays.MapEventsReceiver;
import org.osmdroid.bonuspack.overlays.Marker;
import org.osmdroid.bonuspack.overlays.MarkerInfoWindow;
import org.osmdroid.bonuspack.overlays.Polyline;
import org.osmdroid.bonuspack.routing.Road;
import org.osmdroid.bonuspack.routing.RoadManager;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Overlay;

import java.util.ArrayList;
import java.util.Iterator;


public class MapFragment extends Fragment implements ConnectionCallbacks, OnConnectionFailedListener, LocationListener, MapEventsReceiver, JSONReceiver {
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
    private static final String CURRENT_ROAD_KEY = "current road key";
    private static final String NAVIGATING_KEY = "navigating";

    private static int REQUEST_CODE_RECOVER_PLAY_SERVICES = 200;

    private static final  GeoPoint DEFAULT_MAP_CENTER = SOMEWHERE_IN_TAIWAN;
    private static final GeoPoint DEFAULT_USER_LOCATION = SOMEWHERE_IN_TAIWAN;
    private static final int DEFAULT_ZOOM_LEVEL = 9;
    private static final int DEFAULT_SHOW_ITEM = SHOW_TAIPEI;

    private MapController mapController;
    private Marker userLocationMarker;
    private MapView map;
    private Polyline currentRoadOverlay;
    private Road currentRoad;
    private int zoomLevel;
    private GeoPoint mapCenter;
    private GeoPoint userLocation;
    private Marker pinPointMarker;
    private RadiusMarkerClusterer clusterer;
    private int showItem;

    private Activity activity;
    private boolean navigating = false;
    private boolean firstStartUp;
    private boolean recreate;

    public static String jsonStr = null;

    private GoogleApiClient googleApiClient;
    private LocationRequest locationRequest;

    private JSONReceiver jsonReceiver;
    private RoadReceiver roadReceiver;

    private static boolean MANUAL_LOCATION_DEBUG = true;

    private boolean checkGooglePlayServices(){
        int checkGooglePlayServices = GooglePlayServicesUtil
                .isGooglePlayServicesAvailable(getActivity());
        if (checkGooglePlayServices != ConnectionResult.SUCCESS) {
		/*
		* Google Play Services is missing or update is required
		*  return code could be
		* SUCCESS,
		* SERVICE_MISSING, SERVICE_VERSION_UPDATE_REQUIRED,
		* SERVICE_DISABLED, SERVICE_INVALID.
		*/
            GooglePlayServicesUtil.getErrorDialog(checkGooglePlayServices,
                    (Activity)getActivity(), REQUEST_CODE_RECOVER_PLAY_SERVICES).show();

            return false;
        }

        return true;
    }


    protected synchronized void buildGoogleApiClient() {
        googleApiClient = new GoogleApiClient.Builder(getActivity())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    protected void createLocationRequest() {
        locationRequest = new LocationRequest();
        locationRequest.setInterval(20000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    protected void startLocationUpdates() {
        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
    }

    protected void stopLocationUpdates() {
        if (googleApiClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(
                    googleApiClient, this);
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        Location lastLocation = LocationServices.FusedLocationApi.getLastLocation(
                googleApiClient);

        if (lastLocation != null) {
            Toast.makeText(getActivity(), "Latitude:" + lastLocation.getLatitude()+", Longitude:"+lastLocation.getLongitude(),Toast.LENGTH_SHORT).show();
            if (firstStartUp){
                GeoPoint location = new GeoPoint(lastLocation.getLatitude(), lastLocation.getLongitude());
                userLocation = location;
                mapCenter = location;

                /*An akward way to test if onCreateView has been called*/
                if (userLocationMarker != null){
                    mapController.setCenter(mapCenter);
                    updateUserLocation(userLocation);
                }
            }
        }
        startLocationUpdates();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    public static MapFragment newInstance() {
        Bundle args = new Bundle();
        MapFragment fragment = new MapFragment();
        fragment.setArguments(args);
        return fragment;

    }


    public void fetchShelterAndDisplay(int showItemID){
        showItem = showItemID;
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
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        clusterer = null;
        pinPointMarker = null;
        currentRoadOverlay = null;
        if (savedInstanceState == null){
            firstStartUp = true;
            userLocation = DEFAULT_USER_LOCATION;
            mapCenter = DEFAULT_MAP_CENTER;
            zoomLevel = DEFAULT_ZOOM_LEVEL;
            showItem = DEFAULT_SHOW_ITEM;
            currentRoad = null;
            recreate = false;
            navigating = false;
        }else{
            firstStartUp = false;
            userLocation = new GeoPoint(savedInstanceState.getDouble(USER_LOCATION_LATITUDE_KEY),
                    savedInstanceState.getDouble(USER_LOCATION_LONGITUDE_KEY));
            mapCenter = new GeoPoint(savedInstanceState.getDouble(MAP_CENTER_LATITUDE_KEY),
                    savedInstanceState.getDouble(MAP_CENTER_LONGITUDE_KEY));
            zoomLevel = savedInstanceState.getInt(CURRENT_ZOOM_LEVEL_KEY);
            showItem = savedInstanceState.getInt(SHOW_ITEM_KEY);
            currentRoad = savedInstanceState.getParcelable(CURRENT_ROAD_KEY);
            recreate = true;
            navigating = savedInstanceState.getBoolean(NAVIGATING_KEY);
        }

        if (checkGooglePlayServices()) {
            buildGoogleApiClient();
            createLocationRequest();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (googleApiClient != null) {
            googleApiClient.connect();
        }

       /* IntentFilter filter1 = new IntentFilter(JSONReceiver.RECEIVE_JSON_ACTION);
        filter1.addCategory(Intent.CATEGORY_DEFAULT);
        jsonReceiver = new JSONReceiver();
        getActivity().registerReceiver(jsonReceiver, filter1);*/

        IntentFilter filter2= new IntentFilter(RoadReceiver.RECEIVE_ROAD_ACTION);
        filter2.addCategory(Intent.CATEGORY_DEFAULT);
        roadReceiver = new RoadReceiver();
        getActivity().registerReceiver(roadReceiver, filter2);
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
        Drawable userIcon = getResources().getDrawable(R.drawable.masculineavatar32);
        userLocationMarker.setIcon(userIcon);

        map.getOverlays().add(userLocationMarker);

        //fetchShelterAndDisplay(showItem);


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
        updateRoadOverlay(RoadManager.buildRoadOverlay(road, getActivity()));
    }

    private void updateRoadOverlay(Polyline roadOverlay){
        clearRoad();
        currentRoadOverlay = roadOverlay;
        addOverlay(roadOverlay);
    }

    private void addOverlay(Overlay overlay){
        map.getOverlays().add(overlay);
        map.invalidate();
    }

    private void removeOverlay(Overlay overlay){
        map.getOverlays().remove(map.getOverlays().indexOf(overlay));
        map.invalidate();
    }

    private void updateClusterer(RadiusMarkerClusterer newClusterer){
        clearClusterer();
        clusterer = newClusterer;
        addOverlay(newClusterer);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.activity = activity;
        Log.e("onAttach", "activity == null?" + (activity == null));
    }

    private RadiusMarkerClusterer buildClustererFromJSONObject(JSONObject dataset) throws JSONException{
        Log.e("buildClustere", "activity == null?" + (activity == null));
        RadiusMarkerClusterer newClusterer = new RadiusMarkerClusterer(activity);
        JSONArray array = dataset.getJSONArray("features");
        for (int i =0; i<array.length(); i++){
            Double latitude = array.getJSONObject(i).getJSONObject("geometry").getJSONArray("coordinates").getDouble(1);
            Double longitude = array.getJSONObject(i).getJSONObject("geometry").getJSONArray("coordinates").getDouble(0);

            Marker newMarker = new Marker(map);
            newMarker.setPosition(new GeoPoint(latitude, longitude));
            Drawable shelterIcon = getResources().getDrawable(R.drawable.position_mark_32);
            newMarker.setIcon(shelterIcon);
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
            newMarker.setTitle("omg " + i%8 + "+" + i%5 + "="  + (i%4-2)*2);
            newClusterer.add(newMarker);
        }

        Drawable clusterIconD = getResources().getDrawable(R.drawable.marker_cluster);
        Bitmap clusterIcon = ((BitmapDrawable)clusterIconD).getBitmap();
        newClusterer.setIcon(clusterIcon);
        newClusterer.setRadius(70);
        return newClusterer;
    }

    public void
    updateUIWithJSON(JSONObject dataset) throws JSONException{
        updateClusterer(buildClustererFromJSONObject(dataset));
        //clearRoad();
        //clearInfoWindow();
    }

    public void clearClusterer(){
        if (clusterer != null){
            removeOverlay(clusterer);
            clusterer = null;
        }
    }

    public void clearRoad(){
        if (currentRoadOverlay != null){
            removeOverlay(currentRoadOverlay);
            currentRoadOverlay = null;
        }
    }

    public void clearPinpoint(){
        if (pinPointMarker != null){
            removeOverlay(pinPointMarker);
            pinPointMarker = null;
        }
    }

    public void clearInfoWindow(){
        InfoWindow.closeAllInfoWindowsOn(map);
    }

    @Override
    public void handleJSON(JSONObject jsonObject) throws JSONException{
        updateUIWithJSON(jsonObject);
        if (recreate){
            if (navigating){
                updateUIWithRoad(currentRoad);
            }
            recreate = false;
        }else{
            clearInfoWindow();
            clearRoad();
        }
    }

    private class NavigateInfoWindow extends MarkerInfoWindow {
        ListView list;
        ArrayAdapter<String> adapter;

        Button naviBtn,closeBtn;
        public NavigateInfoWindow(MapView mapView, final Marker marker) {
            super(R.layout.bonuspack_bubble, mapView);

            Context context = mapView.getContext();
            String packageName = context.getPackageName();
            list = (ListView)this.mView.findViewById(R.id.bubble_list);
            list.setVisibility(View.VISIBLE);



            ArrayList<String> array = new ArrayList<String>();
            adapter = new ArrayAdapter<String>(context, R.layout.property_list_item, array);
            list.setAdapter(adapter);

            naviBtn = (Button)(mView.findViewById(R.id.bubble_btn));
            naviBtn.setText("Navigate");
            naviBtn.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    //if (!navigating) {
                        fetchRoadAndDisplay(userLocation, marker.getPosition());
                        /*navigating = true;
                        btn.setText("Cancel Navi");*/
                    //} else {
                        /*Log.i("NAVITAG", "currentRoadOverlay null?" + (currentRoadOverlay == null));
                        if(currentRoadOverlay != null){
                            removeOverlay(currentRoadOverlay);
                            currentRoadOverlay = null;
                            map.invalidate();
                            navigating = false;
                            btn.setText("Navigate");
                        }*/
                    //}
                }
            });

            closeBtn = (Button)(mView.findViewById(R.id.bubble_close));
            closeBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    close();
                }
            });
        }

        @Override public void onOpen(Object item){
            super.onOpen(item);
            mView.findViewById(R.id.bubble_btn).setVisibility(View.VISIBLE);
            clearInfoWindow();
        }

        public void addProperty(String key, String value){
            adapter.add(key + ":" + value);
        }
    }

    //marker past into constuctor must have position set first.
    private class SetLocationInfoWindow extends MarkerInfoWindow {
        public SetLocationInfoWindow(MapView mapView, final Marker marker) {
            super(R.layout.bonuspack_bubble, mapView);
            Button locationBtn = (Button)(mView.findViewById(R.id.bubble_btn));

            if (MANUAL_LOCATION_DEBUG){
                locationBtn.setText("Set location");
                locationBtn.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View view) {
                        if (!userLocation.equals(marker.getPosition())){
                            Toast.makeText(view.getContext(), "setting location...", Toast.LENGTH_SHORT).show();
                            updateUserLocation(marker.getPosition());
                            removeOverlay(marker);
                            pinPointMarker = null;
                        }
                    }
                });
            }

            Button closeBtn = (Button)(mView.findViewById(R.id.bubble_close));
            closeBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    close();
                }
            });

        }

        @Override public void onOpen(Object item){
            super.onOpen(item);
            mView.findViewById(R.id.bubble_btn).setVisibility(View.VISIBLE);
            InfoWindow.closeAllInfoWindowsOn(map);
        }
    }

    private void updateUserLocation(double latitude, double longitude){
        userLocation = new GeoPoint(latitude, longitude);
        userLocationMarker.setPosition(userLocation);
        map.invalidate();
    }

    private void updateUserLocation(GeoPoint location){
        userLocation = location;
        userLocationMarker.setPosition(userLocation);
        map.invalidate();
    }

    @Override
    public void onSaveInstanceState(Bundle saveInstanceState){
        saveInstanceState.putDouble(MAP_CENTER_LATITUDE_KEY, map.getMapCenter().getLatitude());
        saveInstanceState.putDouble(MAP_CENTER_LONGITUDE_KEY, map.getMapCenter().getLongitude());
        saveInstanceState.putInt(CURRENT_ZOOM_LEVEL_KEY, map.getZoomLevel());
        saveInstanceState.putDouble(USER_LOCATION_LATITUDE_KEY, userLocation.getLatitude());
        saveInstanceState.putDouble(USER_LOCATION_LONGITUDE_KEY, userLocation.getLongitude());
        saveInstanceState.putInt(SHOW_ITEM_KEY, showItem);
        saveInstanceState.putBoolean(NAVIGATING_KEY, navigating);
        saveInstanceState.putParcelable(CURRENT_ROAD_KEY, currentRoad);

    }

    @Override
    public void onLocationChanged(Location location) {

        String msg = "New Latitude: " + location.getLatitude()
                + "New Longitude: " + location.getLongitude();


        Toast.makeText(getActivity(), msg, Toast.LENGTH_SHORT).show();
        updateUserLocation(location.getLatitude(), location.getLongitude());
        if (navigating){
            fetchRoadAndDisplay(userLocation, new GeoPoint(location.getLatitude(), location.getLongitude()));
        }
}


    @Override
    public boolean singleTapConfirmedHelper(GeoPoint p) {
        navigating = false;
        currentRoad = null;
        clearInfoWindow();
        clearPinpoint();
        clearRoad();
        return true;
    }

    private void updatePinPoint(Marker newPinpoint){
        clearPinpoint();
        pinPointMarker = newPinpoint;
        addOverlay(newPinpoint);
    }

    @Override public boolean longPressHelper(GeoPoint p) {

        clearRoad();

        Marker newPinPoint = new Marker(map);
        newPinPoint.setPosition(p);
        newPinPoint.setInfoWindow(new SetLocationInfoWindow(map, newPinPoint));
        newPinPoint.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER);
        Drawable pinpointIcon = getResources().getDrawable(R.drawable.spyhole_32);
        newPinPoint.setIcon(pinpointIcon);
        updatePinPoint(newPinPoint);
        return true;
    }

    @Override
    public void onPause(){
        super.onPause();
        zoomLevel = map.getZoomLevel();
        mapCenter = (GeoPoint)map.getMapCenter();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (googleApiClient != null) {
            stopLocationUpdates();
            googleApiClient.disconnect();
        }

        getActivity().unregisterReceiver(roadReceiver);
       /* getActivity().unregisterReceiver(jsonReceiver);*/
    }


    public void fetchRoadAndDisplay(GeoPoint startPoint, GeoPoint endPoint){
        Intent serviceIntent = new Intent(getActivity(), FetchRoadIntentService.class);
        serviceIntent.putExtra(FetchRoadIntentService.START_POINT_LAT, startPoint.getLatitude());
        serviceIntent.putExtra(FetchRoadIntentService.START_POINT_LONG, startPoint.getLongitude());
        serviceIntent.putExtra(FetchRoadIntentService.END_POINT_LAT, endPoint.getLatitude());
        serviceIntent.putExtra(FetchRoadIntentService.END_POINT_LONG, endPoint.getLongitude());
        getActivity().startService(serviceIntent);
    }

    /*public class JSONReceiver extends BroadcastReceiver{
        public static final String RECEIVE_JSON_ACTION = "com.mad.openisdm.madnew.JSONReceive";

        @Override
        public void onReceive(Context context, Intent intent) {
            JSONObject jsonObject = null;
            String jsonText = intent.getStringExtra(FetchJSONIntentService.JSON_OBJECT_KEY);
            boolean exception = intent.getBooleanExtra(FetchJSONIntentService.EXCEPTION_KEY, false);
            if (exception){
                Toast.makeText(context, "MapFrag-IO ERROR", Toast.LENGTH_SHORT).show();
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
                        if (recreate){
                            if (navigating){
                                updateUIWithRoad(currentRoad);
                            }
                            recreate = false;
                        }else{
                            clearInfoWindow();
                            clearRoad();
                        }
                    } catch (JSONException e) {
                        Log.i("JSONTAG", "Dataset doesn't follow GEOJSON format");
                        Toast.makeText(map.getContext(), "GEOJSON format error", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
    }*/

    public class RoadReceiver extends BroadcastReceiver{
        public static final String RECEIVE_ROAD_ACTION ="com.mad.openisdm.madnew.MapFragment.roadReceive";
        @Override
        public void onReceive(Context context, Intent intent) {
            Road road = (Road)(intent.getParcelableExtra(FetchRoadIntentService.ROAD_KEY));
            if (road.mStatus != Road.STATUS_OK){
                Log.i("ROADTAG", "Not ok");
                Toast.makeText(map.getContext(), "ROAD STATUS NOT OK", Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(context, "navigating...", Toast.LENGTH_SHORT).show();
                updateUIWithRoad(road);
                navigating = true;
                currentRoad = road;
            }
        }
    }
}

