package com.example.habemusfesta.products;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.habemusfesta.R;
import com.example.habemusfesta.events.Event;
import com.example.habemusfesta.events.EventsCollabs;
import com.example.habemusfesta.utils.Utils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class RemoveProductsActivity extends AppCompatActivity {

    private LinearLayout productsLayout;

    private FirebaseAuth mAuth;
    private ProgressBar removeProductsProgressBar;
    private Spinner eventSpinner;
    private String event_id;
    private ArrayList<String> event_ids;
    private ArrayList<String> event_names;
    private ArrayAdapter<String> adapter;
    private LinearLayout currentProductLinearLayout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_remove_products);
        event_ids = new ArrayList<>();
        event_names = new ArrayList<>();
        mAuth = FirebaseAuth.getInstance();

        removeProductsProgressBar = findViewById(R.id.remProductsProgressBar);
        productsLayout = findViewById(R.id.rem_products);
        eventSpinner = findViewById(R.id.event_spinner);
        getEvents();
    }

    private void getEvents(){
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
        ref.child("Eventos-Collabs").orderByChild("collab_id").equalTo(mAuth.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    if(snapshot.getValue()!=null){
                        for (DataSnapshot child : snapshot.getChildren()) {
                            EventsCollabs ec = child.getValue(EventsCollabs.class);
                            event_ids.add(ec.getEvento_id());
                            event_names.add(ec.getNome());
                        }
                        createAndAddAdapter();
                    }
                }else{
                    Toast.makeText(RemoveProductsActivity.this, R.string.remove_event_not_found_colaborator,
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(RemoveProductsActivity.this, R.string.event_page_event_not_found,
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void createAndAddAdapter(){
        //create an adapter to describe how the items are displayed, adapters are used in several places in android.
        adapter = new ArrayAdapter<>(getApplicationContext(), R.layout.spinner_item, event_names);

        //set the spinners adapter to the previously created one.
        eventSpinner.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        eventSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                event_id = event_ids.get(position);
                showProducts(event_id);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {}
        });
    }

    private void showProducts(String event_id){

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
        ref.child("Produtos").child(event_id).addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    if(snapshot.getValue()!=null){
                        if(currentProductLinearLayout !=null){
                            productsLayout.removeAllViews();
                        }
                        for (DataSnapshot child : snapshot.getChildren()) {
                            Product p = child.getValue(Product.class);
                            createNewProductView(event_id, child.getKey(), p.getNome(),  Integer.toString(p.getPontos()), p.getImage_url());
                        }

                        removeProductsProgressBar.setVisibility(View.INVISIBLE);
                    }
                }else{
                    Toast.makeText(RemoveProductsActivity.this, R.string.products_not_found_event,
                            Toast.LENGTH_SHORT).show();
                    removeProductsProgressBar.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(RemoveProductsActivity.this, R.string.event_page_event_not_found,
                        Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void createNewProductView(String event_id, String product_id, String name, String points,  String image_url){ //Creates all the elements necessary to show the Products on the screen
        LinearLayout ll = new LinearLayout(this);
        ll.setOrientation(LinearLayout.HORIZONTAL);

        ImageView iv = new ImageView(this);
        LinearLayout.LayoutParams lp1 = new LinearLayout.LayoutParams(200, 200);
        lp1.setMargins(30, 30, 30, 30);
        iv.setLayoutParams(lp1);

        if(image_url != null) {
            Glide.with(RemoveProductsActivity.this)
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
                if(!Utils.checkInternetConnection(getApplicationContext())){
                    Toast.makeText(getApplicationContext(), R.string.toast_main_internet,
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                removeProduct(event_id,product_id,image_url);
            }
        });


        currentProductLinearLayout = ll;
        productsLayout.addView(ll);
    }

    private void removeProduct(String event_id, String product_id, String image_url){

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
        StorageReference storageRef = FirebaseStorage.getInstance().getReferenceFromUrl(image_url);

        ref.child("Produtos").child(event_id).child(product_id).removeValue();
        ref.child("Eventos-Imgs").child(event_id).child(product_id).removeValue();

        //Images from event
        storageRef.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                finish();
                startActivity(getIntent());
                Toast.makeText(RemoveProductsActivity.this, R.string.products_remove_sucess,
                        Toast.LENGTH_SHORT).show();
            }
        });
    }


}