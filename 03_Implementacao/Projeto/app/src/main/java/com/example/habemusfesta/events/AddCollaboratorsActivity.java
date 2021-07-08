package com.example.habemusfesta.events;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.habemusfesta.R;
import com.example.habemusfesta.users.User;
import com.example.habemusfesta.utils.Utils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.ArrayList;

public class AddCollaboratorsActivity extends AppCompatActivity {

    private Spinner collabSpinner;
    private Spinner eventSpinner;
    private Button addCollabConfirmBtn;
    private ArrayList<String> event_ids = new ArrayList<>();
    private ArrayList<String> event_names = new ArrayList<>();
    private ArrayList<String> collab_ids = new ArrayList<>();
    private ArrayList<String> collab_names = new ArrayList<>();
    private String event_id;
    private String collab_id;
    private String event_name;
    private String collab_name;
    private ArrayAdapter<String> adapter;
    private boolean canContinue = true;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_collaborators);

        mAuth = FirebaseAuth.getInstance();
        collabSpinner = findViewById(R.id.collabSpinner);
        eventSpinner = findViewById(R.id.eventSpinner);
        addCollabConfirmBtn = findViewById(R.id.add_collab_confirm_btn);
        addCollabConfirmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!Utils.checkInternetConnection(getApplicationContext())){
                    Toast.makeText(getApplicationContext(), R.string.toast_main_internet,
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                addCollabConfirmBtn.setClickable(false);
                addCollaboratorToEvent();
            }
        });
        getCollaboratorsAndEvents();
    }

    private void createAndAddAdapter(String type, ArrayList<String> arr, Spinner selectedSpinner){
        //create an adapter to describe how the items are displayed, adapters are used in several places in android.
        adapter = new ArrayAdapter<>(getApplicationContext(), R.layout.spinner_collab, arr);

        //set the spinners adapter to the previously created one.
        selectedSpinner.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        selectedSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                if(type.equals("EVENT")) {
                    event_id = event_ids.get(position);
                    event_name = event_names.get(position);
                }else{
                    collab_id = collab_ids.get(position);
                    collab_name = collab_names.get(position);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {}
        });
    }

    private void getCollaboratorsAndEvents(){
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
        ref.child("Users").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    for (DataSnapshot child : snapshot.getChildren()) {
                        User u = child.getValue(User.class);
                        collab_ids.add(child.getKey());
                        collab_names.add(u.getNome()+ " | " +(u.getUsername().equals("") ? "None" : u.getUsername()));
                    }
                }
                createAndAddAdapter("COLLAB", collab_names, collabSpinner);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
        ref.child("Eventos-Collabs").orderByChild("collab_id").equalTo(mAuth.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    for (DataSnapshot child : snapshot.getChildren()) {
                        EventsCollabs ec = child.getValue(EventsCollabs.class);
                        event_ids.add(ec.getEvento_id());
                        event_names.add(ec.getNome());
                    }
                }
                createAndAddAdapter("EVENT", event_names, eventSpinner);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void addCollaboratorToEvent(){
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
        EventsCollabs ec = new EventsCollabs(event_id, collab_id, event_name);

        //check if collab is already linked to the chosen event
        ref.child("Eventos-Collabs").orderByChild("collab_id").equalTo(collab_id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()) {
                    for (DataSnapshot child : snapshot.getChildren()) {
                        EventsCollabs ecTest = child.getValue(EventsCollabs.class);
                        if(ecTest.getEvento_id().equals(event_id)){
                            addCollabConfirmBtn.setClickable(true);
                            canContinue = false;
                            Toast.makeText(getApplicationContext(), R.string.toast_colaborator_double_added, Toast.LENGTH_LONG).show();
                            break;
                        }
                    }
                    if(canContinue) {
                        //add reference to DB
                        ref.child("Eventos-Collabs").push().setValue(ec);

                        //change user_type(if needed) to "Admin"
                        ref.child("Users").child(collab_id).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.exists()) {
                                    User u = snapshot.getValue(User.class);
                                    if (u.getUser_type().equals("Normal")) {
                                        ref.child("Users").child(collab_id).child("user_type").setValue("Admin");
                                    }
                                    canContinue = true;
                                    addCollabConfirmBtn.setClickable(true);
                                    Toast.makeText(getApplicationContext(), R.string.toast_colaborator_event_added, Toast.LENGTH_LONG).show();
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {}
                        });
                    }else{
                        canContinue = true;
                    }
                }else{
                    //add reference to DB
                    ref.child("Eventos-Collabs").push().setValue(ec);

                    //change user_type(if needed) to "Admin"
                    ref.child("Users").child(collab_id).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                User u = snapshot.getValue(User.class);
                                if (u.getUser_type().equals("Normal")) {
                                    ref.child("Users").child(collab_id).child("user_type").setValue("Admin");
                                }
                                canContinue = true;
                                addCollabConfirmBtn.setClickable(true);
                                Toast.makeText(getApplicationContext(), R.string.toast_colaborator_event_added, Toast.LENGTH_LONG).show();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {}
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }
}