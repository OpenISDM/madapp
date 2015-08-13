package com.mad.openisdm.madnew.main;

import android.content.res.Configuration;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.ActionBarDrawerToggle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.mad.openisdm.madnew.R;
import com.mad.openisdm.madnew.app.OnLocationChangedListener;
import com.mad.openisdm.madnew.app.Shelter;
import com.mad.openisdm.madnew.app.ShelterManager;
import com.mad.openisdm.madnew.app.MapFragment;
import com.mad.openisdm.madnew.app.ShelterListFragment;
import com.mad.openisdm.madnew.app.ShelterSourceSelector;

import org.osmdroid.util.GeoPoint;

import java.util.ArrayList;
import java.util.Calendar;


public class  MainActivity extends ActionBarActivity implements Shelter.OnShelterReceiveListener, OnLocationChangedListener {
    private static final String CURRENT_ITEM_KEY = "current item key";
    private static final String[] LIST_OF_ITEMS = {"Taipei", "Hsinchu", "New Taipei"};
    private static final String LIST_FRAGMENT_KEY = "LIST_FRAGMENT_KEY";
    private static final String MAP_FRAGMENT_KEY = "MAP_FRAGMENT_KEY";
    private static final int DEFAULT_ITEM = 1;

    private int currentItem = 0;
    private DrawerLayout drawerLayout;
    private ListView drawerList;
    private ActionBarDrawerToggle drawerToggle;
    private MapFragment mapFragment;
    private ShelterListFragment listFragment;

    private ArrayList<Shelter> shelters;

    MyFragmentStatePagerAdapter pagerAdapter;
    ViewPager viewPager;

    ShelterManager shelterManager;
    

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.e("Activity---", "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        shelterManager = new ShelterManager(this, this);

        if (savedInstanceState != null){
            currentItem = savedInstanceState.getInt(CURRENT_ITEM_KEY);
            listFragment = (ShelterListFragment)getSupportFragmentManager().getFragment(savedInstanceState, LIST_FRAGMENT_KEY);
            //shelterManager.addReceiver(listFragment);
            mapFragment = (MapFragment)getSupportFragmentManager().getFragment(savedInstanceState, MAP_FRAGMENT_KEY);
            //shelterManager.addReceiver(mapFragment);
        }else{
            currentItem = DEFAULT_ITEM;
        }


        pagerAdapter = new MyFragmentStatePagerAdapter(getSupportFragmentManager());
        viewPager = (ViewPager) findViewById(R.id.pager);
        viewPager.setAdapter(pagerAdapter);

        viewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener(){
            @Override
            public void onPageSelected(int position) {
                getSupportActionBar().setSelectedNavigationItem(position);
            }
        });

        drawerList = (ListView)findViewById(R.id.left_drawer);
        drawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);
        drawerList.setAdapter(new ArrayAdapter(this, R.layout.drawer_list_item, LIST_OF_ITEMS));

        //shelterManager.registerJSONBroadcastReceiver();
        shelterManager.connect();

        drawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                currentItem = position;
                String item = (String) parent.getAdapter().getItem(position);
                int sourceID = 0;
                if (item.equals("Taipei")) {
                    sourceID = ShelterSourceSelector.ShelterID.SHOW_TAIPEI;
                } else if (item.equals("Hsinchu")) {
                    sourceID = ShelterSourceSelector.ShelterID.SHOW_HSINCHU;
                } else if (item.equals("New Taipei")) {
                    sourceID = ShelterSourceSelector.ShelterID.SHOW_NEW_TAIPEI;
                }
                new ShelterSourceSelector(MainActivity.this).selectShelterSource(sourceID).fetchFromSource();
                setActionBarTitle(LIST_OF_ITEMS[currentItem]);
                drawerList.setItemChecked(position, true);
                drawerLayout.closeDrawers();
            }
        });


        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.drawer_open, R.string.drawer_close) {

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                setActionBarTitle("View options");
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };

        drawerLayout.setDrawerListener(drawerToggle);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);

        for (int i = 0; i < pagerAdapter.getCount(); i++) {

            /**
             * Create a tab with text corresponding to the page title defined by
             * the adapter. Also specify this Activity object, which implements
             * the TabListener interface, as the callback (listener) for when
             * this tab is selected.
             */
            actionBar.addTab(
                    actionBar.newTab()
                            .setText(pagerAdapter.getPageTitle(i))
                            .setTabListener(new ActionBar.TabListener() {

                                @Override
                                public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
                                    viewPager.setCurrentItem(tab.getPosition());
                                }

                                @Override
                                public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft) {

                                }

                                @Override
                                public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft) {

                                }
                            })
            );
        }
    }

    private void setActionBarTitle(String str){
        getSupportActionBar().setTitle(str);
    }

    @Override
    public void onSaveInstanceState(Bundle outState){
        super.onSaveInstanceState(outState);
        outState.putInt(CURRENT_ITEM_KEY, currentItem);
        getSupportFragmentManager().putFragment(outState, LIST_FRAGMENT_KEY, listFragment);
        getSupportFragmentManager().putFragment(outState, MAP_FRAGMENT_KEY, mapFragment);
    }


    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        drawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        drawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Pass the event to ActionBarDrawerToggle, if it returns
        // true, then it has handled the app icon touch event
        if (drawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        else if(item.getItemId() == R.id.menuitem_about){
            Calendar calendar = Calendar.getInstance();
            AlertDialogFragment alertDlgFragment = AlertDialogFragment.newInstance("Academia Sinica - OPENISDM",
                    "Mobile MAD App ?" + String.valueOf(calendar.get(Calendar.YEAR)));
            alertDlgFragment.setCancelable(false);
            alertDlgFragment.show(getSupportFragmentManager(), "dialog");
            return true;
        }
        // Handle your other action bar items...

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onShelterReceive(ArrayList<Shelter> shelters) {
        this.shelters = shelters;
        mapFragment.setAndUpdateShelters(shelters);
        listFragment.setAndUpdateShelters(shelters);
    }

    @Override
    public void onLocationChanged(GeoPoint userLocation) {
        if (shelters != null){
            for (Shelter shelter : shelters){
                shelter.calculateDistance(userLocation);
                Log.e("sheltertag", "distance:" + shelter.distance + "m");
            }
        }
        mapFragment.setAndUpdateShelters(shelters);
        listFragment.setAndUpdateShelters(shelters);
    }

    private class MyFragmentStatePagerAdapter extends FragmentStatePagerAdapter{
        public MyFragmentStatePagerAdapter(FragmentManager fm){
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            Log.e("Activity ---", "getItem called");
            switch (position) {
                case 0:
                    mapFragment = new MapFragment();
                    //shelterManager.addReceiver(mapFragment);
                    return mapFragment;
                case 1:
                    listFragment = new ShelterListFragment();
                    //shelterManager.addReceiver(listFragment);
                    return listFragment;
                default: return null;
            }
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "Map";
                case 1:
                    return "List";
            }
            return "Default";
        }
    }

    @Override
    protected void onDestroy() {
        Log.e("Activity---", "onDestroy");
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        Log.e("Activity---", "onPause");
        super.onPause();
    }

    @Override
    protected void onResume() {
        Log.e("Activity---", "onResume");
        super.onResume();
    }

    @Override
    protected void onStart() {
        Log.e("Activity---", "onStart");
        super.onStart();
        //shelterManager.registerJSONBroadcastReceiver();
        shelterManager.connect();

        drawerList.performItemClick(
                drawerList.getAdapter().getView(currentItem, null, null),
                currentItem,
                drawerList.getAdapter().getItemId(currentItem));
    }

    public void onStop(){
        Log.e("Activity---", "onStop");
        super.onStop();

        //shelterManager.unregisterJSONBroadcastReceiver();
        shelterManager.disconnect();
    }


}
