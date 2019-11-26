package com.hal9000.tourmania.rest_api.files_upload_download;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import androidx.annotation.Nullable;

public class FileDownloadImageObj {
    @SerializedName("mime") @Expose
    @Nullable
    public String mime;

    @SerializedName("b") @Expose @Nullable
    public String base64;
}
