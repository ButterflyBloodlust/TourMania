package com.hal9000.tourmania.ui.create_tour;

import android.app.Application;
import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.hal9000.tourmania.AppUtils;
import com.hal9000.tourmania.BuildConfig;
import com.hal9000.tourmania.FileUtil;
import com.hal9000.tourmania.MainActivity;
import com.hal9000.tourmania.SharedPrefUtils;
import com.hal9000.tourmania.database.AppDatabase;
import com.hal9000.tourmania.database.FavouriteTourDAO;
import com.hal9000.tourmania.database.MyTourDAO;
import com.hal9000.tourmania.database.TourDAO;
import com.hal9000.tourmania.model.FavouriteTour;
import com.hal9000.tourmania.model.MyTour;
import com.hal9000.tourmania.model.Tour;
import com.hal9000.tourmania.model.TourTag;
import com.hal9000.tourmania.model.TourWaypoint;
import com.hal9000.tourmania.model.TourWithWpWithPaths;
import com.hal9000.tourmania.model.TourWpWithPicPaths;
import com.hal9000.tourmania.model.User;
import com.hal9000.tourmania.rest_api.RestClient;
import com.hal9000.tourmania.rest_api.files_upload_download.FileDownloadImageObj;
import com.hal9000.tourmania.rest_api.files_upload_download.TourFileDownloadResponse;
import com.hal9000.tourmania.rest_api.files_upload_download.FileUploadDownloadService;
import com.hal9000.tourmania.rest_api.tour_guides.GetTourGuideLocationResponse;
import com.hal9000.tourmania.rest_api.tour_guides.TourGuidesService;
import com.hal9000.tourmania.rest_api.tours.ToursService;
import com.hal9000.tourmania.rest_api.tours.TourUpsertResponse;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.internal.EverythingIsNonNull;

import static com.hal9000.tourmania.ui.search_tours.SearchToursFragment.TOUR_SEARCH_CACHE_DIR_NAME;

public class CreateTourSharedViewModel extends AndroidViewModel {

    private MutableLiveData<Tour> tour = new MutableLiveData<Tour>();
    private MutableLiveData<GetTourGuideLocationResponse> tourGuideLoc = new MutableLiveData<GetTourGuideLocationResponse>();
    private ArrayList<TourWpWithPicPaths> tourWaypointList = new ArrayList<>();
    private ArrayList<TourTag> tourTagsList = new ArrayList<>();
    private User user;
    private int choosenLocateWaypointIndex = -1;
    private MutableLiveData<Boolean> loadedFromLocalDb = new MutableLiveData<Boolean>(false);
    private MutableLiveData<Boolean> loadedFromServerDb = new MutableLiveData<Boolean>(false);
    private boolean editingEnabled = true;
    private boolean editingInitialised = false;
    private boolean editingPossible = false;
    private int viewType = -1;
    private boolean checkedForCacheLink = false;
    boolean savedToLocalDb = false;

    static int VIEW_TYPE_NONE = 0;
    static int VIEW_TYPE_MY_TOUR = 1;
    static int VIEW_TYPE_FAV_TOUR = 2;

    public CreateTourSharedViewModel(@NonNull Application application) {
        super(application);
        //Log.d("crashTest", "CreateTourSharedViewModel.CreateTourSharedViewModel()");
        tour.setValue(new Tour());
    }

    public TourWithWpWithPaths getTourWithWpWithPaths() {
        TourWithWpWithPaths tourWithWpWithPaths = new TourWithWpWithPaths();
        tourWithWpWithPaths.tour = getTour();
        tourWithWpWithPaths.user = getUser();
        tourWithWpWithPaths._tourWpsWithPicPaths = getTourWaypointList();
        tourWithWpWithPaths.tourTags = getTourTagsList();
        return tourWithWpWithPaths;
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
        return tour.getValue();
    }

    public LiveData<Tour> getTourLiveData() {
        return tour;
    }

    public LiveData<GetTourGuideLocationResponse> getTourGuideLocLiveData() {
        return tourGuideLoc;
    }

    public Future saveTourToDb(final Context context, final int saveAsType) {
        // Currently does NOT handle additional waypoint pics (PicturePath / TourWpWithPicPaths)
        //Log.d("crashTest", "saveTourToDb()");
        final Future future = saveTourToLocalDb(context, saveAsType);
        if (AppUtils.isUserLoggedIn(context) && editingPossible && saveAsType == VIEW_TYPE_MY_TOUR)
            saveTourToServerDb(context);
        else if (saveAsType == VIEW_TYPE_FAV_TOUR)
            addTourToServerFavs(context);
        return future;
    }

    public Future<?> saveTourToLocalDb(final Context context, final int saveAsType) {
        return AppDatabase.databaseWriteExecutor.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    //Log.d("crashTest", "run()");
                    AppDatabase appDatabase = AppDatabase.getInstance(context);
                    TourDAO tourDAO = appDatabase.tourDAO();

                    TourWithWpWithPaths tourWithWpWithPaths = tourDAO.getTourByServerTourIds(getTour().getServerTourId());
                    int tourId = -1;
                    if (saveAsType == VIEW_TYPE_MY_TOUR ||
                            ((tourWithWpWithPaths == null || TextUtils.isEmpty(getTour().getServerTourId()))
                                    && saveAsType == VIEW_TYPE_FAV_TOUR)) {
                        // Insert the tour into Room db
                        tourId = (int) tourDAO.insertWithTimestamp(getTour());
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
                            tourWaypointList.get(i).tourWaypoint.setTourWpId((int) wpsIds[i]);
                        }

                        for (TourTag tourTag : tourTagsList) {
                            tourTag.setTourId(tourId);
                        }
                        appDatabase.tourTagDAO().insertTourTags(tourTagsList);
                    } else {
                        tourId = tourWithWpWithPaths.tour.getTourId();
                    }

                    savedToLocalDb = true;
                    if (saveAsType == VIEW_TYPE_MY_TOUR) {
                        MyTourDAO myTourDAO = appDatabase.myTourDAO();
                        MyTour myTour = myTourDAO.getMyTourByTourId(tourId);
                        if (myTour == null)
                            myTourDAO.insertMyTour(new MyTour(tourId));
                        else {
                            myTour.setTourId(tourId);
                            myTourDAO.updateMyTour(myTour);
                        }
                    } else if (saveAsType == VIEW_TYPE_FAV_TOUR) {
                        FavouriteTourDAO favouriteTourDAO = appDatabase.favouriteTourDAO();
                        FavouriteTour favouriteTour = favouriteTourDAO.getFavouriteTourByTourId(tourId);
                        if (favouriteTour == null)
                            favouriteTourDAO.insertFavouriteTour(new FavouriteTour(tourId));
                        else {
                            favouriteTour.setTourId(tourId);
                            favouriteTourDAO.updateFavouriteTour(favouriteTour);
                        }
                        getTour().setInFavs(true);
                    } else {
                        throw new RuntimeException("Unknown 'save as' type for given tour");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void addTourToServerFavs(final Context context) {
        ToursService client = RestClient.createService(ToursService.class,
                SharedPrefUtils.getDecryptedString(context, MainActivity.getLoginTokenKey()));
        // Add tour to favourites on server
        Call<ResponseBody> call = client.addTourToFavs(getTour().getServerTourId());
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                getTour().setInFavs(true);
                //Log.d("crashTest", "addTourToFavs onResponse()");
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                t.printStackTrace();
                //Log.d("crashTest", "addTourToFavs onFailure()");
            }
        });
    }

    public void deleteTourFromFavs(final Context context) {
        AppDatabase appDatabase = AppDatabase.getInstance(context);
        FavouriteTourDAO favouriteTourDAO = appDatabase.favouriteTourDAO();
        FavouriteTour favouriteTour = favouriteTourDAO.getFavouriteTourByTourId(getTour().getTourId());
        // Remove tour from favourites
        if (favouriteTour != null) {
            favouriteTourDAO.deleteFavouriteTour(favouriteTour);
            MyTourDAO myTourDAO = appDatabase.myTourDAO();
            MyTour myTour = myTourDAO.getMyTourByTourId(favouriteTour.getTourId());
            if (myTour == null) {
                // Delete tour from db / files cache
                TourDAO tourDAO = appDatabase.tourDAO();
                TourWithWpWithPaths tourWithWpWithPaths = tourDAO.getTourWithTourWps(favouriteTour.getTourId());
                tourDAO.deleteTourWp(tourWithWpWithPaths.tour);
                // delete files related to tour, stored in app's dirs
                String mainImgPath = tourWithWpWithPaths.tour.getTourImgPath();
                if (mainImgPath != null)
                    new File(context.getExternalCacheDir(), new File(mainImgPath).getName()).delete();
                for (TourWpWithPicPaths tourWpWithPicPaths : tourWithWpWithPaths._tourWpsWithPicPaths) {
                    mainImgPath = tourWpWithPicPaths.tourWaypoint.getMainImgPath();
                    if (mainImgPath != null)
                        new File(context.getExternalCacheDir(), new File(mainImgPath).getName()).delete();
                }
            }
        }
        getTour().setInFavs(false);
        ToursService client = RestClient.createService(ToursService.class,
                SharedPrefUtils.getDecryptedString(context, MainActivity.getLoginTokenKey()));
        Call<ResponseBody> call = client.deleteTourFromFavs(getTour().getServerTourId());
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Log.d("crashTest", "deleteTourFromFavs onResponse()");
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                t.printStackTrace();
                Log.d("crashTest", "deleteTourFromFavs onFailure()");
            }
        });
    }

    private void saveTourToServerDb(final Context context) {
        ToursService client = RestClient.createService(ToursService.class, SharedPrefUtils.getDecryptedString(context, MainActivity.getLoginTokenKey()));
        Call<TourUpsertResponse> call = client.upsertTour(new TourWithWpWithPaths(getTour(), tourTagsList, tourWaypointList));
        call.enqueue(new Callback<TourUpsertResponse>() {
            @Override
            @EverythingIsNonNull
            public void onResponse(Call<TourUpsertResponse> call, final Response<TourUpsertResponse> response) {
                if (response.isSuccessful()) {
                    getTour().setServerSynced(true);
                    TourUpsertResponse resp = response.body();
                    if (resp != null && resp.tourServerId != null)
                        getTour().setServerTourId(resp.tourServerId);

                    AppDatabase.databaseWriteExecutor.submit(new Runnable() {
                        @Override
                        public void run() {
                            AppDatabase appDatabase = AppDatabase.getInstance(context);
                            appDatabase.tourDAO().updateTour(getTour());  // purposely without timestamp
                        }
                    });

                    sendImgFilesToServer(context);
                } else {
                    System.out.println(response.errorBody());
                }
            }

            @Override
            @EverythingIsNonNull
            public void onFailure(Call<TourUpsertResponse> call, Throwable t) {
                t.printStackTrace();
            }
        });
    }

    public void sendImgFilesToServer(final Context context) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                // Compress and send tour images
                LinkedList<File> imageFiles = new LinkedList<>();
                File imgFile = FileUtil.compressImage(context, getTour().getTourImgPath(), "TourPictures");
                //if (imgFile != null)
                    imageFiles.addLast(imgFile);

                for (TourWpWithPicPaths tourWpWithPicPaths : tourWaypointList) {
                    imgFile = FileUtil.compressImage(context, tourWpWithPicPaths.tourWaypoint.getMainImgPath(), "TourPictures");
                    //if (imgFile != null)
                        imageFiles.addLast(imgFile);
                }

                sendImgFilesToServerHelper(imageFiles, context);
            }
        }).start();
    }

    public void sendImgFilesToServerHelper(LinkedList<File> imageFiles, final Context context) {
        // create list of file parts
        List<MultipartBody.Part> parts = new ArrayList<>(imageFiles.size());

        // add dynamic amount
        int i = 0;
        for (File imgFile : imageFiles) {
            String label;
            label = Integer.toString(i++);
            //label = imgFile.getName();
            parts.add(RestClient.prepareFilePart(label, imgFile));
        }

        // add the description part within the multipart request
        RequestBody description = RestClient.createPartFromString(getTour().getServerTourId());

        // create upload service client
        FileUploadDownloadService service = RestClient.createService(FileUploadDownloadService.class, SharedPrefUtils.getDecryptedString(context, MainActivity.getLoginTokenKey()));

        // execute the request
        Call<ResponseBody> call = service.uploadMultipleTourImagesFilesDynamic(description, parts);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                //Log.d("crashTest", "sendImgFilesToServerHelper.onResponse()");
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                //Log.d("crashTest", "sendImgFilesToServerHelper.onFailure()");
            }
        });
    }

    // Needs to be called from non-ui thread
    private void detectViewType(final Context context, final int tourId) {
        AppDatabase appDatabase = AppDatabase.getInstance(context);
        MyTour myTour = appDatabase.myTourDAO().getMyTourByTourId(tourId);
        if (myTour != null)
            viewType = VIEW_TYPE_MY_TOUR;
        else
            viewType = VIEW_TYPE_FAV_TOUR;
    }

    public int getViewType() {
        return viewType;
    }

    public Future loadTourFromDb(final int tourId, final Context context) {
        return AppDatabase.databaseWriteExecutor.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    if (!loadedFromLocalDb.getValue()) {
                        AppDatabase appDatabase = AppDatabase.getInstance(context);
                        TourWithWpWithPaths tourWpWithPicPaths = appDatabase.tourDAO().getTourWithTourWps(tourId);
                        tour.postValue(tourWpWithPicPaths.tour);
                        tourWaypointList.addAll(tourWpWithPicPaths.getSortedTourWpsWithPicPaths());
                        tourTagsList.addAll(tourWpWithPicPaths.tourTags);
                        user = tourWpWithPicPaths.user;
                        detectViewType(context, tourWpWithPicPaths.tour.getTourId());
                        loadedFromLocalDb.postValue(true);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public boolean checkIfInCachedFavs(final Context context) {
        AppDatabase appDatabase = AppDatabase.getInstance(context);
        FavouriteTourDAO favouriteTourDAO = appDatabase.favouriteTourDAO();
        int tourId = getTour().getTourId();
        FavouriteTour favouriteTour = favouriteTourDAO.getFavouriteTourByTourId(tourId);
        ToursService client = RestClient.createService(ToursService.class,
                SharedPrefUtils.getDecryptedString(context, MainActivity.getLoginTokenKey()));
        return favouriteTour != null;
    }

    public void loadTourFromServerDb(final String tourServerId, final Context context, final String subDirName) {
        if (!loadedFromServerDb.getValue()) {
            ToursService client = RestClient.createService(ToursService.class, SharedPrefUtils.getDecryptedString(context, MainActivity.getLoginTokenKey()));
            Call<TourWithWpWithPaths> call = client.getTour(tourServerId);
            call.enqueue(new Callback<TourWithWpWithPaths>() {
                @Override
                public void onResponse(Call<TourWithWpWithPaths> call, final Response<TourWithWpWithPaths> response) {
                    if (response.isSuccessful()) {
                        //Log.d("crashTest", "loadToursFromServerDb onResponse");
                        TourWithWpWithPaths tourWithTourWps = response.body();
                        if (tourWithTourWps != null) {
                            user = tourWithTourWps.user;
                            if (user.getUsername().equals(SharedPrefUtils.getDecryptedString(context, MainActivity.getUsernameKey())))
                                setEditingPossible(true);
                            tour.setValue(tourWithTourWps.tour);
                            loadedFromServerDb.setValue(true);
                            tourTagsList.addAll(tourWithTourWps.tourTags);
                            tourWaypointList.addAll(tourWithTourWps.getSortedTourWpsWithPicPaths());

                            linkToCacheIfExistsWithSrvId(context);
                            loadTourImagesFromServerDb(tourServerId, context, subDirName);
                        }
                    }
                }

                @Override
                public void onFailure(Call<TourWithWpWithPaths> call, Throwable t) {
                    t.printStackTrace();
                    //Log.d("crashTest", "loadToursFromServerDb onFailure");
                }
            });
        }
    }

    public void loadTourImagesFromServerDb(String tourServerId, final Context context, final String subDirName) {
        FileUploadDownloadService client = RestClient.createService(FileUploadDownloadService.class);
        //Log.d("crashTest", "Missing tour: " + Integer.toString(missingTourIds.size()));
        Call<TourFileDownloadResponse> call = client.getTourImages(tourServerId);
        call.enqueue(new Callback<TourFileDownloadResponse>() {
            @Override
            public void onResponse(Call<TourFileDownloadResponse> call, Response<TourFileDownloadResponse> response) {
                TourFileDownloadResponse res = response.body();
                if (res != null) {
                    try {
                        loadToursImagesFromServerDbProcessResponse(res, context, subDirName);
                        tour.setValue(getTour());
                    } catch (Exception e) { // IOException
                        //Log.d("crashTest", "Unknown expection while reading file download response");
                        e.printStackTrace();
                    }
                }
                //Log.d("crashTest", "loadToursImagesFromServerDb onResponse");
            }

            @Override
            public void onFailure(Call<TourFileDownloadResponse> call, Throwable t) {
                t.printStackTrace();
                //Log.d("crashTest", "loadToursImagesFromServerDb onFailure");
            }
        });
    }

    private void loadToursImagesFromServerDbProcessResponse(TourFileDownloadResponse tourFileDownloadResponse, final Context context, final String subDirName) {
        //Log.d("crashTest", "Missing tour: " + Integer.toString(res.size()));
        if (tourFileDownloadResponse.images != null) {
            // for each image in tour
            for (Map.Entry<String, FileDownloadImageObj> entry : tourFileDownloadResponse.images.entrySet()) {
                FileDownloadImageObj fileDownloadImageObj = entry.getValue();
                //Log.d("crashTest", entry.getKey() + " / " + entry.getValue());
                if (fileDownloadImageObj.base64 != null && fileDownloadImageObj.mime != null) {
                    File file = AppUtils.saveImageFromBase64(context, fileDownloadImageObj.base64, fileDownloadImageObj.mime, subDirName);
                    // process main tour image
                    if (entry.getKey().equals("0")) {
                        //Log.d("crashTest", "updating main tour image");
                        getTour().setTourImgPath(file.toURI().toString());
                    }
                    // process tour waypoints images
                    else {
                        //Log.d("crashTest", "updating tour waypoint image");
                        TourWaypoint tourWaypoint = tourWaypointList.get(Integer.parseInt(entry.getKey()) - 1).tourWaypoint;
                        tourWaypoint.setMainImgPath(file.toURI().toString());
                    }
                }
            }
        }
    }

    public void linkToCacheIfExistsWithSrvId(final Context context) {
        if (TextUtils.isEmpty(getTour().getServerTourId()))
            return;
        AppDatabase.databaseWriteExecutor.submit(new Runnable() {
            @Override
            public void run() {
                AppDatabase appDatabase = AppDatabase.getInstance(context);
                TourWithWpWithPaths tourWithWpWithPaths = appDatabase.tourDAO().getTourByServerTourIds(getTour().getServerTourId());
                if (tourWithWpWithPaths != null)
                    getTour().setTourId(tourWithWpWithPaths.tour.getTourId());
                checkedForCacheLink = true;
                tour.setValue(getTour());
            }
        });
    }

    public void saveTourRatingToLocalDb(final Context context) {
        AppDatabase.databaseWriteExecutor.submit(new Runnable() {
            @Override
            public void run() {
                AppDatabase appDatabase = AppDatabase.getInstance(context);
                TourWithWpWithPaths tourWithWpWithPaths = appDatabase.tourDAO().getTourByServerTourIds(getTour().getServerTourId());
                if (tourWithWpWithPaths != null) {
                    Tour dbTour = tourWithWpWithPaths.tour;
                    Tour thisTour = getTour();
                    dbTour.setRateCount(thisTour.getRateCount());
                    dbTour.setRateVal(thisTour.getRateVal());
                    dbTour.setMyRating(thisTour.getMyRating());
                    appDatabase.tourDAO().updateTour(dbTour);
                }
            }
        });
    }

    public void saveTourRatingToServerDb(final Context context) {
        ToursService client = RestClient.createService(ToursService.class, SharedPrefUtils.getDecryptedString(context, MainActivity.getLoginTokenKey()));
        //Log.d("crashTest", "Missing tour: " + Integer.toString(missingTourIds.size()));
        Call<ResponseBody> call = client.rateTour(getTour().getServerTourId(), getTour().getMyRating());
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                //Log.d("crashTest", "saveTourRatingToServerDb onResponse");
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                if (BuildConfig.DEBUG)
                    t.printStackTrace();
                //Log.d("crashTest", "saveTourRatingToServerDb onFailure");
            }
        });
    }

    public void updateTourRating(final Context context, float newRating) {
        Tour tour = getTour();
        float oldRating = tour.getMyRating();
        // if tour was not rated before by current user
        if (oldRating == 0.0f) {
            tour.setRateCount(tour.getRateCount() + 1);
            tour.setRateVal(tour.getRateVal() + newRating);
        }
        else {
            tour.setRateVal(tour.getRateVal() + newRating - oldRating);
        }
        tour.setMyRating(newRating);

        saveTourRatingToServerDb(context);
        saveTourRatingToLocalDb(context);
    }

    public void handleTourGuideLocation(@NonNull final Context context, String token) {
        TourGuidesService client = RestClient.createService(TourGuidesService.class,
                SharedPrefUtils.getDecryptedString(context, MainActivity.getLoginTokenKey()));
        Call<GetTourGuideLocationResponse> call = client.getTourGuideLocation(token);
        call.enqueue(new Callback<GetTourGuideLocationResponse>() {
            @Override
            public void onResponse(Call<GetTourGuideLocationResponse> call, Response<GetTourGuideLocationResponse> response) {
                Log.d("crashTest", "handleTourGuideLocation onResponse");
                if (response.isSuccessful()) {
                    GetTourGuideLocationResponse responseBody = response.body();
                    tourGuideLoc.setValue(responseBody);  // notify observers
                }
                else if (response.code()==401) {
                    tourGuideLoc.setValue(null);
                }
            }

            @Override
            public void onFailure(Call<GetTourGuideLocationResponse> call, Throwable t) {
                t.printStackTrace();
                if (context != null)
                    Toast.makeText(context,"A connection error has occurred",Toast.LENGTH_SHORT).show();
                Log.d("crashTest", "handleTourGuideLocation onFailure");
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

    public boolean isEditingPossible() {
        return editingPossible;
    }

    public void setEditingPossible(boolean editingPossible) {
        this.editingPossible = editingPossible;
    }

    public boolean isCheckedForCacheLink() {
        return checkedForCacheLink;
    }

    public boolean getLoadedFromLocalDb() {
        return loadedFromLocalDb.getValue();
    }

    public boolean isLoadedFromServerDb() {
        return loadedFromServerDb.getValue();
    }

    public User getUser() {
        return user;
    }

    public LiveData<Boolean> getLoadedFromDb() {
        return loadedFromLocalDb;
    }

    public LiveData<Boolean> getLoadedFromServerDb() {
        return loadedFromServerDb;
    }

    /*
    public void notifyTourWpsChanged() {
        // In case of advanced operations on observed lists in the future, replace with dedicated methods to work on list within ViewModel.
        tourWaypointList.setValue(tourWaypointList.getValue());
    }
    */

    @Override
    public void onCleared() {
        super.onCleared();
        Log.d("crashTest", "CreateTourSharedViewModel.onCleared()");
        try {
            if (!loadedFromLocalDb.getValue() && !savedToLocalDb) {
                // delete files related to tour, stored in app's dirs
                String mainImgPath = getTour().getTourImgPath();
                if (mainImgPath != null) {
                    new File(Uri.parse(mainImgPath).getPath()).delete();
                }
                for (TourWpWithPicPaths tourWpWithPicPaths : tourWaypointList) {
                    mainImgPath = tourWpWithPicPaths.tourWaypoint.getMainImgPath();
                    if (mainImgPath != null)
                        new File(Uri.parse(mainImgPath).getPath()).delete();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
