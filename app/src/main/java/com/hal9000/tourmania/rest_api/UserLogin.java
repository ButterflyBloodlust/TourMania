package com.hal9000.tourmania.rest_api;

import retrofit2.Call;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface UserLogin {
    @POST("mongo_auth/login/") @Multipart
    Call<LoginResponse> login(@Part("username") String nickname, @Part("password") String password);
}
