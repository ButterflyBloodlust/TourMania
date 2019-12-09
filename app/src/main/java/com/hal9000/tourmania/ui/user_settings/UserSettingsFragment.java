package com.hal9000.tourmania.ui.user_settings;

import android.os.Bundle;

import androidx.lifecycle.ViewModelProviders;
import androidx.preference.PreferenceFragmentCompat;

import com.hal9000.tourmania.R;

public class UserSettingsFragment extends PreferenceFragmentCompat {

    private UserSettingsModel userSettingsModel;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        userSettingsModel = ViewModelProviders.of(this).get(UserSettingsModel.class);
        setPreferencesFromResource(R.xml.user_settings, rootKey);
    }
}