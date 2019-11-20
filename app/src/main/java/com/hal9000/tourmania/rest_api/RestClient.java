package com.hal9000.tourmania.rest_api;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.hal9000.tourmania.database.AppDatabase;

import androidx.room.Room;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class RestClient {

    private static final String BASE_API_URL = "http://192.168.1.20:8000";
    private static Retrofit retrofitInstance;

    public static Retrofit getInstance() {
        if (retrofitInstance == null) {
            Retrofit.Builder builder = new Retrofit.Builder()
                    .baseUrl(BASE_API_URL)
                    .addConverterFactory(ScalarsConverterFactory.create())
                    .addConverterFactory(GsonConverterFactory.create());
            retrofitInstance = builder.build();
        }
        return retrofitInstance;
    }
}