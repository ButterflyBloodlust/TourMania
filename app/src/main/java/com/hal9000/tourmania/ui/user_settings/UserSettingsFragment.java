package com.hal9000.tourmania.ui.user_settings;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.core.graphics.drawable.DrawableCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProviders;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import com.hal9000.tourmania.AppUtils;
import com.hal9000.tourmania.FileUtil;
import com.hal9000.tourmania.MainActivity;
import com.hal9000.tourmania.R;
import com.hal9000.tourmania.SharedPrefUtils;
import com.hal9000.tourmania.rest_api.RestClient;
import com.hal9000.tourmania.rest_api.files_upload_download.FileDownloadImageObj;
import com.hal9000.tourmania.rest_api.files_upload_download.FileUploadDownloadService;
import com.hal9000.tourmania.rest_api.files_upload_download.TourFileDownloadResponse;
import com.hal9000.tourmania.rest_api.tour_guides.TourGuidesService;
import com.hal9000.tourmania.rest_api.users.UsersService;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.File;

public class UserSettingsFragment extends PreferenceFragmentCompat {

    private UserSettingsModel userSettingsModel;
    private SharedPreferences.OnSharedPreferenceChangeListener sharedPreferenceChangeListener;
    // Target is by default a weak reference, which isn't desired when using it to only to load drawable from path
    private Target picassoTarget;

    public static final String TOUR_GUIDE_STATUS_KEY = "tour_guide_status";
    public static final String PHONE_NUM_KEY = "phone_num";
    public static final String TOUR_GUIDE_IMAGE_KEY = "tour_guide_image";
    public static final String SHARE_LOCATION_KEY = "share_location";
    public static final String REVOKE_LOCATION_TOKENS_KEY = "revoke_location_tokens";
    public static final String SHARE_LOCATION_TOKEN_TTL_KEY = "location_token_lifetime";
    private static final String USERS_CACHE_DIR_NAME = "Users";
    private static int PICK_IMAGE_REQUEST_CODE = 100;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        userSettingsModel = ViewModelProviders.of(this).get(UserSettingsModel.class);
        setPreferencesFromResource(R.xml.user_settings, rootKey);

        //Log.d("crashTest", "onCreatePreferences rootKey : " + rootKey);

        ClickableIconPreference tourGuideImagePreference = findPreference(TOUR_GUIDE_IMAGE_KEY);
        SharedPreferences prefs = getPreferenceManager().getSharedPreferences();
        String filePath = prefs.getString(TOUR_GUIDE_IMAGE_KEY, null);
        if (filePath != null) {
            loadImageIntoClickableIconPref(filePath);
        } else {
            // Get tour guide image from server
            FileUploadDownloadService client = RestClient.createService(FileUploadDownloadService.class,
                    SharedPrefUtils.getDecryptedString(requireContext(), MainActivity.getLoginTokenKey()));
            Call<FileDownloadImageObj> call = client.getTourGuideImage();
            call.enqueue(new Callback<FileDownloadImageObj>() {
                @Override
                public void onResponse(Call<FileDownloadImageObj> call, Response<FileDownloadImageObj> response) {
                    //Log.d("crashTest", "getTourGuideImage onResponse");
                    if (response.isSuccessful()) {
                        final FileDownloadImageObj fileDownloadImageObj = response.body();
                        if (fileDownloadImageObj != null) {
                            File file = AppUtils.saveImageFromBase64(requireContext(), fileDownloadImageObj.base64,
                                    fileDownloadImageObj.mime, USERS_CACHE_DIR_NAME);
                            String filePath = file.toURI().toString();
                            loadImageIntoClickableIconPref(filePath);
                            SharedPreferences prefs = getPreferenceManager().getSharedPreferences();
                            prefs.edit().putString(TOUR_GUIDE_IMAGE_KEY, filePath).apply();
                        }
                    }
                }

                @Override
                public void onFailure(Call<FileDownloadImageObj> call, Throwable t) {
                    t.printStackTrace();
                    //Log.d("crashTest", "getTourGuideImage onFailure");
                }
            });

        }
        tourGuideImagePreference.setOnIconClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences prefs = getPreferenceManager().getSharedPreferences();
                String filePath = prefs.getString(TOUR_GUIDE_IMAGE_KEY, null);
                if (filePath != null) {
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_VIEW);
                    intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    intent.setDataAndType(Uri.parse(filePath), AppUtils.getMimeType(filePath));
                    startActivity(intent);
                }
            }
        });
        tourGuideImagePreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select image"), PICK_IMAGE_REQUEST_CODE);
                return true;
            }
        });

        Preference revokeLocationTokensPreference = findPreference(REVOKE_LOCATION_TOKENS_KEY);
        revokeLocationTokensPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
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
        if (preference instanceof NumberPickerPreference)
        {
            dialogFragment = new NumberPickerPreferenceDialogFragmentCompat();
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == PICK_IMAGE_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            // Save file path to shared prefs
            String filePath = data.getData().toString();
            SharedPreferences prefs = getPreferenceManager().getSharedPreferences();
            prefs.edit().putString(TOUR_GUIDE_IMAGE_KEY, filePath).apply();

            // Get drawable from path and load it into preference icon
            loadImageIntoClickableIconPref(filePath);

            // Send image to server
            FileUploadDownloadService service = RestClient.createService(FileUploadDownloadService.class,
                    SharedPrefUtils.getDecryptedString(requireContext(), MainActivity.getLoginTokenKey()));
            File imgFile = FileUtil.compressImage(requireContext(), filePath, USERS_CACHE_DIR_NAME);
            Call<ResponseBody> call = service.uploadTourGuideImageFile(RestClient.prepareFilePart("tg_img", imgFile));
            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    //Log.d("crashTest", "uploadTourGuideImageFile.onResponse()");
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    t.printStackTrace();
                    //Log.d("crashTest", "uploadTourGuideImageFile.onFailure()");
                    Context context = getContext();
                    if (context != null)
                        Toast.makeText(context,"A connection error has occurred",Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void loadImageIntoClickableIconPref(String filePath) {
        picassoTarget = new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                Drawable drawable = new BitmapDrawable(getResources(), bitmap);
                ClickableIconPreference tourGuideImagePreference = findPreference(TOUR_GUIDE_IMAGE_KEY);
                if (tourGuideImagePreference != null)
                    tourGuideImagePreference.setIcon(drawable);
                picassoTarget = null;
            }

            @Override
            public void onBitmapFailed(Exception e, Drawable errorDrawable) {
                picassoTarget = null;
            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {}
        };
        Picasso.get().load(filePath).into(picassoTarget);
    }
}