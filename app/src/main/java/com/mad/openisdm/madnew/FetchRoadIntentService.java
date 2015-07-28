package com.mad.openisdm.madnew;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;

import org.osmdroid.bonuspack.routing.OSRMRoadManager;
import org.osmdroid.bonuspack.routing.Road;
import org.osmdroid.bonuspack.routing.RoadManager;
import org.osmdroid.util.GeoPoint;

import java.util.ArrayList;


public class FetchRoadIntentService extends IntentService {
    public static final String START_POINT_LAT = "START_POINT_LAT";
    public static final String START_POINT_LONG = "START_POINT_LONG";
    public static final String END_POINT_LAT = "END_POINT_LAT";
    public static final String END_POINT_LONG = "END_POINT_LONG";

    public static final String ROAD_KEY = "ROAD_KEY";

    public FetchRoadIntentService() {
        super("FetchRoadIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        GeoPoint startPoint = new GeoPoint(intent.getDoubleExtra(START_POINT_LAT, 0), intent.getDoubleExtra(START_POINT_LONG, 0));
        GeoPoint endPoint = new GeoPoint(intent.getDoubleExtra(END_POINT_LAT, 0), intent.getDoubleExtra(END_POINT_LONG, 0));

        ArrayList<GeoPoint> waypoints = new ArrayList<GeoPoint>();
        waypoints.add(startPoint);
        waypoints.add(endPoint);

        RoadManager roadManager = null;
        roadManager = new OSRMRoadManager();
        Road road = roadManager.getRoad(waypoints);

        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(MapFragment.RoadReceiver.RECEIVE_ROAD_ACTION);
        broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
        broadcastIntent.putExtra(ROAD_KEY, road);
        sendBroadcast(broadcastIntent);
    }

}
