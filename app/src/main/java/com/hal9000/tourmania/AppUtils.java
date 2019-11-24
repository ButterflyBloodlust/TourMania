package com.hal9000.tourmania;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.google.android.material.navigation.NavigationView;

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

}
