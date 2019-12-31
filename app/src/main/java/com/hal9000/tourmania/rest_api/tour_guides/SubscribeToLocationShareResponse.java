package com.hal9000.tourmania.rest_api.tour_guides;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import androidx.annotation.Nullable;

public class SubscribeToLocationShareResponse {
    @SerializedName("tour_id") @Expose
    public String tourId;
}
