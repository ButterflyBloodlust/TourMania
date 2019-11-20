package com.hal9000.tourmania.rest_api;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import androidx.annotation.Nullable;

public class SignUpResponseData {
    @SerializedName("id") @Expose
    @Nullable
    private String id;

    @SerializedName("email") @Expose
    @Nullable
    private String email;

    @SerializedName("nickname") @Expose
    @Nullable
    private String nickname;

    @SerializedName("error_msg") @Expose @Nullable
    private String errorMsg;
}
