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
import com.mad.openisdm.madnew.app.JSONBroadcastReceiverManager;
import com.mad.openisdm.madnew.app.MapFragment;
import com.mad.openisdm.madnew.app.ShelterListFragment;
import com.mad.openisdm.madnew.app.ShelterSourceSelector;

import java.util.Calendar;


public class  MainActivity extends ActionBarActivity {
    private static final String CURRENT_ITEM_KEY = "current item key";
    private static final String[] LIST_OF_ITEMS = {"Taipei", "Hsinchu", "New Taipei"};
    private int currentItem = 0;
    private FragmentManager fm;
    DrawerLayout drawerLayout;
    ListView drawerList;
    private ActionBarDrawerToggle drawerToggle;
    private MapFragment mapFragment;
    public ShelterListFragment listFragment;

    MyFragmentStatePagerAdapter pagerAdapter;
    ViewPager viewPager;

    JSONBroadcastReceiverManager jsonBroadcastReceiverManager;
    


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.e("Activity---", "onCreate");
        super.onCreate(savedInstanceState);
        Log.e("Activity---", "breakpoint");
        setContentView(R.layout.activity_main);

        //mapFragment = new MapFragment();
        if (savedInstanceState == null){
            listFragment = new ShelterListFragment();

            listFragment.equalsOrNot(this);

        }

        jsonBroadcastReceiverManager = new JSONBroadcastReceiverManager(this);

        if (savedInstanceState != null){
            currentItem = savedInstanceState.getInt(CURRENT_ITEM_KEY);
        }

        pagerAdapter = new MyFragmentStatePagerAdapter(getSupportFragmentManager());
        viewPager = (ViewPager) findViewById(R.id.pager);
        viewPager.setAdapter(pagerAdapter);

        drawerList = (ListView)findViewById(R.id.left_drawer);
        drawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);
        drawerList.setAdapter(new ArrayAdapter(this, R.layout.drawer_list_item, LIST_OF_ITEMS));

        /*fm = getSupportFragmentManager();
        mapFragment = (MapFragment)fm.findFragmentById(R.id.content_frame);

        final ShelterListFragment listFragment = new ShelterListFragment();

        if (mapFragment == null){
            mapFragment = MapFragment.newInstance();
            fm.beginTransaction().add(R.id.content_frame, listFragment).commit();
        }
*/

        //jsonBroadcastReceiverManager.addReceiver(mapFragment);
        jsonBroadcastReceiverManager.addReceiver(listFragment);
        Log.e("Activity---:", "OnCreate-List.Activity null?" + (listFragment.activity == null));
        jsonBroadcastReceiverManager.registerJSONBroadcastReceiver();
        new ShelterSourceSelector(this).selectShelterSource(ShelterSourceSelector.ShelterID.SHOW_TAIPEI).fetchFromSource();

        setActionBarTitle(LIST_OF_ITEMS[currentItem] + " view");

        drawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                currentItem = position;
                String item = (String) parent.getAdapter().getItem(position);
                if (item.equals("Taipei")) {
                    new ShelterSourceSelector(MainActivity.this).selectShelterSource(ShelterSourceSelector.ShelterID.SHOW_TAIPEI).fetchFromSource();
                    //mapFragment.fetchShelterAndDisplay(MapFragment.SHOW_TAIPEI);
                    //listFragment.fetchShelterAndDisplay(ShelterListFragment.SHOW_TAIPEI);
                } else if (item.equals("Hsinchu")) {
                    new ShelterSourceSelector(MainActivity.this).selectShelterSource(ShelterSourceSelector.ShelterID.SHOW_HSINCHU).fetchFromSource();
                    // mapFragment.fetchShelterAndDisplay(MapFragment.SHOW_HSINCHU);
                    //listFragment.fetchShelterAndDisplay(ShelterListFragment.SHOW_HSINCHU);
                } else if (item.equals("New Taipei")) {
                    new ShelterSourceSelector(MainActivity.this).selectShelterSource(ShelterSourceSelector.ShelterID.SHOW_NEW_TAIPEI).fetchFromSource();
                    //mapFragment.fetchShelterAndDisplay(MapFragment.SHOW_NEW_TAIPEI);
                    //listFragment.fetchShelterAndDisplay(ShelterListFragment.SHOW_NEW_TAIPEI);
                }
                drawerList.setItemChecked(position, true);
                drawerLayout.closeDrawers();
            }
        });

        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.drawer_open, R.string.drawer_close) {

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                setActionBarTitle(LIST_OF_ITEMS[currentItem] + " view");
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


    private class MyFragmentStatePagerAdapter extends FragmentStatePagerAdapter{
        private Fragment fragmentMaps, fragmentList;
        public MyFragmentStatePagerAdapter(FragmentManager fm){
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    fragmentList =listFragment;
                    return fragmentList;
                default:
                    fragmentMaps = mapFragment;
                    return fragmentMaps;
            }
        }

        @Override
        public int getCount() {
            return 1;
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
        Log.e("Activity---:", "OnResume-List.Activity null?"+(listFragment.activity == null));
        super.onResume();
    }

    @Override
    protected void onStart() {
        Log.e("Activity---", "onStart");
        Log.e("Activity---:", "OnStart-List.Activity null?"+(listFragment.activity == null));
        super.onStart();
    }

    public void onStop(){
        Log.e("Activity---", "onStop");
        super.onStop();

        jsonBroadcastReceiverManager.unregisterJSONBroadcastReceiver();
    }
}
