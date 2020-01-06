package com.hal9000.tourmania.ui.tour_tags_list;

import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.ViewModelProvider;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelStoreOwner;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.hal9000.tourmania.AppUtils;
import com.hal9000.tourmania.R;
import com.hal9000.tourmania.model.TourTag;
import com.hal9000.tourmania.ui.tour_waypoints_list.TourWaypointsAdapter;
import com.hal9000.tourmania.ui.create_tour.CreateTourSharedViewModel;
import com.hal9000.tourmania.ui.create_tour.CreateTourSharedViewModelFactory;

import java.util.List;

public class TourTagsListFragment extends Fragment {

    private CreateTourSharedViewModel mViewModel;

    private RecyclerView recyclerView;
    private TourTagsAdapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;

    public static TourTagsListFragment newInstance() {
        return new TourTagsListFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_tour_tags_list, container, false);
        return root;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onViewCreated (@NonNull View view, Bundle savedInstanceState) {
        // Scope ViewModel to nested nav graph.
        ViewModelStoreOwner owner = Navigation.findNavController(view).getViewModelStoreOwner(R.id.nav_nested_create_tour);
        //CreateTourSharedViewModelFactory factory = new CreateTourSharedViewModelFactory();
        ViewModelProvider.AndroidViewModelFactory factory = ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().getApplication());
        mViewModel = new ViewModelProvider(owner, factory).get(CreateTourSharedViewModel.class);

        FloatingActionButton fab = view.findViewById(R.id.fab_add_tour_tag);
        if (mViewModel.isEditingEnabled()) {
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Context context = requireContext();
                    LayoutInflater li = LayoutInflater.from(context);
                    View promptsView = li.inflate(R.layout.text_prompt_dialog, null);
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
                    alertDialogBuilder.setView(promptsView);
                    final EditText userInput = (EditText) promptsView.findViewById(R.id.editTextDialogUserInput);

                    // set dialog message
                    alertDialogBuilder
                            .setCancelable(false)
                            .setPositiveButton("OK",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog,int id) {
                                            String tourTag = userInput.getText().toString();
                                            //Log.d("crashTest", tourTag);
                                            if (!tourTag.isEmpty()) {
                                                List<TourTag> tourTagList = mViewModel.getTourTagsList();
                                                tourTagList.add(new TourTag(userInput.getText().toString()));
                                                mAdapter.notifyItemInserted(mViewModel.getTourTagsList().size() - 1);
                                            }
                                        }
                                    })
                            .setNegativeButton("Cancel",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog,int id) {
                                            dialog.cancel();
                                        }
                                    });

                    AlertDialog alertDialog = alertDialogBuilder.create();
                    Window alerWindow = alertDialog.getWindow();
                    if (alerWindow != null)
                        alerWindow.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
                    alertDialog.show();
                }
            });
            fab.show();
        }

        createRecyclerView(view);
    }

    private void createRecyclerView(View root) {
        // Create recycler view
        recyclerView = (RecyclerView) root.findViewById(R.id.tour_tags_recycler_view);
        recyclerView.setHasFixedSize(true);
        // use a linear layout manager
        layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);

        // specify an adapter
        mAdapter = new TourTagsAdapter(mViewModel.getTourTagsList());
        recyclerView.setAdapter(mAdapter);

        if (mViewModel.isEditingEnabled()) {
            ItemTouchHelper.Callback callback = new ItemTouchHelperCallback(mAdapter);
            ItemTouchHelper touchHelper = new ItemTouchHelper(callback);
            touchHelper.attachToRecyclerView(recyclerView);
        }
    }


}
