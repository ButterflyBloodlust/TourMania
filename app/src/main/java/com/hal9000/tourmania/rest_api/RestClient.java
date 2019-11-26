package com.hal9000.tourmania.rest_api;

import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.hal9000.tourmania.AppUtils;
import com.hal9000.tourmania.FileUtil;
import com.hal9000.tourmania.database.AppDatabase;
import com.mapbox.android.core.FileUtils;

import java.io.File;
import java.io.IOException;

import androidx.annotation.NonNull;
import androidx.room.Room;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
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
                            //.serializeNulls()
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

    @NonNull
    public static RequestBody createPartFromString(String descriptionString) {
        return RequestBody.create(
                okhttp3.MultipartBody.FORM, descriptionString);
    }

    @NonNull
    public static MultipartBody.Part prepareFilePart(String partName, Uri fileUri, final Context context) {

        File file = null;
        try {
            file = FileUtil.from(context, fileUri);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // create RequestBody instance from file
        RequestBody requestFile =
                RequestBody.create(MediaType.parse(context.getContentResolver().getType(fileUri)),file);

        // MultipartBody.Part is used to send also the actual file name
        return MultipartBody.Part.createFormData(partName, file.getName(), requestFile);
    }

    @NonNull
    public static MultipartBody.Part prepareFilePart(String partName, File file, final Context context) {
        if (file == null)
            return MultipartBody.Part.createFormData(partName, "");

        // create RequestBody instance from file
        RequestBody requestFile =
                RequestBody.create(MediaType.parse(AppUtils.getMimeType(Uri.fromFile(file).toString())),file);

        // MultipartBody.Part is used to send also the actual file name
        return MultipartBody.Part.createFormData(partName, file.getName(), requestFile);
    }
}