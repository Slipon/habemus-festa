package com.example.habemusfesta.users;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.example.habemusfesta.MainActivity;
import com.example.habemusfesta.R;
import com.example.habemusfesta.utils.Utils;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class UserProfile extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private Dialog mDialog;

    private final String TAG = "QR_CODE:";
    private int width;

    private TextView usernameTxt;
    private TextView nameTxt;
    private TextView birthDateTxt;
    private TextView emailTxt;

    private Button showQRCodeBtn;
    private Button mergeGoogleSign;
    private Button mergeEmailSign;

    private CheckBox checkBoxEmail;
    private CheckBox checkBoxGoogle;

    public static final int NORMAL_LINK_REQUEST = 2;
    public static final int GOOGLE_LINK_REQUEST = 3;

    private int google_auth;
    private int normal_auth;

    private GoogleSignInOptions mGoogleSignInOptions;
    private GoogleSignInClient mGoogleSignInClient;
    private GoogleSignInAccount mGoogleSignInAccount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        //Google Sign-In
        mGoogleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.google_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, mGoogleSignInOptions);


        mAuth = FirebaseAuth.getInstance();
        usernameTxt = findViewById(R.id.user_nickname);
        nameTxt = findViewById(R.id.user_name);
        birthDateTxt = findViewById(R.id.user_birthdate);
        usernameTxt = findViewById(R.id.user_nickname);
        emailTxt = findViewById(R.id.user_email);
        checkBoxEmail = findViewById(R.id.checkBoxEmail);
        checkBoxGoogle = findViewById(R.id.checkBoxGoogle);

        if(!Utils.checkInternetConnection(getApplicationContext())){
            Toast.makeText(getApplicationContext(), R.string.toast_main_internet,
                    Toast.LENGTH_SHORT).show();
        }else{
            getUsernameInfo();
        }

        mergeEmailSign = findViewById(R.id.mergeEmailSign);
        mergeEmailSign.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //ask for a password
                if(!Utils.checkInternetConnection(getApplicationContext())){
                    Toast.makeText(getApplicationContext(), R.string.toast_main_internet,
                            Toast.LENGTH_SHORT).show();
                }else {
                    if (normal_auth == 0) {
                        if (!Utils.isConnected) {
                            Toast.makeText(getApplicationContext(), R.string.toast_main_internet,
                                    Toast.LENGTH_SHORT).show();
                            return;
                        } else {
                            askPasswordDialog();
                        }
                    } else {
                        Toast.makeText(UserProfile.this, R.string.user_account_already_linked,
                                Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        mergeGoogleSign = findViewById(R.id.mergeGoogleSign);
        mergeGoogleSign.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!Utils.checkInternetConnection(getApplicationContext())){
                    Toast.makeText(getApplicationContext(), R.string.toast_main_internet,
                            Toast.LENGTH_SHORT).show();
                }else {
                    unlinkAuthType();
                    if (google_auth == 0) {
                        //unlinkAuthType();
                        if (!Utils.isConnected) {
                            Toast.makeText(getApplicationContext(), R.string.toast_main_internet,
                                    Toast.LENGTH_SHORT).show();
                            return;
                        } else {
                            startActivitySignWithGoogle();//changeAuthType("GOOGLE", "");
                        }
                    } else {
                        Toast.makeText(UserProfile.this, R.string.user_account_already_linked_google,
                                Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        mDialog = new Dialog(this);

        width = getScreenWidth();


        showQRCodeBtn = findViewById(R.id.showQRCodeBtn);
        showQRCodeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utils.openQRCodePopup(mAuth, mDialog, width);
            }
        });

    }

    public int getScreenWidth(){
        //Get screen width
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        return size.x;
    }

    private void getUsernameInfo(){
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
        ref.child("Users").child(mAuth.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // if given username exists
                    User u = dataSnapshot.getValue(User.class);

                    usernameTxt.setText(u.getUsername());
                    emailTxt.setText(u.getEmail());
                    nameTxt.setText(u.getNome());
                    birthDateTxt.setText(u.getData_nascimento());

                    normal_auth = u.getNormal_auth();
                    checkBoxEmail.setChecked(normal_auth == 1);

                    google_auth = u.getGoogle_auth();
                    checkBoxGoogle.setChecked(google_auth == 1 );

                } else {
                    Toast.makeText(UserProfile.this, R.string.main_user_invalid,
                            Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(UserProfile.this, R.string.main_error_try_again,
                        Toast.LENGTH_SHORT).show();
                return;
            }
        });

    }

    private void startActivitySignWithGoogle() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, GOOGLE_LINK_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == GOOGLE_LINK_REQUEST) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try{
                GoogleSignInAccount account = task.getResult(ApiException.class);
                String idToken = account.getIdToken();
                linkAuthType("GOOGLE","",idToken);
            } catch (ApiException e) {
                // The ApiException status code indicates the detailed failure reason.
                // Please refer to the GoogleSignInStatusCodes class reference for more information.
                Log.w(TAG, "signInResult:failed code=" + e.getStatusCode());
            }
        }
    }

    private void linkAuthType(String authType, String password, String token){

        AuthCredential credential;
        String auth_provider;

        switch (authType){
            case "GOOGLE":
                //Google Sign-In
                credential = GoogleAuthProvider.getCredential(token, null);
                auth_provider = "google_auth";
                break;
            case "EMAIL":
                credential = EmailAuthProvider.getCredential(emailTxt.getText().toString(), password);
                auth_provider = "normal_auth";
                break;
            default:
                credential = null;
                auth_provider = null;
                break;
        }
        if(credential!=null) {
            mAuth.getCurrentUser().linkWithCredential(credential)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getUid());
                                ref.child(auth_provider).setValue((int)1);
                                Log.d(TAG, "linkWithCredential:success");
                                Toast.makeText(UserProfile.this, R.string.user_link_success,
                                        Toast.LENGTH_SHORT).show();
                            } else {
                                Log.w(TAG, "linkWithCredential:failure", task.getException());
                                Toast.makeText(UserProfile.this, R.string.user_link_failed,
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }

    private void unlinkAuthType(){
        AuthCredential credential;
        List<? extends UserInfo> providerData = mAuth.getCurrentUser().getProviderData();
        for (UserInfo userInfo : providerData ) {

            String providerId = userInfo.getProviderId();
            if(providerId.equals("google.com")){
                mAuth.getCurrentUser().unlink(providerId);
                break;
            }
            Log.d(TAG, "providerId = " + providerId);
        }
    }

    private void askPasswordDialog(){

        Button continuePasswordDialogBtn;
        Button cancelPasswordDialogBtn;
        EditText passwordDialogTxt;

        mDialog.setContentView(R.layout.password_popup_window);
        mDialog.getWindow().setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);

        passwordDialogTxt = mDialog.findViewById(R.id.passwordDialogTxt);

        continuePasswordDialogBtn = mDialog.findViewById(R.id.continuePasswordDialogBtn);
        continuePasswordDialogBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(passwordDialogTxt.getText().toString().length() < 6 || passwordDialogTxt.getText().toString().length() > 15){
                    Toast.makeText(UserProfile.this, R.string.password_between_values,
                            Toast.LENGTH_SHORT).show();
                }else {
                    linkAuthType("EMAIL", passwordDialogTxt.getText().toString(), "");
                    mDialog.dismiss();
                }
            }
        });

        cancelPasswordDialogBtn = mDialog.findViewById(R.id.cancelPasswordDialogBtn);
        cancelPasswordDialogBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { mDialog.dismiss(); }
        });

        mDialog.show();
        Window window = mDialog.getWindow();
        window.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
    }
}