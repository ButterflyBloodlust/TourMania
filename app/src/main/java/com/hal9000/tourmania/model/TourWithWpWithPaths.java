package com.hal9000.tourmania.model;

import com.google.gson.annotations.SerializedName;

import java.util.Collections;
import java.util.List;

import androidx.annotation.Nullable;
import androidx.room.Embedded;
import androidx.room.Relation;

public class TourWithWpWithPaths {
    @Embedded
    @SerializedName("tour")
    public Tour tour;

    @Embedded
    @SerializedName("author")
    public User user;

    @SerializedName("tags")
    @Relation(parentColumn = "tour_id_pk", entityColumn = "tour_id", entity=TourTag.class)
    public List<TourTag> tourTags;

    @SerializedName("wpsWPics")
    @Relation(parentColumn = "tour_id_pk", entityColumn = "tour_id", entity=TourWaypoint.class)
    public List<TourWpWithPicPaths> _tourWpsWithPicPaths;

    public TourWithWpWithPaths () {}

    public TourWithWpWithPaths(Tour tour, List<TourTag> tourTags, List<TourWpWithPicPaths> tourWpsWithPicPaths) {
        this.tour = tour;
        this.tourTags = tourTags;
        this._tourWpsWithPicPaths = tourWpsWithPicPaths;
    }

    public List<TourWpWithPicPaths> getSortedTourWpsWithPicPaths(){
        Collections.sort(_tourWpsWithPicPaths);
        return _tourWpsWithPicPaths;
    }
}
