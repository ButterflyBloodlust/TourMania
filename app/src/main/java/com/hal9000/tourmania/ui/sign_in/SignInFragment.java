package com.hal9000.tourmania.ui.sign_in;

import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProviders;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.internal.EverythingIsNonNull;

import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.hal9000.tourmania.AppUtils;
import com.hal9000.tourmania.MainActivity;
import com.hal9000.tourmania.R;
import com.hal9000.tourmania.SharedPrefUtils;
import com.hal9000.tourmania.rest_api.LoginResponse;
import com.hal9000.tourmania.rest_api.RestClient;
import com.hal9000.tourmania.rest_api.UserLogin;

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
                Editable username = ((TextInputEditText) mainView.findViewById(R.id.text_input_edit_text_login)).getText();
                Editable passwd = ((TextInputEditText) mainView.findViewById(R.id.text_input_edit_text_password)).getText();
                if (username != null && passwd != null) {
                    Call<LoginResponse> call = client.login(username.toString(), passwd.toString());
                    call.enqueue(new Callback<LoginResponse>() {
                        @Override
                        @EverythingIsNonNull
                        public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                            if (response.isSuccessful()) {
                                Context ctx = requireContext();
                                FragmentActivity fragmentActivity = requireActivity();
                                LoginResponse loginResponse = response.body();
                                SharedPrefUtils.putString(ctx, MainActivity.getLoginTokenKey(), loginResponse.data.token);
                                AppUtils.hideSoftKeyboard(fragmentActivity);
                                Toast.makeText(ctx,"Logged in", Toast.LENGTH_SHORT).show();
                                AppUtils.updateUserAccDrawer(fragmentActivity);
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
