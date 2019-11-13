package com.hal9000.tourmania.model;

import java.util.Collections;
import java.util.List;

import androidx.room.Embedded;
import androidx.room.Relation;

public class TourWithWpWithPaths {
    @Embedded
    public Tour tour;

    @Relation(parentColumn = "id", entityColumn = "tour_id", entity=TourWaypoint.class)
    public List<TourWpWithPicPaths> _tourWpsWithPicPaths;

    public List<TourWpWithPicPaths> getSortedTourWpsWithPicPaths(){
        Collections.sort(_tourWpsWithPicPaths);
        return _tourWpsWithPicPaths;
    }
}
