package com.hal9000.tourmania.rest_api.login;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import androidx.annotation.Nullable;

public class LoginResponseSubTo {
    @SerializedName("token") @Expose
    public String token;

    @SerializedName("tour_id") @Expose
    public String tourId;

}
