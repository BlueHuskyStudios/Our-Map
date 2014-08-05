package org.bh.app.ourmap;

import android.app.Fragment;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.bh.app.ourmap.util.Helper;
import org.bh.app.ourmap.util.SignalStateListener;
import org.bh.app.ourmap.util.Stats;

public class MapsActivity extends FragmentActivity {

    private GoogleMap map; // Might be null if Google Play services APK is not available.
    private ActionBarDrawerToggle drawerToggle;
    private ListView drawerList;
    private DrawerLayout drawerLayout;
    private Fragment settingsFrag;
    private SupportMapFragment mapsFrag;

    private double sigStr;
    private Stats stats;
    private Marker marker;
    private boolean tracking = true;

    private static final boolean ONLY_UPDATE_ONCE = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        stats =
            new Stats(this)
            .setDelay(5);

        setUpMapIfNeeded();
        buildDrawer();
        drawerLayout.openDrawer(drawerList);

        settingsFrag = new SettingsActivity.GeneralPreferenceFragment();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

    private void buildDrawer()
    {
        setDrawerItems(new String[]{getString(R.string.loading_text)});

        stats.awaitSignalStrength(new SignalStateListener() {
            @Override
            public void signalStrengthUpdate(double signalStrength) {
                String[] items = getResources().getStringArray(R.array.drawer_items);
                {
                    stats.fetchAll();
                    items = new String[]
                    {
                        "Geo Location: \t ("   + stats.geoLocation.getLatitude() + ", " + stats.geoLocation.getLongitude() + ")",
                        "Signal Strength: \t " + signalStrength,
                        "Signal Type: \t "     + stats.signalType,
                        "Provider name: \t "   + stats.providerName,
                        "Provider ID: \t "     + stats.providerID
                    };
                }

                setDrawerItems(items);



                LatLng latLng =
                    new LatLng(
                        stats.geoLocation.getLatitude(),
                        stats.geoLocation.getLongitude()
                    )
                ;

                if (marker != null)
                    marker.setPosition(latLng);

                if (tracking) {
                    CameraUpdate cu =
                        CameraUpdateFactory.newCameraPosition(
                            CameraPosition.fromLatLngZoom(
                                latLng,
                                map.getMaxZoomLevel() / 2
//                            Helper.locationAccuracyToCameraZoom(stats.geoLocation.getAccuracy())
                            )
                        );
                    map.animateCamera(cu);
                }
            }
        },
        ONLY_UPDATE_ONCE);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);
    }

    private void setDrawerItems(String[] items) {
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawerList = (ListView) findViewById(R.id.left_drawer);
        drawerToggle = new ActionBarDrawerToggle(
            MapsActivity.this,
            drawerLayout,
            R.drawable.ic_drawer_dark,
            R.string.drawer_open,
            R.string.drawer_close
        );
        drawerToggle.setDrawerIndicatorEnabled(true);

        drawerList.setAdapter(
            new ArrayAdapter<String>(
                MapsActivity.this,
                android.R.layout.simple_list_item_1,
                items)
        );
        drawerList.setOnItemClickListener(new DrawerItemClickListener());

        drawerLayout.setDrawerListener(drawerToggle);
    }

    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView parent, View view, int position, long id) {
            selectItem(position);
        }
    }

    /** Swaps fragments in the main content view */
    private void selectItem(int position) {
        /*
        Bundle args = new Bundle();
        switch (position)
        {
            case 0:
                mapsFrag.setArguments(args);
                getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.map_holder, mapsFrag, null)
                    .commit();
                break;
            case 1:
                settingsFrag.setArguments(args);
                // Insert the fragment by replacing any existing fragment
                getFragmentManager()
                    .beginTransaction()
                    .replace(R.id.map_holder, settingsFrag, null)
                    .commit();
                break;
            default:
                throw new AssertionError("Position must be 0 or 1 (was " + position + ")");
        }


        // Highlight the selected item, update the title, and close the drawer
        drawerList.setItemChecked(position, true);
//        setTitle(mPlanetTitles[position]);
        drawerLayout.closeDrawer(drawerList);
        */
    }

    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setUpMap()} once when {@link #map} is not null.
     * <p>
     * If it isn't installed {@link SupportMapFragment} (and
     * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
     * install/update the Google Play services APK on their device.
     * <p>
     * A user can return to this FragmentActivity after following the prompt and correctly
     * installing/updating/enabling the Google Play services. Since the FragmentActivity may not
     * have been completely destroyed during this process (it is likely that it would only be
     * stopped or paused), {@link #onCreate(Bundle)} may not be called again so we should call this
     * method in {@link #onResume()} to guarantee that it will be called.
     */
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (map == null) {
            // Try to obtain the map from the SupportMapFragment.
            map = (mapsFrag = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (map != null) {
                setUpMap();
            }
        }
    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near Africa.
     * <p>
     * This should only be called once and when we are sure that {@link #map} is not null.
     */
    private void setUpMap() {
        marker = map.addMarker(new MarkerOptions().position(new LatLng(0, 0)).title("Marker"));
        map.setLocationSource(stats);
        map.setMyLocationEnabled(true);
        map.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
            @Override
            public boolean onMyLocationButtonClick() {
                return !(tracking = !tracking);
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (drawerToggle.onOptionsItemSelected(item))
            return true;
        return super.onOptionsItemSelected(item);
    }
}