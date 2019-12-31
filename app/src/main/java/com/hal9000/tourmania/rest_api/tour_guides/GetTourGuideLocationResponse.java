package com.hal9000.tourmania.rest_api.tour_guides;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class GetTourGuideLocationResponse {
    @SerializedName("long") @Expose
    public double longitude;

    @SerializedName("lat") @Expose
    public double latitude;
}
