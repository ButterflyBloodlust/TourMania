package com.hal9000.tourmania.rest_api.files_upload_download;

import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface FileUploadDownloadService {
    @Multipart
    @POST("tour/images/upsert/")
    Call<ResponseBody> uploadMultipleTourImagesFilesDynamic(
            @Part("trSrvrId") RequestBody description,
            @Part List<MultipartBody.Part> files);

    @POST("tour/images/by_id/")
    Call<List<TourFileDownloadResponse>> downloadMultipleToursImagesFiles(@Body List<String> tourIds, @Query("incl_wps") boolean includeWaypoints);

    @GET("tour/i/id/{tourId}/")
    Call<TourFileDownloadResponse> getTourImages(@Path("tourId") String tourId);

    @Multipart
    @POST("tour_guide/image/upsert")
    Call<ResponseBody> uploadTourGuideImageFile(@Part MultipartBody.Part file);

    @GET("tour_guide/image/get")
    Call<FileDownloadImageObj> getTourGuideImage();
}