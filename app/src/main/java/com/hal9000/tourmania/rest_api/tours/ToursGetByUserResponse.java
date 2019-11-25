package com.hal9000.tourmania.rest_api.tours;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.hal9000.tourmania.model.TourWithWpWithPaths;

import java.util.List;

// not tested
public class ToursGetByUserResponse {
    @Expose
    public TourWithWpWithPaths tourWithWpWithPaths;

    @Expose @SerializedName("username")
    public String username;
}
