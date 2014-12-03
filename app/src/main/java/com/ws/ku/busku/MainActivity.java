package com.ws.ku.busku;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;


public class MainActivity extends FragmentActivity{
    private final String ALL_LINE = "All Line";
    private final String LINE_1 = "Line 1";
    private final String LINE_2 = "Line 2";
    private final String LINE_3 = "Line 3";
    private final String LINE_4 = "Line 4";
    private final String LINE_5 = "Line 5";

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private Marker marker;
    private LatLng userPos;
    private Requesttask rt;
    private URL url;
    private LocationManager lm;
    private double lat, lng;
    private Timer timer;
    private TimerTask task;
    private HashMap<String,Marker> markers;
    private Spinner lineSpinner;
    private Button curPosBtn;
    private LatLng curPos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        setUpMapIfNeeded();
        markers = new HashMap<String, Marker>();
        timer = new Timer();
        task = new TimerTask() {
            @Override
            public void run() {
                System.out.println("fuck");
                new Requesttask(mMap, markers).execute("http://10.2.14.60:8080/busesposition");
            }
        };
        try {
            setClient();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected  void setClient() throws IOException {
        timer.schedule(task,0,15000);
        addComponent();
    }

    private void addComponent(){
        lineSpinner = (Spinner)findViewById(R.id.bus_line_spinner);
        curPosBtn = (Button)findViewById(R.id.curPosBtn);
        addBusLineToSpinner();
        setCurPosBtn();
        Toast.makeText(getApplicationContext(), "Add Busline Already", Toast.LENGTH_SHORT).show();
    }

    private void addBusLineToSpinner()
    {
//        Set<String> temp = markers.keySet();
//        String[] temp2 = temp.toArray(new String[0]);
//        Set<String> busLine = new HashSet<String>();
//        for(String s : temp2)
//        {
//            busLine.add(markers.get(s).getSnippet());
//        }
//        temp2 = busLine.toArray(new String[0]);

        String[] temp = {ALL_LINE,LINE_1,LINE_2,LINE_3,LINE_4,LINE_5};

        ArrayAdapter<String> myAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item,temp);
        myAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        lineSpinner.setAdapter(myAdapter);
        lineSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String[] busID = markers.keySet().toArray(new String[0]);
                int lineFilter = parent.getSelectedItemPosition();

                if (lineFilter == 0)
                    for (String s : busID)
                        markers.get(s).setVisible(true);

                else
                    for (String s : busID)
                    {
                        Marker marker = markers.get(s);
                        if (lineFilter == Integer.parseInt(marker.getSnippet()))
                            marker.setVisible(true);
                        else
                            marker.setVisible(false);
                    }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void setCurPosBtn(){
        curPosBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                moveToCurrent();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setUpMap()} once when {@link #mMap} is not null.
     * <p/>
     * If it isn't installed {@link com.google.android.gms.maps.SupportMapFragment} (and
     * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
     * install/update the Google Play services APK on their device.
     * <p/>
     * A user can return to this FragmentActivity after following the prompt and correctly
     * installing/updating/enabling the Google Play services. Since the FragmentActivity may not
     * have been completely destroyed during this process (it is likely that it would only be
     * stopped or paused), {@link #onCreate(Bundle)} may not be called again so we should call this
     * method in {@link #onResume()} to guarantee that it will be called.
     */
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        SupportMapFragment mapFrag=(SupportMapFragment)getSupportFragmentManager().findFragmentById(R.id.map);
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = mapFrag.getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }
    }

    public void moveToCurrent(){
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(curPos, 15));
    }

    LocationListener listener = new LocationListener() {
        @Override
        public void onLocationChanged(Location loc) {
            curPos = new LatLng(loc.getLatitude(), loc.getLongitude());
            lat = loc.getLatitude();
            lng = loc.getLongitude();

            if( marker != null ){
                marker.setPosition(new LatLng(lat, lng));
            }
            else{
                marker = mMap.addMarker(new MarkerOptions().position(new LatLng(lat, lng)));
                moveToCurrent();
            }

        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    };

    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near Africa.
     * <p/>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private void setUpMap() {
        lm = (LocationManager)getSystemService(Context.LOCATION_SERVICE);

        boolean isNetwork =
                lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        boolean isGPS =
                lm.isProviderEnabled(LocationManager.GPS_PROVIDER);

        if(isNetwork) {
            lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER
                    , 5000, 10, listener);
            Location loc = lm.getLastKnownLocation(
                    LocationManager.NETWORK_PROVIDER);
            if(loc != null) {
                lat = loc.getLatitude();
                lng = loc.getLongitude();
            }
        }

//        if(isGPS) {
//            lm.requestLocationUpdates(LocationManager.GPS_PROVIDER
//                    , 5000, 10, listener);
//            Location loc = lm.getLastKnownLocation(
//                    LocationManager.GPS_PROVIDER);
//            if(loc != null) {
//                lat = loc.getLatitude();
//                lng = loc.getLongitude();
//            }
//        }
    }

    public void onPause(){
        super.onPause();
        lm.removeUpdates(listener);
    }
}
