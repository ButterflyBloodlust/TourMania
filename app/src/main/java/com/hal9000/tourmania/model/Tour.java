package com.hal9000.tourmania.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.hal9000.tourmania.rest_api.Exclude;
import com.hal9000.tourmania.rest_api.SerializationExclude;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "Tours")
public class Tour {

    @Exclude
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    private int tourId;

    @ColumnInfo(name = "title")
    private String title;

    @ColumnInfo(name = "img_path")
    private String tourImgPath;

    @ColumnInfo(name = "rating")
    private float rating;

    @Exclude
    @ColumnInfo(name = "server_synced")
    private boolean serverSynced = false;

    @SerializationExclude
    @SerializedName("trSrvrId")
    @ColumnInfo(name = "server_tour_id")
    private String serverTourId = "";

    public Tour() {}

    @Ignore
    public Tour(String title, String tourImgPath, float rating) {
        this.title = title;
        this.tourImgPath = tourImgPath;
        this.rating = rating;
    }

    public int getTourId() {
        return tourId;
    }

    public void setTourId(int tourId) {
        this.tourId = tourId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTourImgPath() {
        return tourImgPath;
    }

    public void setTourImgPath(String tourImgPath) {
        this.tourImgPath = tourImgPath;
    }

    public float getRating() {
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }

    public boolean isServerSynced() {
        return serverSynced;
    }

    public void setServerSynced(boolean serverSynced) {
        this.serverSynced = serverSynced;
    }

    public String getServerTourId() {
        return serverTourId;
    }

    public void setServerTourId(String serverTourId) {
        this.serverTourId = serverTourId;
    }
}
