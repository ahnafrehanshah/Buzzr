package com.example.buzzr;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.buzzr.HelperClasses.sharedPrefs;
import com.google.android.gms.common.SignInButton;

public class SplashScreen extends AppCompatActivity {

    private static int screenTime = 4000;

    Animation topAnimation, bottomAnimation, personBottomAnimation, circle1Animation, circle2Animation;

    TextView logo, logoBox;
    ImageView personIllustration;

    ImageView circle1, circle2;

    SharedPreferences onBoardingScreenSP;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.splash_screen);

        //Animations
        topAnimation = AnimationUtils.loadAnimation(this, R.anim.ss_titledown);
        bottomAnimation = AnimationUtils.loadAnimation(this, R.anim.ss_boxup);
        personBottomAnimation = AnimationUtils.loadAnimation(this, R.anim.ss_personup);
        circle1Animation = AnimationUtils.loadAnimation(this, R.anim.ss_circle1up);
        circle2Animation = AnimationUtils.loadAnimation(this, R.anim.ss_circle2up);

        //Hooks
        logo = findViewById(R.id.ssTitleTV);
        logoBox = findViewById(R.id.ssTitleBox);
        personIllustration = findViewById(R.id.ssPerson);
        circle1 = findViewById(R.id.ssCircle1);
        circle2 = findViewById(R.id.ssCircle2);

        //Animation Assignment
        logo.setAnimation(topAnimation);
        logoBox.setAnimation(bottomAnimation);
        personIllustration.setAnimation(personBottomAnimation);
        circle1.setAnimation(circle1Animation);
        circle2.setAnimation(circle2Animation);

        //Handler Process
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                sharedPrefs preference = new sharedPrefs(getApplicationContext());

                if (preference.getIsFirstTime()) {
                    Intent intent = new Intent(getApplicationContext(), OnBoardScreen.class);
                    startActivity(intent);
                    finish();
                } else if (preference.getIsLoggedIn()) {
                    Intent intent = new Intent(getApplicationContext(), MainDashboard.class);
                    startActivity(intent);
                    finish();
                } else if (preference.getIsLoggedOut()) {
                    Intent intent = new Intent(getApplicationContext(), LoginScreen.class);
                    startActivity(intent);
                    finish();
                } else {
                    Intent intent = new Intent(getApplicationContext(), SignUpScreen.class);
                    startActivity(intent);
                    finish();
                }

            }
        }, screenTime);
    }
}
