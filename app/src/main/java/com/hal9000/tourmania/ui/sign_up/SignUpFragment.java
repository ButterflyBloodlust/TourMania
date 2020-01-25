package com.hal9000.tourmania.ui.sign_up;

import androidx.lifecycle.ViewModelProviders;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.internal.EverythingIsNonNull;

import android.text.Editable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.hal9000.tourmania.AppUtils;
import com.hal9000.tourmania.R;
import com.hal9000.tourmania.rest_api.RestClient;
import com.hal9000.tourmania.rest_api.sign_up.SignUpResponse;
import com.hal9000.tourmania.rest_api.sign_up.UserSignUp;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class SignUpFragment extends Fragment {

    private SignUpViewModel mViewModel;

    public static SignUpFragment newInstance() {
        return new SignUpFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_sign_up, container, false);

        Button singUpButton = root.findViewById(R.id.button_sign_up);
        singUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UserSignUp client = RestClient.createService(UserSignUp.class);
                View mainView = requireView();
                Editable email = ((TextInputEditText) mainView.findViewById(R.id.text_input_edit_text_email)).getText();
                Editable nickname = ((TextInputEditText) mainView.findViewById(R.id.text_input_edit_text_login)).getText();
                Editable passwd = ((TextInputEditText) mainView.findViewById(R.id.text_input_edit_text_password)).getText();
                Editable passwdRepeat = ((TextInputEditText) mainView.findViewById(R.id.text_input_edit_text_password_again)).getText();
                if (email != null && nickname != null && passwd != null && passwdRepeat != null) {
                    if (!passwd.toString().equals(passwdRepeat.toString())) {
                        Toast.makeText(requireContext(), "Passwords don't match", Toast.LENGTH_SHORT).show();
                    }
                    else if (email.toString().isEmpty() || nickname.toString().isEmpty() || passwd.toString().isEmpty() || passwdRepeat.toString().isEmpty()) {
                        Toast.makeText(requireContext(), "All fields must be filled", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        Call<SignUpResponse> call = client.signUp(email.toString(), nickname.toString(), passwd.toString());
                        call.enqueue(new Callback<SignUpResponse>() {
                            @Override
                            @EverythingIsNonNull
                            public void onResponse(Call<SignUpResponse> call, Response<SignUpResponse> response) {
                                if (response.isSuccessful()) {
                                    //SignUpResponse res = response.body();
                                    AppUtils.hideSoftKeyboard(requireActivity());
                                    Toast.makeText(requireContext(),"Signed up", Toast.LENGTH_SHORT).show();
                                    Navigation.findNavController(requireView()).popBackStack();
                                } else {
                                    //System.out.println(response.errorBody());
                                    if (response.code() == 405) {
                                        try {
                                            JSONObject jObjError = new JSONObject(response.errorBody().string());
                                            Toast.makeText(requireContext(), jObjError.getJSONObject("data").getString("error_msg"),
                                                    Toast.LENGTH_SHORT).show();
                                        } catch (JSONException | IOException e) {
                                            e.printStackTrace();
                                            Toast.makeText(requireContext(), "An error has occurred but description could not be parsed",
                                                    Toast.LENGTH_SHORT).show();
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                            Toast.makeText(requireContext(), "An unknown error has occurred", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                    else
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

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = ViewModelProviders.of(this).get(SignUpViewModel.class);
        // TODO: Use the ViewModel
    }

}
