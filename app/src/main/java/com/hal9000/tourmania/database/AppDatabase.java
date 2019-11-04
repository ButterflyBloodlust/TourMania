package com.hal9000.tourmania.database;

import android.content.Context;

import com.hal9000.tourmania.model.PicturePath;
import com.hal9000.tourmania.model.TourWaypoint;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;


@Database(entities = {TourWaypoint.class, PicturePath.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    private static final String LOG_TAG = AppDatabase.class.getSimpleName();
    private static final Object LOCK = new Object();
    private static final String DATABASE_NAME = "TourManiaDb";
    private static AppDatabase sInstance;

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
}
