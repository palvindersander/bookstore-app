package com.bookstore.palvindersingh;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class viewSearchBookActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_search_book);

        //initialise toolbar
        Toolbar myChildToolbar = findViewById(R.id.my_child_toolbar);
        myChildToolbar.setTitle("book details");
        setSupportActionBar(myChildToolbar);
        ActionBar ab = getSupportActionBar();
        assert ab != null;
        ab.setDisplayHomeAsUpEnabled(true);

        //get book class serialised in the intent
        final book meta = (book) getIntent().getSerializableExtra("book");

        //initialise ui components
        networkFAB requestBook = findViewById(R.id.request);
        TextView rentingText = findViewById(R.id.rentingtext);
        networkFAB rentBookStop = findViewById(R.id.rent);
        TextView rentBookStopText = findViewById(R.id.rentingtext2);

        if (meta.getRenting()) {
            requestBook.setVisibility(View.INVISIBLE);
            rentingText.setVisibility(View.INVISIBLE);
            rentBookStop.setVisibility(View.VISIBLE);
            rentBookStopText.setVisibility(View.VISIBLE);
        }

        View.OnClickListener stopRentClick = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final FirebaseFirestore db = FirebaseFirestore.getInstance();
                final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                Map<String, Object> dataBook = new HashMap<>();
                dataBook.put("renting", false);
                dataBook.put("rentingID", "");
                db.collection("books").document(meta.getFirestoreID()).set(dataBook, SetOptions.merge());
                db.collection("users").document(user.getUid()).update("borrowedBooks", FieldValue.arrayRemove(meta.getFirestoreID()));
                db.collection("users").document(meta.getOwnerReference()).update("loanedBooks", FieldValue.arrayRemove(meta.getFirestoreID()));
                Toast.makeText(getApplicationContext(), "returned book", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(viewSearchBookActivity.this, homeActivity.class);
                startActivity(intent);
                finish();
            }
        };

        rentBookStop.setClickEvent(stopRentClick);

        //display all data is it exists
        if (meta.getIsbn() != null) {
            TextView bookISBN = findViewById(R.id.isbn);
            bookISBN.setText(meta.getIsbn());
            bookISBN.setVisibility(View.VISIBLE);
        }
        if (meta.getTitle() != null) {
            TextView bookTitle = findViewById(R.id.bookTitle);
            bookTitle.setText(meta.getTitle());
            bookTitle.setVisibility(View.VISIBLE);
        }
        if (meta.getAuthors() != null) {
            TextView bookAuthors = findViewById(R.id.bookAuthors);
            bookAuthors.setText(arrayToDisplayString(meta.getAuthors()));
            bookAuthors.setVisibility(View.VISIBLE);
        }
        if (meta.getGenres() != null) {
            TextView bookGenres = findViewById(R.id.bookGenres);
            bookGenres.setText(arrayToDisplayString(meta.getAuthors()));
            bookGenres.setVisibility(View.VISIBLE);
        }
        if (meta.getImage() != null) {
            ImageView bookImage = findViewById(R.id.bookImage);
            Picasso.get().load(meta.getImage()).into(bookImage);
            bookImage.setContentDescription(meta.getImage());
            bookImage.setVisibility(View.VISIBLE);
        }

        //initialise firestore connection
        final FirebaseFirestore db = FirebaseFirestore.getInstance();
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        final String userID = user.getUid();
        //get user who owns the book data
        db.collection("users").document(meta.getOwnerReference()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        //display data if it exists
                        TextView name = findViewById(R.id.name);
                        TextView year = findViewById(R.id.year);
                        TextView form = findViewById(R.id.form);
                        TextView rating = findViewById(R.id.rating);
                        name.setText(document.get("name").toString());
                        year.setText(document.get("yearGroup").toString());
                        form.setText(document.get("formRoom").toString());
                        rating.setText(document.get("userRating").toString());
                        name.setVisibility(View.VISIBLE);
                        year.setVisibility(View.VISIBLE);
                        form.setVisibility(View.VISIBLE);
                        rating.setVisibility(View.VISIBLE);
                    } else {
                        //prompt the user doc does not exist
                        Toast.makeText(getApplicationContext(), "error occurred", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "error occurred", Toast.LENGTH_SHORT).show();
                }
            }
        });

        View.OnClickListener rentEvent = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Map<String, Object> dataBook = new HashMap<>();
                dataBook.put("market", false);
                dataBook.put("renting", true);
                dataBook.put("rentingID", userID);
                db.collection("books").document(meta.getFirestoreID()).set(dataBook, SetOptions.merge());
                DocumentReference docRef = db.collection("users").document(userID);
                docRef.update("borrowedBooks", FieldValue.arrayUnion(meta.getFirestoreID()));
                String bookOwnerID = meta.getOwnerReference();
                db.collection("users").document(bookOwnerID).update("loanedBooks", FieldValue.arrayUnion(meta.getFirestoreID()));
                Toast.makeText(getApplicationContext(), "renting book", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(viewSearchBookActivity.this, homeActivity.class);
                startActivity(intent);
                finish();
            }
        };
        requestBook.setClickEvent(rentEvent);
    }

    //method to convert array into a readable string
    private String arrayToDisplayString(ArrayList data) {
        String string = "";
        for (int i = 0; i < data.size(); i++) {
            string = string + data.get(i);
            if (i != data.size() - 1) {
                string = string + " ";
            }
        }
        return string;
    }
}
