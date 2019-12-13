package com.hal9000.tourmania.ui.tour_guide_details;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.ViewModelProviders;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.hal9000.tourmania.AppUtils;
import com.hal9000.tourmania.R;
import com.hal9000.tourmania.model.TourWithWpWithPaths;
import com.hal9000.tourmania.rest_api.RestClient;
import com.hal9000.tourmania.rest_api.files_upload_download.FileDownloadImageObj;
import com.hal9000.tourmania.rest_api.files_upload_download.FileDownloadResponse;
import com.hal9000.tourmania.rest_api.files_upload_download.FileUploadDownloadService;
import com.hal9000.tourmania.rest_api.tours.ToursService;
import com.hal9000.tourmania.ui.InfiniteTourAdapter;
import com.hal9000.tourmania.ui.create_tour.CreateTourFragmentArgs;
import com.hal9000.tourmania.ui.search.OnLoadMoreListener;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.hal9000.tourmania.ui.home.HomeFragment.TOUR_RECOMMENDED_CACHE_DIR_NAME;

public class TourGuideDetailsFragment extends Fragment {

    private TourGuideDetailsViewModel mViewModel;

    private RecyclerView recyclerView;
    private InfiniteTourAdapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;
    private List<TourWithWpWithPaths> toursWithTourWps = new ArrayList<>(100);
    private int pageNumber = 1;
    private int currentFragmentId;
    private boolean reachedEnd = false;
    private double longitude = 0.0;
    private double latitude = 0.0;

    public static TourGuideDetailsFragment newInstance() {
        return new TourGuideDetailsFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_tour_guide_details, container, false);
        String tourGuideServerId = TourGuideDetailsFragmentArgs.fromBundle(requireArguments()).getTourGuideServerId();

        AppCompatActivity appCompatActivity = ((AppCompatActivity)getActivity());
        ActionBar actionBar = null;
        if (appCompatActivity != null)
            actionBar = appCompatActivity.getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(tourGuideServerId);
        }

        // ------
        createRecyclerView(root);
        initRecommendedTours();

        return root;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = ViewModelProviders.of(this).get(TourGuideDetailsViewModel.class);
    }

    // ------------------------------------------

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
                            int oldSize = mAdapter.mDataset.size();
                            for (int i = 0; i < mAdapter.mDataset.size(); i++) {
                                TourWithWpWithPaths t = mAdapter.mDataset.get(i);
                                //Log.d("crashTest", t.tour.getServerTourId());
                                if (t.tour.getServerTourId().equals(fileDownloadResponse.tourServerId)) {
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
        recyclerView = (RecyclerView) root.findViewById(R.id.offered_tours_recycler_view);
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
