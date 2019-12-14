package com.hal9000.tourmania.rest_api.login;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import androidx.annotation.Nullable;

public class LoginResponsePrefs {
    @SerializedName("is_guide") @Expose @Nullable
    public Boolean isGuide;

    @SerializedName("phone_num") @Expose @Nullable
    public String phoneNum;

    @SerializedName("share_loc") @Expose @Nullable
    public Boolean shareLoc;
}
