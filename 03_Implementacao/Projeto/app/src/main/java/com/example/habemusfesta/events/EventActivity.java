package com.example.habemusfesta.events;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.habemusfesta.MainActivity;
import com.example.habemusfesta.R;
import com.example.habemusfesta.gps.SearchMap;
import com.example.habemusfesta.utils.Utils;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;


public class EventActivity extends AppCompatActivity {

    private static int IMAGE_REQUEST = 1;
    private FirebaseAuth mAuth;

    private EditText event_name;
    private TextView event_init_date;
    private TextView event_end_date;
    private EditText event_description;
    private Button event_confirm_btn;
    private Button event_image_btn;
    private String curDate;
    private boolean isInitDateTimeComplete;
    private boolean isEndDateTimeComplete;
    private ImageView calendar_init_btn;
    private ImageView calendar_end_btn;
    private ImageView event_image_view;
    private Uri event_image_uri;

    private Spinner dropdown;
    private ArrayList<String> tag_names;
    private ArrayAdapter<String> adapter;
    private String eventTag;
    private int eventTagID;

    private Dialog mDialog;

    private StorageTask mUploadTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event);
        curDate = "";
        mDialog = new Dialog(this);
        mAuth = FirebaseAuth.getInstance();
        eventTag = "";

        //get the spinner from the xml.
        dropdown = findViewById(R.id.eventSpinner);
        tag_names = new ArrayList<>();
        getTags();

        isInitDateTimeComplete = false;
        isEndDateTimeComplete = false;

        event_name = findViewById(R.id.event_name);
        event_image_btn = findViewById(R.id.event_image_btn);
        event_image_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseImage();
            }
        });

        event_image_view = findViewById(R.id.event_image_view);

        event_init_date = findViewById(R.id.event_init_date);

        calendar_init_btn = findViewById(R.id.calendar_init);
        calendar_init_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCalendarDialog("init");
            }
        });

        calendar_end_btn = findViewById(R.id.calendar_end);
        calendar_end_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCalendarDialog("end");
            }
        });

        event_end_date = findViewById(R.id.event_end_date);
        event_description = findViewById(R.id.event_description);
        event_confirm_btn = findViewById(R.id.event_confirm_btn);
        event_confirm_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!Utils.checkInternetConnection(getApplicationContext())){
                    Toast.makeText(getApplicationContext(), R.string.toast_main_internet,
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                event_confirm_btn.setClickable(false);
                if(mUploadTask != null && mUploadTask.isInProgress()){ //to prevent the user from creating multiple events
                    Toast.makeText(EventActivity.this, R.string.event_activity_create_progress, Toast.LENGTH_SHORT).show();
                }else{
                    createEvent();
                }
            }
        });

        FragmentManager fragmentManager = getSupportFragmentManager();
        SearchMap fragment = new SearchMap();
        fragmentManager.beginTransaction().replace(R.id.testFragment, fragment).commit();

    }

    private void getTags(){
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
        //get all the event ids that are associated to this user
        ref.child("tags").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){

                    ArrayList<String> dS = (ArrayList<String>) (snapshot.getValue());
                    for(String i : dS) {
                        System.out.println(i);
                        if( i!=null && i.length() > 0 ){
                            tag_names.add(i);
                        }
                    }
                    createAndAddAdapter();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void createAndAddAdapter(){
        //create an adapter to describe how the items are displayed, adapters are used in several places in android.
        adapter = new ArrayAdapter<>(getApplicationContext(), R.layout.spinner_collab, tag_names);

        //set the spinners adapter to the previously created one.
        dropdown.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        dropdown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                eventTagID = position + 1;
                eventTag = tag_names.get(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {

            }
        });
    }

    private void showCalendarDialog(String dateType) {

        SimpleDateFormat formatter = new SimpleDateFormat("MM-dd-yyyy");
        Date d = new Date();
        curDate = formatter.format(d);
        mDialog.setContentView(R.layout.calendar_popup_window);
        mDialog.getWindow().setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);

        CalendarView calendarView = mDialog.findViewById(R.id.calendarView);

        if (dateType.equals("end") && event_init_date.getText().toString().length() > 0) {
            try {
                Date d1 = formatter.parse(event_init_date.getText().toString().split("\\|")[0].trim());
                long ms = d1.getTime();
                calendarView.setMinDate(ms);
                if(event_end_date.getText().toString().length() > 0){
                    d1 = formatter.parse(event_end_date.getText().toString().split("\\|")[0].trim());
                    ms = d1.getTime();
                    calendarView.setDate(ms);
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }else if(dateType.equals("init") && event_end_date.getText().toString().length() > 0) {
            try {
                Date d1 = formatter.parse(event_end_date.getText().toString().split("\\|")[0].trim());
                long ms = d1.getTime();
                calendarView.setMaxDate(ms);
                if(event_init_date.getText().toString().length() > 0){
                    d1 = formatter.parse(event_init_date.getText().toString().split("\\|")[0].trim());
                    ms = d1.getTime();
                    calendarView.setDate(ms);
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView view, int year, int month, int dayOfMonth) {
                curDate = String.format("%02d", (month+1))+"-"+String.format("%02d", dayOfMonth)+"-"+year;
            }
        });

        TimePicker picker = mDialog.findViewById(R.id.timePicker);
        picker.setIs24HourView(true);

        Button calendarConfirmBtn = mDialog.findViewById(R.id.calendarConfirm);
        calendarConfirmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(curDate.length() > 0){
                    String time = String.format("%02d:%02d", picker.getCurrentHour(), picker.getCurrentMinute());
                    if(dateType.equals("init")){

                        if(event_end_date.getText().toString().length() > 0){
                            if(!checkIfDatesAreValid(curDate + " | " + time, event_end_date.getText().toString())){
                                Toast.makeText(EventActivity.this, R.string.event_activity_date_init_warning,
                                        Toast.LENGTH_SHORT).show();
                                return;
                            }
                        }

                        event_init_date.setText(curDate+" | "+time);
                        isInitDateTimeComplete = true;
                    }else{

                        if(event_init_date.getText().toString().length() > 0){
                            if(!checkIfDatesAreValid(event_init_date.getText().toString(), curDate + " | " + time)){
                                Toast.makeText(EventActivity.this, R.string.event_activity_date_end_warning,
                                        Toast.LENGTH_SHORT).show();
                                return;
                            }
                        }

                        event_end_date.setText(curDate+" | "+time);
                        isEndDateTimeComplete = true;
                    }
                    mDialog.dismiss();
                }
            }
        });

        Button calendarCloseBtn = mDialog.findViewById(R.id.calendarCloseBtn);
        calendarCloseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDialog.dismiss();
            }
        });

        mDialog.show();
        Window window = mDialog.getWindow();
        window.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
    }

    private void createEvent() {
        if(event_name.getText().toString().length() > 0 && isInitDateTimeComplete == true && isEndDateTimeComplete == true) {

            if(event_image_uri == null){
                Toast.makeText(EventActivity.this, R.string.event_activity_image_select_warning,
                        Toast.LENGTH_SHORT).show();
                return;
            }

            DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
            // Create a Cloud Storage reference from the app
            StorageReference storageRef = FirebaseStorage.getInstance().getReference();

            String id = ref.child("Eventos").push().getKey();
            String[] datetime_init = event_init_date.getText().toString().split("\\|");
            String[] datetime_fin = event_end_date.getText().toString().split("\\|");

            //Create Event in DB
            Event e = new Event(id, Integer.toString(eventTagID), event_name.getText().toString(), event_description.getText().toString(), "", 10, 0, datetime_init[0].trim(), datetime_fin[0].trim(), datetime_init[1].trim(), datetime_fin[1].trim());
            ref.child("Eventos").child(id).setValue(e);

            //Create Event-Collaborator reference in DB
            EventsCollabs ce = new EventsCollabs(id, mAuth.getUid(), event_name.getText().toString());
            ref.child("Eventos-Collabs").push().setValue(ce);

            //Upload event image into Cloud Storage
            mUploadTask = storageRef.child(id).child("event_img").putFile(event_image_uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Task<Uri> firebaseUri = taskSnapshot.getStorage().getDownloadUrl();
                    firebaseUri.addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {

                            String url = uri.toString();
                            EventsImages img = new EventsImages(url,"event_img");
                            Log.e("TAG:", "the url is: " + url);

                            ref.child("Eventos-Imgs").child(id).push().setValue(img);

                            //Reference to DB
                            DatabaseReference ref2 = FirebaseDatabase.getInstance().getReference("Eventos-Locs");
                            GeoFire geoFire= new GeoFire(ref2);
                            geoFire.setLocation(id, new GeoLocation(SearchMap.address.getLatitude(), SearchMap.address.getLongitude()));

                            event_confirm_btn.setClickable(true);
                            Toast.makeText(EventActivity.this, R.string.event_activity_event_create_success,
                                    Toast.LENGTH_SHORT).show();

                        }
                    });
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    event_confirm_btn.setClickable(true);
                    Toast.makeText(EventActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

        }else{
            event_confirm_btn.setClickable(true);
            Toast.makeText(EventActivity.this, R.string.event_activity_missing_parameters,
                    Toast.LENGTH_SHORT).show();
        }
    }

    private boolean checkIfDatesAreValid(String stringDate1, String stringDate2) {

        stringDate1 = stringDate1.replaceAll("\\|","");
        stringDate1 = stringDate1.replaceAll("  "," ");
        stringDate2 = stringDate2.replaceAll("\\|","");
        stringDate2 = stringDate2.replaceAll("  "," ");

        System.out.println(stringDate1);
        System.out.println("----------");
        System.out.println(stringDate2);

        SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd-yyyy HH:mm") ;
        try {
            if (dateFormat.parse(stringDate1).after(dateFormat.parse(stringDate2))) {
                return false;
            } else {
                return true;
            }
        }catch (ParseException e){
            e.printStackTrace();
        }
        return false;
    }

    private void chooseImage(){
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null){
            event_image_uri = data.getData();
            try {
                Bitmap yourBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), event_image_uri);
                Bitmap resized = Bitmap.createScaledBitmap(yourBitmap, event_image_view.getWidth(), event_image_view.getHeight(), true);
                event_image_view.setImageBitmap(resized);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }
}