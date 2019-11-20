package com.hal9000.tourmania.rest_api;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class SignUpResponse {
    @SerializedName("data") @Expose
    private SignUpResponseData data;
}
