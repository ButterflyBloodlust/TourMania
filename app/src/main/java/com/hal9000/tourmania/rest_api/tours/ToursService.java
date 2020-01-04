package com.hal9000.tourmania.rest_api.tours;

import com.hal9000.tourmania.model.TourServerIdTimestamp;
import com.hal9000.tourmania.model.TourWithWpWithPaths;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
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
    Call<List<TourWithWpWithPaths>> getUserTours(@Path("username") String username, @Part("owndToursIds") List<TourServerIdTimestamp> tourIds);

    @POST("tour/u/{username}/")
    Call<List<TourWithWpWithPaths>> getUserTours(@Path("username") String username);

    @GET("tour/u/{username}/overviews")
    Call<List<TourWithWpWithPaths>> getUserToursOverviews(
            @Path("username") String username, @Query("long") double longitude,
            @Query("lat") double latitude, @Query("page_num") int pageNumber);

    @DELETE("tour/delete/{tourId}/")
    Call<Void> deleteTourById(@Path("tourId") String tourId);

    @GET("tour/search/{phrase}")
    Call<List<TourWithWpWithPaths>> searchToursByPhrase(@Path("phrase") String phrase, @Query("page_num") int pageNumber);

    @GET("tour/near")
    Call<List<TourWithWpWithPaths>> getNearbyTours(@Query("long") double longitude, @Query("lat") double latitude, @Query("page_num") int pageNumber);

    @GET("tour/id/{tourId}")
    Call<TourWithWpWithPaths> getTour(@Path("tourId") String tourId);

    @POST("tour/id/{tourId}/rate") @FormUrlEncoded
    Call<ResponseBody> rateTour(@Path("tourId") String tourId, @Field("rating") float tourRating);

    @POST("user/favs/add/") @FormUrlEncoded
    Call<ResponseBody> addTourToFavs(@Field("trSrvrId") String serverTourId);

    @DELETE("user/favs/delete/{tourId}/")
    Call<ResponseBody> deleteTourFromFavs(@Path("tourId") String tourId);

    @POST("user/{username}/favs/") @Multipart
    Call<List<TourWithWpWithPaths>> getUserFavTours(@Path("username") String username, @Part("owndToursIds") List<TourServerIdTimestamp> tourIds);

    @POST("user/{username}/favs/")
    Call<List<TourWithWpWithPaths>> getUserFavTours(@Path("username") String username);
}
