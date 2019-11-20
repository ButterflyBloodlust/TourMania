package com.hal9000.tourmania.ui.sign_up;

import androidx.lifecycle.ViewModelProviders;

import android.app.Activity;
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
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.hal9000.tourmania.R;
import com.hal9000.tourmania.rest_api.LoginResponse;
import com.hal9000.tourmania.rest_api.RestClient;
import com.hal9000.tourmania.rest_api.SignUpResponse;
import com.hal9000.tourmania.rest_api.UserLogin;
import com.hal9000.tourmania.rest_api.UserSignUp;

public class SignUpFragment extends Fragment {

    private SignUpViewModel mViewModel;

    public static SignUpFragment newInstance() {
        return new SignUpFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_sign_up, container, false);

        Button singInButton = root.findViewById(R.id.button_sign_up);
        singInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Retrofit retrofit = RestClient.getInstance();
                UserSignUp client = retrofit.create(UserSignUp.class);
                View mainView = requireView();
                Editable email = ((TextInputEditText) mainView.findViewById(R.id.text_input_edit_text_email)).getText();
                Editable nickname = ((TextInputEditText) mainView.findViewById(R.id.text_input_edit_text_login)).getText();
                Editable passwd = ((TextInputEditText) mainView.findViewById(R.id.text_input_edit_text_password)).getText();
                Editable passwdRepeat = ((TextInputEditText) mainView.findViewById(R.id.text_input_edit_text_password_again)).getText();
                if (email != null && nickname != null && passwd != null && passwdRepeat != null) {
                    if (!passwd.toString().equals(passwdRepeat.toString())) {
                        Toast.makeText(requireContext(), "Passwords don't match", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        Call<SignUpResponse> call = client.signUp(email.toString(), nickname.toString(), passwd.toString());
                        call.enqueue(new Callback<SignUpResponse>() {
                            @Override
                            @EverythingIsNonNull
                            public void onResponse(Call<SignUpResponse> call, Response<SignUpResponse> response) {
                                if (response.isSuccessful()) {
                                    SignUpResponse rss = response.body();
                                    // TODO process registration
                                    hideSoftKeyboard();
                                    Toast.makeText(requireContext(),"Signed up", Toast.LENGTH_SHORT).show();
                                    Navigation.findNavController(requireView()).popBackStack();
                                } else {
                                    //System.out.println(response.errorBody());
                                    Toast.makeText(requireContext(), "Incorrect credentials", Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            @EverythingIsNonNull
                            public void onFailure(Call<SignUpResponse> call, Throwable t) {
                                t.printStackTrace();
                            }
                        });
                    }
                }
            }
        });
        return root;
    }

    private void hideSoftKeyboard() {
        InputMethodManager inputMethodManager = (InputMethodManager) getActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), 0);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = ViewModelProviders.of(this).get(SignUpViewModel.class);
        // TODO: Use the ViewModel
    }

}
