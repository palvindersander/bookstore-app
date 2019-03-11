package com.bookstore.palvindersingh;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

public class noNetworkActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_no_network);

        //initialise ui components
        final SwipeRefreshLayout mySwipeRefreshLayout = findViewById(R.id.swipeLayout);
        //on page swipe listener
        mySwipeRefreshLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        //check for internet
                        if (checkInternet()) {
                            //go to main activity
                            Intent intent = new Intent(noNetworkActivity.this, mainActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            //prompt the user of no connection
                            Toast.makeText(getApplicationContext(), "no internet connection", Toast.LENGTH_SHORT).show();
                            mySwipeRefreshLayout.setRefreshing(false);
                        }
                    }
                }
        );
    }

    //method to check for internet
    private Boolean checkInternet() {
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }
}
