package com.example.habemusfesta.gps;

import android.Manifest;
import android.app.Dialog;
import android.app.IntentService;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

import com.example.habemusfesta.R;
import com.example.habemusfesta.events.Event;
import com.example.habemusfesta.events.EventUser;
import com.example.habemusfesta.users.HomeUserLogin;
import com.example.habemusfesta.users.User;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class GPSTracker extends IntentService {

    private static final int PERMISSIONS_FINE_LOCATION = 99;

    //Location request is a config file for all settings related to FusedLocationProviderClient
    //Google's API for location services
    private String user_id;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallBack;
    private Location currentLocation;
    private String[] addresses;
    private static String address;
    private LocationManager locationManager;
    private GeoFire geoFire;
    private GeoQuery geoQuery;
    private Dialog mDialog;
    private int i = 1;
    private DatabaseReference refEventosUsers;
    private ValueEventListener valueEventListener;

    public static volatile boolean shouldContinue = true;
    public static volatile boolean scanning = false;


    public GPSTracker() {
        super("GPSTracker");
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {

        shouldContinue = true;
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        while(shouldContinue){
            if(intent.getStringExtra("user_id") != null){
                user_id = intent.getStringExtra("user_id");
            }
            try {
                updateGPS(intent);
                Thread.sleep(1000);
            }catch(Exception e){e.getMessage();}
        }
    }

    public static String getCurrentLocation(){
        return address;
    }

    private void updateGPS(Intent intent){
        // get permissions from the user to track GPS
        // get the current location from the fused client
        // update de UI - i.e. set all properties in their associated text view items

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(GPSTracker.this);
        if(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            //user provided the permission
            fusedLocationProviderClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    currentLocation = location;
                    if (intent.getStringExtra("tracking_type").equals("events_locs")) {
                        Intent myIntent = new Intent(HomeUserLogin.LOCATION_SUCCESS);
                        myIntent.putExtra("user_location_lat", currentLocation.getLatitude());
                        myIntent.putExtra("user_location_lon", currentLocation.getLongitude());

                        sendBroadcast(myIntent);
                        shouldContinue = false;
                        return;
                    }
                    getAddress(intent);
                }
            });
        }
        else{
            Log.d("GPS","Unable to get street address: ");
            if(scanning) {
                Log.d("GPS","GPS not active");
                shouldContinue = false;
                scanning = false;
                sendBroadcast(new Intent(HomeUserLogin.LOCATION_FAILED));
                return;
            }

            /*
            //user did not provided the permission
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_FINE_LOCATION);
            }

            */
        }
    }

    private void getAddress(Intent intent){
        Geocoder geocoder = new Geocoder(GPSTracker.this);
        try {
            List<Address> addresses = geocoder.getFromLocation(currentLocation.getLatitude(), currentLocation.getLongitude(), 1);
            address = addresses.get(0).getAddressLine(0);
            Log.d("GPS",addresses.get(0).getAddressLine(0));
            getEventAddresses(intent);
        } catch (Exception e) {
            Log.d("GPS","Unable to get addresses: "+e.getMessage());
        }
    }

    private void getEventAddresses(Intent intent){

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Eventos-Locs");
        if(geoFire == null) {
            geoFire = new GeoFire(ref);
            //para efeitos de teste
            geoFire.setLocation("-MbuSthrSWRVUEeQALxb", new GeoLocation(currentLocation.getLatitude(), currentLocation.getLongitude()));
        }

        geoQuery = geoFire.queryAtLocation(new GeoLocation(currentLocation.getLatitude(), currentLocation.getLongitude()), 0.6f); //600m
        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {

                if (intent.getStringExtra("tracking_type").equals("events_locs")) {
                    Intent myIntent = new Intent(HomeUserLogin.LOCATION_SUCCESS);
                    myIntent.putExtra("user_location_lat", currentLocation.getLatitude());
                    myIntent.putExtra("user_location_lon", currentLocation.getLongitude());

                    sendBroadcast(myIntent);
                    return;
                }

                FirebaseAuth mAuth = FirebaseAuth.getInstance();
                refEventosUsers = FirebaseDatabase.getInstance().getReference().child("Eventos-Users").child(key).child(mAuth.getUid());
                valueEventListener = new ValueEventListener(){
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(shouldContinue) {
                            if (snapshot.exists()) {
                                if (intent.getStringExtra("tracking_type").equals("qr_scan")) {

                                    Intent myIntent = new Intent(HomeUserLogin.LOCATION_SUCCESS);
                                    myIntent.putExtra("user_id", intent.getStringExtra("user_id"));
                                    myIntent.putExtra("friend_id", intent.getStringExtra("friend_id"));
                                    myIntent.putExtra("event_id", key);
                                    sendBroadcast(myIntent);

                                }
                                shouldContinue = false;
                                Log.d("GPS", "User already entered the event");
                            } else {
                                FirebaseDatabase.getInstance().getReference("Eventos").child(key).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        if (snapshot.exists()) {
                                            Event e = snapshot.getValue(Event.class);
                                            Log.d("GPS", e.getNome());
                                            showEventDialog(e);
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {}
                                });
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                };
                refEventosUsers.addListenerForSingleValueEvent(valueEventListener);
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
                System.out.println("All initial data has been loaded and events have been fired!");
            }

            @Override
            public void onGeoQueryError(DatabaseError error) {
                System.err.println("There was an error with this query: " + error);
            }
        });
    }

    private void showEventDialog(Event e){
        FirebaseApp.initializeApp(this);
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
        while(i==1) {
            handleEventConfirm(mAuth.getUid(), e.getEvent_id());
            Toast.makeText(getApplicationContext(), R.string.gps_entered_event, Toast.LENGTH_LONG).show();
            i++;
        }
    }

    private void handleEventConfirm(String user_id, String event_id){
        EventUser eu = new EventUser(user_id, event_id);
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
        ref.child("Users").child(eu.getUser_id()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    User u = snapshot.getValue(User.class);
                    int pontos = u.getPontos() + 10;
                    ref.child("Users").child(user_id).child("pontos").setValue(pontos);
                    ref.child("Eventos-Users").child(event_id).child(user_id).setValue(1);
                }
                ref.removeEventListener(this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                ref.removeEventListener(this);
            }
        });
    }
}