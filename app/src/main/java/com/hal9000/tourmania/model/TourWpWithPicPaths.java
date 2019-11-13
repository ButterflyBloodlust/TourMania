package com.hal9000.tourmania.model;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.room.Embedded;
import androidx.room.Relation;

public class TourWpWithPicPaths implements Comparable<TourWpWithPicPaths>{
    @Embedded
    public TourWaypoint tourWaypoint;

    @Relation(parentColumn = "id", entityColumn = "tour_wp_id")
    public List<PicturePath> picturePaths;

    @Override
    public int compareTo(@NonNull TourWpWithPicPaths o) {
        return this.tourWaypoint.getWpOrder() - o.tourWaypoint.getWpOrder();
    }
}
