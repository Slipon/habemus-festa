package com.example.habemusfesta.transactions;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.habemusfesta.MainActivity;
import com.example.habemusfesta.events.AddCollaboratorsActivity;
import com.example.habemusfesta.events.EventsCollabs;
import com.example.habemusfesta.users.HomeAdminLogin;
import com.example.habemusfesta.utils.QRScanner;
import com.example.habemusfesta.R;
import com.example.habemusfesta.utils.Utils;
import com.example.habemusfesta.products.Product;
import com.example.habemusfesta.users.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class TransactionActivity extends AppCompatActivity {
    private LinearLayout productsLayout;

    private final String TAG = "TRANSACTION: ";

    public static final int TRANSACTION_COMPLETE = 1;
    public static final int TRANSACTION_FAILED = 2;
    public static final int REQUEST_CAMERA_PERMISSION = 100;
    public static final int REQUEST_LOCATION_FINE_PERMISSION = 200;

    private ArrayList<String> event_ids;
    private String event_id;
    private ArrayList<String> event_names;
    private Spinner eventSpinner;
    private ArrayAdapter<String> adapter;
    private LinearLayout spinnerLayout;
    private LinearLayout currentProductLinearLayout;
    private Button backButton;

    private Dialog mDialog;
    private FirebaseAuth mAuth;
    private ProgressBar productsProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction);
        mAuth = FirebaseAuth.getInstance();
        mDialog = new Dialog(this);

        event_ids = new ArrayList<>();
        event_names = new ArrayList<>();
        eventSpinner = findViewById(R.id.transaction_event_spinner);
        spinnerLayout = findViewById(R.id.spinnerLayout);

        productsProgressBar = findViewById(R.id.productsProgressBar);
        productsLayout = findViewById(R.id.products);

        /*
        backButton = findViewById(R.id.transaction_back_btn);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
         */

        if(!Utils.checkInternetConnection(getApplicationContext())){
            Toast.makeText(getApplicationContext(), R.string.toast_main_internet,
                    Toast.LENGTH_SHORT).show();
        }else {
            //show all the products related to an event
            getEvents();
        }
    }

    private void getEvents(){
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
        ref.child("Eventos-Collabs").orderByChild("collab_id").equalTo(mAuth.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    if(snapshot.getValue()!=null){
                        if(snapshot.getChildrenCount() == 1){
                            findViewById(R.id.spinnerLayout).setVisibility(View.GONE);
                        }
                        for (DataSnapshot child : snapshot.getChildren()) {
                            EventsCollabs ec = child.getValue(EventsCollabs.class);
                            event_ids.add(ec.getEvento_id());
                            event_names.add(ec.getNome());
                        }
                        createAndAddAdapter();
                    }
                }else{
                    Toast.makeText(getApplicationContext(), R.string.remove_event_not_found_colaborator,
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getApplicationContext(), R.string.event_page_event_not_found,
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void createAndAddAdapter(){
        if(spinnerLayout.getVisibility() == View.GONE){
            event_id = event_ids.get(0);
            showProducts(event_ids.get(0));
            return;
        }
        //create an adapter to describe how the items are displayed, adapters are used in several places in android.
        adapter = new ArrayAdapter<>(getApplicationContext(), R.layout.spinner_item, event_names);

        //set the spinners adapter to the previously created one.
        eventSpinner.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        eventSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                event_id = event_ids.get(position);
                showProducts(event_ids.get(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {}
        });
    }

    private void handleProductView(){

        //Get event_id associated with collaborator
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();

        ref.child("Eventos-Collabs").child(mAuth.getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    HashMap<String, HashMap> dS = (HashMap)(snapshot.getValue());

                    for(Map.Entry<String, HashMap> entry : dS.entrySet()) {
                        String event_id = (String)entry.getValue().get("evento_id");

                        //show products associated to event_id
                        showProducts(event_id);
                        break;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void showProducts(String event_id){

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
        ref.child("Produtos").child(event_id).addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(currentProductLinearLayout != null){
                    productsLayout.removeAllViews();
                }
                if(snapshot.exists()){
                    if(snapshot.getValue()!=null){
                        for (DataSnapshot child : snapshot.getChildren()) {
                            Product p = child.getValue(Product.class);
                            createNewProductView(p.getNome(), Integer.toString(p.getPontos()), p.getImage_url());
                        }
                        productsProgressBar.setVisibility(View.INVISIBLE);
                    }
                }else{
                    Toast.makeText(TransactionActivity.this, R.string.products_not_found_event,
                            Toast.LENGTH_SHORT).show();
                    productsProgressBar.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(TransactionActivity.this, R.string.event_page_event_not_found,
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void createNewProductView(String name, String points, String image_url){ //Creates all the elements necessary to show the Products on the screen
        LinearLayout ll = new LinearLayout(this);
        ll.setOrientation(LinearLayout.HORIZONTAL);

        ImageView iv = new ImageView(this);
        LinearLayout.LayoutParams lp1 = new LinearLayout.LayoutParams(200, 200);
        lp1.setMargins(30, 30, 30, 30);
        iv.setLayoutParams(lp1);

        if(image_url != null) {
            Glide.with(TransactionActivity.this)
                    .load(image_url)
                    .placeholder(android.R.drawable.progress_indeterminate_horizontal)
                    .error(android.R.drawable.stat_notify_error)
                    .into(iv);
        }


        LinearLayout.LayoutParams lp2 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        lp2.setMargins(10, 30, 30, 30);
        TextView tv = new TextView(this);
        tv.setText(name+": "+points+" pontos");
        tv.setTextColor(Color.rgb(0,0,0));
        tv.setTextSize(28);
        tv.setLayoutParams(lp2);
        tv.setGravity(Gravity.CENTER);

        LinearLayout.LayoutParams lp3 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp3.setMargins(0, 0, 0, 15);
        ll.addView(iv);
        ll.addView(tv);
        ll.setBackgroundColor(Color.rgb(175,133,229));
        ll.setLayoutParams(lp3);
        ll.setOnClickListener(new View.OnClickListener(){
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View v) {
                if(!Utils.isConnected){
                    Toast.makeText(getApplicationContext(), R.string.toast_main_internet,
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                if (checkSelfPermission(Manifest.permission.CAMERA)
                        != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
                }else{
                    Intent intent = new Intent(getApplicationContext(), QRScanner.class);
                    intent.putExtra("product", name);
                    intent.putExtra("points", points);
                    intent.putExtra("code", TRANSACTION_COMPLETE);
                    startActivityForResult(intent,TRANSACTION_COMPLETE);
                }
            }
        });

        currentProductLinearLayout = ll;
        productsLayout.addView(ll);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Intent intent = new Intent(getApplicationContext(),QRScanner.class);
                //intent.putExtra("product", name);
                //intent.putExtra("points", points);
                intent.putExtra("code", TRANSACTION_COMPLETE);
                startActivityForResult(intent,TRANSACTION_COMPLETE);
            }
            else {
                Toast.makeText(TransactionActivity.this, R.string.camera_permission_failed, Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == TRANSACTION_COMPLETE) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            try{
                String uid = data.getStringExtra("uid");
                String product = data.getStringExtra("product");
                String points = data.getStringExtra("points");
                handleTransaction(uid, event_id, product, points);
            } catch (Exception e) {
                // The ApiException status code indicates the detailed failure reason.
                // Please refer to the GoogleSignInStatusCodes class reference for more information.
                Toast.makeText(TransactionActivity.this, R.string.qr_code_invalid,
                        Toast.LENGTH_SHORT).show();
                Log.w(TAG, "TransactionResult:failed code=" + e.getMessage());
            }
        }
    }

    private void handleTransaction(String uid, String event_id, String product, String points){
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
        ref.child("Users").child(uid).addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    User u = snapshot.getValue(User.class);
                    if(u.getPontos() - Integer.parseInt(points) < 0){
                        Utils.showTransactionStatus(getApplicationContext(),"FAILED", mDialog);
                    }else {
                        int userPoints = u.getPontos() - Integer.parseInt(points);
                        ref.child("Users").child(uid).child("pontos").setValue(userPoints);

                        Calendar calendar = Calendar.getInstance();
                        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
                        String date = dateFormat.format(calendar.getTime());
                        date = date.replaceAll("/","-");

                        int hours = calendar.get(Calendar.HOUR_OF_DAY);
                        int minutes = calendar.get(Calendar.MINUTE);
                        String time = String.format("%02d:%02d", hours, minutes);

                        Transaction transaction = new Transaction(date, time, mAuth.getUid(), uid, product, points);
                        ref.child("Transacoes").child(event_id).child(date).push().setValue(transaction);
                        Utils.showTransactionStatus(getApplicationContext(),"COMPLETE", mDialog);
                        return;
                    }
                }
                else{
                    Toast.makeText(TransactionActivity.this, R.string.transaction_username_not_found,
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(TransactionActivity.this, R.string.main_error_try_again,
                        Toast.LENGTH_SHORT).show();
            }
        });
    }


}