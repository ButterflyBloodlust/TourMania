package com.hal9000.tourmania.rest_api.users;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;

public interface UsersService {
    @POST("user/prefs/") @Multipart
    Call<ResponseBody> updateUserPrefsIsGuide(@Part("is_guide") boolean isGuide);

    @POST("user/prefs/") @Multipart
    Call<ResponseBody> updateUserPrefsPhoneNum(@Part("phone_num") String phoneNum);

    @POST("user/prefs/") @Multipart
    Call<ResponseBody> updateUserPrefsLocationSharing(@Part("share_loc") boolean phoneNum);

    @POST("user/prefs/") @Multipart
    Call<ResponseBody> updateUserPrefsLocationSharingTokenLifetime(@Part("share_loc_token_ttl") int shareLocTokenLifetime);
}
