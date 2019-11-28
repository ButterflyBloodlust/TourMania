package com.hal9000.tourmania.rest_api.tours;

import com.hal9000.tourmania.model.TourWithWpWithPaths;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ToursService {
    @POST("tour/upsert/")
    Call<TourUpsertResponse> upsertTour(@Body TourWithWpWithPaths tourWithWpWithPaths);

    @POST("tour/u/{username}/") @Multipart
    Call<List<TourWithWpWithPaths>> getUserTours(@Path("username") String username, @Part("owndToursIds") List<String> tourIds);

    @POST("tour/u/{username}/")
    Call<List<TourWithWpWithPaths>> getUserTours(@Path("username") String username);

    @DELETE("tour/delete/{tourId}/")
    Call<Void> deleteTourById(@Path("tourId") String tourId);

    @GET("tour/search/{phrase}")
    Call<List<TourWithWpWithPaths>> searchToursByPhrase(@Path("phrase") String phrase, @Query("page_num") int pageNumber);

    @GET("tour/id/{tourId}/")
    Call<TourWithWpWithPaths> getTour(@Path("tourId") String tourId);
}
