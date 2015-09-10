package com.mad.openisdm.madnew.manager;

import android.app.Activity;
import android.content.IntentSender;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.mad.openisdm.madnew.MainActivity;

import java.util.ArrayList;

/**
 * LocationManager is a singleton pattern
 *
 * Created by aming on 2015/9/8.
 */
public class LocationManager implements
        ResultCallback<LocationSettingsResult>,
        GoogleApiClient.ConnectionCallbacks,
        LocationListener,
        GoogleApiClient.OnConnectionFailedListener {
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;

    /**
     * Check location settings
     */
    protected LocationSettingsRequest mLocationSettingsRequest;

    protected Activity mActivity;

    private static int REQUEST_CODE_RECOVER_PLAY_SERVICES = 200;
    private ConnectedCallback mConnectedCallback;
    private ArrayList<LocationListener> mLocationListenerList;

    /**
     * Self instance.
     */
    private static LocationManager mLocationManager;

    private LocationManager () {
        mLocationListenerList = new ArrayList<LocationListener>();
    }

    /**
     * LocationManager singleton object
     *
     * @return
     */
    public static synchronized LocationManager getInstance() {
        if (mLocationManager == null) {
            mLocationManager = new LocationManager();
        }

        return mLocationManager;
    }

    /**
     * Set activity object
     * @param activity
     */
    public void setActivity(Activity activity) {
        mActivity = activity;
    }

    /**
     * Add LocationListener interface into a ArrayList
     * @param locationListener
     */
    public void addLocationChangeListener(LocationListener locationListener) {
        mLocationListenerList.add(locationListener);
    }

    public boolean checkGooglePlayServices() {
        int checkGooglePlayServices = GooglePlayServicesUtil
                .isGooglePlayServicesAvailable(mActivity);
        if (checkGooglePlayServices != ConnectionResult.SUCCESS) {
            /*
            * Google Play Services is missing or update is required
            *  return code could be
            * SUCCESS,
            * SERVICE_MISSING, SERVICE_VERSION_UPDATE_REQUIRED,
            * SERVICE_DISABLED, SERVICE_INVALID.
            */
            GooglePlayServicesUtil.getErrorDialog(checkGooglePlayServices,
                    mActivity, REQUEST_CODE_RECOVER_PLAY_SERVICES).show();

            return false;
        }

        return true;
    }


    public synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(mActivity)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    public void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(20000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
    }

    public void buildLocationSettingsRequest() {
        mLocationSettingsRequest = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest)
                .build();
    }

    public void checkLocationSettings() {
        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(
                        mGoogleApiClient,
                        mLocationSettingsRequest
                );

        result.setResultCallback(this);
    }

    public void startLocationUpdates() {
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,
                mLocationRequest, this);
    }

    public void stopLocationUpdates() {
        if (mGoogleApiClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(
                    mGoogleApiClient, this);
        }
    }

    public void setOnConnectedCallback(ConnectedCallback connectedCallback) {
        mConnectedCallback = connectedCallback;
    }

    public void googleApiClientConnect() {
        if (mGoogleApiClient != null ) {
            mGoogleApiClient.connect();
        }
    }

    public void googleApiClientDisconnect() {
        if (mGoogleApiClient != null) {
            stopLocationUpdates();
            mGoogleApiClient.disconnect();
        }
    }

    public Location getLastLocation () {
        return LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
    }

    @Override
    public void onConnected(Bundle bundle) {
        Location lastLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);

        if (mConnectedCallback != null) {
            mConnectedCallback.onConnectedCallback();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    /**
     * After checking location settings, the onResult() will be invoked.
     *
     * @param locationSettingsResult
     */
    @Override
    public void onResult(LocationSettingsResult locationSettingsResult) {
        final Status status = locationSettingsResult.getStatus();
        switch (status.getStatusCode()) {
            case LocationSettingsStatusCodes.SUCCESS:
                Log.v("MADApp", "All location settings are satisfied.");
                startLocationUpdates();

                break;
            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                Log.v("MADApp", "Location settings are not satisfied.");

                try {
                    status.startResolutionForResult(mActivity,
                            ((MainActivity) mActivity).getLocationSettingsActivityResultCode());
                } catch (IntentSender.SendIntentException e) {
                    e.printStackTrace();
                }

                break;
            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                Log.v("MADApp", "Location settings are inadequate, and cannot be fixed here.");
                Toast.makeText(mActivity, "Unable to find location. Try again later",
                        Toast.LENGTH_SHORT).show();

                break;
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        for (LocationListener locationListener: mLocationListenerList
             ) {
            locationListener.onLocationChanged(location);
        }
    }

    public interface ConnectedCallback {
        void onConnectedCallback();
    }

    public interface LocationResolutionCode {
        int getLocationSettingsActivityResultCode();
    }
}
