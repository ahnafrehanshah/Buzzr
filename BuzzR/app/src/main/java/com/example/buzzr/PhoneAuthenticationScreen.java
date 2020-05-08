package com.example.buzzr;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.buzzr.HelperClasses.sharedPrefs;
import com.example.buzzr.HelperClasses.userHelperClass;
import com.example.buzzr.HelperClasses.userHelperClassFirebase;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskExecutors;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.concurrent.TimeUnit;

public class PhoneAuthenticationScreen extends AppCompatActivity {

    Animation imageFadeUp, textField1FadeUp, headingFadeUp, subHeadingFadeUp, button1FadeUp;

    ImageView phoneAuthIllustration;
    TextView phoneAuthHeading, phoneAuthSubHeading;
    TextInputLayout userOTPInput;
    Button verifyPhoneNumberBtn;

    String verificationCodeBySystem;
    String previousPhoneNo, previousName, previousUsername, previousPassword;

    FirebaseDatabase rootNode;
    DatabaseReference reference;

    public static final String SHARED_PREFS = "sharedPrefs";
    public static final String NAME = "name";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.phone_authentication);

        //Animations
        headingFadeUp = AnimationUtils.loadAnimation(this, R.anim.su_li_headingup);
        subHeadingFadeUp = AnimationUtils.loadAnimation(this, R.anim.su_li_subheadingup);
        imageFadeUp = AnimationUtils.loadAnimation(this, R.anim.su_li_imageup);
        textField1FadeUp = AnimationUtils.loadAnimation(this, R.anim.su_li_textfield1up);
        button1FadeUp = AnimationUtils.loadAnimation(this, R.anim.phoneauth_button1up);

        //Hooks
        phoneAuthHeading = findViewById(R.id.headingPhoneAuth);
        phoneAuthSubHeading = findViewById(R.id.subHeadingPhoneAuth);
        phoneAuthIllustration = findViewById(R.id.illustrationPhoneAuth);
        userOTPInput = findViewById(R.id.phoneAuthOTPInput);
        verifyPhoneNumberBtn = findViewById(R.id.phoneAuthVerifyBtn);

        //Animation Assignment
        phoneAuthHeading.setAnimation(headingFadeUp);
        phoneAuthSubHeading.setAnimation(subHeadingFadeUp);
        phoneAuthIllustration.setAnimation(imageFadeUp);
        userOTPInput.setAnimation(textField1FadeUp);
        verifyPhoneNumberBtn.setAnimation(button1FadeUp);

        previousName = getIntent().getStringExtra("name");
        previousUsername = getIntent().getStringExtra("username");
        previousPhoneNo = getIntent().getStringExtra("phoneNo");
        previousPassword = getIntent().getStringExtra("password");

        sendVerificationCode(previousPhoneNo);
    }

    private void sendVerificationCode(String phoneNo) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                "+91" + phoneNo,
                60,
                TimeUnit.SECONDS,
                TaskExecutors.MAIN_THREAD,
                mCallbacks);
    }

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        @Override
        public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
            super.onCodeSent(s, forceResendingToken);
            verificationCodeBySystem = s;
        }

        @Override
        public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
            String code = phoneAuthCredential.getSmsCode();
            if (code != null) {
                verifyCode(code);
            }
        }

        @Override
        public void onVerificationFailed(@NonNull FirebaseException e) {
            Toast.makeText(PhoneAuthenticationScreen.this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    };

    private void verifyCode(String verificationCodeByUser) {
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationCodeBySystem, verificationCodeByUser);
        signInTheUser(credential);
    }

    private void signInTheUser(PhoneAuthCredential credential) {
        userOTPInput.setError(null);
        userOTPInput.setErrorEnabled(false);
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(PhoneAuthenticationScreen.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            rootNode = FirebaseDatabase.getInstance();
                            reference = rootNode.getReference("users");

                            //SharedPreferences : Storing user Info in Firebase
                            userHelperClassFirebase helperClass = new userHelperClassFirebase(previousName, previousUsername, previousPhoneNo, previousPassword);
                            reference.child(previousUsername).setValue(helperClass);

                            //SharedPreferences : Storing user Info Locally
                            userHelperClass helperClass1 = new userHelperClass(getApplicationContext());
                            helperClass1.setName(previousName);
                            helperClass1.setUsername(previousUsername);
                            helperClass1.setPhoneNo(previousPhoneNo);
                            helperClass1.setPassword(previousPassword);

                            //SharedPreferences : Login Token
                            sharedPrefs preference = new sharedPrefs(getApplicationContext());
                            preference.setIsLoggedIn(true);

                            //Start next activity
                            Intent intent = new Intent(getApplicationContext(), MainDashboard.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                        } else {
                            userOTPInput.setError("Invalid OTP entered");
                            userOTPInput.requestFocus();
                        }
                    }
                });
    }

    public void verifyOTP(View view) {
        String code = userOTPInput.getEditText().getText().toString();

        if (code.isEmpty()) {
            userOTPInput.setError("Field cannot be empty");
            return;
        } else if (code.length() != 6) {
            userOTPInput.setError("OTP entered is not of 6 characters");
            return;
        }

        verifyCode(code);
    }
}
