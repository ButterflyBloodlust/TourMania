/**
 * Copyright 2017 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * Modified for application-specific purposes.
 */

package com.hal9000.tourmania;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.LocationResult;
import com.hal9000.tourmania.rest_api.RestClient;
import com.hal9000.tourmania.rest_api.tour_guides.TourGuidesService;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LocationUpdatesBroadcastReceiver extends BroadcastReceiver {

    static final String ACTION_PROCESS_UPDATES =
            "com.hal9000.tourmania.locationupdatespendingintent.action" +
                    ".PROCESS_UPDATES";
    private static final String TAG = LocationUpdatesBroadcastReceiver.class.getSimpleName();

    // Static variables to reduce operations during subsequent onReceive calls (since service runs in background).
    private static boolean notifiedWorking;
    private static TourGuidesService client;
    private static Callback<Void> callback;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_PROCESS_UPDATES.equals(action)) {
                LocationResult result = LocationResult.extractResult(intent);
                if (result != null) {
                    Location lastLocation = result.getLastLocation();
                    sendLocationToServer(context, lastLocation);
                    //LocationServiceUtils.setLocationUpdatesResult(context, locations);
                    if (!notifiedWorking) {
                        LocationServiceUtils.sendNotification(context, "Active");
                        //Log.d("crashTest", "notifiedWorking = false");
                        notifiedWorking = true;
                    }
                    Log.d("crashTest", LocationServiceUtils.getLocationUpdatesResult(context));
                }
            }
        }
    }

    private void sendLocationToServer(final Context context, Location lastLocation) {
        if (client == null)
            client = RestClient.createService(TourGuidesService.class,
                    SharedPrefUtils.getDecryptedString(context, MainActivity.getLoginTokenKey()));
        if (callback == null)
            callback = new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    Log.d("crashTest", "sendLocationToServer onResponse");
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    try {
                        Toast.makeText(context, "A connection error has occurred", Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        if (BuildConfig.DEBUG)
                            e.printStackTrace();
                    }
                    Log.d("crashTest", "sendLocationToServer onFailure");
                }
            };
        Call<Void> call = client.updateTourGuideLocation(lastLocation.getLongitude(), lastLocation.getLatitude());
        call.enqueue(callback);
    }

    public static void shutdown() {
        notifiedWorking = false;
        callback = null;
        client = null;
    }
}