package com.bookstore.palvindersingh;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class detailsActivity extends AppCompatActivity {

    //initialise attributes
    private DatePicker datePicker;
    private EditText yearGroup;
    private EditText formRoom;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        //initialise firestore connection
        db = FirebaseFirestore.getInstance();

        //initialise ui components
        datePicker = findViewById(R.id.datePicker);
        yearGroup = findViewById(R.id.yearGroup);
        formRoom = findViewById(R.id.formRoom);
    }

    //method to submit input data
    public void submitData(View v) {
        //validate data
        final String date = datePicker.getDayOfMonth() + "/" + datePicker.getMonth() + "/" + datePicker.getYear();
        ArrayList validation = validate();
        if (validation.get(0) == "f") {
            Toast.makeText(getApplicationContext(), validation.get(1).toString(), Toast.LENGTH_SHORT).show();
            return;
        }
        final String yg = yearGroup.getText().toString();
        final String fr = formRoom.getText().toString();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        assert user != null;
        String userID = user.getUid();
        //add data to firestore
        final DocumentReference userData = db.collection("users").document(userID);
        userData.update("dateOfBirth", date)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        userData.update("yearGroup", yg)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        userData.update("formRoom", fr)
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        Intent intent = new Intent(detailsActivity.this, homeActivity.class);
                                                        startActivity(intent);
                                                        finish();
                                                    }
                                                })
                                                .addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                    }
                                                });
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                    }
                                });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                    }
                });
    }

    //method to validate data
    private ArrayList validate() {
        ArrayList result = new ArrayList<>();
        try {
            Integer yg = Integer.parseInt(yearGroup.getText().toString());
            if (yg > 6 && yg < 14) {
                String fr = formRoom.getText().toString();
                if ((fr.length() >= 2) && (fr.length() < 4)) {
                    result.add("t");
                    result.add("valid");
                    return result;
                } else {
                    result.add("f");
                    result.add("form room invalid");
                    return result;
                }
            } else {
                result.add("f");
                result.add("year group must be between 7 and 13");
                return result;
            }
        } catch (Exception e) {
            result.add("f");
            result.add("year group must be an int");
            return result;
        }
    }
}
