package com.hal9000.tourmania.ui.tour_guides;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.hal9000.tourmania.AppUtils;
import com.hal9000.tourmania.MainActivity;
import com.hal9000.tourmania.MainActivityViewModel;
import com.hal9000.tourmania.R;
import com.hal9000.tourmania.model.TourWithWpWithPaths;
import com.hal9000.tourmania.model.User;
import com.hal9000.tourmania.rest_api.RestClient;
import com.hal9000.tourmania.rest_api.files_upload_download.FileDownloadImageObj;
import com.hal9000.tourmania.rest_api.files_upload_download.FileUploadDownloadService;
import com.hal9000.tourmania.rest_api.files_upload_download.TourFileDownloadResponse;
import com.hal9000.tourmania.rest_api.files_upload_download.TourGuideFileDownloadResponse;
import com.hal9000.tourmania.rest_api.tour_guides.TourGuidesService;
import com.hal9000.tourmania.ui.InfiniteTourGuideAdapter;
import com.hal9000.tourmania.ui.OnLoadMoreListener;
import com.hal9000.tourmania.ui.tour_guide_details.TourGuideDetailsFragment;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static com.hal9000.tourmania.ui.user_settings.UserSettingsFragment.USERS_CACHE_DIR_NAME;

public class TourGuidesFragment extends Fragment {

    private TourGuidesViewModel tourGuidesViewModel;
    private MainActivityViewModel activityViewModel;
    private RecyclerView recyclerView;
    private InfiniteTourGuideAdapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;
    private List<User> tourGuides = new ArrayList<>(100);
    private int pageNumber = 1;
    private boolean reachedEnd = false;
    private double longitude = 0.0;
    private double latitude = 0.0;
    private int retries = 0;
    private int checkingDetailsTourGuideIndex = -1;
    private String queryText;
    private boolean inSearchMode = false;

    private static final int RETRIES_LIMIT = 0;
    private static final int PAGE_SIZE = 10;
    public static final String TOUR_GUIDES_SEARCH_CACHE_DIR_NAME = "search";

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityViewModel = ViewModelProviders.of(requireActivity()).get(MainActivityViewModel.class);
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        tourGuidesViewModel = ViewModelProviders.of(this).get(TourGuidesViewModel.class);
        View root = inflater.inflate(R.layout.fragment_tour_guides, container, false);

        //generateTestDataset(40);

        if (!tourGuides.isEmpty() && checkingDetailsTourGuideIndex != -1) {
            Bundle bundle = activityViewModel.getAndClearBundle(TourGuideDetailsFragment.class);
            if (bundle != null) {
                float newRatingVal = bundle.getFloat(TourGuideDetailsFragment.NEW_TOUR_GUIDE_RATING_VAL_BUNDLE_KEY, -1.0f);
                int newRatingCount = bundle.getInt(TourGuideDetailsFragment.NEW_TOUR_GUIDE_RATING_COUNT_BUNDLE_KEY, -1);
                if (newRatingVal != -1.0f && newRatingCount != -1) {
                    tourGuides.get(checkingDetailsTourGuideIndex).setRateVal(newRatingVal);
                    tourGuides.get(checkingDetailsTourGuideIndex).setRateCount(newRatingCount);
                    mAdapter.notifyItemChanged(checkingDetailsTourGuideIndex);
                }
            }
        }

        Context context = requireContext();
        if (AppUtils.isWifiEnabled(context) && AppUtils.isLocationEnabled(context)) {
            createRecyclerView(root);
            getTourGuidesOnLastLocation();
        }
        else {
            root.findViewById(R.id.text_enable_wifi_loc_msg).setVisibility(View.VISIBLE);
        }
        return root;
    }

    private void getTourGuidesOnLastLocation(){
        Context context = requireContext();
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            return;

        FusedLocationProviderClient mFusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext());
        mFusedLocationClient.getLastLocation()
                .addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            longitude = location.getLongitude();
                            latitude = location.getLatitude();
                            initRecommendedTourGuides();
                        }
                        else {
                            Context context = getContext();
                            if (context != null)
                                Toast.makeText(context,"Could not access user location",Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //Log.d("crashTest", "Error trying to get last location");
                        e.printStackTrace();
                        Context context = getContext();
                        if (context != null)
                            Toast.makeText(context,"Could not access user location",Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void initRecommendedTourGuides() {
        mAdapter.setLoading();
        loadTourGuidesFromServerDb();
    }

    private void loadTourGuidesFromServerDb() {
        TourGuidesService client = RestClient.createService(TourGuidesService.class);
        //Log.d("crashTest", "Missing tour: " + Integer.toString(missingTourIds.size()));
        Call<List<User>> call;
        if (inSearchMode)
            call = client.searchTourGuidesByPhrase(queryText, pageNumber++);
        else
            call = client.getNearbyTourGuides(longitude, latitude, pageNumber++);
        call.enqueue(new Callback<List<User>>() {
            @Override
            public void onResponse(Call<List<User>> call, Response<List<User>> response) {
                //Log.d("crashTest", "loadTourGuidesFromServerDb onResponse");
                List<User> tourGuidesList = response.body();
                if (tourGuidesList != null) {
                    //Log.d("crashTest", "tourGuidesList size = " + tourGuidesList.size());
                    if (tourGuidesList.isEmpty()) {
                        if (pageNumber == 1)
                            Toast.makeText(requireContext(), "No results found", Toast.LENGTH_SHORT).show();
                        reachedEnd = true;
                        mAdapter.setLoaded();
                    } else {
                        if (tourGuidesList.size() < PAGE_SIZE)
                            reachedEnd = true;
                        int oldSize = mAdapter.mDataset.size();
                        mAdapter.mDataset.addAll(tourGuidesList);
                        mAdapter.notifyItemRangeInserted(oldSize, tourGuidesList.size());
                        mAdapter.setLoaded();
                        loadTourGuidesImagesFromServerDb(tourGuidesList);
                    }
                }
                else
                    mAdapter.setLoaded();
            }

            @Override
            public void onFailure(Call<List<User>> call, Throwable t) {
                mAdapter.setLoaded();
                t.printStackTrace();
                Context context = getContext();
                if (context != null)
                    Toast.makeText(context,"A connection error has occurred\nRetries left: " + (RETRIES_LIMIT - retries),Toast.LENGTH_SHORT).show();
                if (retries++ >= RETRIES_LIMIT)
                    reachedEnd = true;
                //Log.d("crashTest", "loadTourGuidesFromServerDb onFailure");
            }
        });
    }

    private void loadTourGuidesImagesFromServerDb(List<User> tourGuidesList) {
        final List<String> missingTourGuidesNicknames = new ArrayList<>(tourGuidesList.size());
        for (User tourGuide : tourGuidesList) {
            missingTourGuidesNicknames.add(tourGuide.getUsername());
        }
        FileUploadDownloadService client = RestClient.createService(FileUploadDownloadService.class);
        Call<List<TourGuideFileDownloadResponse>> call = client.downloadMultipleTourGuidesImagesFiles(missingTourGuidesNicknames);
        call.enqueue(new Callback<List<TourGuideFileDownloadResponse>>() {
            @Override
            public void onResponse(Call<List<TourGuideFileDownloadResponse>> call, Response<List<TourGuideFileDownloadResponse>> response) {
                //Log.d("crashTest", "loadToursImagesFromServerDb onResponse");
                final List<TourGuideFileDownloadResponse> res = response.body();
                if (res != null && res.size() > 0) {
                    try {
                        loadTourGuidesImagesFromServerDbProcessResponse(res);
                    } catch (Exception e) { // IOException
                        //Log.d("crashTest", "Unknown expection while reading file download response");
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<List<TourGuideFileDownloadResponse>> call, Throwable t) {
                t.printStackTrace();
                //Log.d("crashTest", "loadToursImagesFromServerDb onFailure");
            }
        });
    }

    private void loadTourGuidesImagesFromServerDbProcessResponse(List<TourGuideFileDownloadResponse> res) {
        //Log.d("crashTest", "Missing users: " + Integer.toString(res.size()));
        Context context = getContext();
        if (context == null)
            return;
        for (TourGuideFileDownloadResponse tourGuideFileDownloadResponse : res) {
            if (tourGuideFileDownloadResponse.image != null) {
                File file = AppUtils.saveImageFromBase64(context, tourGuideFileDownloadResponse.image.base64,
                        tourGuideFileDownloadResponse.image.mime, USERS_CACHE_DIR_NAME);
                for (int i = 0; i < mAdapter.mDataset.size(); i++) {
                    User t = mAdapter.mDataset.get(i);
                    if (t.getUsername().equals(tourGuideFileDownloadResponse.username)) {
                        t.setUserImgPath(file.toURI().toString());
                        while (recyclerView.isComputingLayout());
                        mAdapter.notifyItemChanged(i);
                        break;
                    }
                }
            }
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        inflater.inflate(R.menu.action_search_tour_guides, menu);
        MenuItem item = menu.findItem(R.id.action_search);

        final SearchView searchView = new SearchView(((MainActivity) getActivity()).getSupportActionBar().getThemedContext());
        item.setActionView(searchView);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String queryText) {
                searchView.clearFocus();
                TourGuidesFragment.this.queryText = queryText;
                resetState();
                inSearchMode = true;
                mAdapter.setLoading();
                loadTourGuidesFromServerDb();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return true;
            }

        });
        item.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                resetState();
                inSearchMode = false;
                mAdapter.setLoading();
                loadTourGuidesFromServerDb();
                return true;
            }
        });
    }

    private void resetState() {
        String dirPath = requireContext().getExternalCacheDir() + File.separator + TOUR_GUIDES_SEARCH_CACHE_DIR_NAME;
        File projDir = new File(dirPath);
        if (projDir.exists()) {
            AppUtils.deleteDir(projDir, -1);
        }
        int oldSize = mAdapter.mDataset.size();
        mAdapter.mDataset.clear();
        mAdapter.notifyItemRangeRemoved(0, oldSize);
        pageNumber = 1;
        reachedEnd = false;
    }

    private void createRecyclerView(final View root) {
        // Create recycler view
        recyclerView = (RecyclerView) root.findViewById(R.id.tour_guides_recycler_view);
        recyclerView.setHasFixedSize(true);
        // use a linear layout manager
        layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);

        // specify an adapter
        mAdapter = new InfiniteTourGuideAdapter(tourGuides,
                new InfiniteTourGuideAdapter.ToursAdapterCallback() {
                    @Override
                    public Context getContext() {
                        return requireContext();
                    }

                    @Override
                    public void navigateToViewTour(int position) {
                        checkingDetailsTourGuideIndex = position;
                        NavDirections navDirections =  TourGuidesFragmentDirections
                                .actionNavTourGuidesToNavTourGuideDetails(tourGuides.get(position).getUsername());
                        Navigation.findNavController(requireView()).navigate(navDirections);
                    }
                },
                R.layout.tour_guide_rec_view_row,
                recyclerView);

        mAdapter.setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore() {
                if (!reachedEnd) {
                    mAdapter.setLoading();
                    loadTourGuidesFromServerDb();
                }
            }
        });
        recyclerView.setAdapter(mAdapter);
    }
}