package com.bookstore.palvindersingh;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
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
import com.google.firebase.firestore.Transaction;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class addBookActivity extends AppCompatActivity {

    //book attribute
    book book;

    //convert array into a string with spaces in between each value
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_book);

        //initialise toolbar
        Toolbar myChildToolbar = findViewById(R.id.my_child_toolbar);
        myChildToolbar.setTitle("submit a book");
        setSupportActionBar(myChildToolbar);
        ActionBar ab = getSupportActionBar();
        assert ab != null;
        ab.setDisplayHomeAsUpEnabled(true);

        //initialise ui components
        final TextView isbnText = findViewById(R.id.isbn);
        final String extraData = getIntent().getStringExtra("isbn");
        isbnText.setText(extraData);

        //start async task to get metadata
        new setData().execute(extraData);

        //initialise wrong floating action button
        FloatingActionButton wrong = findViewById(R.id.wrong);
        //on click listener
        wrong.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //go to manual input activity
                Intent intent = new Intent(addBookActivity.this, addBookManualActivity.class);
                startActivity(intent);
                finish();
            }
        });

        //initialise correct floating action button
        networkFAB correct = findViewById(R.id.correct);
        //on click listener
        View.OnClickListener correctClick = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //initialise ui component
                TextView bookTitle = findViewById(R.id.bookTitle);
                //check if data has loaded
                if (bookTitle.getText().equals("title")) {
                    Toast.makeText(getApplicationContext(), "wait for data to load", Toast.LENGTH_SHORT).show();
                } else {
                    //add data to firestore
                    final FirebaseFirestore db = FirebaseFirestore.getInstance();
                    final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    book.setOwnerReference(user.getUid());
                    //transaction processing
                    db.runTransaction(new Transaction.Function<Void>() {
                        @Nullable
                        @Override
                        public Void apply(@NonNull Transaction transaction) {
                            //add book to collection
                            db.collection("books").add(book).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                @Override
                                public void onSuccess(DocumentReference documentReference) {
                                    //update user data
                                    DocumentReference docRef = db.collection("users").document(user.getUid());
                                    docRef.update("scannedBooks", FieldValue.arrayUnion(documentReference.getId()));
                                }
                            });
                            return null;
                        }
                    }).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            //go to home activity if successful
                            Toast.makeText(getApplicationContext(), "book added", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(addBookActivity.this, homeActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            //go to scan book activity if not successful
                            Toast.makeText(getApplicationContext(), "something unexpected occurred", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(addBookActivity.this, scanBookActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    });
                }
            }
        };
        //pass correctClick to correct class
        correct.setClickEvent(correctClick);
    }

    //define setData async task
    private class setData extends AsyncTask<String, Void, bookMetaData> {

        @Override
        protected bookMetaData doInBackground(String... extraData) {
            //run bookMetaData class
            return new bookMetaData(extraData[0]);
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(bookMetaData bookMetaData) {
            //get book object
            book meta = bookMetaData.getBook();
            //set book attribute to meta
            book = meta;
            //check a book object exists
            if (meta == null) {
                //go to home activity
                Toast.makeText(getApplicationContext(), "book does not exist", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(addBookActivity.this, homeActivity.class);
                startActivity(intent);
                finish();
                return;
            }
            //set ui components to visible and change according text if it exists
            TextView bookISBN = findViewById(R.id.isbn);
            bookISBN.setVisibility(View.VISIBLE);
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
                bookGenres.setText(arrayToDisplayString(meta.getGenres()));
                bookGenres.setVisibility(View.VISIBLE);
            }
            if (meta.getImage() != null) {
                ImageView bookImage = findViewById(R.id.bookImage);
                Picasso.get().load(meta.getImage()).into(bookImage);
                bookImage.setContentDescription(meta.getImage());
                bookImage.setVisibility(View.VISIBLE);
            }
        }
    }
}