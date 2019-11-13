package com.hal9000.tourmania.model;

import java.util.Collections;
import java.util.List;

import androidx.room.Embedded;
import androidx.room.Relation;

public class TourWithWpWithPaths {
    @Embedded
    public Tour tour;

    @Relation(parentColumn = "id", entityColumn = "tour_id", entity=TourWaypoint.class)
    private List<TourWpWithPicPaths> tourWpsWithPicPaths;

    public List<TourWpWithPicPaths> getSortedTourWpsWithPicPaths(){
        Collections.sort(tourWpsWithPicPaths);
        return tourWpsWithPicPaths;
    }
}
