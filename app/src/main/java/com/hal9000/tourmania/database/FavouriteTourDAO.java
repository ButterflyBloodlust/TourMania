package com.hal9000.tourmania.database;

import com.hal9000.tourmania.model.FavouriteTour;
import com.hal9000.tourmania.model.TourWithWpWithPaths;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

@Dao
public interface FavouriteTourDAO {
    @Query("SELECT * FROM FavouriteTours")
    List<FavouriteTour> getFavouriteTours();

    @Query("SELECT * FROM FavouriteTours WHERE id = :favouriteTourId")
    FavouriteTour getFavouriteTour(int favouriteTourId);

    @Query("SELECT * FROM FavouriteTours WHERE tour_id = :tourId")
    FavouriteTour getFavouriteTourByTourId(int tourId);

    @Query("SELECT tour_id FROM FavouriteTours")
    int[] getFavouriteTourForeignIds();

    @Query("SELECT FavouriteTours.tour_id FROM FavouriteTours INNER JOIN Tours ON FavouriteTours.tour_id = Tours.tour_id_pk WHERE server_tour_id != '' AND server_tour_id IS NOT NULL")
    int[] getFavTourForeignIdsWithTourServerIds();

    @Query("SELECT FavouriteTours.tour_id FROM FavouriteTours INNER JOIN Tours ON FavouriteTours.tour_id = Tours.tour_id_pk WHERE server_synced = :serverSynced")
    int[] getFavTourForeignIdsByServerSynced(boolean serverSynced);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertFavouriteTour(FavouriteTour favouriteTour);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    long[] insertFavouriteTours(List<FavouriteTour> favouriteTours);

    @Update
    void updateFavouriteTour(FavouriteTour favouriteTour);

    @Delete
    void deleteFavouriteTour(FavouriteTour favouriteTour);

    @Query("DELETE FROM FavouriteTours")
    void deleteAllFavouriteTours();
}
