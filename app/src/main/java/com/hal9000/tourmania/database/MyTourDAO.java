package com.hal9000.tourmania.database;

import com.hal9000.tourmania.model.MyTour;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

@Dao
public interface MyTourDAO {
    @Query("SELECT * FROM MyTours")
    List<MyTour> getMyTours();

    @Query("SELECT * FROM MyTours WHERE id = :myTourId")
    MyTour getMyTour(int myTourId);

    @Query("SELECT * FROM MyTours WHERE tour_id = :tourId")
    MyTour getMyTourByTourId(int tourId);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertMyTour(MyTour myTour);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    long[] insertMyTours(List<MyTour> myTour);

    @Update
    void updateMyTour(MyTour myTour);

    @Delete
    void deleteMyTour(MyTour myTour);
}
