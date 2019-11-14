package com.hal9000.tourmania.ui.my_tours;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.hal9000.tourmania.R;
import com.hal9000.tourmania.TourWaypointsAdapter;
import com.hal9000.tourmania.ToursAdapter;
import com.hal9000.tourmania.database.AppDatabase;
import com.hal9000.tourmania.model.TourWaypoint;
import com.hal9000.tourmania.model.TourWithWpWithPaths;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

public class MyToursFragment extends Fragment {

    private MyToursViewModel myToursViewModel;
    private RecyclerView recyclerView;
    private RecyclerView.Adapter mAdapter;
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
                toursWithTourWps = appDatabase.tourWaypointDAO().getToursWithTourWps();

                //Log.d("crashTest", Integer.toString(toursWithTourWps.size()));
            }
        });
    }

    private void createRecyclerView(View root) {
        // Create recycler view
        recyclerView = (RecyclerView) root.findViewById(R.id.my_tours_recycler_view);
        recyclerView.setHasFixedSize(true);
        // use a linear layout manager
        layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);

        // specify an adapter (see also next example)
        mAdapter = new ToursAdapter(toursWithTourWps,
                new ToursAdapter.ToursAdapterCallback() {
                    @Override
                    public Context getContext() {
                        return requireContext();
                    }

                    @Override
                    public void navigateToViewTour() {
                        Navigation.findNavController(requireView()).navigate(R.id.nav_view_tour, null);
                    }
                });
        recyclerView.setAdapter(mAdapter);
    }
}