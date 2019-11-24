package com.hal9000.tourmania.database;

import com.hal9000.tourmania.model.TourTag;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

@Dao
public interface TourTagDAO {
    @Query("SELECT * FROM TourTags")
    List<TourTag> getTourTags();

    @Query("SELECT * FROM TourTags WHERE id = :tourTagId")
    TourTag getTourTag(int tourTagId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertTourTag(TourTag tourTag);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long[] insertTourTags(List<TourTag> tourTags);

    @Update
    void updateTourTag(TourTag tourTag);

    @Delete
    void deleteTourTag(TourTag tourTag);
}
