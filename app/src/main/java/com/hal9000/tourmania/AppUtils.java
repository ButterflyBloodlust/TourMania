package com.hal9000.tourmania;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.webkit.MimeTypeMap;

import com.google.android.material.navigation.NavigationView;
import com.hal9000.tourmania.database.AppDatabase;
import com.hal9000.tourmania.model.Tour;
import com.hal9000.tourmania.model.TourTag;
import com.hal9000.tourmania.model.TourWaypoint;
import com.hal9000.tourmania.model.TourWithWpWithPaths;
import com.hal9000.tourmania.model.TourWpWithPicPaths;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Future;

import androidx.fragment.app.FragmentActivity;

public class AppUtils {

    public static void hideSoftKeyboard(FragmentActivity fragmentActivity) {
        InputMethodManager inputMethodManager = (InputMethodManager) fragmentActivity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        View currentFocus = fragmentActivity.getCurrentFocus();
        if (currentFocus != null)
            inputMethodManager.hideSoftInputFromWindow(currentFocus.getWindowToken(), 0);
    }

    public static void showSoftKeyboard(FragmentActivity fragmentActivity) {
        InputMethodManager inputMethodManager = (InputMethodManager) fragmentActivity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        View currentFocus = fragmentActivity.getCurrentFocus();
        if (currentFocus != null)
            inputMethodManager.showSoftInput(currentFocus, 0);
    }

    public static boolean isUserLoggedIn(Context context) {
        return SharedPrefUtils.getString(context, MainActivity.getLoginTokenKey()) != null;
    }

    public static void updateUserAccDrawer(FragmentActivity fragmentActivity) {
        NavigationView navigationView = fragmentActivity.findViewById(R.id.nav_view);
        if (AppUtils.isUserLoggedIn(fragmentActivity.getBaseContext())) {
            navigationView.getMenu().findItem(R.id.nav_sign_in).setVisible(false);
            navigationView.getMenu().findItem(R.id.nav_sign_up).setVisible(false);
            navigationView.getMenu().findItem(R.id.sign_out).setVisible(true);
        } else {
            navigationView.getMenu().findItem(R.id.nav_sign_in).setVisible(true);
            navigationView.getMenu().findItem(R.id.nav_sign_up).setVisible(true);
            navigationView.getMenu().findItem(R.id.sign_out).setVisible(false);
        }
    }

    public static String getMimeType(String url) {
        String type = null;
        String extension = MimeTypeMap.getFileExtensionFromUrl(url);
        if (extension != null) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        }
        return type;
    }

    public static Future<?> saveToursToLocalDb(final List<TourWithWpWithPaths> tourWithWpWithPathsList, final Context context) {
        return AppDatabase.databaseWriteExecutor.submit(new Runnable() {
            @Override
            public void run() {
                //Log.d("crashTest", "run()");
                AppDatabase appDatabase = AppDatabase.getInstance(context);
                ArrayList<Tour> tours = new ArrayList<>(tourWithWpWithPathsList.size());
                for (TourWithWpWithPaths tourWithWpWithPaths : tourWithWpWithPathsList) {
                    tours.add(tourWithWpWithPaths.tour);
                }
                long[] tourIds = appDatabase.tourDAO().insertTours(tours);

                LinkedList<TourTag> tourTags = new LinkedList<>();
                LinkedList<TourWaypoint> tourWaypoints = new LinkedList<>();
                for (int i = 0; i < tourWithWpWithPathsList.size(); i++) {
                    int tourId = (int) tourIds[i];
                    tourWithWpWithPathsList.get(i).tour.setTourId(tourId);
                    for (TourTag tourTag : tourWithWpWithPathsList.get(i).tourTags) {
                        tourTag.setTourId(tourId);
                        tourTags.add(tourTag);
                    }
                    for (TourWpWithPicPaths tourWpWithPicPaths : tourWithWpWithPathsList.get(i)._tourWpsWithPicPaths) {
                        tourWpWithPicPaths.tourWaypoint.setTourId(tourId);
                        tourWaypoints.add(tourWpWithPicPaths.tourWaypoint);
                    }
                }
                appDatabase.tourTagDAO().insertTourTags(tourTags);
                appDatabase.tourWaypointDAO().insertTourWps(tourWaypoints);
            }
        });
    }
}
