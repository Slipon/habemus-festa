package com.example.habemusfesta.gps;


import androidx.fragment.app.Fragment;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.appcompat.widget.SearchView;

import com.example.habemusfesta.R;
import com.example.habemusfesta.events.EventPage;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.RectangularBounds;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class SearchMap extends Fragment implements OnMapReadyCallback {

    private GoogleMap map;
    private SupportMapFragment mapFragment;
    private PlacesClient placesClient;
    private AutocompleteSupportFragment autocompleteSupportFragment;
    private static final String TAG = "Info: ";
    private SearchView searchView;
    private View view;
    public static Address address;
    private boolean no_bar = false;
    private boolean isInfoWindowShown = false;
    private Marker currentMarker = null;

    private ArrayList<Marker> mapMarkers;
    private HashMap<String[], GeoLocation> events;

    private Double lat = 0.0;
    private Double lon = 0.0;
    private String currentTag;

    public SearchMap(){}

    public SearchMap(boolean no_bar, String currentTag, HashMap<String[], GeoLocation> events){
        this.no_bar = no_bar;
        this.currentTag = currentTag;
        this.events = events;
    }

    public SearchMap(boolean no_bar, Double lat, Double lon){
        this.no_bar = no_bar;
        this.lat = lat;
        this.lon = lon;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.activity_search_map, container, false);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if(map == null){
            mapFragment = (SupportMapFragment)getChildFragmentManager().findFragmentById(R.id.google_map);
            mapFragment.getMapAsync(this);
        }

        mapMarkers = new ArrayList<>();
        searchView = view.findViewById(R.id.event_localization_searchbar);

        //Initialize the SDK
        if(!Places.isInitialized()){
            Places.initialize(getActivity().getApplicationContext(), getString(R.string.google_places_key));
        }
        //Create a new Places client Instance
        placesClient = Places.createClient(getActivity().getApplicationContext());
        //Initialize the AutocompleteSupportFragment
        autocompleteSupportFragment = (AutocompleteSupportFragment)getChildFragmentManager().findFragmentById(R.id.autocomplete_fragment);
        autocompleteSupportFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG));
        //autocompleteSupportFragment.setTypeFilter(TypeFilter.ADDRESS);
        autocompleteSupportFragment.setLocationBias(RectangularBounds.newInstance(
                //Coords to help faster results
                new LatLng(38.722252, -9.139337), //Lisboa
                new LatLng(41.157944, -8.629105) //Porto
        ));
        autocompleteSupportFragment.setCountries("PT");
        //Types of place data to return
        //autocompleteSupportFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME));

        autocompleteSupportFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                Log.i(TAG, "Place:" + place.getName() + ", " + place.getId());
            }

            @Override
            public void onError(Status status) {
                Log.i("TAG", "An error occurred: "+status);
            }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                String location = searchView.getQuery().toString();
                List<Address> addressList = null;

                if(location!=null || !location.equals("")){
                    Geocoder geocoder = new Geocoder(getContext());
                    try {
                        addressList = geocoder.getFromLocationName(location, 1);
                        if(addressList == null || addressList.size()==0){
                            Toast.makeText(getActivity().getApplicationContext(), R.string.gps_no_location_found,
                                    Toast.LENGTH_SHORT).show();
                            return false;
                        }
                        address = addressList.get(0);
                        LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());
                        map.addMarker(new MarkerOptions().position(latLng).title(location));
                        map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng,10));
                    }catch (IOException e){
                        e.printStackTrace();
                    }
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        mapFragment.getMapAsync(this);

    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;

        if(no_bar){
            searchView.setVisibility(View.INVISIBLE);
            map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                @Override
                public boolean onMarkerClick(Marker marker) {
                    if(marker.isVisible()) {
                        if (isInfoWindowShown && ((String[]) currentMarker.getTag())[0].equals(((String[]) marker.getTag())[0])) {
                            if (marker.getTag() != null) {
                                Intent intent = new Intent(getActivity(), EventPage.class);
                                String[] tags = (String[]) marker.getTag();
                                intent.putExtra("event_id", tags[0]);
                                isInfoWindowShown = false;
                                startActivity(intent);
                                return true;
                            }
                        } else {
                            marker.showInfoWindow();
                            currentMarker = marker;
                            isInfoWindowShown = true;
                        }
                    }
                    return true;
                }
            });
            if(events!=null){
                for (HashMap.Entry<String[], GeoLocation> entry : events.entrySet()) {
                    addLocation(entry.getKey()[0], entry.getKey()[1], entry.getKey()[2], entry.getValue().latitude, entry.getValue().longitude);
                }
                if(currentTag != null && !currentTag.equals("")){
                    hideMarkers(currentTag);
                }
            }else {
                addLocation("", "", "", lat, lon);
            }
        }else{
            //Move the camera to position
            LatLng lisbon = new LatLng(38.736946, -9.142685);
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(lisbon, 10));
        }
    }


    public void hideMarkers(String tag){
        for(Marker m: mapMarkers){
            String marker_tag = ((String[])m.getTag())[1];
            if(!marker_tag.equals(tag) && !tag.equals("0")){
                m.setVisible(false);
            }else{
                m.setVisible(true);
            }
        }
    }



    public void addLocation(String event_id, String event_title, String event_type, Double lat, Double lon){
        LatLng latLng = new LatLng(lat, lon);
        Marker marker = map.addMarker(new MarkerOptions().position(latLng));
        marker.setTag(new String[]{event_id, event_type});
        marker.setTitle(event_title);
        mapMarkers.add(marker);
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng,10));
    }
}