package com.hal9000.tourmania.ui.search;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.lifecycle.ViewModelProviders;

import android.content.Context;
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

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.hal9000.tourmania.AppUtils;
import com.hal9000.tourmania.ui.InfiniteTourAdapter;
import com.hal9000.tourmania.MainActivity;
import com.hal9000.tourmania.OnLoadMoreListener;
import com.hal9000.tourmania.R;
import com.hal9000.tourmania.model.TourWithWpWithPaths;
import com.hal9000.tourmania.rest_api.RestClient;
import com.hal9000.tourmania.rest_api.files_upload_download.FileDownloadImageObj;
import com.hal9000.tourmania.rest_api.files_upload_download.FileDownloadResponse;
import com.hal9000.tourmania.rest_api.files_upload_download.FileUploadDownloadService;
import com.hal9000.tourmania.rest_api.tours.ToursService;
import com.hal9000.tourmania.ui.create_tour.CreateTourFragmentArgs;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SearchFragment extends Fragment {

    private SearchViewModel mViewModel;
    private RecyclerView recyclerView;
    private InfiniteTourAdapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;
    private List<TourWithWpWithPaths> toursWithTourWps = new ArrayList<>(100);
    private int pageNumber = 1;
    private Handler handler = new Handler();
    private String queryText = "";
    private boolean reachedEnd = false;
    public static final String TOUR_SEARCH_CACHE_DIR_NAME = "search";

    public static SearchFragment newInstance() {
        return new SearchFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        View root = inflater.inflate(R.layout.fragment_search, container, false);
        createRecyclerView(root);
        return root;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = ViewModelProviders.of(this).get(SearchViewModel.class);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        inflater.inflate(R.menu.action_search_tours, menu);
        MenuItem item = menu.findItem(R.id.action_search);

        final SearchView searchView = new SearchView(((MainActivity) getActivity()).getSupportActionBar().getThemedContext());
        item.setActionView(searchView);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String queryText) {
                String dirPath = requireContext().getExternalCacheDir() + File.separator + TOUR_SEARCH_CACHE_DIR_NAME;
                File projDir = new File(dirPath);
                if (projDir.exists()) {
                    AppUtils.deleteDir(projDir, -1);
                }
                searchView.clearFocus();
                int oldSize = mAdapter.mDataset.size();
                mAdapter.mDataset.clear();
                mAdapter.notifyItemRangeRemoved(0, oldSize);
                pageNumber = 1;
                reachedEnd = false;
                loadToursFromServerDb(queryText);
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
                Navigation.findNavController(requireView()).popBackStack();
                return true;
            }
        });
        if (toursWithTourWps.isEmpty())
            item.expandActionView();
        else {
            AppCompatActivity appCompatActivity = ((AppCompatActivity)getActivity());
            ActionBar actionBar = null;
            if (appCompatActivity != null)
                actionBar = appCompatActivity.getSupportActionBar();
            if (actionBar != null) {
                actionBar.setTitle("");
            }
        }
    }

    private void loadToursFromServerDb(String queryText) {
        this.queryText = queryText;
        ToursService client = RestClient.createService(ToursService.class);
        //Log.d("crashTest", "Missing tour: " + Integer.toString(missingTourIds.size()));
        Call<List<TourWithWpWithPaths>> call = client.searchToursByPhrase(queryText, pageNumber++);
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
                Toast.makeText(requireContext(),"An error has occurred",Toast.LENGTH_SHORT).show();
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
                        File file = AppUtils.saveImageFromBase64(requireContext(), fileDownloadImageObj.base64, fileDownloadImageObj.mime, TOUR_SEARCH_CACHE_DIR_NAME);
                        // process main tour image
                        if (entry.getKey().equals("0")) {
                            //Log.d("crashTest", "updating main tour image");
                            int i = 0;
                            for (TourWithWpWithPaths t : mAdapter.mDataset) {
                                //Log.d("crashTest", t.tour.getServerTourId());
                                if (t.tour.getServerTourId().equals(fileDownloadResponse.tourServerId)) {
                                    t.tour.setTourImgPath(file.toURI().toString());
                                    while (recyclerView.isComputingLayout());
                                    mAdapter.notifyItemChanged(i);
                                }
                                i++;
                            }
                        }
                    }
                }
            }
        }
    }

    private void createRecyclerView(final View root) {
        // Create recycler view
        recyclerView = (RecyclerView) root.findViewById(R.id.search_recycler_view);
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
                    mAdapter.showProgressBar();
                    loadToursFromServerDb(queryText);
                }
            }
        });
        recyclerView.setAdapter(mAdapter);
    }
}
