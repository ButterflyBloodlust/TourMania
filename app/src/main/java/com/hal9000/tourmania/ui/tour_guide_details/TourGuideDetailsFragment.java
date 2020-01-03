package com.hal9000.tourmania.ui.tour_guide_details;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.ViewModelProviders;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.hal9000.tourmania.AppUtils;
import com.hal9000.tourmania.BuildConfig;
import com.hal9000.tourmania.MainActivity;
import com.hal9000.tourmania.MainActivityViewModel;
import com.hal9000.tourmania.R;
import com.hal9000.tourmania.SharedPrefUtils;
import com.hal9000.tourmania.model.TourWithWpWithPaths;
import com.hal9000.tourmania.model.User;
import com.hal9000.tourmania.rest_api.RestClient;
import com.hal9000.tourmania.rest_api.files_upload_download.FileDownloadImageObj;
import com.hal9000.tourmania.rest_api.files_upload_download.TourFileDownloadResponse;
import com.hal9000.tourmania.rest_api.files_upload_download.FileUploadDownloadService;
import com.hal9000.tourmania.rest_api.tour_guides.TourGuidesService;
import com.hal9000.tourmania.rest_api.tours.ToursService;
import com.hal9000.tourmania.ui.InfiniteTourAdapter;
import com.hal9000.tourmania.ui.create_tour.CreateTourFragment;
import com.hal9000.tourmania.ui.create_tour.CreateTourFragmentArgs;
import com.hal9000.tourmania.ui.OnLoadMoreListener;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.hal9000.tourmania.ui.home.HomeFragment.TOUR_RECOMMENDED_CACHE_DIR_NAME;

public class TourGuideDetailsFragment extends Fragment {

    private TourGuideDetailsViewModel mViewModel;
    private MainActivityViewModel activityViewModel;
    private RecyclerView recyclerView;
    private InfiniteTourAdapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;
    private List<TourWithWpWithPaths> toursWithTourWps = new ArrayList<>(100);
    private int pageNumber = 1;
    private int currentFragmentId;
    private boolean reachedEnd = false;
    private double longitude = 0.0;
    private double latitude = 0.0;
    private int checkingDetailsTourIndex = -1;
    private String tourGuideNickname;
    private User tourGuide;

    public static final String NEW_TOUR_GUIDE_RATING_BUNDLE_KEY = "new_tour_guide_rating";
    public static final String NEW_TOUR_GUIDE_RATING_VAL_BUNDLE_KEY = "new_tour_guide_val_rating";
    public static final String NEW_TOUR_GUIDE_RATING_COUNT_BUNDLE_KEY = "new_tour_guide_count_rating";

    public static TourGuideDetailsFragment newInstance() {
        return new TourGuideDetailsFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityViewModel = ViewModelProviders.of(requireActivity()).get(MainActivityViewModel.class);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        View root = inflater.inflate(R.layout.fragment_tour_guide_details, container, false);
        tourGuideNickname = TourGuideDetailsFragmentArgs.fromBundle(requireArguments()).getTourGuideServerId();

        AppCompatActivity appCompatActivity = ((AppCompatActivity)getActivity());
        ActionBar actionBar = null;
        if (appCompatActivity != null)
            actionBar = appCompatActivity.getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(tourGuideNickname);
        }

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

        loadTourGuideDetailsFromServerDb(root);
        createRecyclerView(root);
        getTourGuideToursOnLastLocation();

        return root;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = ViewModelProviders.of(this).get(TourGuideDetailsViewModel.class);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        if (!tourGuideNickname.equals(SharedPrefUtils.getDecryptedString(requireContext(), MainActivity.getUsernameKey())))
            inflater.inflate(R.menu.tour_guide_details_toolbar_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_rate_tour_guide:
                final View dialogView = getLayoutInflater().inflate(R.layout.dialog_rating_bar, null);
                ((RatingBar) dialogView.findViewById(R.id.rating_bar)).setRating(tourGuide.getMyRating());
                final AlertDialog.Builder builder = new AlertDialog.Builder(requireContext())
                        .setIcon(R.drawable.ic_star_white_border_50dp)
                        .setTitle("Rate this tour guide")
                        .setView(dialogView)
                        .setCancelable(true)
                        .setPositiveButton("Done", null)
                        .setNegativeButton("Cancel", null);
                final AlertDialog alertDialog = builder.create();
                alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(DialogInterface dialog) {
                        alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                RatingBar ratingBar = dialogView.findViewById(R.id.rating_bar);
                                float rating = ratingBar.getRating();
                                if (rating > 0) {
                                    //createTourSharedViewModel.updateTourGuideRating(requireContext(), rating);
                                    updateTourGuideRating(rating);
                                    MainActivityViewModel activityViewModel = ViewModelProviders.of(requireActivity()).get(MainActivityViewModel.class);
                                    Bundle bundle = new Bundle();
                                    bundle.putFloat(NEW_TOUR_GUIDE_RATING_BUNDLE_KEY, rating);
                                    bundle.putFloat(NEW_TOUR_GUIDE_RATING_VAL_BUNDLE_KEY, tourGuide.getRateVal());
                                    bundle.putInt(NEW_TOUR_GUIDE_RATING_COUNT_BUNDLE_KEY, tourGuide.getRateCount());
                                    activityViewModel.putToBundle(TourGuideDetailsFragment.class, bundle);

                                    float globalRating = tourGuide.getRateVal() / (float)tourGuide.getRateCount();
                                    ((RatingBar)requireView().findViewById(R.id.tour_guide_rating_bar)).setRating(globalRating);

                                    alertDialog.dismiss();
                                }
                                else
                                    Toast.makeText(requireContext(),"Rating above 0 required",Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });
                alertDialog.show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void updateTourGuideRating(float newRating) {
        float oldRating = tourGuide.getMyRating();
        // if tour was not rated before by current user
        if (oldRating == 0.0f) {
            tourGuide.setRateCount(tourGuide.getRateCount() + 1);
            tourGuide.setRateVal(tourGuide.getRateVal() + newRating);
        }
        else {
            tourGuide.setRateVal(tourGuide.getRateVal() + newRating - oldRating);
        }
        tourGuide.setMyRating(newRating);

        saveTourGuideRatingToServerDb();
    }

    private void saveTourGuideRatingToServerDb() {
        TourGuidesService client = RestClient.createService(TourGuidesService.class, SharedPrefUtils.getDecryptedString(requireContext(), MainActivity.getLoginTokenKey()));
        //Log.d("crashTest", "Missing tour: " + Integer.toString(missingTourIds.size()));
        Call<ResponseBody> call = client.rateTourGuide(tourGuideNickname, tourGuide.getMyRating());
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Log.d("crashTest", "saveTourGuideRatingToServerDb onResponse");
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                if (BuildConfig.DEBUG)
                    t.printStackTrace();
                Log.d("crashTest", "saveTourGuideRatingToServerDb onFailure");
            }
        });
    }

    private void loadTourGuideDetailsFromServerDb(final View root) {
        TourGuidesService client = RestClient.createService(TourGuidesService.class);
        //Log.d("crashTest", "Missing tour: " + Integer.toString(missingTourIds.size()));
        Call<User> call = client.getTourGuideDetails(tourGuideNickname);
        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                //Log.d("crashTest", "loadTourGuideDetailsFromServerDb onResponse");
                tourGuide = response.body();
                if (tourGuide != null) {
                    if (!TextUtils.isEmpty(tourGuide.getPhoneNumber())) {
                        TextView textViewPhoneNum = root.findViewById(R.id.tour_guide_phone_num_textview);
                        textViewPhoneNum.setText(tourGuide.getPhoneNumber());
                        textViewPhoneNum.setVisibility(View.VISIBLE);
                        root.findViewById(R.id.tour_guide_phone_num_label).setVisibility(View.VISIBLE);
                    }
                    TextView textViewEmail = root.findViewById(R.id.tour_guide_email_textview);
                    textViewEmail.setText(tourGuide.getEmail());
                    float rating = tourGuide.getRateVal() / (float)tourGuide.getRateCount();
                    RatingBar ratingBar = root.findViewById(R.id.tour_guide_rating_bar);
                    ratingBar.setRating(rating);
                }
                else {
                    mAdapter.setLoaded();
                    Toast.makeText(requireContext(), "No results found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                mAdapter.setLoaded();
                Context context = getContext();
                if (context != null)
                    Toast.makeText(context,"An error has occurred",Toast.LENGTH_SHORT).show();
                //Log.d("crashTest", "onQueryTextChange onFailure");
            }
        });
    }

    private void getTourGuideToursOnLastLocation(){
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
                            initTourGuideTours();
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

    private void initTourGuideTours() {
        mAdapter.setLoading();
        loadToursFromServerDb();
    }

    private void loadToursFromServerDb() {
        ToursService client = RestClient.createService(ToursService.class);
        //Log.d("crashTest", "Missing tour: " + Integer.toString(missingTourIds.size()));
        Call<List<TourWithWpWithPaths>> call = client.getUserToursOverviews(tourGuideNickname, longitude, latitude, pageNumber++);
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
        for (TourFileDownloadResponse tourFileDownloadResponse : res) {
            //Log.d("crashTest", tourFileDownloadResponse.tourServerId);
            if (tourFileDownloadResponse.images != null) {
                // for each image in tour
                for (Map.Entry<String, FileDownloadImageObj> entry : tourFileDownloadResponse.images.entrySet()) {
                    FileDownloadImageObj fileDownloadImageObj = entry.getValue();
                    //Log.d("crashTest", entry.getKey() + " / " + entry.getValue());
                    if (fileDownloadImageObj.base64 != null && fileDownloadImageObj.mime != null) {
                        File file = AppUtils.saveImageFromBase64(requireContext(), fileDownloadImageObj.base64, fileDownloadImageObj.mime, TOUR_RECOMMENDED_CACHE_DIR_NAME);
                        // process main tour image
                        if (entry.getKey().equals("0")) {
                            //Log.d("crashTest", "updating main tour image");
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
                    loadToursFromServerDb();
                }
                else
                    mAdapter.setLoaded();
            }
        });
        recyclerView.setAdapter(mAdapter);
    }

}
