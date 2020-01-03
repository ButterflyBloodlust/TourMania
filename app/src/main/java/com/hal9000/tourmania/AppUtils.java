package com.hal9000.tourmania;

import android.app.Activity;
import android.content.Context;
import android.location.LocationManager;
import android.net.wifi.WifiManager;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.webkit.MimeTypeMap;
import android.widget.TextView;

import com.google.android.material.navigation.NavigationView;
import com.hal9000.tourmania.database.AppDatabase;
import com.hal9000.tourmania.database.FavouriteTourDAO;
import com.hal9000.tourmania.database.MyTourDAO;
import com.hal9000.tourmania.model.FavouriteTour;
import com.hal9000.tourmania.model.MyTour;
import com.hal9000.tourmania.model.Tour;
import com.hal9000.tourmania.model.TourTag;
import com.hal9000.tourmania.model.TourWaypoint;
import com.hal9000.tourmania.model.TourWithWpWithPaths;
import com.hal9000.tourmania.model.TourWpWithPicPaths;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Future;

import androidx.fragment.app.FragmentActivity;

public class AppUtils {

    public static int TOUR_TYPE_MY_TOUR = 1;
    public static int TOUR_TYPE_FAV_TOUR = 2;

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
        return SharedPrefUtils.getDecryptedString(context, MainActivity.getLoginTokenKey()) != null;
    }

    public static void updateUserAccDrawerItems(FragmentActivity fragmentActivity) {
        NavigationView navigationView = fragmentActivity.findViewById(R.id.nav_view);
        if (AppUtils.isUserLoggedIn(fragmentActivity.getBaseContext())) {
            navigationView.getMenu().findItem(R.id.nav_sign_in).setVisible(false);
            navigationView.getMenu().findItem(R.id.nav_sign_up).setVisible(false);
            navigationView.getMenu().findItem(R.id.sign_out).setVisible(true);
            navigationView.getMenu().findItem(R.id.nav_user_settings).setVisible(true);
        } else {
            navigationView.getMenu().findItem(R.id.nav_sign_in).setVisible(true);
            navigationView.getMenu().findItem(R.id.nav_sign_up).setVisible(true);
            navigationView.getMenu().findItem(R.id.sign_out).setVisible(false);
            navigationView.getMenu().findItem(R.id.nav_user_settings).setVisible(false);
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

    public static Future<?> saveToursToLocalDb(final List<TourWithWpWithPaths> tourWithWpWithPathsList, final Context context, final Integer saveAsType) {
        return AppDatabase.databaseWriteExecutor.submit(new Runnable() {
            @Override
            public void run() {
                try {
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

                    if (saveAsType == TOUR_TYPE_MY_TOUR) {
                        MyTourDAO myTourDAO = appDatabase.myTourDAO();
                        List<MyTour> myTours = new ArrayList<>(tourIds.length);
                        for (long tourId : tourIds)
                            myTours.add(new MyTour((int) tourId));
                        myTourDAO.insertMyTours(myTours);
                    } else if (saveAsType == TOUR_TYPE_FAV_TOUR) {
                        FavouriteTourDAO favouriteTourDAO = appDatabase.favouriteTourDAO();
                        List<FavouriteTour> favouriteTours = new ArrayList<>(tourIds.length);
                        for (long tourId : tourIds)
                            favouriteTours.add(new FavouriteTour((int) tourId));
                        favouriteTourDAO.insertFavouriteTours(favouriteTours);
                    }
                } catch (Exception e) {
                    if (BuildConfig.DEBUG)
                        e.printStackTrace();
                }
            }
        });
    }

    public static File saveImageFromBase64(final Context context, final String imageData, final String mimeType, String subDirName) {
        final byte[] imgBytesData = android.util.Base64.decode(imageData,
                android.util.Base64.DEFAULT);

        final File file;
        final FileOutputStream fileOutputStream;
        final String extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType);
        try {
            if (subDirName == null)
                file = File.createTempFile("image", "." + extension, context.getExternalCacheDir());
            else {
                String dirPath = context.getExternalCacheDir() + File.separator + subDirName;
                File projDir = new File(dirPath);
                if (!projDir.exists())
                    projDir.mkdirs();
                file = File.createTempFile("image", "." + extension, projDir);
            }

            fileOutputStream = new FileOutputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        final BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(
                fileOutputStream);
        try {
            bufferedOutputStream.write(imgBytesData);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } finally {
            try {
                bufferedOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return file;
    }

    public static void clearInternalFilesDir(Context context) {
        try {
            File dir = context.getFilesDir();
            if (dir != null && dir.isDirectory()) {
                deleteDir(dir, 0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void clearExternalCache(Context context) {
        try {
            File dir = context.getExternalCacheDir();
            if (dir != null && dir.isDirectory()) {
                deleteDir(dir, 0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** Pass deleteTop = -1 to keep top level directory. */
    public static boolean deleteDir(File dir, int deleteTop) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]), deleteTop + 1);
                if (!success) {
                    return false;
                }
            }
        }
        if (deleteTop == -1)
            return true;
        else
            return dir.delete();
    }

    public static boolean isWifiEnabled(final Context context) {
        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        return wifiManager.isWifiEnabled();
    }

    public static boolean isLocationEnabled(final Context context) {
        LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        boolean gps_enabled = false;
        boolean network_enabled = false;

        try {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch(Exception ex) {}

        try {
            network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch(Exception ex) {}
        return gps_enabled && network_enabled;
    }

    public static void setUsernameInNavDrawerHeader(NavigationView navigationView, FragmentActivity fragmentActivity, String username) {
        View headerView = navigationView.getHeaderView(0);
        TextView textView = headerView.findViewById(R.id.username_textView);
        textView.setText(String.format(fragmentActivity.getString(R.string.logged_in_as), username));
    }

    public static void unsetUsernameInNavDrawerHeader(NavigationView navigationView) {
        View headerView = navigationView.getHeaderView(0);
        TextView textView = headerView.findViewById(R.id.username_textView);
        textView.setText("");
    }
}
