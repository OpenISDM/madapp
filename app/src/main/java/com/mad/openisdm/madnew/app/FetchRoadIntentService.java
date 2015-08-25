package com.mad.openisdm.madnew.app;

import android.app.IntentService;
import android.content.Intent;

import org.osmdroid.bonuspack.routing.OSRMRoadManager;
import org.osmdroid.bonuspack.routing.Road;
import org.osmdroid.bonuspack.routing.RoadManager;
import org.osmdroid.util.GeoPoint;

import java.util.ArrayList;

/**
 * This class is an intent service(a worker thread) that downloads Road information(for Routing/navigation purposes) and send
 * the result back in an intent.
 *
 * The context(activity) that starts this Intent service should register a broadcast receiver in order to receive the result(the Road).
 * */
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

        /*The arraylist that contains the start and end point of the road*/
        ArrayList<GeoPoint> waypoints = new ArrayList<GeoPoint>();
        waypoints.add(startPoint);
        waypoints.add(endPoint);

        /*request a Road object using the OSRM routing service*/
        RoadManager roadManager = null;
        roadManager = new OSRMRoadManager();
        Road road = roadManager.getRoad(waypoints);

        /*Send the result back using an implicit Intent when this thread ends.
        * context(activity) should register a broadcast receiver to retrieve the result*/
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(MapFragment.RoadReceiver.RECEIVE_ROAD_ACTION);
        broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
        broadcastIntent.putExtra(ROAD_KEY, road);
        sendBroadcast(broadcastIntent);
    }

}
