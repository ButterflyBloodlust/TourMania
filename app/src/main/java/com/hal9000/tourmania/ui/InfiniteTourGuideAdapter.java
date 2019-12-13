package com.hal9000.tourmania.ui;

import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.hal9000.tourmania.R;
import com.hal9000.tourmania.model.User;
import com.hal9000.tourmania.ui.search.OnLoadMoreListener;
import com.squareup.picasso.Picasso;

import java.util.List;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class InfiniteTourGuideAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
    public List<User> mDataset;
    private InfiniteTourGuideAdapter.ToursAdapterCallback callback;
    private int rowLayoutId;

    private final int VIEW_ITEM = 1;
    private final int VIEW_PROG = 0;

    private int visibleThreshold = 3;
    private int lastVisibleItem, totalItemCount;
    private boolean loading;
    private int progressBarPosition;
    private OnLoadMoreListener onLoadMoreListener;
    private RecyclerView recyclerView;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class MyViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView textView;
        public ImageView userImageView;
        public MyViewHolder(View v) {
            super(v);
            textView = v.findViewById(R.id.tour_guide_title);
            userImageView = v.findViewById(R.id.tour_guide_list_image);
        }
    }

    public static class ProgressViewHolder extends RecyclerView.ViewHolder {
        public ProgressBar progressBar;

        public ProgressViewHolder(View v) {
            super(v);
            progressBar = (ProgressBar) v.findViewById(R.id.progressBar1);
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public InfiniteTourGuideAdapter(List<User> users, InfiniteTourGuideAdapter.ToursAdapterCallback callback,
                                    int rowLayoutId, RecyclerView recyclerView) {
        mDataset = users;
        this.callback = callback;
        this.rowLayoutId = rowLayoutId;
        this.recyclerView = recyclerView;

        if (recyclerView.getLayoutManager() instanceof LinearLayoutManager) {
            final LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView
                    .getLayoutManager();

            recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(RecyclerView recyclerView,
                                       int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);

                    totalItemCount = linearLayoutManager.getItemCount();
                    lastVisibleItem = linearLayoutManager.findLastVisibleItemPosition();
                    if (!loading && totalItemCount <= (lastVisibleItem + visibleThreshold)) {
                        // End has been reached
                        // Do something
                        if (onLoadMoreListener != null) {
                            setLoading();
                            //Log.d("crashTest", "onLoadMore");
                            onLoadMoreListener.onLoadMore();
                        }
                    }
                }
            });
        }
    }

    public void setLoading() {
        loading = true;
        showProgressBar();
    }

    public void showProgressBar() {
        //add null , so the adapter will check view_type and show progress bar at bottom
        try {
            mDataset.add(null);
            progressBarPosition = mDataset.size() - 1;
            recyclerView.post(new Runnable() {
                public void run() {
                    notifyDataSetChanged();
                    //notifyItemInserted(progressBarPosition);
                }
            });
        } catch (Exception e ) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemViewType(int position) {
        return mDataset.get(position) != null ? VIEW_ITEM : VIEW_PROG;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                        int viewType) {
        // create a new view
        RecyclerView.ViewHolder vh;
        if (viewType == VIEW_ITEM) {
            View v = LayoutInflater.from(parent.getContext()).inflate(
                    rowLayoutId, parent, false);

            vh = new MyViewHolder(v);
        } else {
            View v = LayoutInflater.from(parent.getContext()).inflate(
                    R.layout.progressbar_item, parent, false);

            vh = new ProgressViewHolder(v);
        }
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        if (holder instanceof MyViewHolder) {
            ((MyViewHolder) holder).textView.setText(mDataset.get(position).getUsername());

            String mainImgPath = mDataset.get(position).getUserImgPath();
            //Log.d("crashTest", mainImgPath == null ? "null" : mainImgPath);
            if (!TextUtils.isEmpty(mainImgPath)) {
                // asynchronous image loading
                Picasso.get() //
                        .load(Uri.parse(mainImgPath)) //
                        .fit() //
                        .into(((MyViewHolder) holder).userImageView);
            } else {
                ((MyViewHolder) holder).userImageView.setImageResource(R.drawable.ic_person_black_24dp);
            }

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    callback.navigateToViewTour(holder.getAdapterPosition());
                }
            });
        }//if (holder instanceof StudentViewHolder) {
        else {
            ((ProgressViewHolder) holder).progressBar.setIndeterminate(true);
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.size();
    }

    public interface ToursAdapterCallback {
        Context getContext();
        void navigateToViewTour(int position);
    }

    public void setLoaded() {
        if (loading) {
            loading = false;
            //Log.d("crashTest", "setLoaded()");
            // Remove progress item
            if (progressBarPosition < mDataset.size() && progressBarPosition >= 0) {
                mDataset.remove(progressBarPosition);
                recyclerView.post(new Runnable() {
                    public void run() {
                        notifyDataSetChanged();
                        //notifyItemRemoved(progressBarPosition);
                    }
                });
                progressBarPosition = -1;
            }

        }
    }

    public void setOnLoadMoreListener(OnLoadMoreListener onLoadMoreListener) {
        this.onLoadMoreListener = onLoadMoreListener;
    }
}
