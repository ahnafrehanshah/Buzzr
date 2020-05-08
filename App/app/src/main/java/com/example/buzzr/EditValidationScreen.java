package com.example.buzzr;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.buzzr.HelperClasses.userHelperClass;
import com.example.buzzr.HelperClasses.userHelperClassFirebase;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class EditValidationScreen extends AppCompatActivity {

    TextInputLayout editVerifyPasswordInput;

    String previousName, previousUsername;

    String localUsername, localPassword, localPhoneNumber;

    FirebaseDatabase rootNode;
    FirebaseStorage firebaseStorage;
    StorageReference storageReference;
    DatabaseReference reference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_validation_screen);

        //Hooks
        editVerifyPasswordInput = findViewById(R.id.editVerifyPassword);

        //Local data
        userHelperClass userData = new userHelperClass(getApplicationContext());
        localUsername = userData.getUsername();
        localPassword = userData.getPassword();
        localPhoneNumber = userData.getPhoneNo();

        //Data from edit user Activity
        previousName = getIntent().getStringExtra("name");
        previousUsername = getIntent().getStringExtra("username");

        //Firebase Storage
        firebaseStorage = FirebaseStorage.getInstance();
        storageReference = firebaseStorage.getReference("profilePhotos");
    }

    public void verifyUser(View view) {
        String code = editVerifyPasswordInput.getEditText().getText().toString();

        editVerifyPasswordInput.setError(null);
        editVerifyPasswordInput.setErrorEnabled(false);

        if (code.isEmpty()) {
            editVerifyPasswordInput.setError("Field cannot be empty");
            return;
        }

        boolean sameUsername = previousUsername.equals(localUsername);
        boolean passwordMatch = code.equals(localPassword);

        rootNode = FirebaseDatabase.getInstance();
        reference = rootNode.getReference("users");

        if (passwordMatch) {
            if (sameUsername) {
                Log.d("Old Password", localPassword);

                //SharedPreferences : Storing user Info in Firebase
                userHelperClassFirebase helperClass = new userHelperClassFirebase(previousName, previousUsername, localPhoneNumber, localPassword);
                reference.child(previousUsername).setValue(helperClass);

                //SharedPreferences : Storing user Info Locally
                userHelperClass helperClass1 = new userHelperClass(getApplicationContext());
                helperClass1.setName(previousName);
            } else {
                //SharedPreferences : Storing user Info in Firebase
                userHelperClassFirebase helperClass = new userHelperClassFirebase(previousName, previousUsername, localPhoneNumber, localPassword);
                reference.child(previousUsername).setValue(helperClass);
                reference.child(localUsername).removeValue();

                //SharedPreferences : Storing user Info Locally
                userHelperClass helperClass1 = new userHelperClass(getApplicationContext());
                helperClass1.setName(previousName);
                helperClass1.setUsername(previousUsername);

                String filePath = helperClass1.getProfilePhoto();

                getPhoto(filePath);
            }
        } else {
            System.out.println("Old Password " + localPassword);
            System.out.println("Old Password " + localPhoneNumber);
            editVerifyPasswordInput.setError("Wrong Password Entered");
            return;
        }

        Intent intent = new Intent(getApplicationContext(), UserProfileScreen.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivityForResult(intent, 2);
//        finish();
    }

    private void getPhoto(String path) {
        try {
            File f = new File(path, localUsername + ".jpg");
            Bitmap b = BitmapFactory.decodeStream(new FileInputStream(f));
            uploadPhoto(b);
            saveToInternalStorage(b);
        } catch (FileNotFoundException e) {
        }
    }

    private void uploadPhoto(Bitmap bitmap) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference("profilePhotos");

        StorageReference photoRef = storageRef.child(previousUsername);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 20, baos);
        byte[] data = baos.toByteArray();
        UploadTask uploadTask = photoRef.putBytes(data);

        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle unsuccessful uploads
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

            }
        });

        StorageReference oldPhotoRef = storageRef.child(localUsername);
        oldPhotoRef.delete();
    }

    private void saveToInternalStorage(Bitmap bitmapImage) {
        ContextWrapper cw = new ContextWrapper(getApplicationContext());
        File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
        File myPath = new File(directory, previousUsername + ".jpg");

        File oldFile = new File(directory, localUsername + ".jpg");
        oldFile.delete();

        FileOutputStream fos = null;

        try {
            fos = new FileOutputStream(myPath);
            bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, fos);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        String filePath = directory.getAbsolutePath();

        userHelperClass userData = new userHelperClass(getApplicationContext());
        userData.setProfilePhoto(filePath);
    }
}
