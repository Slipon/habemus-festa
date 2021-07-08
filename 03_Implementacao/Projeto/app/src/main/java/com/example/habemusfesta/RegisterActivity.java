package com.example.habemusfesta;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.habemusfesta.users.User;
import com.example.habemusfesta.utils.Utils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.*;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;

public class RegisterActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private String TAG = "REGISTER: ";
    private String curDate;
    private FirebaseDatabase firebaseDatabase;
    private EditText inputUsername;
    private EditText inputName;
    private TextView inputBirthDate;
    private EditText inputEmail;
    private EditText inputPassword;
    private EditText inputRepeatPassword;
    private Button registerBtn;
    private ImageView inputBirthDateImg;
    private TextView registerBack;

    private Dialog mDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        mDialog = new Dialog(this);

        inputUsername = findViewById(R.id.inputUsername);
        inputName = findViewById(R.id.inputName);
        inputEmail = findViewById(R.id.inputEmail);
        inputPassword = findViewById(R.id.inputPassword);
        inputRepeatPassword = findViewById(R.id.inputRepeatPassword);

        inputBirthDate = findViewById(R.id.inputBirthDate);
        inputBirthDate.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View v) {
                showBirthDateDialog();
            }
        });


        registerBtn = findViewById(R.id.buttonSubmitRegister);
        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                registerBtn.setClickable(false);

                if(!Utils.checkInternetConnection(getApplicationContext())){
                    Toast.makeText(RegisterActivity.this, R.string.toast_main_internet,
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                String result = checkInvalidData(inputUsername.getText().toString(), inputName.getText().toString(), inputEmail.getText().toString(), inputBirthDate.getText().toString(), inputPassword.getText().toString(), inputRepeatPassword.getText().toString());
                //Check invalid data
                if(result.equals("Validated")){
                    //proceed with register
                    String[] n = inputName.getText().toString().toLowerCase().split(" ");
                    String name = "";
                    for(String i : n){
                        name += Character.toUpperCase(i.charAt(0)) + i.substring(1)+" ";
                    }
                    name = name.trim();
                    createNewAccount(inputUsername.getText().toString().toLowerCase(), name, inputBirthDate.getText().toString(),inputEmail.getText().toString(),inputPassword.getText().toString());
                }
                else{
                    registerBtn.setClickable(true);
                    Toast.makeText(RegisterActivity.this, result, Toast.LENGTH_SHORT).show();
                }
            }
        });

        registerBack = findViewById(R.id.register_back);
        registerBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        //FirebaseUser currentUser = mAuth.getCurrentUser();
    }

    //Function that validates the entries from the register form, returning either a "Validated" string or the consequent problem
    private String checkInvalidData(String username, String name, String email, String birthDate, String password, String repeatPassword){

        if( username.length() < 3 || username.length() > 12 || !username.matches("[A-Za-z0-9]+") ){ //Check if username only has valid characters (letters and numbers)
            return "Username must be between 3 to 12 characters with no special characters or whitespaces (only letters and numbers)";
        }
        if( name.length() < 1 || name.length() > 255 || name.matches("[a-zA-Z]+") ){ //Check if name only has valid characters (letters)
            return "Name can't have more than 255 characters and special characters (only letters)";
        }
        if( !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() ){
            return "Invalid email. Please try again";
        }
        if( birthDate.length() == 0  || !birthDate.matches("\\d{2}-\\d{2}-\\d{4}")){ //Check if birthDate is valid
            return "BirthDate must be valid.";
        }
        if( password.length() < 6 || password.length() > 15 ){
            return "Password must be between 6 and 15 characters";
        }
        if( !password.equals(repeatPassword) ){
            return "RepeatPassword and Password are not the same";
        }
        return "Validated";
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    private void showBirthDateDialog() {

        SimpleDateFormat formatter = new SimpleDateFormat("MM-dd-yyyy");
        Date d = new Date();
        curDate = formatter.format(d);
        mDialog.setContentView(R.layout.birthdate_popup_window);
        mDialog.getWindow().setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);

        DatePicker birthDateCalendarView = mDialog.findViewById(R.id.birthDateCalendarView);
        birthDateCalendarView.setOnDateChangedListener(new DatePicker.OnDateChangedListener() {
            @Override
            public void onDateChanged(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                curDate = String.format("%02d", (monthOfYear+1))+"-"+String.format("%02d", dayOfMonth)+"-"+year;
            }
        });

        Button birthDateConfirmBtn = mDialog.findViewById(R.id.birthDateConfirm);
        birthDateConfirmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(curDate.length() > 0){
                    inputBirthDate.setText(curDate);
                }
                    mDialog.dismiss();
            }
        });

        Button birthDateCloseBtn = mDialog.findViewById(R.id.birthDateCloseBtn);
        birthDateCloseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDialog.dismiss();
            }
        });

        mDialog.show();
        Window window = mDialog.getWindow();
        window.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
    }

    //Function that creates a new account via Firebase Authentication, and also stores user data in Firebase RealTimeDatabase
    private void createNewAccount(String username, String name, String birthDate, String email, String password){
        //Check if username already in DB
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
        ref.child("Users").orderByChild("username").equalTo(username).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    // if given username exists
                    Toast.makeText(RegisterActivity.this, R.string.user_already_in_use,
                            Toast.LENGTH_SHORT).show();
                    registerBtn.setClickable(true);
                    return;
                } else {
                    //Create user
                    mAuth.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener(RegisterActivity.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) { //User succesfully created
                                        Log.d(TAG, "createUserWithEmail:success");
                                        // if given username does not exist yet
                                        User newUser = new User(username, name, email, "Normal", birthDate,  1, 0, 0);
                                        ref.child("Users").child(mAuth.getUid()).setValue(newUser);

                                        //Authentication complete message
                                        Toast.makeText(RegisterActivity.this, R.string.main_authentication_success,
                                                Toast.LENGTH_SHORT).show();

                                        //Go back to Login Screen
                                        Intent intent = new Intent(RegisterActivity.this, MainActivity.class); // start ResultActivity
                                        startActivityForResult(intent, 1);

                                    } else {

                                        try
                                        {
                                            throw task.getException();
                                        }
                                        catch (FirebaseAuthUserCollisionException existEmail) //If the given email is already registered in the DB
                                        {
                                            registerBtn.setClickable(true);
                                            Log.d(TAG, "onComplete: exist_email");
                                            Toast.makeText(RegisterActivity.this, R.string.user_email_already_in_use,
                                                    Toast.LENGTH_SHORT).show();
                                        }
                                        catch (Exception e) // If sign up fails, display a message to the user.
                                        {
                                            registerBtn.setClickable(true);
                                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                                            Toast.makeText(RegisterActivity.this, R.string.main_authentication_failed_reset_internet,
                                                    Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                }
                            });

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                registerBtn.setClickable(true);
                Toast.makeText(RegisterActivity.this, "Error:"+databaseError.getMessage()+getString(R.string.please_try_again),
                        Toast.LENGTH_SHORT).show();
            }
        });

    }

    //Function that stores user data in Firebase RealTimeDatabase, given from Google Sign-In method
    public static void createNewAccountWithGoogle(String name, String email, FirebaseAuth mAuth){

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
        User newUser;
        newUser = new User("", name,  email, "Normal", "",  0, 1, 0);
        ref.child("Users").child(mAuth.getUid()).setValue(newUser);

    }


}