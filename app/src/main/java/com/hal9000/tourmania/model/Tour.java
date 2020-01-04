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

@Entity(tableName = "Tours",
        foreignKeys = @ForeignKey(entity = User.class,
                parentColumns = "user_id_pk",
                childColumns = "user_id",
                onDelete = ForeignKey.SET_NULL),
        indices={@Index(value="server_tour_id"),
                @Index(value="user_id")})
public class Tour {

    @Exclude
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "tour_id_pk")
    private int tourId;

    @ColumnInfo(name = "title")
    private String title;

    @Exclude
    @ColumnInfo(name = "img_path")
    private String tourImgPath;

    @SerializedName("rateVal")
    @ColumnInfo(name = "rate_val")
    private float rateVal;

    @SerializedName("rateCount")
    @ColumnInfo(name = "rate_count")
    private int rateCount;

    @Exclude
    @SerializedName("rating")
    @ColumnInfo(name = "tr_rating")
    private float myRating = 0.0f;

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
    private Integer userId = null;

    @Exclude
    @SerializedName("in_favs") @Ignore
    private boolean inFavs = false;

    public Tour() {}

    @Ignore
    public Tour(String title, String tourImgPath) {
        this.title = title;
        this.tourImgPath = tourImgPath;
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

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public boolean isInFavs() {
        return inFavs;
    }

    public void setInFavs(boolean inFavs) {
        this.inFavs = inFavs;
    }

    public float getRateVal() {
        return rateVal;
    }

    public void setRateVal(float rateVal) {
        this.rateVal = rateVal;
    }

    public int getRateCount() {
        return rateCount;
    }

    public void setRateCount(int rateCount) {
        this.rateCount = rateCount;
    }

    public float getMyRating() {
        return myRating;
    }

    public void setMyRating(float myRating) {
        this.myRating = myRating;
    }
}
