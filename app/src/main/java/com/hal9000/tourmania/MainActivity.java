package com.hal9000.tourmania;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.hal9000.tourmania.database.AppDatabase;
import com.hal9000.tourmania.rest_api.RestClient;

import androidx.drawerlayout.widget.DrawerLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import static com.hal9000.tourmania.ui.user_settings.UserSettingsFragment.SHARE_LOCATION_KEY;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private String [] permissions = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.READ_EXTERNAL_STORAGE};
    private static final int REQUEST_PERMISSIONS_ID = 100;
    private static final String LOGIN_TOKEN_KEY = "login_token";
    private static final String USERNAME_KEY = "username";
    /**
     * The desired interval for location updates. Inexact. Updates may be more or less frequent.
     */
    private static final long UPDATE_INTERVAL = 2000; // Every 10 seconds.

    /**
     * The fastest rate for active location updates. Updates will never be more frequent
     * than this value, but they may be less frequent.
     */
    private static final long FASTEST_UPDATE_INTERVAL = 1000; // Every 5 seconds

    /**
     * The max time before batched results are delivered by location services. Results may be
     * delivered sooner than this interval.
     */
    private static final long MAX_WAIT_TIME = UPDATE_INTERVAL * 2;

    /**
     * Stores parameters for requests to the FusedLocationProviderApi.
     */
    private LocationRequest mLocationRequest;

    /**
     * Provides access to the Fused Location Provider API.
     */
    private FusedLocationProviderClient mFusedLocationClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        final NavigationView navigationView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_my_tours, R.id.nav_tour_guides,
                R.id.nav_join_tour, R.id.nav_user_settings, R.id.nav_fav_tours)
                .setDrawerLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        /*
        navController.addOnDestinationChangedListener(new NavController.OnDestinationChangedListener() {
            @Override
            public void onDestinationChanged(@NonNull NavController controller, @NonNull NavDestination destination, @Nullable Bundle arguments) {

            }
        });
        */
        if (AppUtils.isUserLoggedIn(getBaseContext()))
            AppUtils.setUsernameInNavDrawerHeader(navigationView, this,
                    SharedPrefUtils.getDecryptedString(getBaseContext(), MainActivity.getUsernameKey()));

        navigationView.getMenu().findItem(R.id.sign_out).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                removeLocationUpdates();
                Context context = getBaseContext();
                SharedPrefUtils.clearSettings(context);
                //SharedPrefUtils.removeItem(context, getLoginTokenKey());
                //SharedPrefUtils.removeItem(context, getUsernameKey());
                RestClient.clearAuth();
                AppDatabase.databaseWriteExecutor.submit(
                        new Runnable() {
                            @Override
                            public void run() {
                                Context context = getBaseContext();
                                AppDatabase.getInstance(context).clearAllTables();
                                AppUtils.clearExternalCache(context);
                            }
                        });
                DrawerLayout drawer = findViewById(R.id.drawer_layout);
                drawer.closeDrawer(GravityCompat.START);
                AppUtils.unsetUsernameInNavDrawerHeader(navigationView);
                AppUtils.updateUserAccDrawerItems(MainActivity.this);
                Navigation.findNavController(MainActivity.this, R.id.nav_host_fragment).navigate(
                        MobileNavigationDirections.actionGlobalNavHome()
                );
                return false;
            }
        });

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        createLocationRequest();
        ActivityCompat.requestPermissions(this, permissions, REQUEST_PERMISSIONS_ID);  // This call is asynchronous

        // Make sure KeyStore keys are generated, so that UI isn't stalled when they are needed later on in the app
        new Thread(new Runnable() {
            @Override
            public void run() {
                SharedPrefUtils.generateKeys(getBaseContext());
            }
        }).start();
    }

    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setMaxWaitTime(MAX_WAIT_TIME);
    }

    private PendingIntent getPendingIntent() {
        Intent intent = new Intent(this, LocationUpdatesBroadcastReceiver.class);
        intent.setAction(LocationUpdatesBroadcastReceiver.ACTION_PROCESS_UPDATES);
        return PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    public void requestLocationUpdates() {
        try {
            Log.d("crashTest", "Starting location updates");
            LocationServiceUtils.setRequestingLocationUpdates(this, true);
            LocationServiceUtils.sendNotification(getBaseContext(), "Starting...");
            mFusedLocationClient.requestLocationUpdates(mLocationRequest, getPendingIntent());
        } catch (SecurityException e) {
            //LocationServiceUtils.setRequestingLocationUpdates(this, false);
            e.printStackTrace();
        }
    }

    public void removeLocationUpdates() {
        Log.d("crashTest", "Removing location updates");
        LocationServiceUtils.setRequestingLocationUpdates(this, false);
        Task<Void> task = mFusedLocationClient.removeLocationUpdates(getPendingIntent());
        task.addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                LocationServiceUtils.removeNotification(getBaseContext());
                LocationUpdatesBroadcastReceiver.shutdown();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    public static String getLoginTokenKey() {
        return LOGIN_TOKEN_KEY;
    }

    public static String getUsernameKey() {
        return USERNAME_KEY;
    }

    @Override
    protected void onResume() {
        super.onResume();
        AppUtils.updateUserAccDrawerItems(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        //Log.d("crashTest", "onRequestPermissionResult");
        if (requestCode == REQUEST_PERMISSIONS_ID) {
            if (grantResults.length <= 0) {
                //Log.d("crashTest", "User interaction was cancelled.");
            } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //Log.d("crashTest", "PERMISSION_GRANTED : " + permissions[0]);
                if (PreferenceManager.getDefaultSharedPreferences(getBaseContext()).getBoolean(SHARE_LOCATION_KEY, false))
                    requestLocationUpdates();
            } else {
                // Permission denied.
                Snackbar.make(
                        findViewById(R.id.content_main),
                        "Location permission was denied. Consider changing.",
                        Snackbar.LENGTH_INDEFINITE)
                        .setAction("Change\npermissions", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                // Build intent that displays the App settings screen.
                                Intent intent = new Intent();
                                intent.setAction(
                                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                Uri uri = Uri.fromParts("package",
                                        BuildConfig.APPLICATION_ID, null);
                                intent.setData(uri);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                            }
                        })
                        .show();
            }
        }
    }
}
