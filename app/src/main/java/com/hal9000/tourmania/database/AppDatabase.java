package com.hal9000.tourmania.database;

import android.content.Context;

import com.hal9000.tourmania.model.FavouriteTour;
import com.hal9000.tourmania.model.MyTour;
import com.hal9000.tourmania.model.PicturePath;
import com.hal9000.tourmania.model.Tour;
import com.hal9000.tourmania.model.TourTag;
import com.hal9000.tourmania.model.TourWaypoint;
import com.hal9000.tourmania.model.User;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;


@Database(entities = {Tour.class, TourWaypoint.class, PicturePath.class, TourTag.class, User.class,
        FavouriteTour.class, MyTour.class}, version = 3, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    private static final String LOG_TAG = AppDatabase.class.getSimpleName();
    private static final Object LOCK = new Object();
    private static final String DATABASE_NAME = "TourManiaDb";
    private static AppDatabase sInstance;
    private static final int NUMBER_OF_THREADS = 4;
    public static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    public static AppDatabase getInstance(Context context) {
        if (sInstance == null) {
            synchronized (LOCK) {

                sInstance = Room.databaseBuilder(context.getApplicationContext(),
                        AppDatabase.class, AppDatabase.DATABASE_NAME)
                        .build();
            }
        }
        return sInstance;
    }

    public abstract TourWaypointDAO tourWaypointDAO();
    public abstract PicturePathDAO picturePathDAO();
    public abstract TourDAO tourDAO();
    public abstract TourTagDAO tourTagDAO();
    public abstract UserDAO userDAO();
    public abstract FavouriteTourDAO favouriteTourDAO();
    public abstract MyTourDAO myTourDAO();
}
