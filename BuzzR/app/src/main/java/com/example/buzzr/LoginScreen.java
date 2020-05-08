package com.example.buzzr;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.buzzr.HelperClasses.sharedPrefs;
import com.example.buzzr.HelperClasses.userHelperClass;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class LoginScreen extends AppCompatActivity {

    Animation imageFadeUp, textField1FadeUp, textField2FadeUp, headingFadeUp, subHeadingFadeUp, button1FadeUp, button2FadeUp;

    ImageView logInIllustration;
    TextView logInHeading, logInSubHeading;
    TextInputLayout logInUsernameInput, logInPasswordInput;
    Button logInBtn, createAccountBtn;

    FirebaseAuth firebaseAuth;

    FirebaseDatabase rootNode;
    DatabaseReference reference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_screen);

        //Animations
        headingFadeUp = AnimationUtils.loadAnimation(this, R.anim.su_li_headingup);
        subHeadingFadeUp = AnimationUtils.loadAnimation(this, R.anim.su_li_subheadingup);
        imageFadeUp = AnimationUtils.loadAnimation(this, R.anim.su_li_imageup);
        textField1FadeUp = AnimationUtils.loadAnimation(this, R.anim.su_li_textfield1up);
        textField2FadeUp = AnimationUtils.loadAnimation(this, R.anim.su_li_textfield2up);
        button1FadeUp = AnimationUtils.loadAnimation(this, R.anim.login_button1up);
        button2FadeUp = AnimationUtils.loadAnimation(this, R.anim.login_button2up);

        //Hooks
        logInHeading = findViewById(R.id.headingLogIn);
        logInSubHeading = findViewById(R.id.subHeadingLogIn);
        logInIllustration = findViewById(R.id.illustrationLogIn);
        logInUsernameInput = findViewById(R.id.logInUsernameInput);
        logInPasswordInput = findViewById(R.id.logInPasswordInput);
        logInBtn = findViewById(R.id.logInBtn);
        createAccountBtn = findViewById(R.id.createAccountBtn);

        //Animation Assignment
        logInHeading.setAnimation(headingFadeUp);
        logInSubHeading.setAnimation(subHeadingFadeUp);
        logInIllustration.setAnimation(imageFadeUp);
        logInUsernameInput.setAnimation(textField1FadeUp);
        logInPasswordInput.setAnimation(textField2FadeUp);
        logInBtn.setAnimation(button1FadeUp);
        createAccountBtn.setAnimation(button2FadeUp);

        //Authentication
        firebaseAuth = FirebaseAuth.getInstance();

        userHelperClass userData = new userHelperClass(getApplicationContext());

        //SetText
        logInUsernameInput.getEditText().setText(userData.getUsername());
    }

    private Boolean validateUsername() {
        String value = logInUsernameInput.getEditText().getText().toString();

        if (value.isEmpty()) {
            logInUsernameInput.setError("Field cannot be empty.");
            return false;
        } else {
            logInUsernameInput.setError(null);
            logInUsernameInput.setErrorEnabled(false);
            return true;
        }
    }

    private Boolean validatePassword() {
        String value = logInPasswordInput.getEditText().getText().toString();

        if (value.isEmpty()) {
            logInPasswordInput.setError("Field cannot be empty.");
            return false;
        } else {
            logInPasswordInput.setError(null);
            logInPasswordInput.setErrorEnabled(false);
            return true;
        }
    }

    public void authenticateUser(View view) {
        if (!validateUsername() || !validatePassword()) {
            return;
        } else {
            isUser();
        }
    }

    private void isUser() {
        final String userEnteredUsername = logInUsernameInput.getEditText().getText().toString().trim();
        final String userEnteredPassword = logInPasswordInput.getEditText().getText().toString().trim();

        rootNode = FirebaseDatabase.getInstance();
        reference = rootNode.getReference("users");

        Query checkUser = reference.orderByChild("username").equalTo(userEnteredUsername);

        checkUser.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String passwordFromDatabase = dataSnapshot.child(userEnteredUsername).child("password").getValue(String.class);

                    logInUsernameInput.setError(null);
                    logInUsernameInput.setErrorEnabled(false);

                    if (passwordFromDatabase.equals(userEnteredPassword)) {
                        logInUsernameInput.setError(null);
                        logInUsernameInput.setErrorEnabled(false);

                        String nameFromDB = dataSnapshot.child(userEnteredUsername).child("name").getValue(String.class);
                        String usernameFromDB = dataSnapshot.child(userEnteredUsername).child("username").getValue(String.class);
                        String phoneNoFromDB = dataSnapshot.child(userEnteredUsername).child("phoneNo").getValue(String.class);

                        //SharedPreferences : Storing user Info
                        userHelperClass helperClass = new userHelperClass(getApplicationContext());
                        helperClass.setName(nameFromDB);
                        helperClass.setUsername(usernameFromDB);
                        helperClass.setPassword(userEnteredPassword);
                        helperClass.setPhoneNo(phoneNoFromDB);

                        //SharedPreferences : Login Token
                        sharedPrefs preference = new sharedPrefs(getApplicationContext());
                        preference.setIsLoggedIn(true);

                        //Start Next Activity : Main Dashboard
                        startActivity(new Intent(getApplicationContext(), MainDashboard.class));
                        finish();
                    } else {
                        logInPasswordInput.setError("Wrong password entered.");
                        logInPasswordInput.requestFocus();
                    }
                } else {
                    logInUsernameInput.setError("No user found. Check username or create an account.");
                    logInUsernameInput.requestFocus();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void openSignUp(View view) {
        startActivity(new Intent(this, SignUpScreen.class));
        finish();
    }
}
