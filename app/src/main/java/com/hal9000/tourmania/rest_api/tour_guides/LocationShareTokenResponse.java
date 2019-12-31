package com.hal9000.tourmania.rest_api.tour_guides;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import androidx.annotation.Nullable;

public class LocationShareTokenResponse {
    @SerializedName("token") @Expose
    public String token;
}
