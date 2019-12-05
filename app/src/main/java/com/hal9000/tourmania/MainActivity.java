package com.hal9000.tourmania;

import android.Manifest;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.navigation.NavigationView;
import com.hal9000.tourmania.database.AppDatabase;
import com.hal9000.tourmania.rest_api.RestClient;

import androidx.drawerlayout.widget.DrawerLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private String [] permissions = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.READ_EXTERNAL_STORAGE};
    private static final int REQUEST_PERMISSIONS_ID = 100;
    private static String LOGIN_TOKEN_KEY = "login_token";
    private static String USERNAME_KEY = "username";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_my_tours, R.id.nav_tour_guides,
                R.id.nav_join_tour, R.id.nav_share, R.id.nav_fav_tours)
                .setDrawerLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        navController.addOnDestinationChangedListener(new NavController.OnDestinationChangedListener() {
            @Override
            public void onDestinationChanged(@NonNull NavController controller, @NonNull NavDestination destination, @Nullable Bundle arguments) {
                //arguments.get
            }
        });

        navigationView.getMenu().findItem(R.id.sign_out).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                SharedPrefUtils.removeItem(getBaseContext(), getLoginTokenKey());
                SharedPrefUtils.removeItem(getBaseContext(), getUsernameKey());
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
                AppUtils.updateUserAccDrawer(MainActivity.this);
                Navigation.findNavController(MainActivity.this, R.id.nav_host_fragment).navigate(
                        MobileNavigationDirections.actionGlobalNavHome()
                );
                return false;
            }
        });

        ActivityCompat.requestPermissions(this, permissions, REQUEST_PERMISSIONS_ID);  // This call is asynchronous

        // Make sure KeyStore keys are generated, so that UI isn't stalled when they are needed later on in the app
        new Thread(new Runnable() {
            @Override
            public void run() {
                SharedPrefUtils.generateKeys(getBaseContext());
            }
        }).start();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
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
        AppUtils.updateUserAccDrawer(this);
    }

}
