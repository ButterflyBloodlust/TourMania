package com.hal9000.tourmania.rest_api.tours;

import com.hal9000.tourmania.model.TourWithWpWithPaths;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;

public interface ToursCRUD {
    @POST("tour/upsert/")
    Call<TourUpsertResponse> upsertTour(@Body TourWithWpWithPaths tourWithWpWithPaths);

    @POST("tour/{username}/") @Multipart
    Call<List<TourWithWpWithPaths>> getUserTours(@Path("username") String username, @Part("owndToursIds") List<String> tourIds);

    @POST("tour/{username}/")
    Call<List<TourWithWpWithPaths>> getUserTours(@Path("username") String username);

    @DELETE("tour/delete/{tourId}/")
    Call<Void> deleteTourById(@Path("tourId") String tourId);
}
