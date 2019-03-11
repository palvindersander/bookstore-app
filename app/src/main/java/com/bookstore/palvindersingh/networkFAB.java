package com.bookstore.palvindersingh;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.design.widget.FloatingActionButton;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Toast;

//class to inherit form floating action button and implement an on click listener interface in order to automate internet checking
public class networkFAB extends FloatingActionButton implements View.OnClickListener {

    //initialise attributes
    OnClickListener clickEvent;

    //initialise parent constructors
    public networkFAB(Context context) {
        super(context);
        init();
    }

    public networkFAB(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public networkFAB(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    //method to set an on click listener
    private void init() {
        setOnClickListener(this);
    }

    //setter
    public void setClickEvent(OnClickListener l) {
        this.clickEvent = l;
    }

    //method to check for internet
    private Boolean checkInternet(Context c) {
        ConnectivityManager connMgr = (ConnectivityManager) c.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

    //override method for on click event
    @Override
    public void onClick(View v) {
        Context c = super.getContext();
        //if there is internet
        if (checkInternet(c)) {
            //perform clickEvent instructions
            clickEvent.onClick(v);
        } else {
            //prompt the user that there is no internet available
            Toast.makeText(c.getApplicationContext(), "no internet connection", Toast.LENGTH_SHORT).show();
        }
    }
}
