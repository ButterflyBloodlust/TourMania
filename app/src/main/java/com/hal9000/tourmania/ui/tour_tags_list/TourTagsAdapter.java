package com.hal9000.tourmania.ui.tour_tags_list;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.hal9000.tourmania.R;
import com.hal9000.tourmania.model.TourTag;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

import androidx.recyclerview.widget.RecyclerView;

public class TourTagsAdapter extends RecyclerView.Adapter<TourTagsAdapter.MyViewHolder> implements ItemTouchHelperAdapter{
    private ArrayList<TourTag> mDataset;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView titleTextView;

        public MyViewHolder(View v) {
            super(v);
            titleTextView = v.findViewById(R.id.tour_tag_text_view);
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public TourTagsAdapter(ArrayList<TourTag> myDataset) {
        mDataset = myDataset;
    }

    // Create new views (invoked by the layout manager)
    @NotNull
    @Override
    public TourTagsAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.tour_tags_rec_view_row, parent, false);
        TourTagsAdapter.MyViewHolder vh = new TourTagsAdapter.MyViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(final TourTagsAdapter.MyViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        holder.titleTextView.setText(mDataset.get(position).getTag());
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.size();
    }

    @Override
    public void onItemDismiss(int position) {
        mDataset.remove(position);
        notifyItemRemoved(position);
    }
}
