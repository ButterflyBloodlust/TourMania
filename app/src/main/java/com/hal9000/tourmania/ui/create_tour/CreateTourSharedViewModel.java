package com.hal9000.tourmania.ui.create_tour;

import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.hal9000.tourmania.AppUtils;
import com.hal9000.tourmania.FileUtil;
import com.hal9000.tourmania.MainActivity;
import com.hal9000.tourmania.SharedPrefUtils;
import com.hal9000.tourmania.database.AppDatabase;
import com.hal9000.tourmania.model.Tour;
import com.hal9000.tourmania.model.TourTag;
import com.hal9000.tourmania.model.TourWaypoint;
import com.hal9000.tourmania.model.TourWithWpWithPaths;
import com.hal9000.tourmania.model.TourWpWithPicPaths;
import com.hal9000.tourmania.rest_api.LoginResponse;
import com.hal9000.tourmania.rest_api.RestClient;
import com.hal9000.tourmania.rest_api.SignUpResponse;
import com.hal9000.tourmania.rest_api.TourSave;
import com.hal9000.tourmania.rest_api.TourUpsertResponse;
import com.hal9000.tourmania.rest_api.UserSignUp;
import com.mapbox.android.core.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Future;

import androidx.lifecycle.ViewModel;
import androidx.navigation.Navigation;
import id.zelory.compressor.Compressor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.internal.EverythingIsNonNull;

public class CreateTourSharedViewModel extends ViewModel {

    private Tour tour = new Tour();
    private ArrayList<TourWpWithPicPaths> tourWaypointList = new ArrayList<>();
    private ArrayList<TourTag> tourTagsList = new ArrayList<>();
    private int choosenLocateWaypointIndex = -1;
    private boolean loadedFromDb = false;
    private boolean editingEnabled = true;
    private boolean editingInitialised = false;

    public CreateTourSharedViewModel() {
        //Log.d("crashTest", "CreateTourSharedViewModel.CreateTourSharedViewModel()");
    }

    public ArrayList<TourWpWithPicPaths> getTourWaypointList() {
        return tourWaypointList;
    }

    public int getChoosenLocateWaypointIndex() {
        return choosenLocateWaypointIndex;
    }

    public void setChoosenLocateWaypointIndex(int choosenLocateWaypointIndex) {
        this.choosenLocateWaypointIndex = choosenLocateWaypointIndex;
    }

    public void removeChoosenLocateWaypointIndex() {
        choosenLocateWaypointIndex = -1;
    }

    public Tour getTour() {
        return tour;
    }

    public Future saveTourToDb(final Context context) {
        // Currently does NOT handle additional waypoint pics (PicturePath / TourWpWithPicPaths)
        //Log.d("crashTest", "saveTourToDb()");
        Future future = saveTourToLocalDb(context);
        saveTourToServerDb(context);
        return future;
    }

    private Future<?> saveTourToLocalDb(final Context context) {
        return AppDatabase.databaseWriteExecutor.submit(new Runnable() {
            @Override
            public void run() {
                //Log.d("crashTest", "run()");
                AppDatabase appDatabase = AppDatabase.getInstance(context);
                int tourId = (int)appDatabase.tourDAO().insertTour(getTour());
                getTour().setTourId(tourId);
                LinkedList<TourWaypoint> tourWps = new LinkedList<>();
                for (int i = 0; i < tourWaypointList.size(); i++) {
                    TourWaypoint tourWaypoint = tourWaypointList.get(i).tourWaypoint;
                    tourWaypoint.setTourId(tourId);
                    tourWaypoint.setWpOrder(i);
                    tourWps.addLast(tourWaypoint);
                }
                long[] wpsIds = appDatabase.tourWaypointDAO().insertTourWps(tourWps);
                for (int i = 0; i < wpsIds.length; i++) {
                    tourWaypointList.get(i).tourWaypoint.setTourWpId((int)wpsIds[i]);
                }

                for (TourTag tourTag : tourTagsList) {
                    tourTag.setTourId(tourId);
                }
                appDatabase.tourTagDAO().insertTourTags(tourTagsList);
            }
        });
    }

    private void saveTourToServerDb(final Context context) {
        TourSave client = RestClient.createService(TourSave.class, SharedPrefUtils.getString(context, MainActivity.getLoginTokenKey()));
        Call<TourUpsertResponse> call = client.upsertTour(new TourWithWpWithPaths(tour, tourTagsList, tourWaypointList));
        call.enqueue(new Callback<TourUpsertResponse>() {
            @Override
            @EverythingIsNonNull
            public void onResponse(Call<TourUpsertResponse> call, final Response<TourUpsertResponse> response) {
                if (response.isSuccessful()) {
                    AppDatabase.databaseWriteExecutor.submit(new Runnable() {
                        @Override
                        public void run() {
                            tour.setServerSynced(true);
                            TourUpsertResponse resp = response.body();
                            if (resp != null && resp.tourServerId != null)
                                tour.setServerTourId(resp.tourServerId);
                            AppDatabase appDatabase = AppDatabase.getInstance(context);
                            appDatabase.tourDAO().updateTour(tour);
                        }
                    });
                } else {
                    System.out.println(response.errorBody());
                }
            }

            @Override
            @EverythingIsNonNull
            public void onFailure(Call<TourUpsertResponse> call, Throwable t) {
                t.printStackTrace();
                //Log.d("crashTest", "saveTourToServerDb onFailure()");
            }
        });

        new Thread(new Runnable() {
            @Override
            public void run() {
                // Compress and send tour images
                compressMainTourImg(context, tour.getTourImgPath());
                for (TourWpWithPicPaths tourWpWithPicPaths : tourWaypointList) {
                    compressMainTourImg(context, tourWpWithPicPaths.tourWaypoint.getMainImgPath());
                }
            }
        }).start();
    }

    private void compressMainTourImg(final Context context, String imgPath) {
        if (!TextUtils.isEmpty(imgPath)) {
            try {
                File originalImageFile = FileUtil.from(context, Uri.parse(imgPath));
                if (!originalImageFile.exists())
                    return;
                String dirPath = context.getFilesDir().getAbsolutePath() + File.separator + "TourPictures";
                File projDir = new File(dirPath);
                if (!projDir.exists())
                    projDir.mkdirs();

                File compressedImageFile = new Compressor(context)
                        .setDestinationDirectoryPath(projDir.getAbsolutePath())
                        .compressToFile(originalImageFile);
                Log.d("crashTest", "compressMainTourImg(): " + compressedImageFile.getName());
            } catch (IOException e) {
                e.printStackTrace();
                Log.d("crashTest", "compressMainTourImg() IOException");
            }
        }
    }

    public Future loadTourFromDb(final int tourId, final Context context) {
        return AppDatabase.databaseWriteExecutor.submit(new Runnable() {
            @Override
            public void run() {
                if (!loadedFromDb) {
                    AppDatabase appDatabase = AppDatabase.getInstance(context);
                    TourWithWpWithPaths tourWpWithPicPaths = appDatabase.tourDAO().getTourWithTourWps(tourId);
                    tour = tourWpWithPicPaths.tour;
                    tourWaypointList.addAll(tourWpWithPicPaths.getSortedTourWpsWithPicPaths());
                    loadedFromDb = true;
                }
            }
        });
    }

    public boolean isEditingEnabled() {
        return editingEnabled;
    }

    public void setEditingEnabled(boolean editingEnabled) {
        this.editingEnabled = editingEnabled;
    }

    public void setInitialEditingEnabled(boolean editingEnabled) {
        if (!editingInitialised) {
            this.editingEnabled = editingEnabled;
            editingInitialised = true;
        }
    }

    public ArrayList<TourTag> getTourTagsList() {
        return tourTagsList;
    }

    /*
    public void notifyTourWpsChanged() {
        // In case of advanced operations on observed lists in the future, replace with dedicated methods to work on list within ViewModel.
        tourWaypointList.setValue(tourWaypointList.getValue());
    }
    */

    /*
    @Override
    public void onCleared() {
        super.onCleared();
        Log.d("crashTest", "CreateTourSharedViewModel.onCleared()");
    }
    */

}
