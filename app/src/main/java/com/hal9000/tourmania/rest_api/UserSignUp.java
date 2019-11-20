package com.hal9000.tourmania.rest_api;

import retrofit2.Call;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface UserSignUp {
    @POST("mongo_auth/signup/") @Multipart
    Call<SignUpResponse> signUp(@Part("email") String email, @Part("nickname") String nickname, @Part("password") String password);
}
