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

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertFavouriteTour(FavouriteTour favouriteTour);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    long[] insertFavouriteTours(List<FavouriteTour> favouriteTours);

    @Update
    void updateFavouriteTour(FavouriteTour favouriteTour);

    @Delete
    void deleteFavouriteTour(FavouriteTour favouriteTour);
}
