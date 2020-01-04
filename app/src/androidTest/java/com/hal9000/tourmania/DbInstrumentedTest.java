package com.hal9000.tourmania;

import android.content.Context;
import android.util.Log;

import com.hal9000.tourmania.database.AppDatabase;
import com.hal9000.tourmania.model.TourWithWpWithPaths;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import static org.junit.Assert.assertEquals;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class DbInstrumentedTest {
    private static final String TAG = DbInstrumentedTest.class.getSimpleName();
    @Test
    public void readDb() {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();

        assertEquals("com.hal9000.tourmania", appContext.getPackageName());

        AppDatabase appDatabase = AppDatabase.getInstance(appContext);
        //List<Tour> toursWithTourWps = AppDatabase.getInstance(requireContext()).tourDAO().getTours();
        List<TourWithWpWithPaths> loadedToursWithTourWps = null;
        loadedToursWithTourWps = appDatabase.tourDAO().getFavouriteToursWithTourWps();
        for (TourWithWpWithPaths tourWithWpWithPaths : loadedToursWithTourWps) {
            Log.d(TAG, "" + tourWithWpWithPaths.tour.getTourImgPath());
    }
    }
}
