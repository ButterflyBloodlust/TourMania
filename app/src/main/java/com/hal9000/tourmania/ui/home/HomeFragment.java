package com.hal9000.tourmania.ui.home;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
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
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.hal9000.tourmania.AppUtils;
import com.hal9000.tourmania.MainActivity;
import com.hal9000.tourmania.MainActivityViewModel;
import com.hal9000.tourmania.R;
import com.hal9000.tourmania.SharedPrefUtils;
import com.hal9000.tourmania.database.AppDatabase;
import com.hal9000.tourmania.model.Tour;
import com.hal9000.tourmania.model.TourWithWpWithPaths;
import com.hal9000.tourmania.rest_api.RestClient;
import com.hal9000.tourmania.rest_api.files_upload_download.FileDownloadImageObj;
import com.hal9000.tourmania.rest_api.files_upload_download.TourFileDownloadResponse;
import com.hal9000.tourmania.rest_api.files_upload_download.FileUploadDownloadService;
import com.hal9000.tourmania.rest_api.tours.ToursService;
import com.hal9000.tourmania.ui.InfiniteTourAdapter;
import com.hal9000.tourmania.ui.create_tour.CreateTourFragment;
import com.hal9000.tourmania.ui.create_tour.CreateTourFragmentArgs;
import com.hal9000.tourmania.ui.OnLoadMoreListener;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static com.hal9000.tourmania.AppUtils.TOUR_TYPE_NONE;
import static com.hal9000.tourmania.ui.create_tour.CreateTourFragment.ACTIVE_TOUR_ID_KEY;
import static com.hal9000.tourmania.ui.create_tour.CreateTourFragment.ACTIVE_TOUR_SERVER_ID_KEY;

public class HomeFragment extends Fragment {

    private HomeViewModel homeViewModel;
    private MainActivityViewModel activityViewModel;
    private RecyclerView recyclerView;
    private InfiniteTourAdapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;
    private List<TourWithWpWithPaths> toursWithTourWps = new ArrayList<>(100);
    private int pageNumber = 1;
    private boolean reachedEnd = false;
    private double longitude = 0.0;
    private double latitude = 0.0;
    private int retries = 0;
    private int checkingDetailsTourIndex = -1;

    public static final String TOUR_RECOMMENDED_CACHE_DIR_NAME = "recommended";
    private static final int RETRIES_LIMIT = 0;
    private static final int PAGE_SIZE = 10;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityViewModel = ViewModelProviders.of(requireActivity()).get(MainActivityViewModel.class);
        // should always be null here; TODO move this to ViewModel's onCleared when implemented
        String dirPath = requireContext().getExternalCacheDir() + File.separator + TOUR_RECOMMENDED_CACHE_DIR_NAME;
        File projDir = new File(dirPath);
        if (projDir.exists()) {
            AppUtils.deleteDir(projDir, -1);
        }
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        homeViewModel = ViewModelProviders.of(this).get(HomeViewModel.class);
        View root = inflater.inflate(R.layout.fragment_home, container, false);

        FloatingActionButton fab = root.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Navigation.findNavController(view).navigate(R.id.createTourFragment, null);
                Navigation.findNavController(view).navigate(R.id.action_nav_home_to_nav_nested_create_tour, null);
                //Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG).setAction("Action", null).show();
            }
        });

        if (!AppUtils.isUserLoggedIn(requireContext())) {
            Button singInButton = root.findViewById(R.id.button_sign_in);
            Button singUpButton = root.findViewById(R.id.button_sign_up);

            singInButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Navigation.findNavController(view).navigate(R.id.nav_sign_in, null);
                }
            });

            singUpButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Navigation.findNavController(view).navigate(R.id.nav_sign_up, null);
                }
            });

            singInButton.setVisibility(View.VISIBLE);
            singUpButton.setVisibility(View.VISIBLE);
        }

        loadActiveTour(root);

        if (!toursWithTourWps.isEmpty() && checkingDetailsTourIndex != -1) {
            Bundle bundle = activityViewModel.getAndClearBundle(CreateTourFragment.class);
            if (bundle != null) {
                float newRatingVal = bundle.getFloat(CreateTourFragment.NEW_TOUR_RATING_VAL_BUNDLE_KEY, -1.0f);
                int newRatingCount = bundle.getInt(CreateTourFragment.NEW_TOUR_RATING_COUNT_BUNDLE_KEY, -1);
                if (newRatingVal != -1.0f && newRatingCount != -1) {
                    toursWithTourWps.get(checkingDetailsTourIndex).tour.setRateVal(newRatingVal);
                    toursWithTourWps.get(checkingDetailsTourIndex).tour.setRateCount(newRatingCount);
                    mAdapter.notifyItemChanged(checkingDetailsTourIndex);
                }
            }
        }

        Context context = requireContext();
        if (AppUtils.isWifiEnabled(context) && AppUtils.isLocationEnabled(context)) {
            createRecyclerView(root);
            getToursOnLastLocation();
        }
        else {
            root.findViewById(R.id.text_enable_wifi_loc_msg).setVisibility(View.VISIBLE);
        }
        return root;
    }

    public void loadActiveTour(View view) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(requireContext());
        String _tourServerId = null;
        int _tourId = -2;
        if (prefs.contains(ACTIVE_TOUR_SERVER_ID_KEY)) {
            _tourServerId = prefs.getString(ACTIVE_TOUR_SERVER_ID_KEY, null);
        } else if (prefs.contains(ACTIVE_TOUR_ID_KEY)) {
            _tourId = prefs.getInt(ACTIVE_TOUR_ID_KEY, -2);
        }
        final String tourServerId = _tourServerId;
        final int tourId = _tourId;
        if (tourServerId != null || prefs.contains(ACTIVE_TOUR_ID_KEY)) {
            AppDatabase.databaseWriteExecutor.submit(new Runnable() {
                @Override
                public void run() {
                    try {
                        AppDatabase appDatabase = AppDatabase.getInstance(getContext());
                        TourWithWpWithPaths tourWithWpWithPaths = tourServerId != null ? appDatabase.tourDAO().getTourByServerTourIds(tourServerId)
                                : appDatabase.tourDAO().getTour(tourId);
                        if (tourWithWpWithPaths != null) {
                            requireActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    loadActiveTourView(view, tourWithWpWithPaths, tourServerId);
                                }
                            });
                        }
                        else {
                            ToursService client = RestClient.createService(ToursService.class,
                                    SharedPrefUtils.getDecryptedString(requireContext(), MainActivity.getLoginTokenKey()));
                            Call<TourWithWpWithPaths> call = client.getTour(tourServerId);
                            call.enqueue(new Callback<TourWithWpWithPaths>() {
                                @Override
                                public void onResponse(Call<TourWithWpWithPaths> call, final Response<TourWithWpWithPaths> response) {
                                    if (response.isSuccessful()) {
                                        //Log.d("crashTest", "loadToursFromServerDb onResponse");
                                        TourWithWpWithPaths tourWithWpWithPaths = response.body();
                                        if (tourWithWpWithPaths != null) {
                                            AppUtils.saveTourToLocalDb(tourWithWpWithPaths, true, requireContext(), TOUR_TYPE_NONE);
                                            loadActiveTourView(view, tourWithWpWithPaths, tourServerId);
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
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    private void loadActiveTourView(View view, TourWithWpWithPaths tourWithWpWithPaths, String tourServerId) {
        try {
            TextView tourTitleTextView = view.findViewById(R.id.tour_title);
            tourTitleTextView.setText(tourWithWpWithPaths.tour.getTitle());
            float rating = tourWithWpWithPaths.tour.getRateCount() == 0 ? 0.0f
                    : tourWithWpWithPaths.tour.getRateVal() / (float)tourWithWpWithPaths.tour.getRateCount();
            String mainImgPath = tourWithWpWithPaths.tour.getTourImgPath();
            ((TextView) view.findViewById(R.id.tour_rating)).setText(String.format("%.2f", rating));
            ((RatingBar) view.findViewById(R.id.tour_rating_bar)).setRating(rating);
            view.findViewById(R.id.group_active_tour).setVisibility(View.VISIBLE);
            // asynchronous image loading
            if (!TextUtils.isEmpty(mainImgPath)) {
                Picasso.get() //
                        .load(Uri.parse(mainImgPath)) //
                        .fit() //
                        .into(((ImageView) view.findViewById(R.id.tour_list_image)));
            }
            ((ImageButton)view.findViewById(R.id.buttonDeleteTour)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(requireContext());
                    prefs.edit().remove(ACTIVE_TOUR_ID_KEY).remove(ACTIVE_TOUR_SERVER_ID_KEY).apply();
                    view.findViewById(R.id.group_active_tour).setVisibility(View.GONE);
                }
            });
            view.findViewById(R.id.layout_active_tour).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Navigation.findNavController(requireView()).navigate(R.id.nav_nested_create_tour,
                            new CreateTourFragmentArgs.Builder().setTourServerId(tourServerId).build().toBundle());
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.action_search_tours, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_search:
                Navigation.findNavController(requireView()).navigate(R.id.nav_search, null);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void getToursOnLastLocation(){
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
                            initRecommendedTours();
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

    private void initRecommendedTours() {
        mAdapter.setLoading();
        loadToursFromServerDb();
    }

    private void loadToursFromServerDb() {
        ToursService client = RestClient.createService(ToursService.class);
        //Log.d("crashTest", "Missing tour: " + Integer.toString(missingTourIds.size()));
        Call<List<TourWithWpWithPaths>> call = client.getNearbyTours(longitude, latitude, pageNumber++);
        call.enqueue(new Callback<List<TourWithWpWithPaths>>() {
            @Override
            public void onResponse(Call<List<TourWithWpWithPaths>> call, Response<List<TourWithWpWithPaths>> response) {
                //Log.d("crashTest", "onQueryTextChange onResponse");
                List<TourWithWpWithPaths> toursList = response.body();
                if (toursList != null) {
                    if (toursList.isEmpty()) {
                        if (pageNumber == 1)
                            Toast.makeText(requireContext(), "No results found", Toast.LENGTH_SHORT).show();
                        reachedEnd = true;
                    } else {
                        if (toursList.size() < PAGE_SIZE)
                            reachedEnd = true;
                        int oldSize = mAdapter.mDataset.size();
                        mAdapter.mDataset.addAll(toursList);
                        mAdapter.notifyItemRangeInserted(oldSize, toursList.size());
                        mAdapter.setLoaded();
                        loadToursImagesFromServerDb(toursList);
                    }
                }
                mAdapter.setLoaded();
            }

            @Override
            public void onFailure(Call<List<TourWithWpWithPaths>> call, Throwable t) {
                mAdapter.setLoaded();
                Context context = getContext();
                if (context != null)
                    Toast.makeText(context,"A connection error has occurred\nRetries left: " + (RETRIES_LIMIT - retries),Toast.LENGTH_SHORT).show();
                if (retries++ >= RETRIES_LIMIT)
                    reachedEnd = true;
                //Log.d("crashTest", "onQueryTextChange onFailure");
            }
        });
    }

    private void loadToursImagesFromServerDb(List<TourWithWpWithPaths> missingToursWithTourWps) {
        final List<String> missingTourIds = new ArrayList<>(missingToursWithTourWps.size());
        for (TourWithWpWithPaths tourWithWpWithPaths : missingToursWithTourWps) {
            missingTourIds.add(tourWithWpWithPaths.tour.getServerTourId());
        }
        FileUploadDownloadService client = RestClient.createService(FileUploadDownloadService.class);
        //Log.d("crashTest", "Missing tour: " + Integer.toString(missingTourIds.size()));
        Call<List<TourFileDownloadResponse>> call = client.downloadMultipleToursImagesFiles(missingTourIds, false);
        call.enqueue(new Callback<List<TourFileDownloadResponse>>() {
            @Override
            public void onResponse(Call<List<TourFileDownloadResponse>> call, Response<List<TourFileDownloadResponse>> response) {
                //Log.d("crashTest", "loadToursImagesFromServerDb onResponse");
                final List<TourFileDownloadResponse> res = response.body();
                if (res != null && res.size() > 0) {
                    try {
                        loadToursImagesFromServerDbProcessResponse(res);
                    } catch (Exception e) { // IOException
                        //Log.d("crashTest", "Unknown expection while reading file download response");
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<List<TourFileDownloadResponse>> call, Throwable t) {
                t.printStackTrace();
                //Log.d("crashTest", "loadToursImagesFromServerDb onFailure");
            }
        });
    }

    private void loadToursImagesFromServerDbProcessResponse(List<TourFileDownloadResponse> res) {
        //Log.d("crashTest", "Missing tour: " + Integer.toString(res.size()));
        // for each tour
        Context context = getContext();
        if (context == null)
            return;
        for (TourFileDownloadResponse tourFileDownloadResponse : res) {
            //Log.d("crashTest", tourFileDownloadResponse.tourServerId);
            if (tourFileDownloadResponse.images != null) {
                // for each image in tour
                for (Map.Entry<String, FileDownloadImageObj> entry : tourFileDownloadResponse.images.entrySet()) {
                    FileDownloadImageObj fileDownloadImageObj = entry.getValue();
                    //Log.d("crashTest", entry.getKey() + " / " + entry.getValue());
                    if (fileDownloadImageObj.base64 != null && fileDownloadImageObj.mime != null) {
                        File file = AppUtils.saveImageFromBase64(context, fileDownloadImageObj.base64, fileDownloadImageObj.mime, TOUR_RECOMMENDED_CACHE_DIR_NAME);
                        // process main tour image
                        if (entry.getKey().equals("0")) {
                            //Log.d("crashTest", "updating main tour image");
                            int oldSize = mAdapter.mDataset.size();
                            for (int i = 0; i < mAdapter.mDataset.size(); i++) {
                                TourWithWpWithPaths t = mAdapter.mDataset.get(i);
                                //Log.d("crashTest", t.tour.getServerTourId());
                                if (t.tour.getServerTourId().equals(tourFileDownloadResponse.tourServerId)) {
                                    t.tour.setTourImgPath(file.toURI().toString());
                                    while (recyclerView.isComputingLayout());
                                    mAdapter.notifyItemChanged(i);
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private void createRecyclerView(final View root) {
        // Create recycler view
        recyclerView = (RecyclerView) root.findViewById(R.id.recommended_tours_recycler_view);
        recyclerView.setHasFixedSize(true);
        // use a linear layout manager
        layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);

        // specify an adapter
        mAdapter = new InfiniteTourAdapter(toursWithTourWps,
                new InfiniteTourAdapter.ToursAdapterCallback() {
                    @Override
                    public Context getContext() {
                        return requireContext();
                    }

                    @Override
                    public void navigateToViewTour(int position) {
                        checkingDetailsTourIndex = position;
                        Navigation.findNavController(requireView()).navigate(R.id.nav_nested_create_tour,
                                new CreateTourFragmentArgs.Builder().setTourServerId(
                                        toursWithTourWps.get(position).tour.getServerTourId()).build().toBundle());
                    }
                },
                R.layout.tour_search_rec_view_row,
                recyclerView);

        mAdapter.setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore() {
                if (!reachedEnd) {
                    mAdapter.setLoading();
                    loadToursFromServerDb();
                }

            }
        });
        recyclerView.setAdapter(mAdapter);
    }
}