package com.hal9000.tourmania.ui.my_tours;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.hal9000.tourmania.AppUtils;
import com.hal9000.tourmania.MainActivity;
import com.hal9000.tourmania.R;
import com.hal9000.tourmania.SharedPrefUtils;
import com.hal9000.tourmania.rest_api.RestClient;
import com.hal9000.tourmania.rest_api.tours.ToursCRUD;
import com.hal9000.tourmania.ui.ToursAdapter;
import com.hal9000.tourmania.database.AppDatabase;
import com.hal9000.tourmania.model.TourWithWpWithPaths;
import com.hal9000.tourmania.ui.create_tour.CreateTourFragmentArgs;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class MyToursFragment extends Fragment {

    private MyToursViewModel myToursViewModel;
    private RecyclerView recyclerView;
    private ToursAdapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;
    private List<TourWithWpWithPaths> toursWithTourWps;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        myToursViewModel =
                ViewModelProviders.of(this).get(MyToursViewModel.class);
        View root = inflater.inflate(R.layout.fragment_my_tours, container, false);

        FloatingActionButton fab = root.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Navigation.findNavController(view).navigate(R.id.createTourFragment, null);
                Navigation.findNavController(view).navigate(R.id.action_nav_my_tours_to_nav_nested_create_tour, null);
                //Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG).setAction("Action", null).show();
            }
        });

        Future future = loadToursFromRoomDb();
        try {
            future.get();
        } catch (ExecutionException | InterruptedException e) {
            //e.printStackTrace();
        }
        createRecyclerView(root);
        loadToursFromServerDb();
        return root;
    }

    private Future loadToursFromRoomDb() {
        // Currently does NOT handle additional waypoint pics (PicturePath / TourWpWithPicPaths)
        //Log.d("crashTest", "loadToursFromRoomDb()");
        return AppDatabase.databaseWriteExecutor.submit(new Runnable() {
            public void run() {
                //Log.d("crashTest", "run()");
                AppDatabase appDatabase = AppDatabase.getInstance(requireContext());
                //List<Tour> toursWithTourWps = AppDatabase.getInstance(requireContext()).tourDAO().getTours();
                toursWithTourWps = appDatabase.tourDAO().getToursWithTourWps();

                //Log.d("crashTest", Integer.toString(toursWithTourWps.size()));
            }
        });
    }

    private void loadToursFromServerDb() {
        AppDatabase.databaseWriteExecutor.submit(
                new Runnable() {
                    @Override
                    public void run() {
                        final AppDatabase appDatabase = AppDatabase.getInstance(requireContext());
                        List<String> serverMyTourIds = appDatabase.tourDAO().getServerMyTourIds(-222);
                        ToursCRUD client = RestClient.createService(ToursCRUD.class);
                        Call<List<TourWithWpWithPaths>> call = serverMyTourIds == null || serverMyTourIds.size() == 0 ?
                                client.getUserTours(SharedPrefUtils.getString(requireContext(), MainActivity.getUsernameKey())) :
                                client.getUserTours(SharedPrefUtils.getString(requireContext(), MainActivity.getUsernameKey()), serverMyTourIds);
                        call.enqueue(new Callback<List<TourWithWpWithPaths>>() {
                            @Override
                            public void onResponse(Call<List<TourWithWpWithPaths>> call, final Response<List<TourWithWpWithPaths>> response) {
                                if (response.isSuccessful()) {
                                    Log.d("crashTest", "loadToursFromServerDb onResponse");
                                    if (response.body() != null) {
                                        AppUtils.saveToursToLocalDb(response.body(), requireContext());
                                        AppDatabase.databaseWriteExecutor.submit(
                                                new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        List<TourWithWpWithPaths> _toursWithTourWps = response.body();
                                                        int oldSize = mAdapter.mDataset.size();
                                                        mAdapter.mDataset.addAll(_toursWithTourWps);
                                                        mAdapter.notifyItemRangeInserted(oldSize, _toursWithTourWps.size());
                                                    }
                                                });
                                    }
                                }
                            }

                            @Override
                            public void onFailure(Call<List<TourWithWpWithPaths>> call, Throwable t) {
                                t.printStackTrace();
                                Log.d("crashTest", "loadToursFromServerDb onFailure");
                            }
                        });
                    }
                });
    }

    private void createRecyclerView(final View root) {
        // Create recycler view
        recyclerView = (RecyclerView) root.findViewById(R.id.my_tours_recycler_view);
        recyclerView.setHasFixedSize(true);
        // use a linear layout manager
        layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);

        // specify an adapter
        mAdapter = new ToursAdapter(toursWithTourWps,
                new ToursAdapter.ToursAdapterCallback() {
                    @Override
                    public Context getContext() {
                        return requireContext();
                    }

                    @Override
                    public void navigateToViewTour(int position) {
                        Navigation.findNavController(requireView()).navigate(R.id.nav_nested_create_tour,
                                new CreateTourFragmentArgs.Builder().setTourId(toursWithTourWps.get(position).tour.getTourId()).build().toBundle());
                    }
                });
        recyclerView.setAdapter(mAdapter);
    }
}