package com.example.buzzr;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.buzzr.HelperClasses.userHelperClass;
import com.example.buzzr.HelperClasses.userHelperClassFirebase;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class ChangePasswordScreen extends AppCompatActivity {

    TextInputLayout oldPasswordEntered, newPasswordEntered;
    String oldPasswordEnteredStr, newPasswordEnteredStr;

    String localName, localUsername, localPhoneNo, localPassword;

    FirebaseDatabase rootNode;
    DatabaseReference reference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.change_password_screen);

        //Locally stored data
        userHelperClass userData = new userHelperClass(getApplicationContext());
        localName = userData.getName();
        localUsername = userData.getUsername();
        localPhoneNo = userData.getPhoneNo();
        localPassword = userData.getPassword();

        //Hooks
        oldPasswordEntered = findViewById(R.id.changePasswordOldPass);
        newPasswordEntered = findViewById(R.id.changePasswordNewPass);
    }

    private Boolean validateOldPassword() {
        String value = oldPasswordEntered.getEditText().getText().toString();

        if (value.isEmpty()) {
            oldPasswordEntered.setError("Field cannot be empty");
            return false;
        } else {
            oldPasswordEntered.setError(null);
            oldPasswordEntered.setErrorEnabled(false);
            return true;
        }
    }

    private Boolean validateNewPassword() {
        String value = newPasswordEntered.getEditText().getText().toString();
        String passwordReq = "^" + "(?=.*[a-zA-Z])" + "(?=.*[@#$%^&+=])" + "(?=\\S+$)" + ".{4,}" + "$";
        // String passwordNoSpace = "(?=\\S+$)";

        if (value.isEmpty()) {
            newPasswordEntered.setError("Field cannot be empty.");
            return false;
        } else if (value.length() < 8) {
            newPasswordEntered.setError("Password is too short. It should be at least 8 characters long.");
            return false;
        } else if (!value.matches(passwordReq)) {
            newPasswordEntered.setError("Password is too weak. Try mixing lowercase, uppercase and special characters for your password.");
            return false;
        }
        // else if(!value.matches(passwordNoSpace)){
        //    newPasswordEntered.setError("Password cannot have spaces.");
        //    return false;
        // }
        else {
            newPasswordEntered.setError(null);
            newPasswordEntered.setErrorEnabled(false);
            return true;
        }
    }

    public void changePassword(View view) {
        rootNode = FirebaseDatabase.getInstance();
        reference = rootNode.getReference("users");

        final String oldPass = oldPasswordEntered.getEditText().getText().toString();
        final String newPass = newPasswordEntered.getEditText().getText().toString();

        if (!validateOldPassword() || !validateNewPassword()) {
            return;
        }

        Query checkUser = reference.orderByChild("username").equalTo(localUsername);

        checkUser.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String passwordFromDatabase = dataSnapshot.child(localUsername).child("password").getValue(String.class);

                    if (passwordFromDatabase.equals(oldPass)) {
                        oldPasswordEntered.setError(null);
                        oldPasswordEntered.setErrorEnabled(false);

                        if (oldPass.equals(newPass)) {
                            newPasswordEntered.setError("New password cannot be same as old password.");
                        } else {
                            userHelperClassFirebase helperClass = new userHelperClassFirebase(localName, localUsername, localPhoneNo, newPass);
                            reference.child(localUsername).setValue(helperClass);

                            userHelperClass userData = new userHelperClass(getApplicationContext());
                            userData.setPassword(newPass);

                            Intent intent = new Intent(getApplicationContext(), UserProfileScreen.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivityForResult(intent, 2);
                        }
                    } else {
                        oldPasswordEntered.setError("Old password does not match.");
                        oldPasswordEntered.requestFocus();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    //OnBack Condition
    /*@Override
    public void onBackPressed() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(ChangePasswordScreen.this);
        builder.setMessage("Are you sure you want to go back?");
        builder.setCancelable(true);
        builder.setNegativeButton("No, go back.", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }*/
}
