package com.hal9000.tourmania.rest_api.login;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import androidx.annotation.Nullable;

public class LoginResponseData {
    @SerializedName("token") @Expose @Nullable
    public String token;

    @SerializedName("prefs") @Expose @Nullable
    public LoginResponsePrefs loginResponsePrefs;

    @SerializedName("subTo") @Expose @Nullable
    public LoginResponseSubTo loginResponseSubTo;

    @SerializedName("error_msg") @Expose @Nullable
    public String errorMsg;
}
