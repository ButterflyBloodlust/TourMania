package com.hal9000.tourmania.rest_api.files_upload_download;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;
import java.util.Map;

import androidx.annotation.Nullable;

public class TourFileDownloadResponse {
    @SerializedName("trSrvrId") @Expose @Nullable
    public String tourServerId;

    @SerializedName("username") @Expose @Nullable
    public String username;

    @SerializedName("imgs") @Expose @Nullable
    public Map<String, FileDownloadImageObj> images;
}
