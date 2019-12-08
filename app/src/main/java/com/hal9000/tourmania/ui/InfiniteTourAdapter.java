package com.hal9000.tourmania.ui;

import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.hal9000.tourmania.MainActivity;
import com.hal9000.tourmania.ui.search.OnLoadMoreListener;
import com.hal9000.tourmania.R;
import com.hal9000.tourmania.SharedPrefUtils;
import com.hal9000.tourmania.database.AppDatabase;
import com.hal9000.tourmania.model.TourWithWpWithPaths;
import com.hal9000.tourmania.model.TourWpWithPicPaths;
import com.hal9000.tourmania.rest_api.RestClient;
import com.hal9000.tourmania.rest_api.tours.ToursService;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.List;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class InfiniteTourAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
    public List<TourWithWpWithPaths> mDataset;
    private InfiniteTourAdapter.ToursAdapterCallback callback;
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
        public ImageView tourImageView;
        public ImageButton deleteImageButton;
        public MyViewHolder(View v) {
            super(v);
            textView = v.findViewById(R.id.tour_title);
            tourImageView = v.findViewById(R.id.tour_list_image);
            deleteImageButton = v.findViewById(R.id.buttonDeleteTour);
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
    public InfiniteTourAdapter(List<TourWithWpWithPaths> toursWithTourWps, InfiniteTourAdapter.ToursAdapterCallback callback,
                               int rowLayoutId, RecyclerView recyclerView) {
        mDataset = toursWithTourWps;
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
            ((MyViewHolder) holder).textView.setText(mDataset.get(position).tour.getTitle());

            String mainImgPath = mDataset.get(position).tour.getTourImgPath();
            //Log.d("crashTest", mainImgPath == null ? "null" : mainImgPath);
            if (!TextUtils.isEmpty(mainImgPath)) {
                // asynchronous image loading
                Picasso.get() //
                        .load(Uri.parse(mainImgPath)) //
                        .fit() //
                        .into(((MyViewHolder) holder).tourImageView);
            } else {
                ((MyViewHolder) holder).tourImageView.setImageResource(R.drawable.ic_menu_gallery);
            }

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    callback.navigateToViewTour(holder.getAdapterPosition());
                }
            });

            if (((MyViewHolder) holder).deleteImageButton != null) {
                ((MyViewHolder) holder).deleteImageButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final TourWithWpWithPaths tourWithWpWithPaths = mDataset.remove(holder.getAdapterPosition());
                        //Log.d("crashTest", "delete tour from server db");
                        notifyItemRemoved(holder.getAdapterPosition());
                        AppDatabase.databaseWriteExecutor.submit(new Runnable() {
                            @Override
                            public void run() {
                                // delete tour from local db
                                AppDatabase appDatabase = AppDatabase.getInstance(callback.getContext());
                                appDatabase.tourDAO().deleteTourWp(tourWithWpWithPaths.tour);
                            }
                        });

                        // delete files related to tour, stored in app's dirs
                        String mainImgPath = tourWithWpWithPaths.tour.getTourImgPath();
                        if (mainImgPath != null)
                            new File(callback.getContext().getExternalCacheDir(), new File(mainImgPath).getName()).delete();
                        for (TourWpWithPicPaths tourWpWithPicPaths : tourWithWpWithPaths._tourWpsWithPicPaths) {
                            mainImgPath = tourWpWithPicPaths.tourWaypoint.getMainImgPath();
                            if (mainImgPath != null)
                                new File(callback.getContext().getExternalCacheDir(), new File(mainImgPath).getName()).delete();
                        }

                        // delete tour from server db
                        ToursService client = RestClient.createService(ToursService.class,
                                SharedPrefUtils.getString(callback.getContext(), MainActivity.getLoginTokenKey()));
                        Call<Void> call = client.deleteTourById(tourWithWpWithPaths.tour.getServerTourId());
                        call.enqueue(new Callback<Void>() {
                            @Override
                            public void onResponse(Call<Void> call, Response<Void> response) {
                                //Log.d("crashTest", "deleteImageButton onResponse");
                            }

                            @Override
                            public void onFailure(Call<Void> call, Throwable t) {
                                t.printStackTrace();
                                //Log.d("crashTest", "deleteImageButton onFailure");
                            }
                        });
                    }
                });
            }
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
