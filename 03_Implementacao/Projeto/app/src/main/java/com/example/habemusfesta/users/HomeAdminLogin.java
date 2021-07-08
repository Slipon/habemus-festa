package com.example.habemusfesta.users;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.example.habemusfesta.MainActivity;
import com.example.habemusfesta.R;
import com.example.habemusfesta.events.AddCollaboratorsActivity;
import com.example.habemusfesta.products.ProductActivity;
import com.example.habemusfesta.events.EventActivity;
import com.example.habemusfesta.events.RemoveEventsActivity;
import com.example.habemusfesta.products.RemoveProductsActivity;
import com.example.habemusfesta.transactions.TransactionActivity;
import com.example.habemusfesta.transactions.TransactionHistoryActivity;
import com.example.habemusfesta.utils.SettingsActivity;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;


public class HomeAdminLogin extends AppCompatActivity {

    private LinearLayout chargePointsBtn;
    private LinearLayout createEventBtn;
    private LinearLayout createProductBtn;
    private LinearLayout showTransactionsBtn;
    private LinearLayout settingsBtn;
    private LinearLayout signOut;
    private LinearLayout showRemoveEvents;
    private LinearLayout showRemoveProducts;
    private LinearLayout showAddColaborator;

    FirebaseAuth mAuth;
    GoogleSignInClient mGoogleSignInClient;
    GoogleSignInOptions mGoogleSignInOptions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_admin_login);

        mAuth = FirebaseAuth.getInstance();

        chargePointsBtn = findViewById(R.id.chargePoints);
        chargePointsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                disableAllButtons();
                Intent intent = new Intent(HomeAdminLogin.this, TransactionActivity.class);
                startActivity(intent);
            }
        });

        createEventBtn = findViewById(R.id.createEvent);
        createEventBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                disableAllButtons();
                Intent intent = new Intent(HomeAdminLogin.this, EventActivity.class);
                startActivity(intent);
            }
        });

        createProductBtn = findViewById(R.id.createProduct);
        createProductBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                disableAllButtons();
                Intent intent = new Intent(HomeAdminLogin.this, ProductActivity.class);
                startActivity(intent);
            }
        });

        showTransactionsBtn = findViewById(R.id.showTransactions);
        showTransactionsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                disableAllButtons();
                Intent intent = new Intent(HomeAdminLogin.this, TransactionHistoryActivity.class);
                startActivity(intent);
            }
        });

        signOut = findViewById(R.id.signOutAdmin);
        signOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                disableAllButtons();
                mGoogleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestEmail()
                        .build();
                mGoogleSignInClient = GoogleSignIn.getClient(HomeAdminLogin.this, mGoogleSignInOptions);
                signOut();
            }
        });

        settingsBtn = findViewById(R.id.showSettings);
        settingsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                disableAllButtons();
                Intent intent = new Intent (HomeAdminLogin.this, SettingsActivity.class);
                startActivity(intent);
            }
        });

        showRemoveEvents = findViewById(R.id.showRemoveEvents);
        showRemoveEvents.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                disableAllButtons();
                Intent intent = new Intent (HomeAdminLogin.this, RemoveEventsActivity.class);
                startActivity(intent);
            }
        });

        showRemoveProducts = findViewById(R.id.showRemoveProducts);
        showRemoveProducts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                disableAllButtons();
                Intent intent = new Intent (HomeAdminLogin.this, RemoveProductsActivity.class);
                startActivity(intent);
            }
        });

        showAddColaborator = findViewById(R.id.addColaborator);
        showAddColaborator.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                disableAllButtons();
                Intent intent = new Intent (HomeAdminLogin.this, AddCollaboratorsActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        enableAllButtons();
    }

    private void signOut(){
        mAuth.signOut();
        mGoogleSignInClient.signOut()
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        //Go back to Login Screen
                        Intent intent = new Intent(HomeAdminLogin.this, MainActivity.class); // start ResultActivity
                        startActivityForResult(intent, 1);
                    }
                });
    }

    private void disableAllButtons(){
        chargePointsBtn.setClickable(false);
        createEventBtn.setClickable(false);
        createProductBtn.setClickable(false);
        showTransactionsBtn.setClickable(false);
        signOut.setClickable(false);
        settingsBtn.setClickable(false);
        showRemoveEvents.setClickable(false);
        showRemoveProducts.setClickable(false);
        showAddColaborator.setClickable(false);
    }

    private void enableAllButtons(){
        chargePointsBtn.setClickable(true);
        createEventBtn.setClickable(true);
        createProductBtn.setClickable(true);
        showTransactionsBtn.setClickable(true);
        signOut.setClickable(true);
        settingsBtn.setClickable(true);
        showRemoveEvents.setClickable(true);
        showRemoveProducts.setClickable(true);
        showAddColaborator.setClickable(true);
    }

}