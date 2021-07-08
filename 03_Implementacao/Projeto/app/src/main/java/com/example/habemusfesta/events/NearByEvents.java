package com.example.habemusfesta.events;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.habemusfesta.R;
import com.example.habemusfesta.events.Event;
import com.example.habemusfesta.gps.GPSTracker;
import com.example.habemusfesta.gps.SearchMap;
import com.example.habemusfesta.users.HomeUserLogin;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class NearByEvents extends Fragment {

    private GoogleMap map;

    private GeoFire geoFire;
    private GeoQuery geoQuery;
    private HashMap<String[], GeoLocation> events = new HashMap<>();
    private Intent gpsTrackerServiceIntent;
    BroadcastReceiver batchProcessReceiver;
    private SearchMap searchMap;
    private String currentTag;

    public NearByEvents(String tag){ this.currentTag = tag; }

    private OnMapReadyCallback callback = new OnMapReadyCallback() {

        /**
         * Manipulates the map once available.
         * This callback is triggered when the map is ready to be used.
         * This is where we can add markers or lines, add listeners or move the camera.
         * In this case, we just add a marker near Sydney, Australia.
         * If Google Play services is not installed on the device, the user will be prompted to
         * install it inside the SupportMapFragment. This method will only be triggered once the
         * user has installed Google Play services and returned to the app.
         */
        @Override
        public void onMapReady(GoogleMap googleMap) {
            map = googleMap;
            //Move the camera to position
            LatLng lisbon = new LatLng(38.736946, -9.142685);
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(lisbon, 10));
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_near_by_events, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SupportMapFragment mapFragment =
                (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(callback);
        }


        gpsTrackerServiceIntent = new Intent(getActivity().getApplicationContext(), GPSTracker.class);
        gpsTrackerServiceIntent.putExtra("tracking_type","user_event");

        batchProcessReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(HomeUserLogin.LOCATION_SUCCESS)) {

                    Double lat = intent.getDoubleExtra("user_location_lat",0);
                    Double lon = intent.getDoubleExtra("user_location_lon",0);

                    getNearestLocations(lat, lon);
                }
                if(intent.getAction().equals(HomeUserLogin.LOCATION_FAILED)){
                    Toast.makeText(getContext(), R.string.location_turn_on_warning, Toast.LENGTH_SHORT).show();
                    gpsTrackerServiceIntent.putExtra("tracking_type", "user_event");
                    getActivity().startService(gpsTrackerServiceIntent);
                }
            }
        };

        callGPSTrackingService();


    }

    @Override
    public void onResume() {
        super.onResume();

        IntentFilter myIntentFilter = new IntentFilter();
        myIntentFilter.addAction(HomeUserLogin.LOCATION_SUCCESS);
        myIntentFilter.addAction(HomeUserLogin.LOCATION_FAILED);
        getActivity().registerReceiver(batchProcessReceiver, myIntentFilter);

    }

    @Override
    public void onStop() {
        super.onStop();
        getActivity().unregisterReceiver(batchProcessReceiver);
    }

    public void changeFilter(String tag){
        if(searchMap != null) {
            this.currentTag = tag;
            searchMap.hideMarkers(tag);
        }
    }

    private void callGPSTrackingService(){
        //Start GPS Tracking
        gpsTrackerServiceIntent.putExtra("tracking_type", "events_locs");

        GPSTracker.shouldContinue = false;
        GPSTracker.scanning = true;

        getActivity().startService(gpsTrackerServiceIntent);
    }

    private void getNearestLocations(Double lat, Double lon){
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Eventos-Locs");
        geoFire = new GeoFire(ref);
        geoQuery = geoFire.queryAtLocation(new GeoLocation(lat, lon), 3.0f); //3000m
        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
                ref.child("Eventos").child(key).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.exists()){
                            Event e = snapshot.getValue(Event.class);
                            events.put(new String[]{key, e.getNome(), e.getEvent_type()}, location);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
                System.out.println(String.format("Key %s entered the search area at [%f,%f]", key, location.latitude, location.longitude));
            }

            @Override
            public void onKeyExited(String key) {
                System.out.println(String.format("Key %s is no longer in the search area", key));
            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {
                System.out.println(String.format("Key %s moved within the search area to [%f,%f]", key, location.latitude, location.longitude));
            }

            @Override
            public void onGeoQueryReady() {
                searchMap = new SearchMap(true, currentTag, events);
                FragmentManager fragmentManager = getParentFragmentManager();
                fragmentManager.beginTransaction().replace(R.id.map, searchMap).commit();

                System.out.println("All initial data has been loaded and events have been fired!");
            }

            @Override
            public void onGeoQueryError(DatabaseError error) {
                System.err.println("There was an error with this query: " + error);
            }
        });
    }


}