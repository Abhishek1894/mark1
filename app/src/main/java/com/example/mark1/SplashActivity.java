package com.example.mark1;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

class SplashThread extends Thread // thread to display splash screen
{
    SplashActivity activity;    // object reference of splash activity
    Intent intent;              // intent to pass from splash to login activity or home activity
    boolean isLoggedIn;        // user logged in or not
    FirebaseAuth auth = FirebaseAuth.getInstance();

    SplashThread(SplashActivity activity, boolean isLoggedIn) // constructor for thread
    {
        this.activity = activity;
        this.isLoggedIn = isLoggedIn;
    }

    public void run()
    {
        try
        {
            Thread.sleep(2000); // splash screen will appear for 2 seconds

            FirebaseUser user = auth.getCurrentUser(); // to get the current user of app


            if(user != null) // if user is already logged in, will be directed to home activity directly, else user has to log in
                intent = new Intent(activity, HomeActivity.class);
            else
                intent = new Intent(activity, OptionsActivity.class);

            activity.startActivity(intent); // this function actually changes activity
            activity.finish(); // finishes current activity
        }
        catch(InterruptedException e)
        {
            //
        }
    }
}


public class SplashActivity extends AppCompatActivity
{
    // used to store data related to user such as name phoneNo and if user is logged in or not etc
    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getSupportActionBar().hide(); // to remove app bar for splash activity

        sharedPreferences = getSharedPreferences("userInfo",MODE_PRIVATE);
        boolean isLoggedIn = sharedPreferences.getBoolean("isLoggedIn",false); // returns whether user is logged in or not

        SplashThread mySplash = new SplashThread(this, isLoggedIn);
        mySplash.start();
    }
}