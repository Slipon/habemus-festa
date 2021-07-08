package com.example.habemusfesta.events;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.habemusfesta.gps.SearchMap;
import com.example.habemusfesta.products.ImageActivity;
import com.example.habemusfesta.R;
import com.example.habemusfesta.utils.Utils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

public class EventPage extends FragmentActivity {

    private TextView eventTxt;
    private TextView likesTxt;
    private TextView initDate;
    private TextView endDate;
    private Button yesBtn;
    private Button noBtn;
    private ImageView eventImg;
    private FirebaseAuth mAuth;
    private LinearLayout locLayout;
    private String eventImageUrl;
    private ProgressBar eventPageProgressBar;

    private SearchMap fragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.page_event);

        mAuth = FirebaseAuth.getInstance();

        initDate = findViewById(R.id.init_date);
        endDate = findViewById(R.id.end_date);
        eventTxt = findViewById(R.id.txtEvent);
        likesTxt = findViewById(R.id.likes);

        yesBtn = findViewById(R.id.yesBtn);
        yesBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleLikeSystem("ADD");
            }
        });

        noBtn = findViewById(R.id.noBtn);
        noBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleLikeSystem("REMOVE");
            }
        });

        eventImg = findViewById(R.id.imgEvent);
        eventPageProgressBar = findViewById(R.id.progressBarEventPage);

        if(!Utils.checkInternetConnection(getApplicationContext())){
            Toast.makeText(getApplicationContext(), R.string.toast_main_internet,
                    Toast.LENGTH_SHORT).show();
            return;
        }else {
            getEventInfo();
        }
    }

    private void getEventInfo() {
        Intent givenIntent = getIntent();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
        //Event image
        ref.child("Eventos-Imgs").child(givenIntent.getStringExtra("event_id")).orderByChild("nome").equalTo("event_img").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    HashMap dS = (HashMap)(snapshot.getValue());
                    String uid = dS.keySet().toArray()[0].toString();
                    EventsImages ei = snapshot.child(uid).getValue(EventsImages.class);
                    eventImageUrl = ei.getImage_url();
                    if(eventImageUrl != null) {
                        Glide.with(EventPage.this)
                                .load(eventImageUrl)
                                .placeholder(android.R.drawable.progress_indeterminate_horizontal)
                                .error(android.R.drawable.stat_notify_error)
                                .into(eventImg);
                        eventPageProgressBar.setVisibility(View.INVISIBLE);
                        //create onClickListener for fullscreen view
                        eventImg.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent(EventPage.this, ImageActivity.class);
                                intent.putExtra("image_url", eventImageUrl);
                                startActivity(intent);
                            }
                        });
                    }else{
                        Toast.makeText(EventPage.this, R.string.event_page_url_warning,
                                Toast.LENGTH_SHORT).show();
                    }
                }else{
                    Toast.makeText(EventPage.this, R.string.event_page_event_not_found,
                            Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        //Event Info
        ref.child("Eventos").child(givenIntent.getStringExtra("event_id")).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //TODO: ADD INTERNET VALIDATION
                if(snapshot.exists()){
                    Event e = snapshot.getValue(Event.class);
                    eventTxt.setText(e.getNome());
                    initDate.setText(e.getData_inicial()+" | "+e.getHora_inicial());
                    endDate.setText(e.getData_final()+" | "+e.getHora_final());
                    likesTxt.setText(Integer.toString(e.getLikes()));

                    //User Likes
                    ref.child("Users-Likes").child(mAuth.getUid()).child(givenIntent.getStringExtra("event_id")).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if(snapshot.exists()){
                                int reaction = snapshot.getValue(Integer.class);
                                if(reaction == 1){
                                    yesBtn.setClickable(false);
                                }else if(reaction == -1){
                                    noBtn.setClickable(false);
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });


                        //Localização do evento no Google Maps
                    ref.child("Eventos-Locs").child(givenIntent.getStringExtra("event_id")).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if(snapshot.exists()) {
                                HashMap<String, ArrayList> dS = (HashMap)(snapshot.getValue());
                                Double lat = (Double)(dS.get("l").get(0));
                                Double lon = (Double)(dS.get("l").get(1));

                                FragmentManager fragmentManager = getSupportFragmentManager();
                                fragment = new SearchMap(true, lat, lon);
                                fragmentManager.beginTransaction().replace(R.id.event_map, fragment).commit();

                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }else{
                    Toast.makeText(EventPage.this, R.string.event_page_event_not_found,
                            Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void handleLikeSystem(String reactionType) {

        Intent givenIntent = getIntent();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
        ref.child("Eventos").child(givenIntent.getStringExtra("event_id")).addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Event e = snapshot.getValue(Event.class);
                    int likes = e.getLikes();
                    int value = 0;
                    switch (reactionType) {
                        case "ADD":

                            likes += 1;
                            value = 1;

                            yesBtn.setClickable(false);
                            noBtn.setClickable(true);

                            break;
                        case "REMOVE":

                            likes = likes == 0 ? 0 : likes - 1;
                            value = -1;

                            yesBtn.setClickable(true);
                            noBtn.setClickable(false);

                            break;
                        default:
                            break;
                    }
                    e.setLikes(likes);
                    ref.child("Eventos").child(givenIntent.getStringExtra("event_id")).setValue(e);
                    ref.child("Users-Likes").child(mAuth.getUid()).child(givenIntent.getStringExtra("event_id")).setValue(value);
                    likesTxt.setText(Integer.toString(likes));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

}