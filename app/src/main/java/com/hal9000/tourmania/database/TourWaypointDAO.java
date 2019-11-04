package com.hal9000.tourmania.database;

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
public interface TourWaypointDAO {

    @Query("SELECT * FROM TourWaypoints")
    LiveData<List<TourWaypoint>> getTourWps();

    @Query("SELECT * FROM TourWaypoints WHERE id = :tourWpId")
    LiveData<TourWaypoint> getTourWp(int tourWpId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertTourWp(TourWaypoint tourWaypoint);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertTourWps(List<TourWaypoint> tourWaypoints);

    @Update
    void updateTourWp(TourWaypoint tourWaypoint);

    @Delete
    void deleteTourWp(TourWaypoint tourWaypoint);
}
