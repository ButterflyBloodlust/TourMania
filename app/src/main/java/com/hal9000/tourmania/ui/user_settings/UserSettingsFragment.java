package com.hal9000.tourmania.ui.user_settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import androidx.lifecycle.ViewModelProviders;
import androidx.preference.PreferenceFragmentCompat;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import com.hal9000.tourmania.MainActivity;
import com.hal9000.tourmania.R;
import com.hal9000.tourmania.SharedPrefUtils;
import com.hal9000.tourmania.rest_api.RestClient;
import com.hal9000.tourmania.rest_api.users.UsersService;

public class UserSettingsFragment extends PreferenceFragmentCompat {

    private UserSettingsModel userSettingsModel;
    SharedPreferences.OnSharedPreferenceChangeListener sharedPreferenceChangeListener;

    public static final String TOUR_GUIDE_STATUS_KEY = "tour_guide_status";
    public static final String PHONE_NUM_KEY = "phone_num";
    public static final String SHARE_LOCATION_KEY = "share_location";

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        userSettingsModel = ViewModelProviders.of(this).get(UserSettingsModel.class);
        setPreferencesFromResource(R.xml.user_settings, rootKey);

        //Log.d("crashTest", "onCreatePreferences rootKey : " + rootKey);

        // OnSharedPreferenceChangeListener must not be local or anonymous,
        // since SharedPreferences listeners are stored in a weak hash map (hence they are susceptible to GC)
        sharedPreferenceChangeListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
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
            }
        };
        SharedPreferences sharedPreferences = getPreferenceScreen().getSharedPreferences();
        sharedPreferences.registerOnSharedPreferenceChangeListener(sharedPreferenceChangeListener);
    }
}