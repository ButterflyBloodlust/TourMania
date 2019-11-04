package com.hal9000.tourmania.model;

import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

public interface PicturePathDAO {

    @Query("SELECT * FROM PicturePaths")
    LiveData<List<PicturePath>> getPicPaths();

    @Query("SELECT * FROM PicturePaths WHERE id = :picPathId")
    LiveData<PicturePath> getPicPath(int picPathId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertPicPath(PicturePath picturePath);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertPicPaths(List<PicturePath> picturePaths);

    @Update
    void updatePicPath(PicturePath picturePath);

    @Delete
    void deletePicPath(PicturePath picturePath);

    @Transaction
    @Query("SELECT * FROM TourWaypoints WHERE id IN (SELECT DISTINCT(tour_wp_id) FROM PicturePaths)")
    LiveData<List<TourWpWithPicPaths>> getTourWpsWithPaths();
}
