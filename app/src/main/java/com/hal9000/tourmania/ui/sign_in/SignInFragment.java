package com.hal9000.tourmania.ui.sign_in;

import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProviders;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.internal.EverythingIsNonNull;

import android.preference.PreferenceManager;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;
import com.google.android.material.textfield.TextInputEditText;
import com.hal9000.tourmania.AppUtils;
import com.hal9000.tourmania.MainActivity;
import com.hal9000.tourmania.R;
import com.hal9000.tourmania.SharedPrefUtils;
import com.hal9000.tourmania.database.AppDatabase;
import com.hal9000.tourmania.model.User;
import com.hal9000.tourmania.rest_api.login.LoginResponse;
import com.hal9000.tourmania.rest_api.RestClient;
import com.hal9000.tourmania.rest_api.login.LoginResponsePrefs;
import com.hal9000.tourmania.rest_api.login.LoginResponseSubTo;
import com.hal9000.tourmania.rest_api.login.UserLogin;

import static com.hal9000.tourmania.ui.join_tour.JoinTourFragment.LOCATION_SHARING_TOKEN_KEY;
import static com.hal9000.tourmania.ui.join_tour.JoinTourFragment.LOCATION_SHARING_TOKEN_TOUR_ID_KEY;
import static com.hal9000.tourmania.ui.user_settings.UserSettingsFragment.PHONE_NUM_KEY;
import static com.hal9000.tourmania.ui.user_settings.UserSettingsFragment.SHARE_LOCATION_KEY;
import static com.hal9000.tourmania.ui.user_settings.UserSettingsFragment.SHARE_LOCATION_TOKEN_TTL_KEY;
import static com.hal9000.tourmania.ui.user_settings.UserSettingsFragment.TOUR_GUIDE_STATUS_KEY;

public class SignInFragment extends Fragment {

    private SignInViewModel mViewModel;

    public static SignInFragment newInstance() {
        return new SignInFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_sign_in, container, false);

        Button singInButton = root.findViewById(R.id.button_sign_in);
        singInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UserLogin client = RestClient.createService(UserLogin.class);
                View mainView = requireView();
                final Editable username = ((TextInputEditText) mainView.findViewById(R.id.text_input_edit_text_login)).getText();
                Editable passwd = ((TextInputEditText) mainView.findViewById(R.id.text_input_edit_text_password)).getText();
                if (username != null && passwd != null) {
                    Call<LoginResponse> call = client.login(username.toString(), passwd.toString());
                    call.enqueue(new Callback<LoginResponse>() {
                        @Override
                        @EverythingIsNonNull
                        public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                Context ctx = requireContext();
                                FragmentActivity fragmentActivity = requireActivity();
                                LoginResponse loginResponse = response.body();
                                SharedPrefUtils.putEncryptedString(ctx, MainActivity.getLoginTokenKey(), loginResponse.data.token);
                                SharedPrefUtils.putEncryptedString(ctx, MainActivity.getUsernameKey(), username.toString());
                                LoginResponsePrefs loginResponsePrefs = loginResponse.data.loginResponsePrefs;
                                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
                                SharedPreferences.Editor editor = prefs.edit();
                                if (loginResponsePrefs != null) {
                                    if (loginResponsePrefs.isGuide != null)
                                        editor.putBoolean(TOUR_GUIDE_STATUS_KEY, loginResponsePrefs.isGuide);
                                    if (loginResponsePrefs.phoneNum != null)
                                        editor.putString(PHONE_NUM_KEY, loginResponsePrefs.phoneNum);
                                    if (loginResponsePrefs.shareLoc != null) {
                                        editor.putBoolean(SHARE_LOCATION_KEY, loginResponsePrefs.shareLoc);
                                        if (fragmentActivity instanceof MainActivity) {
                                            if (loginResponsePrefs.shareLoc) {
                                                ((MainActivity) fragmentActivity).requestLocationUpdates();
                                            } else {
                                                ((MainActivity) fragmentActivity).removeLocationUpdates();
                                            }
                                        }
                                    }
                                    if (loginResponsePrefs.shareLocTokenLifetime != null)
                                        editor.putInt(SHARE_LOCATION_TOKEN_TTL_KEY, loginResponsePrefs.shareLocTokenLifetime);
                                    editor.apply();
                                }
                                LoginResponseSubTo loginResponseSubTo = loginResponse.data.loginResponseSubTo;
                                if (loginResponseSubTo != null) {
                                    editor.putString(LOCATION_SHARING_TOKEN_TOUR_ID_KEY, loginResponseSubTo.tourId);
                                    editor.putString(LOCATION_SHARING_TOKEN_KEY, loginResponseSubTo.token);
                                    editor.apply();
                                }
                                AppDatabase.databaseWriteExecutor.submit(
                                        new Runnable() {
                                            public void run() {
                                                AppDatabase appDatabase = AppDatabase.getInstance(requireContext());
                                                appDatabase.userDAO().insertUser(new User(username.toString()));
                                            }
                                        });
                                AppUtils.setUsernameInNavDrawerHeader((NavigationView)fragmentActivity.findViewById(R.id.nav_view),
                                        fragmentActivity, username.toString());
                                AppUtils.hideSoftKeyboard(fragmentActivity);
                                Toast.makeText(ctx,"Logged in", Toast.LENGTH_SHORT).show();
                                AppUtils.updateUserAccDrawerItems(fragmentActivity);
                                Navigation.findNavController(requireView()).popBackStack();
                            } else {
                                //System.out.println(response.errorBody());
                                Toast.makeText(requireContext(),"Incorrect credentials", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        @EverythingIsNonNull
                        public void onFailure(Call<LoginResponse> call, Throwable t) {
                            t.printStackTrace();
                        }
                    });
                }
            }
        });
        return root;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = ViewModelProviders.of(this).get(SignInViewModel.class);
        // TODO: Use the ViewModel
    }

}
