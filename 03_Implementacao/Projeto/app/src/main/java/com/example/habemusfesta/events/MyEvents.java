package com.example.habemusfesta.events;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.habemusfesta.R;
import com.example.habemusfesta.gps.SearchMap;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.LocationCallback;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

/**
 * A fragment representing a list of Items.
 */
public class MyEvents extends Fragment {

    private GoogleMap map;
    private HashMap<String[], GeoLocation> events = new HashMap<>();
    private GeoFire geoFire;
    private Long count = 0L;
    private Long events_size = 0L;
    private FirebaseAuth mAuth;
    private SearchMap searchMap;
    private String currentTag;

    public MyEvents(String tag){ this.currentTag = tag; }


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
            getMyEvents();
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_my_events, container, false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SupportMapFragment mapFragment =
                (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map_4);
        if (mapFragment != null) {
            mapFragment.getMapAsync(callback);
        }
        mAuth = FirebaseAuth.getInstance();
        getMyEvents();
    }


    public void changeFilter(String tag){
        if(searchMap != null) {
            this.currentTag = tag;
            searchMap.hideMarkers(tag);
        }
    }

    private void getMyEvents() { //its going to select all the confirmed user events
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
        ref.child("Users-Likes").child(mAuth.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    events_size = snapshot.getChildrenCount();
                    for (DataSnapshot child : snapshot.getChildren()) {
                        if((Long)child.getValue() == 1L) {
                            ref.child("Eventos").child(child.getKey()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if (snapshot.exists()) {
                                        Event e = snapshot.getValue(Event.class);
                                        geoFire = new GeoFire(ref.child("Eventos-Locs"));
                                        LocationCallback locationCallback = new LocationCallback() {
                                            @Override
                                            public void onLocationResult(String key, GeoLocation location) {
                                                events.put(new String[]{key, e.getNome(), e.getEvent_type()}, location);
                                                count++;
                                                System.out.println("COUNT: "+count+" "+events_size);
                                                if (count == events_size) {
                                                    count = 0L;
                                                    searchMap = new SearchMap(true, currentTag, events);
                                                    FragmentManager fragmentManager = getParentFragmentManager();
                                                    fragmentManager.beginTransaction().replace(R.id.map_4, searchMap).commit();
                                                }
                                            }

                                            @Override
                                            public void onCancelled(DatabaseError databaseError) {
                                                Toast.makeText(getActivity().getApplicationContext(), "Error: location not found.", Toast.LENGTH_SHORT);
                                            }
                                        };
                                        geoFire.getLocation(e.getEvent_id(), locationCallback);
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                }
                            });
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }
}