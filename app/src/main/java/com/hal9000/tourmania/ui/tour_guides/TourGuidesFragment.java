package com.hal9000.tourmania.ui.tour_guides;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.hal9000.tourmania.R;
import com.hal9000.tourmania.model.TourWithWpWithPaths;
import com.hal9000.tourmania.ui.InfiniteTourAdapter;
import com.hal9000.tourmania.ui.create_tour.CreateTourFragmentArgs;
import com.hal9000.tourmania.ui.search.OnLoadMoreListener;

import java.util.ArrayList;
import java.util.List;

public class TourGuidesFragment extends Fragment {

    private TourGuidesViewModel tourGuidesViewModel;
    private RecyclerView recyclerView;
    private InfiniteTourAdapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;
    private List<TourWithWpWithPaths> toursWithTourWps = new ArrayList<>(100);
    private int pageNumber = 1;
    private int currentFragmentId;
    private boolean reachedEnd = false;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        tourGuidesViewModel =
                ViewModelProviders.of(this).get(TourGuidesViewModel.class);
        View root = inflater.inflate(R.layout.fragment_tour_guides, container, false);
        return root;
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
                    //loadToursFromServerDb();
                }
                else
                    mAdapter.setLoaded();
            }
        });
        recyclerView.setAdapter(mAdapter);
    }
}