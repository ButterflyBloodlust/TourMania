package com.hal9000.tourmania.model;

import com.hal9000.tourmania.rest_api.Exclude;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "PicturePaths",
        foreignKeys = @ForeignKey(entity = TourWaypoint.class,
        parentColumns = "id",
        childColumns = "tour_wp_id",
        onDelete = ForeignKey.CASCADE),
        indices=@Index(value="tour_wp_id"))

public class PicturePath {

    @Exclude
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    private int id;

    @ColumnInfo(name = "pic_path")
    private String picPath;

    @Exclude
    @ColumnInfo(name = "tour_wp_id")
    private int tourWpId;

    public PicturePath(String picPath) {
        this.setPicPath(picPath);
        //this.setTourWpId(tourWpId);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getPicPath() {
        return picPath;
    }

    public void setPicPath(String picPath) {
        this.picPath = picPath;
    }

    public int getTourWpId() {
        return tourWpId;
    }

    public void setTourWpId(int tourWpId) {
        this.tourWpId = tourWpId;
    }
}
