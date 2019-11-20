package com.hal9000.tourmania.rest_api;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import androidx.annotation.Nullable;

public class LoginResponseData {
    @SerializedName("token") @Expose @Nullable
    private String token;

    @SerializedName("error_msg") @Expose @Nullable
    private String errorMsg;
}
