package com.hal9000.tourmania;

import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import com.hal9000.tourmania.model.TourWpWithPicPaths;

import java.util.ArrayList;

import androidx.recyclerview.widget.RecyclerView;

public class TourWaypointsAdapter extends RecyclerView.Adapter<TourWaypointsAdapter.MyViewHolder> {
    private ArrayList<TourWpWithPicPaths> mDataset;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class MyViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public EditText titleEditText;
        public MyViewHolder(View v) {
            super(v);
            titleEditText = v.findViewById(R.id.tour_title);
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public TourWaypointsAdapter(ArrayList<TourWpWithPicPaths> myDataset) {
        mDataset = myDataset;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public TourWaypointsAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent,
                                                        int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.tour_waypoints_rec_view_row, parent, false);
        //...
        MyViewHolder vh = new MyViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        holder.titleEditText.setText(mDataset.get(position).tourWaypoint.getTitle());

        // Handle annotation label updates.
        holder.titleEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    mDataset.get(position).tourWaypoint.setTitle(v.getText().toString());
                }
                return false;   // don't consume action (allow propagation)
            }
        });
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.size();
    }
}
