package com.bookstore.palvindersingh;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class mainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //initialise splash screen
        int SPLASH_TIME_OUT = 250;
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                manageAuth();
            }
        }, SPLASH_TIME_OUT);
    }

    //method to check for internet connection
    private Boolean checkInternet() {
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

    private double calcRating(double rented, double loaned) {
        try {
            rented = rented + 1;
            loaned = loaned + 1;
            int x = (int) (loaned / rented);
            double rating = Math.round((1 / (1 + Math.exp(-x))) * 5);
            return rating;
        } catch (Exception e) {
            return -1;
        }
    }

    //method to manage authentication
    private void manageAuth() {
        //initialise firestore connection
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setTimestampsInSnapshotsEnabled(true)
                .build();
        firestore.setFirestoreSettings(settings);
        //check if there is an internet connection
        final Boolean internetConnection = checkInternet();
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (!internetConnection) {
            //prompt the user there is no connection
            Toast.makeText(getApplicationContext(), "no internet connection", Toast.LENGTH_SHORT).show();
        }
        //if the user is not logged in
        if (auth.getCurrentUser() == null) {
            //if there is no internet
            if (!internetConnection) {
                //go to no network activity
                Intent intent = new Intent(mainActivity.this, noNetworkActivity.class);
                startActivity(intent);
                finish();
            } else {
                //go to login activity
                Intent intent = new Intent(mainActivity.this, logInActivity.class);
                startActivity(intent);
                finish();
            }
        } else {
            //initialise firestore connection
            final FirebaseUser user = auth.getCurrentUser();
            final FirebaseFirestore db = FirebaseFirestore.getInstance();
            final String userID = user.getUid();
            DocumentReference userDocument = db.collection("users").document(userID);
            //get users document
            userDocument.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    //check if extra details have been input before
                    ArrayList<String> array = new ArrayList<>();
                    array.add(task.getResult().get("dateOfBirth").toString());
                    array.add(task.getResult().get("formRoom").toString());
                    array.add(task.getResult().get("yearGroup").toString());
                    //if they have been input
                    if (!Objects.equals(array.get(0), "") && array.get(1) != "" && array.get(2) != "") {
                        //update rating
                        ArrayList currentlyRented = (ArrayList) task.getResult().get("borrowedBooks");
                        double currentlyRentedSize = currentlyRented.size();
                        ArrayList loanedBooks = (ArrayList) task.getResult().get("loanedBooks");
                        double loanedBooksSize = loanedBooks.size();
                        Integer rating = (int) calcRating(currentlyRentedSize, loanedBooksSize);
                        Integer userRating = Integer.valueOf(task.getResult().get("userRating").toString().charAt(0));
                        if (rating != userRating && rating != -1) {
                            Map<String, Object> data = new HashMap<>();
                            data.put("userRating", rating);
                            db.collection("users").document(userID).set(data, SetOptions.merge());
                        }

                        //go to home activity
                        Intent intent = new Intent(mainActivity.this, homeActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        //if there is no internet
                        if (!internetConnection) {
                            //go to no network activity
                            Intent intent = new Intent(mainActivity.this, noNetworkActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            //go to details activity
                            Intent intent = new Intent(mainActivity.this, detailsActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    }
                }
            });
        }
    }
}