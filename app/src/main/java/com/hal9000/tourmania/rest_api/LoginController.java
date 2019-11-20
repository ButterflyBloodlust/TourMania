package com.hal9000.tourmania.rest_api;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.internal.EverythingIsNonNull;

public class LoginController implements Callback<LoginResponse> {

    private Context context;

    public void start(Context context) {
        this.context = context;
        Retrofit retrofit = RestClient.getInstance();
        UserLogin client = retrofit.create(UserLogin.class);
        Call<LoginResponse> call = client.login("asd", "asd");
        call.enqueue(this);
    }

    @Override @EverythingIsNonNull
    public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
        if (response.isSuccessful()) {
            if (response.code() == 200) {
                LoginResponse rss = response.body();
            }
            else {
                LoginResponse rss = response.body();
            }
        } else {
            System.out.println(response.errorBody());
        }
        Log.d("crashTest", "onResponse");
    }

    @Override @EverythingIsNonNull
    public void onFailure(Call<LoginResponse> call, Throwable t) {
        t.printStackTrace();
    }
}
