package com.hal9000.tourmania.ui.tour_waypoints_list;

import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelStoreOwner;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.hal9000.tourmania.R;
import com.hal9000.tourmania.TourWaypointsAdapter;
import com.hal9000.tourmania.ui.create_tour.CreateTourSharedViewModel;
import com.hal9000.tourmania.ui.create_tour.CreateTourSharedViewModelFactory;

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
        //mViewModel = ViewModelProviders.of(requireActivity()).get(CreateTourSharedViewModel.class);
        //fillDataset(100);
        return root;
    }

    @Override
    public void onViewCreated (@NonNull View view, Bundle savedInstanceState) {
        // Scope ViewModel to nested nav graph.
        ViewModelStoreOwner owner = Navigation.findNavController(view).getViewModelStoreOwner(R.id.nav_nested_create_tour);
        CreateTourSharedViewModelFactory factory = new CreateTourSharedViewModelFactory();
        mViewModel = new ViewModelProvider(owner, factory).get(CreateTourSharedViewModel.class);
        createRecyclerView(view);
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
        mAdapter = new TourWaypointsAdapter(mViewModel.getTourWaypointList(),
                new TourWaypointsAdapter.TourWaypointsOnClickListener() {
            @Override
            public void onClick(View v, int position) {
                mViewModel.setChoosenLocateWaypointIndex(position);
                Navigation.findNavController(requireView()).popBackStack();
            }
        });
        recyclerView.setAdapter(mAdapter);
    }

}
