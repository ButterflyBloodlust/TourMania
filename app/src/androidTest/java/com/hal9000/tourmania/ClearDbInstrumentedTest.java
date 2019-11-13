package com.hal9000.tourmania;

import android.content.Context;

import com.hal9000.tourmania.database.AppDatabase;

import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ClearDbInstrumentedTest {
    @Test
    public void clearDb() {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();

        assertEquals("com.hal9000.tourmania", appContext.getPackageName());

        AppDatabase.getInstance(appContext).clearAllTables();
    }
}
