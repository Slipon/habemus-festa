package com.example.habemusfesta.products;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.habemusfesta.R;
import com.example.habemusfesta.events.EventsImages;
import com.example.habemusfesta.utils.Utils;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ProductActivity extends AppCompatActivity {

    private static final int IMAGE_REQUEST = 1;

    private FirebaseAuth mAuth;
    private ArrayList<String> event_ids;
    private ArrayList<String> event_names;
    private ArrayAdapter<String> adapter;
    private Spinner dropdown;
    private String event_id;
    private EditText name, points;

    private Button product_confirm_btn;
    private Button product_image_btn;

    private ImageView product_image;
    private Uri product_image_uri;

    private StorageTask mUploadTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product);
        event_id = "";
        mAuth = FirebaseAuth.getInstance();
        name = findViewById(R.id.product_name);
        points = findViewById(R.id.product_points);

        //get the spinner from the xml.
        dropdown = findViewById(R.id.spinner1);
        //create a list of items for the spinner.
        event_ids = new ArrayList<>();
        event_names = new ArrayList<>();

        product_image = findViewById(R.id.product_img);

        getEventIds();

        product_image_btn = findViewById(R.id.product_img_btn);
        product_image_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseImage();
            }
        });

        product_confirm_btn = findViewById(R.id.product_confirm_btn);
        product_confirm_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!Utils.checkInternetConnection(getApplicationContext())){
                    Toast.makeText(getApplicationContext(), R.string.toast_main_internet,
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                product_confirm_btn.setClickable(false);
                createProduct();
            }
        });
    }

    private void chooseImage(){
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, IMAGE_REQUEST);
    }

    private void getEventIds(){
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
        //get all the event ids that are associated to this user
        ref.child("Eventos-Collabs").orderByChild("collab_id").equalTo(mAuth.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    HashMap<String, HashMap> dS = (HashMap)(snapshot.getValue());
                    for(Map.Entry<String, HashMap> entry : dS.entrySet()) {
                        event_ids.add((String)entry.getValue().get("evento_id"));
                        event_names.add((String)entry.getValue().get("nome"));
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
        adapter = new ArrayAdapter<>(getApplicationContext(), R.layout.spinner_collab, event_names);

        //set the spinners adapter to the previously created one.
        dropdown.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        dropdown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                event_id = event_ids.get(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {

            }
        });
    }

    private void createProduct(){
        if(name.getText().toString().length() == 0 || points.getText() == null || product_image_uri == null){
            product_confirm_btn.setClickable(true);
            Toast.makeText(ProductActivity.this, R.string.products_fields_incomplete,
                    Toast.LENGTH_SHORT).show();
            return;
        }
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();

        // Create a Cloud Storage reference from the app
        StorageReference storageRef = FirebaseStorage.getInstance().getReference();

        String id = ref.push().getKey();
        //Upload event image into Cloud Storage
        mUploadTask = storageRef.child(event_id).child(id).putFile(product_image_uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Task<Uri> firebaseUri = taskSnapshot.getStorage().getDownloadUrl();
                firebaseUri.addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {

                        String url = uri.toString();
                        EventsImages img = new EventsImages(url, name.getText().toString());
                        Log.e("TAG:", "the url is: " + url);

                        Product p = new Product( event_id, name.getText().toString(), Integer.parseInt(points.getText().toString()), url );
                        String id = ref.child("Produtos").child(event_id).push().getKey();

                        ref.child("Produtos").child(event_id).child(id).setValue(p);
                        ref.child("Eventos-Imgs").child(event_id).child(id).setValue(img);

                        product_confirm_btn.setClickable(true);
                        Toast.makeText(ProductActivity.this, R.string.products_success,
                                Toast.LENGTH_SHORT).show();

                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                product_confirm_btn.setClickable(true);
                Toast.makeText(ProductActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null){
            product_image_uri = data.getData();
            try {
                Bitmap yourBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), product_image_uri);
                Bitmap resized = Bitmap.createScaledBitmap(yourBitmap, product_image.getWidth(), product_image.getHeight(), true);
                product_image.setImageBitmap(resized);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

}