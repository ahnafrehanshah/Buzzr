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

import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class SignUpScreen extends AppCompatActivity {

    Animation imageFadeUp, textField1FadeUp, textField2FadeUp, textField3FadeUp, textField4FadeUp, headingFadeUp, subHeadingFadeUp, button1FadeUp, button2FadeUp;

    ImageView signUpIllustration;
    TextView signUpHeading, signUpSubHeading;
    TextInputLayout signUpNameInput, signUpUsernameInput, signUpPhoneNumInput, signUpPasswordInput;
    Button signUpBtn, alreadyMemberBtn;

    FirebaseDatabase rootNode;
    DatabaseReference reference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.signup_screen);

        //Animations
        headingFadeUp = AnimationUtils.loadAnimation(this, R.anim.su_li_headingup);
        subHeadingFadeUp = AnimationUtils.loadAnimation(this, R.anim.su_li_subheadingup);
        imageFadeUp = AnimationUtils.loadAnimation(this, R.anim.su_li_imageup);
        textField1FadeUp = AnimationUtils.loadAnimation(this, R.anim.su_li_textfield1up);
        textField2FadeUp = AnimationUtils.loadAnimation(this, R.anim.su_li_textfield2up);
        textField3FadeUp = AnimationUtils.loadAnimation(this, R.anim.signup_textfield3up);
        textField4FadeUp = AnimationUtils.loadAnimation(this, R.anim.signup_textfield4up);
        button1FadeUp = AnimationUtils.loadAnimation(this, R.anim.signup_button1up);
        button2FadeUp = AnimationUtils.loadAnimation(this, R.anim.signup_button2up);

        //Hooks
        signUpIllustration = findViewById(R.id.illustrationSignUp);
        signUpHeading = findViewById(R.id.headingSignUp);
        signUpSubHeading = findViewById(R.id.subHeadingSignUp);
        signUpNameInput = findViewById(R.id.signUpNameInput);
        signUpUsernameInput = findViewById(R.id.signUpUsernameInput);
        signUpPhoneNumInput = findViewById(R.id.signUpPhoneNumInput);
        signUpPasswordInput = findViewById(R.id.signUpPasswordInput);
        signUpBtn = findViewById(R.id.signUpBtn);
        alreadyMemberBtn = findViewById(R.id.alreadyMemberBtn);

        //Animation Assignment
        signUpHeading.setAnimation(headingFadeUp);
        signUpSubHeading.setAnimation(subHeadingFadeUp);
        signUpIllustration.setAnimation(imageFadeUp);
        signUpNameInput.setAnimation(textField1FadeUp);
        signUpUsernameInput.setAnimation(textField2FadeUp);
        signUpPhoneNumInput.setAnimation(textField3FadeUp);
        signUpPasswordInput.setAnimation(textField4FadeUp);
        signUpBtn.setAnimation(button1FadeUp);
        alreadyMemberBtn.setAnimation(button2FadeUp);
    }

    public void openLogIn(View view) {
        startActivity(new Intent(this, LoginScreen.class));
        finish();
    }

    private Boolean validateName() {
        String value = signUpNameInput.getEditText().getText().toString();

        if (value.isEmpty()) {
            signUpNameInput.setError("Field cannot be empty.");
            return false;
        } else {
            signUpNameInput.setError(null);
            signUpNameInput.setErrorEnabled(false);
            return true;
        }
    }

    private Boolean validateUsername() {
        String value = signUpUsernameInput.getEditText().getText().toString();
        boolean hasWhiteSpace = value.contains(" ");
        boolean hasUpperCase = value.equals(value.toLowerCase());

        String pattern = "^[a-zA-Z0-9_]*$";

        if (value.isEmpty()) {
            signUpUsernameInput.setError("Field cannot be empty.");
            return false;
        } else if (value.length() >= 15) {
            signUpUsernameInput.setError("Username too long.");
            return false;
        } else if (hasWhiteSpace) {
            signUpUsernameInput.setError("Username cannot contain white spaces.");
            return false;
        } else if (hasUpperCase == false) {
            signUpUsernameInput.setError("Username can only have lowercase characters.");
            return false;
        } else if (!value.matches(pattern)) {
            signUpUsernameInput.setError("Username can only have underscores.");
            return false;
        } else {
            signUpUsernameInput.setError(null);
            signUpUsernameInput.setErrorEnabled(false);
            return true;
        }
    }

    private Boolean validatePhoneNumber() {
        String value = signUpPhoneNumInput.getEditText().getText().toString();

        if (value.isEmpty()) {
            signUpPhoneNumInput.setError("Field cannot be empty.");
            return false;
        } else if (value.length() < 10 || value.length() > 10) {
            signUpPhoneNumInput.setError("Enter a valid phone number.");
            return false;
        } else {
            signUpPhoneNumInput.setError(null);
            signUpPhoneNumInput.setErrorEnabled(false);
            return true;
        }
    }

    private Boolean validatePassword() {
        String value = signUpPasswordInput.getEditText().getText().toString();
        String passwordReq = "^" + "(?=.*[a-zA-Z])" + "(?=.*[@#$%^&+=])" + "(?=\\S+$)" + ".{4,}" + "$";
        // String passwordNoSpace = "(?=\\S+$)";

        if (value.isEmpty()) {
            signUpPasswordInput.setError("Field cannot be empty.");
            return false;
        } else if (value.length() < 8) {
            signUpPasswordInput.setError("Password is too short. It should be at least 8 characters long.");
            return false;
        } else if (!value.matches(passwordReq)) {
            signUpPasswordInput.setError("Password is too weak. Try mixing lowercase, uppercase and special characters for your password.");
            return false;
        }
        // else if(!value.matches(passwordNoSpace)){
        //    signUpPasswordInput.setError("Password cannot have spaces.");
        //    return false;
        // }
        else {
            signUpPasswordInput.setError(null);
            signUpPasswordInput.setErrorEnabled(false);
            return true;
        }
    }

    public void createNewUser(View view) {
        rootNode = FirebaseDatabase.getInstance();
        reference = rootNode.getReference("users");

        //Validation
        if (!validateName() || !validateUsername() || !validatePhoneNumber() || !validatePassword()) {
            return;
        }

        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String userName = signUpNameInput.getEditText().getText().toString();
                String userUsername = signUpUsernameInput.getEditText().getText().toString();
                String userPhoneNumber = signUpPhoneNumInput.getEditText().getText().toString();
                String userPassword = signUpPasswordInput.getEditText().getText().toString();

                if (dataSnapshot.hasChild(userUsername)) {
                    signUpUsernameInput.setError("Username already taken.");
                } else {
                    // userHelperClass helperClass = new userHelperClass(userName, userUsername ,userPhoneNumber, userPassword);
                    // reference.child(userUsername).setValue(helperClass);

                    Intent intent = new Intent(getApplicationContext(), PhoneAuthenticationScreen.class);
                    intent.putExtra("name", userName);
                    intent.putExtra("username", userUsername);
                    intent.putExtra("phoneNo", userPhoneNumber);
                    intent.putExtra("password", userPassword);
                    startActivity(intent);
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
