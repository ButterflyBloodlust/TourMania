package com.hal9000.tourmania.model;

import java.util.List;

import androidx.room.Embedded;
import androidx.room.Relation;

public class TourWpWithPicPaths {
    @Embedded
    TourWaypoint plant;

    @Relation(parentColumn = "id", entityColumn = "tour_wp_id")
    List<PicturePath> gardenPlantings;
}
