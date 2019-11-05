package com.hal9000.tourmania.ui.tour_waypoints_list;

import androidx.lifecycle.ViewModelProviders;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.hal9000.tourmania.R;
import com.hal9000.tourmania.TourWaypointsAdapter;
import com.hal9000.tourmania.ui.create_tour.CreateTourSharedViewModel;

public class TourWaypointsListFragment extends Fragment {

    private CreateTourSharedViewModel mViewModel;

    private RecyclerView recyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;
    private String[] myDataset;

    public static TourWaypointsListFragment newInstance() {
        return new TourWaypointsListFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_tour_waypoints_list, container, false);
        mViewModel = ViewModelProviders.of(requireActivity()).get(CreateTourSharedViewModel.class);
        fillDataset(100);
        createRecyclerView(root);
        return root;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    private void fillDataset(int arrSize) {
        myDataset = new String[arrSize];
        for (int i = 0, c = 0; i < arrSize; i++) {
            myDataset[i] = Integer.toString(i);
        }
    }

    private void createRecyclerView(View root) {
        // Create recycler view
        recyclerView = (RecyclerView) root.findViewById(R.id.tour_waypoints_recycler_view);
        recyclerView.setHasFixedSize(true);
        // use a linear layout manager
        layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);

        // specify an adapter (see also next example)
        mAdapter = new TourWaypointsAdapter(mViewModel.getTourWaypointList());
        recyclerView.setAdapter(mAdapter);
    }

}
