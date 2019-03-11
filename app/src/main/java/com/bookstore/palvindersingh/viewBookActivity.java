package com.bookstore.palvindersingh;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class viewBookActivity extends AppCompatActivity {

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_book);

        //initialise toolbar
        Toolbar myChildToolbar = findViewById(R.id.my_child_toolbar);
        myChildToolbar.setTitle("book details");
        setSupportActionBar(myChildToolbar);
        ActionBar ab = getSupportActionBar();
        assert ab != null;
        ab.setDisplayHomeAsUpEnabled(true);

        //initialise book object serialised within the intent
        final book meta = (book) getIntent().getSerializableExtra("book");
        networkFAB deleteBook = findViewById(R.id.delete);

        //display all data if it exists
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

        networkFAB market = findViewById(R.id.market);

        //display book status
        TextView bookMarket = findViewById(R.id.bookMarket);
        TextView marketname = findViewById(R.id.marketname);
        TextView deletename = findViewById(R.id.deletename);
        bookMarket.setVisibility(View.VISIBLE);
        if (!meta.getMarket()) {
            if (meta.getRenting()) {
                bookMarket.setText("on rent");
                deleteBook.setVisibility(View.INVISIBLE);
                market.setVisibility(View.INVISIBLE);
                marketname.setVisibility(View.INVISIBLE);
                deletename.setVisibility(View.INVISIBLE);
            } else {
                bookMarket.setText("not on the market");
                deleteBook.setVisibility(View.VISIBLE);
                market.setVisibility(View.VISIBLE);
            }
        } else {
            bookMarket.setText("on the market");
            market.setVisibility(View.VISIBLE);
        }

        //display delete button if it is not on rent
        if (!meta.getRenting()) {
            deleteBook.setVisibility(View.VISIBLE);
        }

        //initialise on click listener
        View.OnClickListener deleteEvent = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //initialise firestore connection
                final FirebaseFirestore db = FirebaseFirestore.getInstance();
                final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                final String userID = user.getUid();
                //delete document
                db.collection("books").document(meta.getFirestoreID())
                        .delete()
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                //delete data from user document
                                DocumentReference docRef = db.collection("users").document(user.getUid());
                                docRef.update("scannedBooks", FieldValue.arrayRemove(meta.getFirestoreID()));
                                //go to home activity
                                Toast.makeText(getApplicationContext(), "deleted " + meta.getTitle(), Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(viewBookActivity.this, homeActivity.class);
                                startActivity(intent);
                                finish();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(getApplicationContext(), "failed to delete " + meta.getTitle(), Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        };
        deleteBook.setClickEvent(deleteEvent);

        //initialise on click listener
        View.OnClickListener clickEventMarket = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //identify book status
                if (!meta.getMarket()) {
                    if (!meta.getRenting()) {
                        //put on market
                        final FirebaseFirestore db = FirebaseFirestore.getInstance();
                        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                        final String userID = user.getUid();
                        db.collection("books").document(meta.getFirestoreID())
                                .update("market", true)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Toast.makeText(getApplicationContext(), "on the market", Toast.LENGTH_SHORT).show();
                                        Intent intent = new Intent(viewBookActivity.this, homeActivity.class);
                                        startActivity(intent);
                                        finish();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        //failed
                                        Toast.makeText(getApplicationContext(), "something went wrong", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                } else {
                    //take off market
                    final FirebaseFirestore db = FirebaseFirestore.getInstance();
                    final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    final String userID = user.getUid();
                    db.collection("books").document(meta.getFirestoreID())
                            .update("market", false)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Toast.makeText(getApplicationContext(), "off the market", Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(viewBookActivity.this, homeActivity.class);
                                    startActivity(intent);
                                    finish();
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    //failed
                                    Toast.makeText(getApplicationContext(), "something went wrong", Toast.LENGTH_SHORT).show();
                                }
                            });
                }
            }
        };
        market.setClickEvent(clickEventMarket);
    }

    //convert array to readable string
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
