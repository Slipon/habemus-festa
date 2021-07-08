package com.example.habemusfesta.users;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.habemusfesta.events.EventsList;
import com.example.habemusfesta.MainActivity;
import com.example.habemusfesta.utils.QRScanner;
import com.example.habemusfesta.R;
import com.example.habemusfesta.transactions.TransactionActivity;
import com.example.habemusfesta.utils.SettingsActivity;
import com.example.habemusfesta.utils.Utils;
import com.example.habemusfesta.gps.GPSTracker;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class HomeUserLogin extends AppCompatActivity {

    LinearLayout signOutBtn;
    LinearLayout showProfileBtn;
    LinearLayout showQRCodeBtn;
    LinearLayout showEventsBtn;
    LinearLayout scanBtn;
    LinearLayout settingsBtn;
    Dialog mDialog;

    FirebaseAuth mAuth;
    GoogleSignInClient mGoogleSignInClient;
    GoogleSignInOptions mGoogleSignInOptions;

    private int width;
    private final int FRIEND_REQUEST = 100;
    public static final String LOCATION_SUCCESS = "101";
    public static final String LOCATION_FAILED = "102";
    private final String TAG = "FRIEND REQUEST: ";
    private Intent gpsTrackerServiceIntent;
    private BroadcastReceiver batchProcessReceiver;
    private TextView userPoints;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_user_login);

        gpsTrackerServiceIntent = new Intent(getApplicationContext(), GPSTracker.class);
        gpsTrackerServiceIntent.putExtra("tracking_type","user_event");

        batchProcessReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(LOCATION_SUCCESS)) {

                    String event_id = intent.getStringExtra("event_id");
                    String user_id = intent.getStringExtra("user_id");
                    String friend_id = intent.getStringExtra("friend_id");
                    System.out.println(event_id+" "+user_id+" "+friend_id);

                    //Date
                    Calendar calendar = Calendar.getInstance();
                    SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
                    String date = dateFormat.format(calendar.getTime());
                    date = date.replaceAll("/","-");

                    checkIfUsersAreLinked(user_id, friend_id, event_id, date);
                }
                if(intent.getAction().equals(LOCATION_FAILED)){
                    Toast.makeText(HomeUserLogin.this, R.string.location_turn_on_warning, Toast.LENGTH_SHORT).show();
                    gpsTrackerServiceIntent.putExtra("tracking_type", "user_event");
                    startService(gpsTrackerServiceIntent);
                }
            }
        };

        mDialog = new Dialog(this);
        mAuth = FirebaseAuth.getInstance();
        width = getScreenWidth();

        showProfileBtn = findViewById(R.id.showProfileBtn);
        showProfileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                disableAllButtons();
                Intent intent = new Intent (HomeUserLogin.this, UserProfile.class);
                startActivityForResult(intent, 1);
            }
        });

        showQRCodeBtn = findViewById(R.id.showQRCode);
        showQRCodeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { Utils.openQRCodePopup(mAuth, mDialog, width); }
        });

        showEventsBtn = findViewById(R.id.showEvents);
        showEventsBtn.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View v) {
                if(checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, TransactionActivity.REQUEST_LOCATION_FINE_PERMISSION);
                }
                else{
                    disableAllButtons();
                    unregisterReceiver(batchProcessReceiver);
                    Intent intent = new Intent(HomeUserLogin.this, EventsList.class);
                    intent.putExtra("event_id","-MbrYpDD91O4L8R30lSk");
                    startActivity(intent);
                }
            }
        });

        scanBtn = findViewById(R.id.scanBtn);
        scanBtn.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View v) {
                if (checkSelfPermission(Manifest.permission.CAMERA)
                        != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{Manifest.permission.CAMERA}, TransactionActivity.REQUEST_CAMERA_PERMISSION);
                }else{
                    disableAllButtons();
                    Intent intent = new Intent(getApplicationContext(), QRScanner.class);
                    intent.putExtra("code", FRIEND_REQUEST);
                    startActivityForResult(intent,FRIEND_REQUEST);
                }
            }
        });

        signOutBtn = findViewById(R.id.signOut);
        signOutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mGoogleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestEmail()
                        .build();
                mGoogleSignInClient = GoogleSignIn.getClient(HomeUserLogin.this, mGoogleSignInOptions);
                signOut();
            }
        });

        settingsBtn = findViewById(R.id.showSettings);
        settingsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                disableAllButtons();
                Intent intent = new Intent (HomeUserLogin.this, SettingsActivity.class);
                startActivity(intent);
            }
        });

        userPoints = findViewById(R.id.user_points_available);
        if(!Utils.checkInternetConnection(getApplicationContext())){
            Toast.makeText(getApplicationContext(), R.string.toast_main_internet,
                    Toast.LENGTH_SHORT).show();
        }else {
            getUserPoints();
        }

        //Start GPS Tracking
        startService(gpsTrackerServiceIntent);
    }

    public int getScreenWidth(){
        //Get screen width
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        return size.x;
    }

    private void signOut(){
        disableAllButtons();
        GPSTracker.shouldContinue = false;
        //user logout
        mAuth.signOut();
        mGoogleSignInClient.signOut()
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        //Go back to Login Screen
                        Intent intent = new Intent(HomeUserLogin.this, MainActivity.class); // start ResultActivity
                        startActivityForResult(intent, 1);
                    }
                });


    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == TransactionActivity.REQUEST_CAMERA_PERMISSION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Intent intent = new Intent(getApplicationContext(),QRScanner.class);
                intent.putExtra("code", FRIEND_REQUEST);
                startActivityForResult(intent,FRIEND_REQUEST);
            }
            else {
                Toast.makeText(HomeUserLogin.this, R.string.camera_permission_failed, Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == FRIEND_REQUEST) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            try{
                //proceder com o link
                String uid_friend = data.getStringExtra("uid");
                checkUserLocation(mAuth.getUid(), uid_friend);

            } catch (Exception e) {
                // The ApiException status code indicates the detailed failure reason.
                // Please refer to the GoogleSignInStatusCodes class reference for more information.
                Log.w(TAG, "signInResult:failed code=" + e.getStackTrace());
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter myIntentFilter = new IntentFilter();
        myIntentFilter.addAction(LOCATION_SUCCESS);
        myIntentFilter.addAction(LOCATION_FAILED);
        registerReceiver(batchProcessReceiver, myIntentFilter);
        getUserPoints();
        enableAllButtons();
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(batchProcessReceiver);

    }

    private void checkUserLocation(String user_uid, String friend_uid){

        //Start GPS Tracking
        gpsTrackerServiceIntent.putExtra("tracking_type", "qr_scan");
        gpsTrackerServiceIntent.putExtra("user_id", user_uid);
        gpsTrackerServiceIntent.putExtra("friend_id", friend_uid);

        GPSTracker.shouldContinue = false;
        GPSTracker.scanning = true;

        startService(gpsTrackerServiceIntent);
    }

    private void getUserPoints(){
        //check if uid_friend is in DB
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
        ref.child("Users").child(mAuth.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if(snapshot.exists()) {
                    User u = snapshot.getValue(User.class);
                    userPoints.setText(Integer.toString(u.getPontos()));
                }
                ref.removeEventListener(this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                ref.removeEventListener(this);
                Toast.makeText(HomeUserLogin.this, R.string.main_error_try_again,
                        Toast.LENGTH_SHORT).show();
                return;
            }
        });
    }

    private void checkIfUsersAreLinked(String uid_user, String uid_friend, String event_id, String date){

        //check if uid_friend is in DB
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
        ref.child("Users").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if(snapshot.exists() && uid_friend != null) {
                    if (snapshot.hasChild(uid_friend)) {
                        handleLink(event_id, uid_user, uid_friend, date);
                    } else {
                        Toast.makeText(HomeUserLogin.this, R.string.qr_code_invalid,
                                Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(HomeUserLogin.this, R.string.main_error_try_again,
                        Toast.LENGTH_SHORT).show();
                return;
            }
        });
    }

    private void handleLink(String event_id, String uid_user, String uid_friend, String date){
        //check which string is comes first in alfabetical order
        String first_user;
        String second_user;

        if(uid_user.compareTo(uid_friend) < 0 || uid_user.compareTo(uid_friend) == 0){
            first_user = uid_user;
            second_user = uid_friend;
        }
        else{
            first_user = uid_friend;
            second_user = uid_user;
        }

        //check if users have already linked before
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();

        ref.child("Links").orderByChild("uid_user_1").equalTo(first_user).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Link mLink;
                if(snapshot.exists()) {
                    for (DataSnapshot child : snapshot.getChildren()) {
                        Link l = child.getValue(Link.class);
                        if (l.getUid_user_2().equals(second_user) && l.getEvent_id().equals(event_id)) {
                            Toast.makeText(HomeUserLogin.this, R.string.user_already_linked_event,
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }
                }else {
                    mLink = new Link(first_user, second_user, event_id, date);
                    ref.child("Links").push().setValue(mLink);
                    Toast.makeText(HomeUserLogin.this, R.string.user_event_linked_success,
                            Toast.LENGTH_SHORT).show();
                    //TODO: ADD POINTS TO USERS
                    handleRewards(first_user, second_user);
                }
                return;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(HomeUserLogin.this, R.string.main_error_try_again,
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void handleRewards(String first_user, String second_user){
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
        ref.child("Users").child(first_user).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    User u = snapshot.getValue(User.class);
                    int pontos = u.getPontos();
                    pontos += 10;
                    ref.child("Users").child(first_user).child("pontos").setValue(pontos);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        ref.child("Users").child(second_user).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    User u = snapshot.getValue(User.class);
                    int pontos = u.getPontos();
                    pontos += 10;
                    ref.child("Users").child(second_user).child("pontos").setValue(pontos);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void disableAllButtons(){
        signOutBtn.setClickable(false);
        showProfileBtn.setClickable(false);
        showQRCodeBtn.setClickable(false);
        showEventsBtn.setClickable(false);
        scanBtn.setClickable(false);
        settingsBtn.setClickable(false);
    }

    private void enableAllButtons(){
        signOutBtn.setClickable(true);
        showProfileBtn.setClickable(true);
        showQRCodeBtn.setClickable(true);
        showEventsBtn.setClickable(true);
        scanBtn.setClickable(true);
        settingsBtn.setClickable(true);
    }

}