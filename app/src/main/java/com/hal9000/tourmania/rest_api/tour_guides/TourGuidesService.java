package com.hal9000.tourmania.rest_api.tour_guides;

import com.hal9000.tourmania.model.User;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface TourGuidesService {
    @GET("tour_guide/near")
    Call<List<User>> getNearbyTourGuides(@Query("long") double longitude, @Query("lat") double latitude, @Query("page_num") int pageNumber);

    @GET("tour_guide")
    Call<User> getTourGuideDetails(@Query("nickname") String nickname);

    @POST("tour_guide/{username}/rate") @FormUrlEncoded
    Call<ResponseBody> rateTourGuide(@Path("username") String tourGuideNickname, @Field("rating") float tourGuideRating);

    @GET("tour_guide/search/{phrase}")
    Call<List<User>> searchTourGuidesByPhrase(@Path("phrase") String phrase, @Query("page_num") int pageNumber);

    @POST("tour_guide/loc/update") @FormUrlEncoded
    Call<Void> updateTourGuideLocation(@Field("long") double longitude, @Field("lat") double latitude);

    @POST("tour_guide/loc/token") @FormUrlEncoded
    Call<LocationShareTokenResponse> getTourGuideLocationSharingToken(@Field("tour_id") String tourId);

    @POST("tour_guide/loc/token/revoke")
    Call<Void> revokeTourGuideLocationSharingToken();

    @POST("tour_guide/loc/sub") @FormUrlEncoded
    Call<SubscribeToLocationShareResponse> subscribeToTourGuideLocationSharing(@Field("token") String token);

    @POST("tour_guide/loc/get") @FormUrlEncoded
    Call<GetTourGuideLocationResponse> getTourGuideLocation(@Field("token") String token);
}
