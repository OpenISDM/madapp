/**This fragment handles a map UI. Activity that hosts this fragment must implement the interface: OnLocationChangedListener
* The map UI is implemented using OSMDroid's map view. ODMDroid's MapView class permits offline-viewing, nevertheless, other
* functionality such as routing(navigation) requires wireless access.
*
* This fragment  fetches GEOJSON files containing shelter information from a server and displays them on a map. It also provides
 * other basic map functionalities.
*
* When this fragment first creates, the map uses the GPS system to locate the current user location, and display such on the map.
* During first launch, when user location is undetectable(GPS is weak when used indoor), the default location is determined by the constant DEFAULT_USER_LOCATION, which
* is currently set to (somewhere in) Taipei.
*
* To update the map with a list of shelters, call the method updateUIWithShelters.
*
* Note: this fragment by itself will not display any shelter information
* User must first create an instance of ShelterManager and call ShelterManager.connect(), then choose the
* shelter to display using ShelterSourceSelector.
*
* */


package com.mad.openisdm.madnew.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
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
import org.osmdroid.util.BoundingBoxE6;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Overlay;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;


public class MapFragment extends Fragment implements ConnectionCallbacks, OnConnectionFailedListener, LocationListener, MapEventsReceiver, ResultCallback<LocationSettingsResult> {


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

    private Activity activity;
    private OnLocationChangedListener listener;
    private boolean navigating = false;
    private boolean firstStartUp;
    private boolean recreate;

    private GoogleApiClient googleApiClient;
    private LocationRequest locationRequest;

    private RoadReceiver roadReceiver;

    private static boolean MANUAL_LOCATION_DEBUG = true;

    /**
     * Check location settings
     */
    protected LocationSettingsRequest mLocationSettingsRequest;



    private boolean checkGooglePlayServices() {
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
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
    }

    protected void buildLocationSettingsRequest() {
        mLocationSettingsRequest = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest)
                .build();
    }

    protected void checkLocationSettings() {
        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(
                        googleApiClient,
                        mLocationSettingsRequest
                );

        result.setResultCallback(this);
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
            if (firstStartUp){
                GeoPoint location = new GeoPoint(lastLocation.getLatitude(), lastLocation.getLongitude());
                userLocation = location;
                DataHolder.userLocation = userLocation;
                mapCenter = location;

                /*An akward way to test if onCreateView has been called*/
                if (userLocationMarker != null){
                    mapController.animateTo(mapCenter);
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


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        clusterer = null;
        pinPointMarker = null;
        currentRoadOverlay = null;
        if (savedInstanceState == null){
            firstStartUp = true;
            userLocation = DEFAULT_USER_LOCATION;
            DataHolder.userLocation = userLocation;
            mapCenter = DEFAULT_MAP_CENTER;
            zoomLevel = DEFAULT_ZOOM_LEVEL;
            currentRoad = null;
            recreate = false;
            navigating = false;
        }else{
            firstStartUp = false;
            userLocation = new GeoPoint(savedInstanceState.getDouble(USER_LOCATION_LATITUDE_KEY),
                    savedInstanceState.getDouble(USER_LOCATION_LONGITUDE_KEY));
            DataHolder.userLocation = userLocation;
            mapCenter = new GeoPoint(savedInstanceState.getDouble(MAP_CENTER_LATITUDE_KEY),
                    savedInstanceState.getDouble(MAP_CENTER_LONGITUDE_KEY));
            zoomLevel = savedInstanceState.getInt(CURRENT_ZOOM_LEVEL_KEY);
            currentRoad = savedInstanceState.getParcelable(CURRENT_ROAD_KEY);
            recreate = true;
            navigating = savedInstanceState.getBoolean(NAVIGATING_KEY);
        }

        if (checkGooglePlayServices()) {
            buildGoogleApiClient();
            createLocationRequest();
            buildLocationSettingsRequest();
            checkLocationSettings();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (googleApiClient != null) {
            googleApiClient.connect();
        }

        IntentFilter filter= new IntentFilter(RoadReceiver.RECEIVE_ROAD_ACTION);
        filter.addCategory(Intent.CATEGORY_DEFAULT);
        roadReceiver = new RoadReceiver();
        getActivity().registerReceiver(roadReceiver, filter);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_map, container, false);

        ImageButton findLocation = (ImageButton)root.findViewById(R.id.fab_image_button);
        findLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Location lastLocation = LocationServices.FusedLocationApi.getLastLocation(
                        googleApiClient);

                if (lastLocation != null){
                    GeoPoint location = new GeoPoint(lastLocation.getLatitude(), lastLocation.getLongitude());
                    mapCenter = location;
                    updateUserLocation(location);
                    DataHolder.userLocation = location;
                    listener.onLocationChanged(location);
                    mapController.animateTo(mapCenter);
                    clearRoad();
                    clearInfoWindow();
                }else{
                    Toast.makeText(getActivity(), "Unable to find location. Try again later", Toast.LENGTH_SHORT).show();
                }
            }
        });

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
        Drawable userIcon = getResources().getDrawable(R.drawable.user_marker);
        userLocationMarker.setIcon(userIcon);

        map.getOverlays().add(userLocationMarker);

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
        try {
            this.listener = (OnLocationChangedListener)activity;
        }catch(ClassCastException e){
            Log.e("ClassCastException", "Hosting activity must implement onLocationChangedListener");
        }
    }

    private RadiusMarkerClusterer buildClustererFromShelters(ArrayList<Shelter> shelters){
        RadiusMarkerClusterer newClusterer = new RadiusMarkerClusterer(activity);
        for (Shelter shelter: shelters){
            Marker newMarker = new Marker(map);
            newMarker.setPosition(shelter.getPosition());

            Drawable shelterIcon;
            if (shelter.distance <= 1000){
                shelterIcon = getResources().getDrawable(R.drawable.green_marker);
            }else if (shelter.distance > 1000 && shelter.distance <= 3000){
                shelterIcon = getResources().getDrawable(R.drawable.orange_marker);
            }else if (shelter.distance > 3000 && shelter.distance <= 10000){
                shelterIcon = getResources().getDrawable(R.drawable.pink_marker);
            }else{
                shelterIcon = getResources().getDrawable(R.drawable.red_marker);
            }
            newMarker.setIcon(shelterIcon);
            NavigateInfoWindow infoWindow = new NavigateInfoWindow(map, newMarker);
            HashMap<String, String> properties = shelter.getProperties();
            for (String key:properties.keySet()) {
                infoWindow.addProperty(key, properties.get(key));
            }
            newMarker.setInfoWindow(infoWindow);
            newMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
            newClusterer.setMaxClusteringZoomLevel(14);
            newClusterer.add(newMarker);
        }


        Drawable clusterIconD = getResources().getDrawable(R.drawable.marker_cluster);
        Bitmap clusterIcon = ((BitmapDrawable)clusterIconD).getBitmap();
        newClusterer.setIcon(clusterIcon);
        newClusterer.setRadius(70);
        return newClusterer;
    }

    public void
    updateUIWithShelters(ArrayList<Shelter> shelters){
        updateClusterer(buildClustererFromShelters(shelters));
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
            currentRoad = null;
            navigating = false;
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


    public void setAndUpdateShelters(ArrayList<Shelter> shelters){
        updateUIWithShelters(shelters);
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

    @Override
    public void onResult(LocationSettingsResult locationSettingsResult) {

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
                    fetchRoadAndDisplay(userLocation, marker.getPosition());
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
                            DataHolder.userLocation = userLocation;
                            listener.onLocationChanged(userLocation);
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
        saveInstanceState.putBoolean(NAVIGATING_KEY, navigating);
        saveInstanceState.putParcelable(CURRENT_ROAD_KEY, currentRoad);

    }

    @Override
    public void onLocationChanged(Location location) {

    }


    @Override
    public boolean singleTapConfirmedHelper(GeoPoint p) {
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
        Drawable pinpointIcon = getResources().getDrawable(R.drawable.pinpoint);
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
    }


    public void fetchRoadAndDisplay(GeoPoint startPoint, GeoPoint endPoint){
        Intent serviceIntent = new Intent(getActivity(), FetchRoadIntentService.class);
        serviceIntent.putExtra(FetchRoadIntentService.START_POINT_LAT, startPoint.getLatitude());
        serviceIntent.putExtra(FetchRoadIntentService.START_POINT_LONG, startPoint.getLongitude());
        serviceIntent.putExtra(FetchRoadIntentService.END_POINT_LAT, endPoint.getLatitude());
        serviceIntent.putExtra(FetchRoadIntentService.END_POINT_LONG, endPoint.getLongitude());
        getActivity().startService(serviceIntent);
    }

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

