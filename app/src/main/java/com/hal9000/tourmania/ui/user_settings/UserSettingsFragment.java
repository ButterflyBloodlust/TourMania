package com.hal9000.tourmania.ui.user_settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProviders;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import com.hal9000.tourmania.MainActivity;
import com.hal9000.tourmania.R;
import com.hal9000.tourmania.SharedPrefUtils;
import com.hal9000.tourmania.rest_api.RestClient;
import com.hal9000.tourmania.rest_api.tour_guides.TourGuidesService;
import com.hal9000.tourmania.rest_api.users.UsersService;

public class UserSettingsFragment extends PreferenceFragmentCompat {

    private UserSettingsModel userSettingsModel;
    private SharedPreferences.OnSharedPreferenceChangeListener sharedPreferenceChangeListener;

    public static final String TOUR_GUIDE_STATUS_KEY = "tour_guide_status";
    public static final String PHONE_NUM_KEY = "phone_num";
    public static final String SHARE_LOCATION_KEY = "share_location";
    public static final String REVOKE_LOCATION_TOKENS_KEY = "revoke_location_tokens";
    public static final String SHARE_LOCATION_TOKEN_TTL_KEY = "location_token_lifetime";

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        userSettingsModel = ViewModelProviders.of(this).get(UserSettingsModel.class);
        setPreferencesFromResource(R.xml.user_settings, rootKey);

        //Log.d("crashTest", "onCreatePreferences rootKey : " + rootKey);

        Preference preference = findPreference(REVOKE_LOCATION_TOKENS_KEY);
        preference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Log.d("crashTest", "onPreferenceClick (\"revoke_location_tokens\")");
                TourGuidesService client = RestClient.createService(TourGuidesService.class,
                        SharedPrefUtils.getDecryptedString(getContext(), MainActivity.getLoginTokenKey()));
                Call<Void> call = client.revokeTourGuideLocationSharingToken();
                call.enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        //Log.d("crashTest", "revokeTourGuideLocationSharingToken onResponse");
                        Context context = getContext();
                        if (context != null)
                            Toast.makeText(context,"Active location tokens revoked", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        Context context = getContext();
                        if (context != null)
                            Toast.makeText(context,"A connection error has occurred",Toast.LENGTH_SHORT).show();
                        //Log.d("crashTest", "revokeTourGuideLocationSharingToken onFailure");
                    }
                });
                return true;
            }
        });

        // OnSharedPreferenceChangeListener must not be local or anonymous,
        // since SharedPreferences listeners are stored in a weak hash map (hence they are susceptible to GC)
        sharedPreferenceChangeListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                Log.d("crashTest", "OnSharedPreferenceChangeListener onSharedPreferenceChanged()");
                //Log.d("crashTest", "onChanged key : " + key);
                Context context = getContext();
                if (context == null || !sharedPreferences.contains(key))
                    return;
                if (key.equals(TOUR_GUIDE_STATUS_KEY)) {
                    // Send preference to server
                    UsersService client = RestClient.createService(UsersService.class,
                            SharedPrefUtils.getDecryptedString(context, MainActivity.getLoginTokenKey()));
                    boolean isGuide = sharedPreferences.getBoolean(TOUR_GUIDE_STATUS_KEY, false);
                    Call<ResponseBody> call = client.updateUserPrefsIsGuide(isGuide);
                    call.enqueue(new Callback<ResponseBody>() {
                        @Override
                        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                            //Log.d("crashTest", "onSharedPreferenceChanged onResponse");
                        }

                        @Override
                        public void onFailure(Call<ResponseBody> call, Throwable t) {
                            Context context = getContext();
                            if (context != null)
                                Toast.makeText(context,"A connection error has occurred",Toast.LENGTH_SHORT).show();
                            //Log.d("crashTest", "onSharedPreferenceChanged onFailure");
                        }
                    });
                }//if (key.equals(TOUR_GUIDE_STATUS_KEY))
                else if (key.equals(PHONE_NUM_KEY)) {
                    // Send preference to server
                    UsersService client = RestClient.createService(UsersService.class,
                            SharedPrefUtils.getDecryptedString(getContext(), MainActivity.getLoginTokenKey()));
                    String phoneNum = sharedPreferences.getString(PHONE_NUM_KEY, "");
                    Call<ResponseBody> call = client.updateUserPrefsPhoneNum(phoneNum);
                    call.enqueue(new Callback<ResponseBody>() {
                        @Override
                        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                            //Log.d("crashTest", "onSharedPreferenceChanged onResponse");
                        }

                        @Override
                        public void onFailure(Call<ResponseBody> call, Throwable t) {
                            Context context = getContext();
                            if (context != null)
                                Toast.makeText(context,"A connection error has occurred",Toast.LENGTH_SHORT).show();
                            //Log.d("crashTest", "onSharedPreferenceChanged onFailure");
                        }
                    });
                }//else if (key.equals(PHONE_NUM_KEY))
                else if (key.equals(SHARE_LOCATION_KEY)) {
                    // Send preference to server
                    UsersService client = RestClient.createService(UsersService.class,
                            SharedPrefUtils.getDecryptedString(getContext(), MainActivity.getLoginTokenKey()));
                    boolean shareLocation = sharedPreferences.getBoolean(SHARE_LOCATION_KEY, false);
                    FragmentActivity fragmentActivity = requireActivity();
                    if (fragmentActivity instanceof MainActivity) {
                        if (shareLocation) {
                            ((MainActivity) fragmentActivity).requestLocationUpdates();
                        } else {
                            ((MainActivity) fragmentActivity).removeLocationUpdates();
                        }
                    }
                    Call<ResponseBody> call = client.updateUserPrefsLocationSharing(shareLocation);
                    call.enqueue(new Callback<ResponseBody>() {
                        @Override
                        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                            //Log.d("crashTest", "onSharedPreferenceChanged onResponse");
                        }

                        @Override
                        public void onFailure(Call<ResponseBody> call, Throwable t) {
                            Context context = getContext();
                            if (context != null)
                                Toast.makeText(context,"A connection error has occurred",Toast.LENGTH_SHORT).show();
                            //Log.d("crashTest", "onSharedPreferenceChanged onFailure");
                        }
                    });
                }//else if (key.equals(SHARE_LOCATION_KEY))
                else if (key.equals(SHARE_LOCATION_TOKEN_TTL_KEY)) {
                    // Send preference to server
                    UsersService client = RestClient.createService(UsersService.class,
                            SharedPrefUtils.getDecryptedString(getContext(), MainActivity.getLoginTokenKey()));
                    int shareLocTokenLifetime = sharedPreferences.getInt(SHARE_LOCATION_TOKEN_TTL_KEY, 1);
                    Call<ResponseBody> call = client.updateUserPrefsLocationSharingTokenLifetime(shareLocTokenLifetime);
                    call.enqueue(new Callback<ResponseBody>() {
                        @Override
                        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                            //Log.d("crashTest", "onSharedPreferenceChanged onResponse");
                        }

                        @Override
                        public void onFailure(Call<ResponseBody> call, Throwable t) {
                            Context context = getContext();
                            if (context != null)
                                Toast.makeText(context,"A connection error has occurred",Toast.LENGTH_SHORT).show();
                            //Log.d("crashTest", "onSharedPreferenceChanged onFailure");
                        }
                    });
                    Log.d("crashTest", SHARE_LOCATION_TOKEN_TTL_KEY);
                }
            }
        };
        SharedPreferences sharedPreferences = getPreferenceScreen().getSharedPreferences();
        sharedPreferences.registerOnSharedPreferenceChangeListener(sharedPreferenceChangeListener);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        SharedPreferences sharedPreferences = getPreferenceScreen().getSharedPreferences();
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(sharedPreferenceChangeListener);
    }

    @Override
    public void onDisplayPreferenceDialog(Preference preference)
    {
        DialogFragment dialogFragment = null;
        if (preference instanceof TimePreference)
        {
            dialogFragment = new TimePreferenceDialogFragmentCompat();
            Bundle bundle = new Bundle(1);
            bundle.putString("key", preference.getKey());
            dialogFragment.setArguments(bundle);
        }

        if (dialogFragment != null)
        {
            dialogFragment.setTargetFragment(this, 0);
            dialogFragment.show(this.getFragmentManager(), "androidx.preference.PreferenceFragment.DIALOG");
        }
        else
        {
            super.onDisplayPreferenceDialog(preference);
        }
    }
}