package com.mad.openisdm.madnew;


import android.os.AsyncTask;
import android.os.Bundle;

import android.app.ProgressDialog;
import android.content.res.Configuration;

import android.util.Log;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;

import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.mad.openisdm.madnew.listener.OnLocationChangedListener;
import com.mad.openisdm.madnew.model.DataHolder;
import com.mad.openisdm.madnew.model.Shelter;
import com.mad.openisdm.madnew.util.JsonReader;

import org.json.JSONArray;
import org.json.JSONObject;

import org.osmdroid.bonuspack.clustering.RadiusMarkerClusterer;
import org.osmdroid.util.GeoPoint;

import java.util.ArrayList;
import java.util.Calendar;



public class  MainActivity extends AppCompatActivity implements OnLocationChangedListener {

    private static final String CURRENT_ITEM_KEY = "current item key";
    private static final String LIST_FRAGMENT_KEY = "LIST_FRAGMENT_KEY";
    private static final String MAP_FRAGMENT_KEY = "MAP_FRAGMENT_KEY";

    private int mCurrentItem = 0;
    private DrawerLayout mDrawerLayout;
    private ArrayAdapter mDrawerAdapter;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;
    private MapFragment mapFragment;
    private ShelterListFragment mListFragment;
    private ArrayList<Shelter> mShelters;

    MyFragmentStatePagerAdapter mPagerAdapter;
    ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.e("Activity---", "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState != null) {
            mCurrentItem = savedInstanceState.getInt(CURRENT_ITEM_KEY);
            mListFragment =
                    (ShelterListFragment) getSupportFragmentManager().getFragment(savedInstanceState, LIST_FRAGMENT_KEY);
            mapFragment =
                    (MapFragment) getSupportFragmentManager().getFragment(savedInstanceState, MAP_FRAGMENT_KEY);
        }

        mPagerAdapter = new MyFragmentStatePagerAdapter(getSupportFragmentManager());
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mPagerAdapter);

        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                getSupportActionBar().setSelectedNavigationItem(position);
            }
        });

        mDrawerList = (ListView) findViewById(R.id.left_drawer);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerAdapter = new ArrayAdapter(this, R.layout.drawer_list_item);
        mDrawerList.setAdapter(mDrawerAdapter);

        new AsyncCityListViewLoader().execute(Config.INTERFACE_SERVER_CITY_LIST_URL);

        mDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                String item = (String) parent.getAdapter().getItem(position);
                setActionBarTitle(item);
                mDrawerList.setItemChecked(position, true);
                mDrawerLayout.closeDrawers();

                final String url = Config.INTERFACE_SERVER_CITY_DATA_URL_PREFIX + item;
                new MarkComputation().execute(url);

            }
        });


        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.drawer_open, R.string.drawer_close) {

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

        mDrawerLayout.setDrawerListener(mDrawerToggle);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);

        for (int i = 0; i < mPagerAdapter.getCount(); i++) {

            /**
             * Create a tab with text corresponding to the page title defined by
             * the adapter. Also specify this Activity object, which implements
             * the TabListener interface, as the callback (listener) for when
             * this tab is selected.
             */
            actionBar.addTab(
                    actionBar.newTab()
                            .setText(mPagerAdapter.getPageTitle(i))
                            .setTabListener(new ActionBar.TabListener() {

                                @Override
                                public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
                                    mViewPager.setCurrentItem(tab.getPosition());
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

    private void setActionBarTitle(String str) {
        getSupportActionBar().setTitle(str);
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(CURRENT_ITEM_KEY, mCurrentItem);
        getSupportFragmentManager().putFragment(outState, LIST_FRAGMENT_KEY, mListFragment);
        getSupportFragmentManager().putFragment(outState, MAP_FRAGMENT_KEY, mapFragment);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
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
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        } else if (item.getItemId() == R.id.menuitem_about) {
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


    private void onShelterReceive(ArrayList<Shelter> shelters) {
        this.mShelters = shelters;
        mapFragment.setAndUpdateShelters(shelters);
        mListFragment.setAndUpdateShelters(shelters);
    }

    @Override
    public void onLocationChanged(GeoPoint userLocation) {
        if (mShelters != null) {
            for (Shelter shelter : mShelters) {
                shelter.calculateDistance(userLocation);
            }
        }
        mapFragment.setAndUpdateShelters(mShelters);
        mListFragment.setAndUpdateShelters(mShelters);
    }


    private class MyFragmentStatePagerAdapter extends FragmentStatePagerAdapter {
        public MyFragmentStatePagerAdapter(FragmentManager fm) {

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
                    mListFragment = new ShelterListFragment();
                    //shelterManager.addReceiver(listFragment);
                    return mListFragment;
                default:
                    return null;
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

//        drawerList.performItemClick(
//                drawerList.getAdapter().getView(currentItem, null, null),
//                currentItem,
//                drawerList.getAdapter().getItemId(currentItem));
    }

    public void onStop() {
        Log.e("Activity---", "onStop");
        super.onStop();
    }

    private class AsyncCityListViewLoader extends AsyncTask<String, Void, Void> {
        private final ProgressDialog dialog = new ProgressDialog(MainActivity.this);

        @Override
        protected void onPostExecute(Void aVoid) {
            dialog.dismiss();
        }

        @Override
        protected void onPreExecute() {
            dialog.setMessage("Downloading city list...");
            dialog.show();
        }

        @Override
        protected Void doInBackground(String... params) {
            try {
                String jsonText = JsonReader.readJsonFromUrl(params[0]);
                JSONObject root = new JSONObject(jsonText);
                JSONArray jsonResult = root.getJSONArray("results");

                for (int i = 0; i < jsonResult.length(); i++) {
                    mDrawerAdapter.add(jsonResult.getString(i));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

    }

    private class MarkComputation extends AsyncTask<String, Void, RadiusMarkerClusterer> {
        private final ProgressDialog dialog = new ProgressDialog(MainActivity.this);

        @Override
        protected void onPostExecute(RadiusMarkerClusterer rmc) {
            dialog.dismiss();
            mapFragment.updateClusterer(rmc);
            mListFragment.setAndUpdateShelters(mShelters);
        }

        @Override
        protected void onPreExecute() {
            dialog.setMessage("Downloading city list...");
            dialog.show();
        }

        @Override
        protected RadiusMarkerClusterer doInBackground(String... params) {
            try {

                Log.d(this.toString(), "Download time START = " + System.currentTimeMillis());
                DataHolder.jsonStr = JsonReader.readJsonFromUrl(params[0]);
                mShelters = Shelter.parseFromRoot(new JSONObject(DataHolder.jsonStr));
                Log.d(this.toString(), "Download time END = " + System.currentTimeMillis());

                return mapFragment.buildClustererFromShelters(mShelters);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

    }
}
