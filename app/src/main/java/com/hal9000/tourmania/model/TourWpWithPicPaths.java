package com.hal9000.tourmania.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.room.Embedded;
import androidx.room.Relation;

public class TourWpWithPicPaths implements Comparable<TourWpWithPicPaths>{
    @SerializedName("tourWp")
    @Embedded
    public TourWaypoint tourWaypoint;

    @SerializedName("picPaths")
    @Relation(parentColumn = "id", entityColumn = "tour_wp_id")
    public List<PicturePath> picturePaths;

    public TourWpWithPicPaths() {}

    public TourWpWithPicPaths(TourWaypoint tourWaypoint, List<PicturePath> picturePaths) {
        this.tourWaypoint = tourWaypoint;
        this.picturePaths = picturePaths;
    }

    @Override
    public int compareTo(@NonNull TourWpWithPicPaths o) {
        return this.tourWaypoint.getWpOrder() - o.tourWaypoint.getWpOrder();
    }
}
