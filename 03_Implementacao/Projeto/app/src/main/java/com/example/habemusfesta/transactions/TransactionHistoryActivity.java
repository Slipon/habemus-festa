package com.example.habemusfesta.transactions;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.habemusfesta.R;
import com.example.habemusfesta.events.EventsCollabs;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class TransactionHistoryActivity extends AppCompatActivity {

    private LinearLayout transactionsLayout;
    private FirebaseAuth mAuth;
    private ProgressBar transactionHistoryProgressBar;
    private ArrayList<String> event_ids;
    private ArrayList<String> event_names;
    private Spinner eventSpinner;
    private ArrayAdapter<String> adapter;
    private LinearLayout spinnerLayout;
    private LinearLayout currentTransactionLinearLayout;
    private Button backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction_history);

        event_ids = new ArrayList<>();
        event_names = new ArrayList<>();

        mAuth = FirebaseAuth.getInstance();
        eventSpinner = findViewById(R.id.transaction_event_spinner);
        spinnerLayout = findViewById(R.id.spinnerLayout);
        transactionsLayout = findViewById(R.id.transactions);
        transactionHistoryProgressBar = findViewById(R.id.transactionHistoryProgressBar);

        /*
        backButton = findViewById(R.id.transaction_history_back_btn);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        */
        getEvents();
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
            showTransactions(event_ids.get(0));
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
                showTransactions(event_ids.get(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {}
        });
    }

    private void showTransactions(String event_id){
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
        ref.child("Transacoes").child(event_id).orderByChild("hora").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(currentTransactionLinearLayout != null){
                    transactionsLayout.removeAllViews();
                }
                if(snapshot.exists()){
                    if(snapshot.getValue()!=null){
                        for (DataSnapshot child : snapshot.getChildren()) {
                            for(DataSnapshot transactionSnapshot : child.getChildren()) {
                                System.out.println(transactionSnapshot.getValue());
                                Transaction t = transactionSnapshot.getValue(Transaction.class);
                                String uid_collab = t.getUid_collab().substring(0, 4)+"...";
                                String uid_user = t.getUid_user().substring(0, 4)+"...";
                                createNewTransactionView(uid_collab, uid_user, t.getData(), t.getHora(), t.getPontos());
                            }
                        }
                       transactionHistoryProgressBar.setVisibility(View.INVISIBLE);
                    }
                }
                else{
                    Toast.makeText(TransactionHistoryActivity.this, R.string.transactions_not_found,
                            Toast.LENGTH_SHORT).show();
                    transactionHistoryProgressBar.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(TransactionHistoryActivity.this, R.string.main_error_try_again,
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void createNewTransactionView(String uid_collab, String uid_user, String date, String hours, String points){ //Creates all the elements necessary to show the Products on the screen
        LinearLayout ll = new LinearLayout(this);
        ll.setOrientation(LinearLayout.HORIZONTAL);

        LinearLayout.LayoutParams lp2 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        lp2.setMargins(10, 30, 30, 30);
        TextView tv = new TextView(this);
        tv.setText(uid_collab+" : "+uid_user+" : "+points+" pontos"+" : " +date+" | "+hours);
        tv.setTextColor(Color.rgb(0,0,0));
        tv.setTextSize(28);
        //tv.setBackgroundColor(Color.rgb(178,135,31));
        tv.setLayoutParams(lp2);
        tv.setGravity(Gravity.CENTER);

        LinearLayout.LayoutParams lp3 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp3.setMargins(0, 0, 0, 15);
        //ll.addView(iv);
        ll.addView(tv);
        ll.setBackgroundColor(Color.rgb(175,133,229));
        ll.setLayoutParams(lp3);

        currentTransactionLinearLayout = ll;
        transactionsLayout.addView(ll);
    }

}