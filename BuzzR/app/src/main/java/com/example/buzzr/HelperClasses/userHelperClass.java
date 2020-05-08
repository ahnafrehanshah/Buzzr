package com.example.buzzr.HelperClasses;

import android.content.Context;
import android.content.SharedPreferences;

public class userHelperClass {
    Context context;
    SharedPreferences sharedPref;

    public userHelperClass(Context context) {
        this.context = context;
        sharedPref = context.getSharedPreferences("User Details", 0);
    }

    public String getName() {
        return sharedPref.getString("Name", "");
    }

    public void setName(String name) {
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("Name", name);
        editor.commit();
    }

    public String getUsername() {
        return sharedPref.getString("Username", "");
    }

    public void setUsername(String username) {
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("Username", username);
        editor.commit();
    }

    public String getPhoneNo() {
        return sharedPref.getString("Phone No", "");
    }

    public void setPhoneNo(String phoneNo) {
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("Phone No", phoneNo);
        editor.commit();
    }

    public String getPassword() {
        return sharedPref.getString("Password", "");
    }

    public void setPassword(String password) {
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("Password", password);
        editor.commit();
    }

    public String getProfilePhoto() {
        return sharedPref.getString("Profile Photo", "");
    }

    public void setProfilePhoto(String filePath) {
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("Profile Photo", filePath);
        editor.commit();
    }

    public String getDeviceName() {
        return sharedPref.getString("Device Name", "No devices found");
    }

    public void setDeviceName(String deviceName) {
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("Device Name", deviceName);
        editor.commit();
    }
}
