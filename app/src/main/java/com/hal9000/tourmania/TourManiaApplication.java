package com.hal9000.tourmania;

import android.app.Application;

import com.mapbox.mapboxsdk.Mapbox;

import androidx.appcompat.app.AppCompatDelegate;

public class TourManiaApplication extends Application {
    public void onCreate() {
        super.onCreate();

        // Setting night mode as default in DayNight theme
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);

        // Mapbox Access token
        Mapbox.getInstance(getApplicationContext(), getString(R.string.mapbox_access_token));
    }
}
