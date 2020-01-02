package com.hal9000.tourmania.model;

import com.google.gson.annotations.SerializedName;
import com.hal9000.tourmania.rest_api.Exclude;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "Users")
public class User {

    @Exclude
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "user_id_pk")
    private int userId;

    @SerializedName("nickname")
    @ColumnInfo(name = "username")
    private String username;

    @Exclude @Ignore
    @ColumnInfo(name = "img_path")
    private String userImgPath;

    @SerializedName("trGdSrvrId")
    @ColumnInfo(name = "server_tour_guide_id")
    private String serverTourGdId = "";

    @SerializedName("rateVal")
    @ColumnInfo(name = "usr_rate_val")
    private float rateVal;

    @SerializedName("rateCount")
    @ColumnInfo(name = "usr_rate_count")
    private int rateCount;

    @SerializedName("rating")
    @ColumnInfo(name = "usr_rating")
    private float myRating = 0.0f;

    @SerializedName("phone_num")
    @ColumnInfo(name = "phone_num")
    private String phoneNumber = "";

    @SerializedName("email")
    @ColumnInfo(name = "email")
    private String email;

    @SerializedName("is_guide")
    @ColumnInfo(name = "is_guide")
    private boolean isTourGuide;


    public User() {}

    @Ignore
    public User(String username) {
        this.setUsername(username);
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getUserImgPath() {
        return userImgPath;
    }

    public void setUserImgPath(String userImgPath) {
        this.userImgPath = userImgPath;
    }

    public String getServerTourGdId() {
        return serverTourGdId;
    }

    public void setServerTourGdId(String serverTourGdId) {
        this.serverTourGdId = serverTourGdId;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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

    public boolean isTourGuide() {
        return isTourGuide;
    }

    public void setTourGuide(boolean tourGuide) {
        isTourGuide = tourGuide;
    }
}
