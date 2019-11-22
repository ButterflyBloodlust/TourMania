package com.hal9000.tourmania.rest_api;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import androidx.annotation.Nullable;

public class SignUpResponseData {
    @SerializedName("id") @Expose
    @Nullable
    public String id;

    @SerializedName("email") @Expose
    @Nullable
    public String email;

    @SerializedName("nickname") @Expose
    @Nullable
    public String nickname;

    @SerializedName("error_msg") @Expose @Nullable
    public String errorMsg;
}
