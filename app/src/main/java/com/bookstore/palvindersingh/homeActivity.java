package com.bookstore.palvindersingh;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;


public class homeActivity extends AppCompatActivity implements homeFragment.OnFragmentInteractionListener, booksFragment.OnFragmentInteractionListener, searchFragment.OnFragmentInteractionListener {

    //method to manage fragments for bottom nav
    private final BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            Fragment selectedFragment;
            switch (item.getItemId()) {
                case R.id.action_home:
                    FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                    selectedFragment = homeFragment.newInstance("home", "fragment");
                    transaction.replace(R.id.content, selectedFragment);
                    transaction.commit();
                    return true;
                case R.id.action_books:
                    FragmentTransaction transaction2 = getSupportFragmentManager().beginTransaction();
                    selectedFragment = booksFragment.newInstance("books", "fragment");
                    transaction2.replace(R.id.content, selectedFragment);
                    transaction2.commit();
                    return true;
                case R.id.action_search:
                    FragmentTransaction transaction3 = getSupportFragmentManager().beginTransaction();
                    selectedFragment = searchFragment.newInstance("search", "fragment");
                    transaction3.replace(R.id.content, selectedFragment);
                    transaction3.commit();
                    return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        //initialise toolbar
        Toolbar myToolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);


        //set up first fragment
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.content, homeFragment.newInstance("first", "fragment"));
        transaction.commit();

        //initialise bottom navigation
        BottomNavigationView navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        navigation.setSelectedItemId(R.id.action_books);
    }

    //initialise toolbar options
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.action_bar, menu);
        return true;
    }

    //initialise toolbar actions
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_logout:
                logoutUser();
                return true;

            case R.id.action_restart:
                Intent intent = new Intent(homeActivity.this, mainActivity.class);
                startActivity(intent);
                finish();

            default:
                return super.onOptionsItemSelected(item);

        }
    }

    //method to log current user out
    private void logoutUser() {
        AuthUI.getInstance()
                .signOut(this)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    public void onComplete(@NonNull Task<Void> task) {
                        Toast.makeText(getApplicationContext(), "logged out", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(homeActivity.this, mainActivity.class);
                        startActivity(intent);
                        finish();
                    }
                });
    }

    @Override
    public void onFragmentInteraction(Uri uri) {
    }
}
