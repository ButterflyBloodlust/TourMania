package com.hal9000.tourmania.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.hal9000.tourmania.rest_api.Exclude;
import com.hal9000.tourmania.rest_api.SerializationExclude;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "Tours",/*
        foreignKeys = @ForeignKey(entity = User.class,
                parentColumns = "id",
                childColumns = "user_id",
                onDelete = ForeignKey.SET_NULL),*/
        indices=@Index(value="server_tour_id", unique = true))
public class Tour {

    @Exclude
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    private int tourId;

    @ColumnInfo(name = "title")
    private String title;

    @Exclude
    @ColumnInfo(name = "img_path")
    private String tourImgPath;

    @ColumnInfo(name = "rating")
    private float rating;

    @Exclude
    @ColumnInfo(name = "server_synced")
    private boolean serverSynced = false;

    @SerializedName("trSrvrId")
    @ColumnInfo(name = "server_tour_id")
    private String serverTourId = "";

    @SerializedName("modified_at")
    @ColumnInfo(name = "modified_at")
    private long modifiedAt;

    @Exclude
    @ColumnInfo(name = "user_id")
    private int userId = 0;

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

    public long getModifiedAt() {
        return modifiedAt;
    }

    public void setModifiedAt(long modifiedAt) {
        this.modifiedAt = modifiedAt;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }
}
