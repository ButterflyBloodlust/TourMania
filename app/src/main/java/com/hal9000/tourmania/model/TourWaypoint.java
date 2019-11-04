package com.hal9000.tourmania.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "TourWaypoints")
public class TourWaypoint {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    private int tourWpId;

    @ColumnInfo(name = "latitude")
    private double latitude;

    @ColumnInfo(name = "longtitude")
    private double longtitude;

    @ColumnInfo(name = "title")
    private String title;

    @ColumnInfo(name = "main_img_path")
    private String mainImgPath;

    public TourWaypoint(double latitude, double longtitude, String title, String mainImgPath) {
        this.setLatitude(latitude);
        this.setLongtitude(longtitude);
        this.setTitle(title);
        this.setMainImgPath(mainImgPath);
    }

    public int getTourWpId() {
        return tourWpId;
    }

    public void setTourWpId(int tourWpId) {
        this.tourWpId = tourWpId;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongtitude() {
        return longtitude;
    }

    public void setLongtitude(double longtitude) {
        this.longtitude = longtitude;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMainImgPath() {
        return mainImgPath;
    }

    public void setMainImgPath(String mainImgPath) {
        this.mainImgPath = mainImgPath;
    }
}
