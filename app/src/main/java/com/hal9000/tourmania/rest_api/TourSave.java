package com.hal9000.tourmania.rest_api;

import com.hal9000.tourmania.model.TourWithWpWithPaths;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface TourSave {
    @POST("tour/upsert/")
    Call<Void> upsertTour(@Body TourWithWpWithPaths tourWithWpWithPaths);
}
