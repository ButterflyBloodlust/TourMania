package com.hal9000.tourmania.model;

import com.hal9000.tourmania.rest_api.Exclude;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "TourWaypoints",
        foreignKeys = @ForeignKey(entity = Tour.class,
                parentColumns = "id",
                childColumns = "tour_id",
                onDelete = ForeignKey.CASCADE),
        indices=@Index(value="tour_id"))
public class TourWaypoint implements Comparable<TourWaypoint>{

    @Exclude
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

    @Exclude
    @ColumnInfo(name = "tour_id")
    private int tourId;

    @ColumnInfo(name = "wp_order")
    private int wpOrder;

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

    public int getTourId() { return tourId; }

    public void setTourId(int tourId) { this.tourId = tourId; }

    public int getWpOrder() {
        return wpOrder;
    }

    public void setWpOrder(int wpOrder) {
        this.wpOrder = wpOrder;
    }

    @Override
    public int compareTo(@NonNull TourWaypoint o) {
        return this.wpOrder - o.wpOrder;
    }
}
