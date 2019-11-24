package com.hal9000.tourmania.rest_api;

import android.content.Context;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.hal9000.tourmania.database.AppDatabase;

import androidx.room.Room;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public abstract class RestClient {

    private static final String API_BASE_URL = "http://192.168.1.20:8000";

    private static OkHttpClient.Builder httpClient = new OkHttpClient.Builder();

    private static Retrofit.Builder builder =
            new Retrofit.Builder()
                    .baseUrl(API_BASE_URL)
                    .addConverterFactory(ScalarsConverterFactory.create())
                    .addConverterFactory(GsonConverterFactory.create(new GsonBuilder()
                            .setExclusionStrategies(new AnnotationExclusionStrategy())
                            .addSerializationExclusionStrategy(new AnnotationSerializationExclusionStrategy())
                            .create()));

    private static Retrofit retrofit = builder.build();

    public static <S> S createService(Class<S> serviceClass) {
        return createService(serviceClass, null);
    }

    public static <S> S createService(
            Class<S> serviceClass, final String authToken) {
        if (!TextUtils.isEmpty(authToken)) {
            AuthenticationInterceptor interceptor =
                    new AuthenticationInterceptor(authToken);

            if (!httpClient.interceptors().contains(interceptor)) {
                httpClient.addInterceptor(interceptor);

                builder.client(httpClient.build());
                retrofit = builder.build();
            }
        }
        return retrofit.create(serviceClass);
    }
}