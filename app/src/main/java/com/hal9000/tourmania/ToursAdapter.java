package com.hal9000.tourmania;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.hal9000.tourmania.model.TourWithWpWithPaths;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;

import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

public class ToursAdapter extends RecyclerView.Adapter<ToursAdapter.MyViewHolder> {
    private List<TourWithWpWithPaths> mDataset;
    ToursAdapterCallback callback;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class MyViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView textView;
        public ImageView tourImage;
        public MyViewHolder(View v) {
            super(v);
            textView = v.findViewById(R.id.tour_title);
            tourImage = v.findViewById(R.id.tour_list_image);
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public ToursAdapter(List<TourWithWpWithPaths> toursWithTourWps, ToursAdapterCallback callback) {
        mDataset = toursWithTourWps;
        this.callback = callback;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public ToursAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent,
                                                     int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.tour_rec_view_row, parent, false);
        //...
        MyViewHolder vh = new MyViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        holder.textView.setText(mDataset.get(position).tour.getTitle());

        String mainImgPath = mDataset.get(position).tour.getTourImgPath();
        //Log.d("crashTest", mainImgPath == null ? "null" : mainImgPath);
        if (mainImgPath != null) {
            try {
                InputStream inStream = callback.getContext().getContentResolver().openInputStream(Uri.parse(mainImgPath));
                Bitmap bmp = BitmapFactory.decodeStream(inStream);
                holder.tourImage.setImageBitmap(bmp);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
        else {
            holder.tourImage.setImageResource(R.drawable.ic_menu_gallery);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callback.navigateToViewTour();
            }
        });
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.size();
    }

    public interface ToursAdapterCallback {
        Context getContext();
        void navigateToViewTour();
    }
}
