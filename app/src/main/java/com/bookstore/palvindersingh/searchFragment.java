package com.bookstore.palvindersingh;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;


public class searchFragment extends Fragment {

    //initialise boilerplate attributes and methods
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private ArrayList<book> bookList = new ArrayList<>();
    private OnFragmentInteractionListener mListener;

    //constructor
    public searchFragment() {
    }

    public static searchFragment newInstance(String param1, String param2) {
        searchFragment fragment = new searchFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        Log.e("search", "fragment newInstance");
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            String mParam1 = getArguments().getString(ARG_PARAM1);
            String mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.e("books", "fragment newInstance");
        final View view = inflater.inflate(R.layout.fragment_search, container, false);
        //initialise ui components
        networkFAB searchFAB = view.findViewById(R.id.floating_action_button);
        final EditText searchInput = view.findViewById(R.id.search);

        //initialise on click event
        View.OnClickListener clickEvent = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //call get search results method
                getSearchResults(searchInput.getText().toString(), view);
            }
        };
        searchFAB.setClickEvent(clickEvent);

        return view;
    }

    //method to get and display search results
    private void getSearchResults(String input, final View view) {
        //initialise firestore connection
        final FirebaseFirestore db = FirebaseFirestore.getInstance();
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        //query all documents on the market with the input isbn
        db.collection("books").whereEqualTo("isbn", input).whereEqualTo("market", true).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    ArrayList<book> books = new ArrayList<>();
                    String userid = user.getUid();
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        book book = document.toObject(book.class);
                        book.setFirestoreID(document.getId());
                        if (!book.getOwnerReference().equals(userid)) {
                            //if the user does not own the book add it to an array
                            books.add(book);
                        }
                    }
                    //if no books are found
                    if (books.size() == 0) {
                        //add a boilerplate book class to indicate no books found
                        books.add(new book(null, null, null, null, "no books found", null, false, false, null));
                    }
                    if (books.get(0).getTitle() != "no books found") {
                        sortBooks(books, view);
                    }
                    TextView results = view.findViewById(R.id.results);
                    results.setText("RESULTS: " + books.size());
                }
            }
        });
    }

    private void sortBooks(final ArrayList<book> books, final View view) {
        final ArrayList ratings = new ArrayList();
        final FirebaseFirestore db = FirebaseFirestore.getInstance();
        for (book book : books) {
            String owner = book.getOwnerReference();
            final ArrayList data = new ArrayList();
            data.add(book);
            db.collection("users").document(owner).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            data.add(document.get("userRating"));
                            ratings.add(data);
                        }
                    } else {
                        data.add(0);
                        ratings.add(data);
                    }
                    if (ratings.size() == books.size()) {
                        int n = ratings.size();
                        for (int i = 1; i < n; i++) {
                            ArrayList temp = (ArrayList) ratings.get(i);
                            int j = i - 1;
                            ArrayList nextArray = (ArrayList) ratings.get(j);

                            Double tempRating = Double.valueOf(temp.get(1).toString());
                            Double nextRating = Double.valueOf(nextArray.get(1).toString());
                            while (j >= 0 && nextRating > tempRating) {
                                ratings.set(j + 1, ratings.get(j));
                                j = j - 1;
                            }
                            ratings.set(j + 1, temp);
                        }
                        ArrayList<book> sorted = new ArrayList<>();
                        for (int i = ratings.size() - 1; i > -1; i--) {
                            sorted.add((book) ((ArrayList) ratings.get(i)).get(0));
                        }
                        bookList = sorted;
                        initSearchBooksRecyclerView(view, bookList);
                    }
                }
            });
        }
    }

    //method to initialise recycle view
    private void initSearchBooksRecyclerView(View view, ArrayList<book> books) {
        RecyclerView recyclerView = view.findViewById(R.id.searchBooksView);
        searchBooksRecyclerViewAdapter adapter = new searchBooksRecyclerViewAdapter(getContext(), books);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
    }

    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }
}
