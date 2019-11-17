package com.hal9000.tourmania.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.hal9000.tourmania.R;
import com.hal9000.tourmania.model.TourWpWithPicPaths;
import com.hal9000.tourmania.ui.create_tour.CreateTourSharedViewModel;
import com.hal9000.tourmania.ui.tour_waypoints_list.ItemTouchHelperAdapter;

import org.jetbrains.annotations.NotNull;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;

import androidx.recyclerview.widget.RecyclerView;

public class TourWaypointsAdapter extends RecyclerView.Adapter<TourWaypointsAdapter.MyViewHolder> implements ItemTouchHelperAdapter {
    private ArrayList<TourWpWithPicPaths> mDataset;
    private TourWaypointsOnClickListener callbackOnClickListener;
    public boolean dragButtonPressed = false;
    CreateTourSharedViewModel mViewModel;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class MyViewHolder extends RecyclerView.ViewHolder {
        public EditText titleEditText;
        public ImageButton buttonMoreImages;
        public ImageButton buttonDragRow;
        public ImageButton buttonShowWaypointLocation;
        public ImageView tourWpImage;

        public MyViewHolder(View v) {
            super(v);
            titleEditText = v.findViewById(R.id.tour_title);
            buttonDragRow = v.findViewById(R.id.buttonDragRow);
            buttonShowWaypointLocation = v.findViewById(R.id.buttonShowWaypointLocation);
            tourWpImage = v.findViewById(R.id.tour_list_image);
            buttonMoreImages = v.findViewById(R.id.buttonMoreImages);
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public TourWaypointsAdapter(ArrayList<TourWpWithPicPaths> myDataset,
                                TourWaypointsOnClickListener callbackOnClickListener,
                                CreateTourSharedViewModel mViewModel) {
        mDataset = myDataset;
        this.callbackOnClickListener = callbackOnClickListener;
        this.mViewModel = mViewModel;
    }

    // Create new views (invoked by the layout manager)
    @NotNull
    @Override
    public TourWaypointsAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.tour_waypoints_rec_view_row, parent, false);
        //...
        MyViewHolder vh = new MyViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        holder.titleEditText.setText(mDataset.get(position).tourWaypoint.getTitle());

        String mainImgPath = mDataset.get(position).tourWaypoint.getMainImgPath();
        //Log.d("crashTest", mainImgPath == null ? "null" : mainImgPath);
        if (mainImgPath != null) {
            try {
                InputStream inStream = callbackOnClickListener.getContext().getContentResolver().openInputStream(Uri.parse(mainImgPath));
                Bitmap bmp = BitmapFactory.decodeStream(inStream);
                holder.tourWpImage.setImageBitmap(bmp);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
        else {
            holder.tourWpImage.setImageResource(R.drawable.ic_menu_gallery);
        }

        if (mViewModel.isEditingEnabled()) {
            // Handle annotation label updates.
            holder.titleEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    if (actionId == EditorInfo.IME_ACTION_DONE) {
                        mDataset.get(holder.getAdapterPosition()).tourWaypoint.setTitle(v.getText().toString());
                    }
                    return false;   // don't consume action (allow propagation)
                }
            });
            holder.titleEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    if (!hasFocus && holder.getAdapterPosition() >= 0) {
                        mDataset.get(holder.getAdapterPosition()).tourWaypoint.setTitle(((TextView) v).getText().toString());
                    }
                }
            });

            // Handle adding tour waypoint images
            holder.tourWpImage.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    callbackOnClickListener.pickPictureMainOnLongClick(holder.getAdapterPosition());
                    return false;
                }
            });

            // Handle row drag button (sets a flag when pressed and is furhter handled in ItemItemTouchHelperCallback)
            holder.buttonDragRow.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (event.getAction() == MotionEvent.ACTION_DOWN)
                        dragButtonPressed = true;
                    else if (event.getAction() == MotionEvent.ACTION_UP)
                        dragButtonPressed = false;
                    return false;
                }
            });
        }
        else {
            disableEditText(holder.titleEditText);
            holder.buttonDragRow.setVisibility(View.GONE);
        }

        // Handle show location button
        holder.buttonShowWaypointLocation.setOnClickListener(new ImageButton.OnClickListener() {
            @Override
            public void onClick(View v) {
                callbackOnClickListener.locateWaypointOnClick(v, holder.getAdapterPosition());
            }
        });

        // Handle displaying tour waypoint images
        holder.tourWpImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String mainImgPath = mDataset.get(holder.getAdapterPosition()).tourWaypoint.getMainImgPath();
                if (mainImgPath != null)
                    callbackOnClickListener.getContext().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(mainImgPath)));
            }
        });
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.size();
    }

    // Callback interface for parent activity / fragment
    public interface TourWaypointsOnClickListener {
        void locateWaypointOnClick(View v, int position);
        void pickPictureMainOnLongClick(int position);
        Context getContext();
    }

    @Override
    public void onItemDismiss(int position) {
        mDataset.remove(position);
        notifyItemRemoved(position);
    }

    @Override
    public void onItemMove(int fromPosition, int toPosition) {
        if (fromPosition < toPosition) {
            for (int i = fromPosition; i < toPosition; i++) {
                Collections.swap(mDataset, i, i + 1);
            }
        } else {
            for (int i = fromPosition; i > toPosition; i--) {
                Collections.swap(mDataset, i, i - 1);
            }
        }
        notifyItemMoved(fromPosition, toPosition);
    }

    public boolean isDragButtonPressed() {
        return dragButtonPressed;
    }

    private void disableEditText(EditText editText) {
        editText.setFocusable(false);
        editText.setFocusableInTouchMode(false);
        editText.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
        //editText.setEnabled(false);
        editText.setCursorVisible(false);
        editText.setTextColor(0xffffffff);
    }
}
