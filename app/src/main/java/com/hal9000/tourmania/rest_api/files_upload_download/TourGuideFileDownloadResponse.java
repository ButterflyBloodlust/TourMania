package com.hal9000.tourmania.rest_api.files_upload_download;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.Map;

import androidx.annotation.Nullable;

public class TourGuideFileDownloadResponse {
    @SerializedName("nickname") @Expose @Nullable
    public String username;

    @SerializedName("img") @Expose @Nullable
    public FileDownloadImageObj image;
}
