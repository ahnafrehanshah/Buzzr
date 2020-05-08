package com.example.buzzr;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.example.buzzr.HelperClasses.sharedPrefs;
import com.example.buzzr.HelperClasses.userHelperClass;
import com.example.buzzr.R;
import com.google.firebase.auth.FirebaseAuth;

public class trial extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.trial_screen);
    }

    public void logOutUser(View view){
        sharedPrefs preference = new sharedPrefs(getApplicationContext());
        preference.setIsLoggedIn(false);
        preference.setIsLoggedOut(true);

        FirebaseAuth.getInstance().signOut();
        startActivity(new Intent(this, LoginScreen.class));
        finish();
    }
}
