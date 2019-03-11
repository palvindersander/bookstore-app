package com.bookstore.palvindersingh;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Transaction;

import java.util.ArrayList;

public class addBookManualActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_book_manual);


        //initialise toolbar
        Toolbar myChildToolbar = findViewById(R.id.my_child_toolbar);
        myChildToolbar.setTitle("submit a book");
        setSupportActionBar(myChildToolbar);
        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);

        //initialise correct floating action button
        networkFAB correct = findViewById(R.id.correct);
        //on click listener
        View.OnClickListener correctClick = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //initialise ui components
                EditText isbn = findViewById(R.id.isbn);
                EditText title = findViewById(R.id.title);
                EditText author = findViewById(R.id.author);
                EditText genre = findViewById(R.id.genre);
                //check if input data is valid
                if (validateInputs(isbn.getText().toString(), title.getText().toString(), author.getText().toString(), genre.getText().toString())) {
                    //add data to firestore
                    final FirebaseFirestore db = FirebaseFirestore.getInstance();
                    final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    ArrayList<String> authorList = new ArrayList<>();
                    authorList.add(author.getText().toString());
                    ArrayList<String> genreList = new ArrayList<>();
                    genreList.add(genre.getText().toString());
                    //create book class
                    final book book = new book(authorList, genreList, null, isbn.getText().toString(), title.getText().toString(), user.getUid(), false, false, null);
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
                            Intent intent = new Intent(addBookManualActivity.this, homeActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            //go to scan activity if not successful
                            Toast.makeText(getApplicationContext(), "something unexpected occurred", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(addBookManualActivity.this, scanBookActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    });
                } else {
                    //prompt user to input valid data
                    Toast.makeText(getApplicationContext(), "enter valid data", Toast.LENGTH_SHORT).show();
                }
            }
        };
        //pass correctClick into correct class
        correct.setClickEvent(correctClick);
    }
    //validate user inputs
    private Boolean validateInputs(String isbn, String title, String author, String genre) {
        //check if isbn is valid
        if (!(isbn.length() == 10 || isbn.length() == 13)) {
            return false;
        }
        //check if other data lengths are appropriate
        return title.length() >= 3 && author.length() >= 3 && genre.length() >= 3;
    }
}
