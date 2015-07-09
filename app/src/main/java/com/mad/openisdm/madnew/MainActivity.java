package com.mad.openisdm.madnew;

import android.content.res.Configuration;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.ActionBarDrawerToggle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.Calendar;
import java.util.Map;


public class MainActivity extends ActionBarActivity {
    private static final String CURRENT_ITEM_KEY = "current item key";
    private static final String[] LIST_OF_ITEMS = {"Police Station", "Hospital", "Sport Fields"};
    private int currentItem = 0;
    private FragmentManager fm;
    DrawerLayout drawerLayout;
    ListView drawerList;
    private ActionBarDrawerToggle drawerToggle;
    private MapFragment mapFragment;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState != null){
            currentItem = savedInstanceState.getInt(CURRENT_ITEM_KEY);
        }

        drawerList = (ListView)findViewById(R.id.left_drawer);
        drawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);
        drawerList.setAdapter(new ArrayAdapter(this, R.layout.drawer_list_item, LIST_OF_ITEMS));

        fm = getSupportFragmentManager();
        mapFragment = (MapFragment)fm.findFragmentById(R.id.content_frame);

        if (mapFragment == null){
            mapFragment = MapFragment.newInstance(MapFragment.SHOW_POLICE);
            fm.beginTransaction().add(R.id.content_frame, mapFragment).commit();
        }

        setActionBarTitle(LIST_OF_ITEMS[currentItem] + " view");

        drawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                currentItem = position;
                String item = (String)parent.getAdapter().getItem(position);
                if (item.equals("Police Station")){
                    mapFragment.display(MapFragment.SHOW_POLICE);
                }else {
                    mapFragment.display(MapFragment.SHOW_HOSPITAL);
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
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

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
}
