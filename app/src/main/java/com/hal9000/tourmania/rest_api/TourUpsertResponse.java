package com.hal9000.tourmania.rest_api;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import androidx.annotation.Nullable;

public class TourUpsertResponse {
    @SerializedName("tourServerId") @Expose
    @Nullable
    public String tourServerId;
}
