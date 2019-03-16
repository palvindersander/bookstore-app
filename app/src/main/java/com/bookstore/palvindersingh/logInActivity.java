package com.bookstore.palvindersingh;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class logInActivity extends AppCompatActivity {

    //initialise attributes
    private static final int RC_SIGN_IN = 123;
    private final List<AuthUI.IdpConfig> providers = Collections.singletonList(
            new AuthUI.IdpConfig.EmailBuilder().build());
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);

        //initialise firestore connection
        db = FirebaseFirestore.getInstance();

        //initialise start sign in/up activity
        startActivityForResult(AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .setLogo(R.mipmap.ic_launcher)
                .build(), RC_SIGN_IN);
    }

    //override method to manage the results of sign up
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);
            if (resultCode == RESULT_OK) {
                //sign in is successful
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                Toast.makeText(getApplicationContext(), "sign in successful", Toast.LENGTH_SHORT).show();
                manageDBChecks(user);
            } else {
                //sign in failed
                Toast.makeText(getApplicationContext(), "sign in failed", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    //method to check if the user has input extra data about themselves before
    private void manageDBChecks(final FirebaseUser user) {
        final String userID = user.getUid();
        //get the users document
        DocumentReference userDocument = db.collection("users").document(userID);
        userDocument.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                //check if user exists in firestore
                if (!Objects.requireNonNull(task.getResult()).exists()) {
                    //make the users document
                    manageDBAddUserDocument(userID, user);
                    //go to details activity
                    Intent intent = new Intent(logInActivity.this, detailsActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    ArrayList<String> array = new ArrayList<>();
                    array.add(task.getResult().get("dateOfBirth").toString());
                    array.add(task.getResult().get("formRoom").toString());
                    array.add(task.getResult().get("yearGroup").toString());
                    if (Objects.equals(array.get(0), "") && array.get(1) == "" && array.get(2) == "") {
                        //go to details activity
                        Intent intent = new Intent(logInActivity.this, detailsActivity.class);
                        startActivity(intent);
                        finish();
                    }else{
                        //go to home activity
                        Intent intent = new Intent(logInActivity.this, homeActivity.class);
                        startActivity(intent);
                        finish();
                    }
                }
            }
        });

    }

    //method to make a new document for a user
    private void manageDBAddUserDocument(String userID, FirebaseUser user) {
        //initialise hashmap
        Map<String, Object> userData = new HashMap<>();
        String name = user.getDisplayName();
        double rating = 3;
        double bookNumber = 0;
        userData.put("name", name);
        userData.put("scannedBooks", Collections.emptyList());
        userData.put("borrowedBooks", Collections.emptyList());
        userData.put("loanedBooks", Collections.emptyList());
        userData.put("userRating", rating);
        userData.put("dateOfBirth", "");
        userData.put("yearGroup", "");
        userData.put("formRoom", "");
        //add user data
        db.collection("users").document(userID)
                .set(userData)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                    }
                });
    }
}
