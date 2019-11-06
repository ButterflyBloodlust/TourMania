package com.hal9000.tourmania.ui.tour_waypoints_list;

import androidx.lifecycle.ViewModelProvider;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelStoreOwner;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.hal9000.tourmania.R;
import com.hal9000.tourmania.TourWaypointsAdapter;
import com.hal9000.tourmania.ui.create_tour.CreateTourSharedViewModel;
import com.hal9000.tourmania.ui.create_tour.CreateTourSharedViewModelFactory;

import java.io.FileNotFoundException;
import java.io.InputStream;

public class TourWaypointsListFragment extends Fragment {

    private CreateTourSharedViewModel mViewModel;

    private RecyclerView recyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;
    private String[] myDataset;

    private static int PICK_IMAGE_REQUEST_CODE = 0xf000;   // used with bitmask to encode up to ‭4095‬ positions
    private static String ADAPTER_POSITION_INTENT_EXTRA_ID = "item_position";

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
                    public void locateWaypointOnClick(View v, int position) {
                        mViewModel.setChoosenLocateWaypointIndex(position);
                        Navigation.findNavController(requireView()).popBackStack();
                    }

                    @Override
                    public void pickPictureMainOnLongClick(int position) {
                        if (position < 65535) {
                            Intent intent = new Intent();
                            intent.setType("image/*");
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            // Position is saved in request code using bitmask 0xf0000 (position has to be less than ‭4095‬)
                            startActivityForResult(Intent.createChooser(intent, "Select image"), (PICK_IMAGE_REQUEST_CODE | position));
                        }
                        else {
                            Toast.makeText(requireContext(), "Too many waypoints for image storage", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public Context getContext() {
                        return requireContext();
                    }
                });
        recyclerView.setAdapter(mAdapter);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if((PICK_IMAGE_REQUEST_CODE & requestCode) == PICK_IMAGE_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            int adapterPostion = (requestCode & ~PICK_IMAGE_REQUEST_CODE);
            mViewModel.getTourWaypointList().get(adapterPostion).tourWaypoint.setMainImgPath(data.getData().toString());
            mAdapter.notifyItemChanged(adapterPostion);
        }
    }

}
