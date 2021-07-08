package com.example.habemusfesta.events;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.habemusfesta.R;
import com.example.habemusfesta.utils.Utils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;

public class RemoveEventsActivity extends AppCompatActivity {

    private LinearLayout eventsLayout;

    private final String TAG = "REM_EVENT:";

    private FirebaseAuth mAuth;
    private ProgressBar removeEventsProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_remove_events);

        mAuth = FirebaseAuth.getInstance();

        removeEventsProgressBar = findViewById(R.id.remEventsProgressBar);
        eventsLayout = findViewById(R.id.rem_products);

        handleEventView();
    }

    private void handleEventView(){

        //Get event_id associated with collaborator
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();

        ref.child("Eventos-Collabs").orderByChild("collab_id").equalTo(mAuth.getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    HashMap<String, HashMap> dS = (HashMap)(snapshot.getValue());

                    for(Map.Entry<String, HashMap> entry : dS.entrySet()) {
                        String event_id = (String)entry.getValue().get("evento_id");
                        //show products associated to event_id
                        showEvents(event_id);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    private void showEvents(String event_id){

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
        ref.child("Eventos").child(event_id).addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    if(snapshot.getValue()!=null){
                        Event e = snapshot.getValue(Event.class);
                        ref.child("Eventos-Imgs").child(event_id).orderByChild("nome").equalTo("event_img").addListenerForSingleValueEvent(new ValueEventListener() {

                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if(snapshot.exists()){
                                    if(snapshot.getValue()!=null){
                                            for (DataSnapshot child : snapshot.getChildren()) {
                                                EventsImages ei = child.getValue(EventsImages.class);
                                                createNewEventView(e.getEvent_id(), e.getNome(),ei.getImage_url());
                                            }
                                        removeEventsProgressBar.setVisibility(View.INVISIBLE);
                                    }
                                }else{
                                    Toast.makeText(RemoveEventsActivity.this, R.string.remove_event_image_not_found,
                                            Toast.LENGTH_SHORT).show();
                                    removeEventsProgressBar.setVisibility(View.INVISIBLE);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Toast.makeText(RemoveEventsActivity.this, R.string.event_page_event_not_found,
                                        Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                        removeEventsProgressBar.setVisibility(View.INVISIBLE);
                }else{
                    Toast.makeText(RemoveEventsActivity.this, R.string.remove_event_not_found_colaborator,
                            Toast.LENGTH_SHORT).show();
                    removeEventsProgressBar.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(RemoveEventsActivity.this, R.string.event_page_event_not_found,
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void createNewEventView(String event_id, String name, String image_url){ //Creates all the elements necessary to show the Products on the screen
        LinearLayout ll = new LinearLayout(this);
        ll.setOrientation(LinearLayout.HORIZONTAL);

        ImageView iv = new ImageView(this);
        LinearLayout.LayoutParams lp1 = new LinearLayout.LayoutParams(200, 200);
        lp1.setMargins(30, 30, 30, 30);
        iv.setLayoutParams(lp1);

        if(image_url != null) {
            Glide.with(RemoveEventsActivity.this)
                    .load(image_url)
                    .placeholder(android.R.drawable.progress_indeterminate_horizontal)
                    .error(android.R.drawable.stat_notify_error)
                    .into(iv);
        }



        LinearLayout.LayoutParams lp2 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        lp2.setMargins(10, 30, 30, 30);
        TextView tv = new TextView(this);
        tv.setText(name);
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
                removeEvent(event_id);
            }
        });



        eventsLayout.addView(ll);
    }

    private void removeEvent(String event_id){

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
        StorageReference storageRef = FirebaseStorage.getInstance().getReference();


        ref.child("Eventos").child(event_id).removeValue();
        ref.child("Eventos-Users").child(event_id).removeValue();
        ref.child("Eventos-Locs").child(event_id).removeValue();
        ref.child("Eventos-Imgs").child(event_id).removeValue();
        ref.child("Produtos").child(event_id).removeValue();

        //Images from event
        storageRef.child(event_id).delete();

        //delete from Users-Likes
        ref.child("Users-Likes").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    for (DataSnapshot child : snapshot.getChildren()) {
                        String user_id = child.getKey();
                        if(((HashMap<String, Integer>)child.getValue()).containsKey(event_id)){
                            ref.child("Users-Likes").child(user_id).child(event_id).removeValue();
                        }

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });


        //delete from Eventos-Collabs
        ref.child("Eventos-Collabs").orderByChild("evento_id").equalTo(event_id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    for (DataSnapshot child : snapshot.getChildren()) {
                        child.getRef().removeValue();
                    }
                    Toast.makeText(RemoveEventsActivity.this, R.string.remove_event_success,
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

}