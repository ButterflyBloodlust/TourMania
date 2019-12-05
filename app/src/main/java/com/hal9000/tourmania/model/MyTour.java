package com.hal9000.tourmania.model;

import com.hal9000.tourmania.rest_api.Exclude;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "MyTours",
        foreignKeys = @ForeignKey(entity = Tour.class,
                parentColumns = "id",
                childColumns = "tour_id",
                onDelete = ForeignKey.CASCADE),
        indices=@Index(value="tour_id", unique=true))
public class MyTour {

    @Exclude
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    private int myTourId;

    @Exclude
    @ColumnInfo(name = "tour_id")
    private int tourId;

    public MyTour() {}

    @Ignore
    public MyTour(int tourId) {
        this.tourId = tourId;
    }

    public int getMyTourId() {
        return myTourId;
    }

    public void setMyTourId(int myTourId) {
        this.myTourId = myTourId;
    }

    public int getTourId() {
        return tourId;
    }

    public void setTourId(int tourId) {
        this.tourId = tourId;
    }
}
