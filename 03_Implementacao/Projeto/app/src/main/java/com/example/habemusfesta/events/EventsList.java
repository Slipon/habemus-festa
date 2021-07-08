package com.example.habemusfesta.events;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.example.habemusfesta.R;
import com.google.android.material.tabs.TabLayout;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;

import com.example.habemusfesta.ui.main.SectionsPagerAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class EventsList extends AppCompatActivity {

    private Spinner eventSpinner;
    private ArrayList<String> tags;
    private ArrayAdapter<String> adapter;

    private TabLayout tabs;
    private ViewPager viewPager;
    private SectionsPagerAdapter sectionsPagerAdapter;
    private String currentTag;

    private String NO_FILTER_TAG = "Sem Filtro";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_events_list);
        currentTag = "";
        sectionsPagerAdapter = new SectionsPagerAdapter(this, getSupportFragmentManager());
        viewPager = findViewById(R.id.view_pager);
        viewPager.setAdapter(sectionsPagerAdapter);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}

            @Override
            public void onPageSelected(int position) {
                Fragment f = sectionsPagerAdapter.getCurrentFragment();
                if(f != null) {
                    switch (f.getClass().getSimpleName()) {
                        case "NearByEvents":
                            ((NearByEvents) f).changeFilter(currentTag);
                            break;
                        case "RecentEvents":
                            ((RecentEvents) f).changeFilter(currentTag);
                            break;
                        case "TrendingEvents":
                            ((TrendingEvents) f).changeFilter(currentTag);
                            break;
                        case "MyEvents":
                            ((MyEvents) f).changeFilter(currentTag);
                            break;
                        default:
                            System.out.println(f.getClass().getSimpleName());
                            break;
                    }
                }
                sectionsPagerAdapter.setCurrentFilter(currentTag);
            }

            @Override
            public void onPageScrollStateChanged(int state) {}
        });

        tabs = findViewById(R.id.tabs);
        tabs.setupWithViewPager(viewPager);

        tags = new ArrayList<>();
        tags.add(NO_FILTER_TAG);

        eventSpinner = findViewById(R.id.event_filter);
        getTags();
    }

    private void getTags(){
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
        ref.child("tags").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){

                    ArrayList<String> dS = (ArrayList<String>) (snapshot.getValue());
                    for(String i : dS) {
                        if( i != null && i.length() > 0 ){
                            tags.add(i);
                        }
                    }
                    createAndAddAdapter();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void createAndAddAdapter(){
        //create an adapter to describe how the items are displayed, adapters are used in several places in android.
        adapter = new ArrayAdapter<>(getApplicationContext(), R.layout.spinner_event_filter, tags);

        //set the spinners adapter to the previously created one.
        eventSpinner.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        eventSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                Fragment f = sectionsPagerAdapter.getCurrentFragment();
                currentTag = Integer.toString(position);
                if(f != null) {
                    switch (f.getClass().getSimpleName()) {
                        case "NearByEvents":
                            ((NearByEvents) f).changeFilter(currentTag);
                            break;
                        case "RecentEvents":
                            ((RecentEvents) f).changeFilter(currentTag);
                            break;
                        case "TrendingEvents":
                            ((TrendingEvents) f).changeFilter(currentTag);
                            break;
                        case "MyEvents":
                            ((MyEvents) f).changeFilter(currentTag);
                            break;
                        default:
                            System.out.println(f.getClass().getSimpleName());
                            break;
                    }
                }
                sectionsPagerAdapter.setCurrentFilter(currentTag);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {}
        });
    }

}