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
    @ColumnInfo(name = "id")
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

    @ColumnInfo(name = "rating")
    private double rating;

    @SerializedName("phone_num")
    @ColumnInfo(name = "phone_num")
    private String phoneNumber = "";

    @ColumnInfo(name = "email")
    private String email;

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

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
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
}
