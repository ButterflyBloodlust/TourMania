package com.hal9000.tourmania.ui.home;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.core.content.ContextCompat;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
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
import com.google.android.material.snackbar.Snackbar;
import com.hal9000.tourmania.AppUtils;
import com.hal9000.tourmania.MainActivity;
import com.hal9000.tourmania.R;
import com.hal9000.tourmania.SharedPrefUtils;
import com.hal9000.tourmania.model.TourWithWpWithPaths;
import com.hal9000.tourmania.rest_api.RestClient;
import com.hal9000.tourmania.rest_api.files_upload_download.FileDownloadImageObj;
import com.hal9000.tourmania.rest_api.files_upload_download.FileDownloadResponse;
import com.hal9000.tourmania.rest_api.files_upload_download.FileUploadDownloadService;
import com.hal9000.tourmania.rest_api.tours.ToursService;
import com.hal9000.tourmania.ui.InfiniteTourAdapter;
import com.hal9000.tourmania.ui.ToursAdapter;
import com.hal9000.tourmania.ui.create_tour.CreateTourFragmentArgs;
import com.hal9000.tourmania.ui.search.OnLoadMoreListener;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static android.content.Context.LOCATION_SERVICE;

public class HomeFragment extends Fragment {

    private HomeViewModel homeViewModel;
    private RecyclerView recyclerView;
    private InfiniteTourAdapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;
    private List<TourWithWpWithPaths> toursWithTourWps = new ArrayList<>(100);
    private int pageNumber = 1;
    private int currentFragmentId;
    private boolean reachedEnd = false;
    private double longitude = 0.0;
    private double latitude = 0.0;

    public static final String TOUR_RECOMMENDED_CACHE_DIR_NAME = "recommended";

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

        String dirPath = requireContext().getExternalCacheDir() + File.separator + TOUR_RECOMMENDED_CACHE_DIR_NAME;
        File projDir = new File(dirPath);
        if (projDir.exists()) {
            AppUtils.deleteDir(projDir, -1);
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

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.action_search_tours, menu);
        MenuItem item = menu.findItem(R.id.action_search);
        item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Navigation.findNavController(requireView()).navigate(R.id.nav_search, null);
                return true;
            }
        });
    }

    private void initRecommendedTours() {
        mAdapter.setLoading();
        loadToursFromServerDb();
    }

    private void getToursOnLastLocation(){
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
                        Log.d("crashTest", "Error trying to get last location");
                        e.printStackTrace();
                        Context context = getContext();
                        if (context != null)
                            Toast.makeText(context,"Could not access user location",Toast.LENGTH_SHORT).show();
                    }
                });
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
                        int oldSize = mAdapter.mDataset.size();
                        mAdapter.mDataset.addAll(toursList);
                        mAdapter.notifyItemRangeInserted(oldSize, toursList.size());
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
                    Toast.makeText(context,"An error has occurred",Toast.LENGTH_SHORT).show();
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
        Call<List<FileDownloadResponse>> call = client.downloadMultipleFiles(missingTourIds, false);
        call.enqueue(new Callback<List<FileDownloadResponse>>() {
            @Override
            public void onResponse(Call<List<FileDownloadResponse>> call, Response<List<FileDownloadResponse>> response) {
                //Log.d("crashTest", "loadToursImagesFromServerDb onResponse");
                final List<FileDownloadResponse> res = response.body();
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
            public void onFailure(Call<List<FileDownloadResponse>> call, Throwable t) {
                t.printStackTrace();
                //Log.d("crashTest", "loadToursImagesFromServerDb onFailure");
            }
        });
    }

    private void loadToursImagesFromServerDbProcessResponse(List<FileDownloadResponse> res) {
        //Log.d("crashTest", "Missing tour: " + Integer.toString(res.size()));
        // for each tour
        for (FileDownloadResponse fileDownloadResponse : res) {
            //Log.d("crashTest", fileDownloadResponse.tourServerId);
            if (fileDownloadResponse.images != null) {
                // for each image in tour
                for (Map.Entry<String, FileDownloadImageObj> entry : fileDownloadResponse.images.entrySet()) {
                    FileDownloadImageObj fileDownloadImageObj = entry.getValue();
                    //Log.d("crashTest", entry.getKey() + " / " + entry.getValue());
                    if (fileDownloadImageObj.base64 != null && fileDownloadImageObj.mime != null) {
                        File file = AppUtils.saveImageFromBase64(requireContext(), fileDownloadImageObj.base64, fileDownloadImageObj.mime, TOUR_RECOMMENDED_CACHE_DIR_NAME);
                        // process main tour image
                        if (entry.getKey().equals("0")) {
                            //Log.d("crashTest", "updating main tour image");
                            int i = 0;
                            int oldSize = mAdapter.mDataset.size();
                            for (int j = 0; j < mAdapter.mDataset.size(); j++) {
                                TourWithWpWithPaths t = mAdapter.mDataset.get(i);
                                //Log.d("crashTest", t.tour.getServerTourId());
                                if (t.tour.getServerTourId().equals(fileDownloadResponse.tourServerId)) {
                                    t.tour.setTourImgPath(file.toURI().toString());
                                    while (recyclerView.isComputingLayout());
                                    mAdapter.notifyItemChanged(i);
                                    break;
                                }
                                i++;
                            }
                            //mAdapter.notifyItemRangeChanged(0);
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
                    loadToursFromServerDb();
                }
                else
                    mAdapter.setLoaded();
            }
        });
        recyclerView.setAdapter(mAdapter);
    }
}