package com.hal9000.tourmania.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "Tours")
public class Tour {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    private int tourId;

    @ColumnInfo(name = "title")
    private String title;

    @ColumnInfo(name = "img_path")
    private String tourImgPath;

    @ColumnInfo(name = "rating")
    private float rating;

    public Tour() {}

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
}
