package com.hal9000.tourmania.model;

import com.hal9000.tourmania.rest_api.Exclude;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "FavouriteTours",
        foreignKeys = @ForeignKey(entity = Tour.class,
                parentColumns = "tour_id_pk",
                childColumns = "tour_id",
                onDelete = ForeignKey.CASCADE),
        indices=@Index(value="tour_id", unique=true))
public class FavouriteTour {

    @Exclude
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    private int favouriteTourId;

    @Exclude
    @ColumnInfo(name = "tour_id")
    private int tourId;

    public FavouriteTour() {}

    @Ignore
    public FavouriteTour(int tourId) {
        this.tourId = tourId;
    }

    public int getFavouriteTourId() {
        return favouriteTourId;
    }

    public void setFavouriteTourId(int favouriteTourId) {
        this.favouriteTourId = favouriteTourId;
    }

    public int getTourId() {
        return tourId;
    }

    public void setTourId(int tourId) {
        this.tourId = tourId;
    }
}
