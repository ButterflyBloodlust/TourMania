package com.hal9000.tourmania.model;

import com.google.gson.annotations.SerializedName;

import androidx.room.ColumnInfo;

public class TourServerIdTimestamp {
    @SerializedName("trSrvrId")
    @ColumnInfo(name = "server_tour_id")
    private String serverTourId = "";

    @SerializedName("modified_at")
    @ColumnInfo(name = "modified_at")
    private long modifiedAt;

    public TourServerIdTimestamp() {}

    public String getServerTourId() {
        return serverTourId;
    }

    public void setServerTourId(String serverTourId) {
        this.serverTourId = serverTourId;
    }

    public long getModifiedAt() {
        return modifiedAt;
    }

    public void setModifiedAt(long modifiedAt) {
        this.modifiedAt = modifiedAt;
    }
}
