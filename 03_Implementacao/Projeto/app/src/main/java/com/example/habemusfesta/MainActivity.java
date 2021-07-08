package com.example.habemusfesta;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.habemusfesta.users.HomeAdminLogin;
import com.example.habemusfesta.users.HomeUserLogin;
import com.example.habemusfesta.users.User;
import com.example.habemusfesta.gps.GPSTracker;
import com.example.habemusfesta.utils.SettingsActivity;
import com.example.habemusfesta.utils.Utils;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    public static final int REGISTER_REQUEST = 1;
    public static final int NORMAL_LOGIN_REQUEST = 2;
    public static final int GOOGLE_LOGIN_REQUEST = 3;
    private FirebaseAuth mAuth;

    private String TAG = "LOGIN: ";
    private TextView inputUsernameEmail;
    private TextView inputPassword;
    private Button loginBtn;
    private SignInButton googleSignBtn;
    private TextView txtRegister;
    private ImageButton imgBtn_settings;

    private GoogleSignInOptions mGoogleSignInOptions;
    private GoogleSignInClient mGoogleSignInClient;
    private GoogleSignInAccount mGoogleSignInAccount;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        imgBtn_settings = findViewById(R.id.imgBtn_settings);
        imgBtn_settings.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                imgBtn_settings.setClickable(false);
                Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(intent);
            }
        });

        //Google Sign-In
        mGoogleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.google_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, mGoogleSignInOptions);

        //Normal Sign-In
        mAuth = FirebaseAuth.getInstance();
        inputUsernameEmail = findViewById(R.id.inputUsername);
        inputPassword = findViewById(R.id.inputPassword);

        loginBtn = findViewById(R.id.loginBtn);
        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginBtn.setClickable(false);
                if(!Utils.checkInternetConnection(getApplicationContext())){
                    loginBtn.setClickable(true);
                    Toast.makeText(MainActivity.this, R.string.toast_main_internet,
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                if( inputUsernameEmail.getText().toString().length() == 0 || inputPassword.getText().toString().length() == 0 ){
                    loginBtn.setClickable(true);
                    Toast.makeText(MainActivity.this, R.string.main_user_pass_missing, Toast.LENGTH_SHORT).show();
                }else {
                    checkAuthenticationEntry(inputUsernameEmail.getText().toString(), inputPassword.getText().toString());
                }
            }
        });

        googleSignBtn = findViewById(R.id.googleSign);
        googleSignBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!Utils.checkInternetConnection(getApplicationContext())){
                    Toast.makeText(MainActivity.this, R.string.toast_main_internet,
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                startActivitySignWithGoogle();
            }
        });

        txtRegister = findViewById(R.id.txtRegister);
        txtRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                txtRegister.setClickable(false);
                Intent intent = new Intent(MainActivity.this, RegisterActivity.class); // start ResultActivity
                startActivity(intent);
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loginBtn.setClickable(true);
        txtRegister.setClickable(true);
        imgBtn_settings.setClickable(true);
    }

    //Verify network and location
    @RequiresApi(api = Build.VERSION_CODES.M)
    private boolean locationEnabled(){
        LocationManager lm = (LocationManager)getSystemService(Context.LOCATION_SERVICE ) ;
        ConnectivityManager connectivityManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        boolean gps_enabled = false;
        boolean network_enabled = false;
        try {
            gps_enabled = lm.isProviderEnabled(LocationManager. GPS_PROVIDER ) ;
        } catch (Exception e) {
            e.printStackTrace() ;
        }
        if (!(connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED)) {
            new AlertDialog.Builder(MainActivity. this )
                    .setMessage( "Serviço de internet inativo" )
                    .setPositiveButton( "Settings" , new
                            DialogInterface.OnClickListener() {
                                @Override
                                public void onClick (DialogInterface paramDialogInterface , int paramInt) {
                                    startActivity( new Intent(Settings. ACTION_NETWORK_OPERATOR_SETTINGS )) ;
                                }
                            })
                    .setNegativeButton( "Cancel" , null )
                    .show() ;
            return false;
        }

        if (!gps_enabled) {
            new AlertDialog.Builder(MainActivity. this )
                    .setMessage( "Serviço de localização inativo" )
                    .setPositiveButton( "Settings" , new
                            DialogInterface.OnClickListener() {
                                @Override
                                public void onClick (DialogInterface paramDialogInterface , int paramInt) {
                                    startActivity( new Intent(Settings. ACTION_LOCATION_SOURCE_SETTINGS )) ;
                                }
                            })
                    .setNegativeButton( "Cancel" , null )
                    .show() ;
            return false;
        }
        return true;
    }

    private void checkAuthenticationEntry(String usernameOrEmail, String password){ //checks if authentication entry is an username or an email, then proceeds to call signInUser method
        if(android.util.Patterns.EMAIL_ADDRESS.matcher(usernameOrEmail).matches()){ //if given entry is an emai
            checkIfAccountIsLinkedWithEmail(usernameOrEmail.toLowerCase(), password);
        }else {// if its a user, check the Firebase RealTimeDatabase to see if its a valid one
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
            ref.child("Users").orderByChild("username").equalTo(usernameOrEmail.toLowerCase()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        // if given username exists
                        HashMap dS = (HashMap)(dataSnapshot.getValue());
                        String uid = dS.keySet().toArray()[0].toString();
                        User u = dataSnapshot.child(uid).getValue(User.class);
                        if(u.getEmail()!=null && !u.getEmail().equals("")) {
                            signInUser(u.getEmail(), password);
                        }else{
                            Toast.makeText(MainActivity.this, R.string.main_email_attached_error,
                                    Toast.LENGTH_SHORT).show();
                            loginBtn.setClickable(true);
                            return;
                        }
                    } else {
                        Toast.makeText(MainActivity.this, R.string.main_user_invalid,
                                Toast.LENGTH_SHORT).show();
                        loginBtn.setClickable(true);
                        return;
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(MainActivity.this, R.string.main_error_try_again,
                            Toast.LENGTH_SHORT).show();
                    return;
                }
            });
        }
    }

    private void signInUser(String email, String password){
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success
                            Log.d(TAG, "signInWithEmail:success");

                            //Check if its a normal user or an employer
                            DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
                            ref.child("Users").orderByChild("email").equalTo(email).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if(dataSnapshot.exists()) {
                                        HashMap dS = (HashMap)(dataSnapshot.getValue());
                                        String uid = dS.keySet().toArray()[0].toString();
                                        System.out.println(dataSnapshot.child(uid).getValue().getClass().getName());
                                        User u = dataSnapshot.child(uid).getValue(User.class);

                                        String user_type = u.getUser_type();

                                        Intent intent;
                                        if (user_type.equals("Normal")) { //If its a normal user
                                            intent = new Intent(MainActivity.this, HomeUserLogin.class); // start ResultActivity
                                        } else if (user_type.equals("Admin")) { //if its an admin
                                            intent = new Intent(MainActivity.this, HomeAdminLogin.class); // start ResultActivity
                                        } else {
                                            Toast.makeText(MainActivity.this, R.string.main_user_not_found, Toast.LENGTH_SHORT).show();
                                            loginBtn.setClickable(true);
                                            return;
                                        }

                                        intent.putExtra("user_id", uid);
                                        intent.putExtra("username", u.getUsername());
                                        startActivityForResult(intent, NORMAL_LOGIN_REQUEST);
                                    }else {
                                        Toast.makeText(MainActivity.this, R.string.main_user_not_found, Toast.LENGTH_SHORT).show();
                                        loginBtn.setClickable(true);
                                        return;
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                    Toast.makeText(MainActivity.this, R.string.main_authentication_failed, Toast.LENGTH_SHORT).show();
                                    loginBtn.setClickable(true);
                                }
                            });
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            Toast.makeText(MainActivity.this, R.string.main_authentication_failed, Toast.LENGTH_SHORT).show();
                            loginBtn.setClickable(true);
                        }
                    }
                });
    }

    private void startActivitySignWithGoogle() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, GOOGLE_LOGIN_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        //mCallbackManager.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == GOOGLE_LOGIN_REQUEST) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try{
                GoogleSignInAccount account = task.getResult(ApiException.class);
                checkIfAccountIsLinkedWithGoogle(account);
            } catch (ApiException e) {
                // The ApiException status code indicates the detailed failure reason.
                // Please refer to the GoogleSignInStatusCodes class reference for more information.
                Log.w(TAG, "signInResult:failed code=" + e.getStatusCode());
            }
        }
    }

    private void signInWithGoogle(String idToken) {
        //Get idToken from GoogleSignInAccount and create a credential
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);

        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");

                            mGoogleSignInAccount = GoogleSignIn.getLastSignedInAccount(MainActivity.this);
                            // Signed in successfully, show authenticated UI.
                            String email = mGoogleSignInAccount.getEmail();

                            //Check if its a normal user or an employer
                            DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
                            ref.child("Users").orderByChild("email").equalTo(email).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if(dataSnapshot.exists()) {
                                        HashMap dS = (HashMap)(dataSnapshot.getValue());
                                        String uid = dS.keySet().toArray()[0].toString();
                                        User u = dataSnapshot.child(uid).getValue(User.class);
                                        String user_type = u.getUser_type();

                                        Intent intent;
                                        if (user_type.equals("Normal")) { //If its a normal user
                                            intent = new Intent(MainActivity.this, HomeUserLogin.class); // start ResultActivity
                                        } else if (user_type.equals("Admin")) { //if its an admin
                                            intent = new Intent(MainActivity.this, HomeAdminLogin.class); // start ResultActivity
                                        } else {
                                            Toast.makeText(MainActivity.this, R.string.main_user_not_found, Toast.LENGTH_SHORT).show();
                                            return;
                                        }

                                        intent.putExtra("user_id", uid);
                                        intent.putExtra("username", u.getUsername());

                                        startActivityForResult(intent, GOOGLE_LOGIN_REQUEST);
                                    }else {
                                        //Create User
                                        String name = mGoogleSignInAccount.getDisplayName();
                                        RegisterActivity.createNewAccountWithGoogle(name, email, mAuth);

                                        Intent intent = new Intent(MainActivity.this, HomeUserLogin.class);
                                        intent.putExtra("user_id", mAuth.getUid());

                                        startActivityForResult(intent, GOOGLE_LOGIN_REQUEST);
                                    }
                                }
                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                    Toast.makeText(MainActivity.this, R.string.main_authentication_failed, Toast.LENGTH_SHORT).show();
                                }
                            });
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                        }
                    }
                });
    }

    private void checkIfAccountIsLinkedWithGoogle(GoogleSignInAccount account){
        String email = account.getEmail();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
        ref.child("Users").orderByChild("email").equalTo(email).addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    HashMap dS = (HashMap)(snapshot.getValue());
                    String uid = dS.keySet().toArray()[0].toString();
                    User u = snapshot.child(uid).getValue(User.class);
                    if(u.getGoogle_auth() == 1){
                        String idToken = account.getIdToken();
                        signInWithGoogle(idToken);
                    }else {
                        Toast.makeText(MainActivity.this, R.string.main_account_not_linked,
                                Toast.LENGTH_SHORT).show();
                        return;
                    }
                }else{
                    String idToken = account.getIdToken();
                    signInWithGoogle(idToken);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MainActivity.this, R.string.event_page_event_not_found,
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void checkIfAccountIsLinkedWithEmail(String email, String password){

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
        ref.child("Users").orderByChild("email").equalTo(email).addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    HashMap dS = (HashMap)(snapshot.getValue());
                    String uid = dS.keySet().toArray()[0].toString();
                    User u = snapshot.child(uid).getValue(User.class);
                    if(u.getNormal_auth() == 1){
                        signInUser(email, password);
                    }else {
                        Toast.makeText(MainActivity.this, R.string.main_account_not_linked,
                                Toast.LENGTH_SHORT).show();
                        loginBtn.setClickable(true);
                        return;
                    }
                }else{
                    Toast.makeText(MainActivity.this, R.string.main_acc_not_registered,
                            Toast.LENGTH_SHORT).show();
                    loginBtn.setClickable(true);
                    return;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MainActivity.this, R.string.main_action_canceled,
                        Toast.LENGTH_SHORT).show();
                loginBtn.setClickable(true);
            }
        });
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                System.out.println(GPSTracker.getCurrentLocation());
                return true;
            }
        }
        return false;
    }

}

