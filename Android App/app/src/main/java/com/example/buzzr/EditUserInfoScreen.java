package com.example.buzzr;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.buzzr.HelperClasses.sharedPrefs;
import com.example.buzzr.HelperClasses.userHelperClass;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import de.hdodenhof.circleimageview.CircleImageView;

public class EditUserInfoScreen extends AppCompatActivity {

    String localName, localUsername, localPhoneNumber, profilePath;

    TextInputLayout editInfoNameInput, editInfoUsernameInput, editInfoPhoneNumInput;
    Button validateBtn, changePhotoBtn;

    Uri imageUri;

    CircleImageView userProfilePhoto;

    FirebaseDatabase rootNode;
    FirebaseStorage firebaseStorage;
    StorageReference storageReference;
    DatabaseReference reference;

    private static final int PICK_IMAGE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_user_info);

        //Retrieving Local User data
        userHelperClass userData = new userHelperClass(getApplicationContext());
        localName = userData.getName();
        localUsername = userData.getUsername();
        localPhoneNumber = userData.getPhoneNo();
        profilePath = userData.getProfilePhoto();

        //Profile Photo
        //if (imageAvailable(profilePath)) {
        loadImageFromStorage(profilePath);
        //}

        //Hooks
        editInfoNameInput = findViewById(R.id.editInfoNameInput);
        editInfoUsernameInput = findViewById(R.id.editInfoUsernameInput);
        validateBtn = findViewById(R.id.updateValidationBtn);
        userProfilePhoto = (CircleImageView) findViewById(R.id.editInfoUserPhoto);

        //Setting text
        editInfoNameInput.getEditText().setText(localName);
        editInfoUsernameInput.getEditText().setText(localUsername);

        firebaseStorage = FirebaseStorage.getInstance();
        storageReference = firebaseStorage.getReference();
    }

    private Boolean validateName() {
        String value = editInfoNameInput.getEditText().getText().toString();

        if (value.isEmpty()) {
            editInfoNameInput.setError("Field cannot be empty.");
            return false;
        } else {
            editInfoNameInput.setError(null);
            editInfoNameInput.setErrorEnabled(false);
            return true;
        }
    }

    private Boolean validateUsername() {
        String value = editInfoUsernameInput.getEditText().getText().toString();
        boolean hasWhiteSpace = value.contains(" ");
        boolean hasUpperCase = value.equals(value.toLowerCase());

        if (value.isEmpty()) {
            editInfoUsernameInput.setError("Field cannot be empty.");
            return false;
        } else if (value.length() >= 15) {
            editInfoUsernameInput.setError("Username too long.");
            return false;
        } else if (hasWhiteSpace) {
            editInfoUsernameInput.setError("Username cannot contain white spaces.");
            return false;
        } else if (hasUpperCase == false) {
            editInfoUsernameInput.setError("Username can only have lowercase characters.");
            return false;
        } else {
            editInfoUsernameInput.setError(null);
            editInfoUsernameInput.setErrorEnabled(false);
            return true;
        }
    }

    public void validateUser(View view) {
        rootNode = FirebaseDatabase.getInstance();
        reference = rootNode.getReference("users");

        String userName = editInfoNameInput.getEditText().getText().toString();
        String userUsername = editInfoUsernameInput.getEditText().getText().toString();

        //Validation
        if (!validateName() || !validateUsername()) {
            return;
        }

        if (userName.equals(localName) && userUsername.equals(localUsername)) {
            Toast toast = Toast.makeText(getApplicationContext(), "No update", Toast.LENGTH_SHORT);
            toast.show();
            return;
        }

        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String userName = editInfoNameInput.getEditText().getText().toString();
                String userUsername = editInfoUsernameInput.getEditText().getText().toString();

                editInfoUsernameInput.setErrorEnabled(false);

                boolean userNameNotChanged = userUsername.equals(localUsername);
                boolean userNameTaken = dataSnapshot.hasChild(userUsername);

                if (!userNameNotChanged) {
                    if (userNameTaken) {
                        editInfoUsernameInput.setError("Username taken.");
                        return;
                    }
                }

                Intent intent = new Intent(getApplicationContext(), EditValidationScreen.class);
                intent.putExtra("name", userName);
                intent.putExtra("username", userUsername);
                startActivity(intent);
                finish();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void previousScreen(View view) {
//        startActivity(new Intent(this, UserProfileScreen.class));
        onBackPressed();
    }

    public void changePhoto(View view) {
        Intent gallery = new Intent();
        gallery.setType("image/*");
        gallery.setAction(Intent.ACTION_GET_CONTENT);

        startActivityForResult(Intent.createChooser(gallery, "Select Profile Photo"), PICK_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK && null != data) {
            imageUri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                userProfilePhoto.setImageBitmap(bitmap);

                sharedPrefs sharedPrefs = new sharedPrefs(getApplicationContext());
                sharedPrefs.setProfilePhotoToken(true);

                String filePath = saveToInternalStorage(bitmap);

                userHelperClass userData = new userHelperClass(getApplicationContext());
                userData.setProfilePhoto(filePath);

                uploadImage();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void uploadImage() {
        if (imageUri != null) {
            final userHelperClass userData = new userHelperClass(getApplicationContext());
            String photoName = userData.getUsername();

            StorageReference reference = storageReference.child("profilePhotos/" + photoName);

            reference.putFile(imageUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Toast.makeText(EditUserInfoScreen.this, "Photo uploaded", Toast.LENGTH_SHORT).show();
//                            startActivity(new Intent(getApplicationContext(), UserProfileScreen.class));
//                            Intent intent = new Intent(getApplicationContext(), UserProfileScreen.class);
//                            ActivityOptionsCompatOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(EditUserInfoScreen.this, userProfilePhoto, ViewCompat.getTransitionName(userProfilePhoto));
//                            startActivity(intent, options.toBundle());
                            finish();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot) {
                            Toast.makeText(EditUserInfoScreen.this, "Photo uploading now", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private String saveToInternalStorage(Bitmap bitmapImage) {
        ContextWrapper cw = new ContextWrapper(getApplicationContext());
        File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
        File myPath = new File(directory, localUsername + ".jpg");

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
        return directory.getAbsolutePath();
    }

    private void loadImageFromStorage(String path) {
        try {
            File f = new File(path, localUsername + ".jpg");
            if (f != null) {
                Bitmap b = BitmapFactory.decodeStream(new FileInputStream(f));
                CircleImageView img = (CircleImageView) findViewById(R.id.editInfoUserPhoto);
                img.setImageBitmap(b);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }

//    @Override
//    public void onBackPressed() {
//        super.onBackPressed();
//        startActivity(new Intent(this, UserProfileScreen.class));
//        finish();
//    }
}
