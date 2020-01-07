package com.hal9000.tourmania.database;

import com.hal9000.tourmania.model.Tour;
import com.hal9000.tourmania.model.TourServerIdTimestamp;
import com.hal9000.tourmania.model.TourWaypoint;
import com.hal9000.tourmania.model.TourWithWpWithPaths;

import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

@Dao
public abstract class TourDAO {
    @Query("SELECT * FROM Tours")
    public abstract List<Tour> getTours();

    @Transaction
    @Query("SELECT * FROM Tours LEFT JOIN Users ON user_id = user_id_pk WHERE tour_id_pk = :tourId")
    public abstract TourWithWpWithPaths getTour(int tourId);

    @Transaction
    @Query("SELECT * FROM Tours LEFT JOIN Users ON user_id = user_id_pk WHERE server_synced = :serverSynced")
    public abstract List<TourWithWpWithPaths> getToursBySynced(boolean serverSynced);

    public List<TourWithWpWithPaths> getUnsyncedTours() {
        return getToursBySynced(false);
    }

    //@Query("SELECT * FROM Tours WHERE id IN (SELECT DISTINCT(tour_id) FROM TourWaypoints)")
    @Transaction
    @Query("SELECT * FROM Tours LEFT JOIN Users ON user_id = user_id_pk")
    public abstract List<TourWithWpWithPaths> getToursWithTourWps();

    /*
    @Transaction
    @Query("SELECT * FROM Tours WHERE user_id = 0")
    public abstract List<TourWithWpWithPaths> getMyToursWithTourWps();
     */

    @Transaction
    @Query("SELECT * FROM Tours LEFT JOIN Users ON user_id = user_id_pk WHERE tour_id_pk = :tourId")
    public abstract TourWithWpWithPaths getTourWithTourWps(int tourId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract long insertTour(Tour tour);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public abstract long[] insertTours(List<Tour> tours);

    @Update
    public abstract void updateTour(Tour tour);

    @Delete
    public abstract void deleteTourWp(Tour tour);

    @Query("DELETE FROM Tours WHERE tour_id_pk IN (:tourId)")
    public abstract void deleteToursByTourIds(int[] tourId);

    @Query("DELETE FROM Tours WHERE tour_id_pk = :tourId")
    public abstract void deleteTourWpByTourId(int tourId);

    public long insertWithTimestamp(Tour tour) {
        tour.setModifiedAt(System.currentTimeMillis());
        return insertTour(tour);
    }

    public long[] insertWithTimestamps(List<Tour> tours) {
        long timestamp = System.currentTimeMillis();
        for (Tour tour : tours)
            tour.setModifiedAt(timestamp);
        return insertTours(tours);
    }

    public void updateWithTimestamp(Tour tour) {
        tour.setModifiedAt(System.currentTimeMillis());
        updateTour(tour);
    }

    @Query("SELECT server_tour_id, modified_at FROM Tours WHERE tour_id_pk IN (SELECT DISTINCT(tour_id) FROM MyTours)")
    public abstract List<TourServerIdTimestamp> getServerMyTourIds();

    @Query("SELECT server_tour_id, modified_at FROM Tours WHERE tour_id_pk IN (SELECT DISTINCT(tour_id) FROM FavouriteTours)")
    public abstract List<TourServerIdTimestamp> getServerFavTourIds();

    @Transaction
    @Query("SELECT * FROM Tours LEFT JOIN Users ON user_id = user_id_pk WHERE server_tour_id = :serverTourId")
    public abstract TourWithWpWithPaths getTourByServerTourIds(String serverTourId);

    @Transaction
    @Query("SELECT * FROM Tours LEFT JOIN Users ON user_id = user_id_pk WHERE server_tour_id = '' OR server_tour_id IS NULL")
    public abstract List<TourWithWpWithPaths> getToursWithNoServerTourIds();

    @Transaction
    @Query("SELECT * FROM Tours LEFT JOIN Users ON user_id = user_id_pk WHERE server_synced = :serverSynced")
    public abstract List<TourWithWpWithPaths> getToursByServerSynced(boolean serverSynced);

    @Transaction
    @Query("SELECT * FROM Tours LEFT JOIN Users ON Tours.user_id = Users.user_id_pk WHERE tour_id_pk IN (SELECT DISTINCT(tour_id) FROM FavouriteTours)")
    public abstract List<TourWithWpWithPaths> getFavouriteToursWithTourWps();

    @Transaction
    @Query("SELECT * FROM Tours LEFT JOIN Users ON user_id = user_id_pk WHERE tour_id_pk IN (SELECT DISTINCT(tour_id) FROM MyTours)")
    public abstract List<TourWithWpWithPaths> getMyToursWithTourWps();
}
