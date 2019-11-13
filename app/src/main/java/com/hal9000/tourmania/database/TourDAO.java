package com.hal9000.tourmania.database;

import com.hal9000.tourmania.model.Tour;
import com.hal9000.tourmania.model.TourWaypoint;

import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

@Dao
public interface TourDAO {
    @Query("SELECT * FROM Tours")
    List<Tour> getTours();

    @Query("SELECT * FROM Tours WHERE id = :tourId")
    Tour getTour(int tourId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertTour(Tour tour);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertTours(List<Tour> tours);

    @Update
    void updateTour(Tour tour);

    @Delete
    void deleteTourWp(Tour tour);
}
