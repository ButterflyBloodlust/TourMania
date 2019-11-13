package com.hal9000.tourmania.ui.create_tour;

import android.content.Context;
import android.util.Log;

import com.hal9000.tourmania.database.AppDatabase;
import com.hal9000.tourmania.model.Tour;
import com.hal9000.tourmania.model.TourWaypoint;
import com.hal9000.tourmania.model.TourWithWpWithPaths;
import com.hal9000.tourmania.model.TourWpWithPicPaths;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Future;

import androidx.lifecycle.ViewModel;

public class CreateTourSharedViewModel extends ViewModel {

    private Tour tour = new Tour();
    private ArrayList<TourWpWithPicPaths> tourWaypointList = new ArrayList<TourWpWithPicPaths>();
    private int choosenLocateWaypointIndex = -1;

    public CreateTourSharedViewModel() {
        //Log.d("crashTest", "CreateTourSharedViewModel.CreateTourSharedViewModel()");
    }

    public ArrayList<TourWpWithPicPaths> getTourWaypointList() {
        return tourWaypointList;
    }

    public int getChoosenLocateWaypointIndex() {
        return choosenLocateWaypointIndex;
    }

    public void setChoosenLocateWaypointIndex(int choosenLocateWaypointIndex) {
        this.choosenLocateWaypointIndex = choosenLocateWaypointIndex;
    }

    public void removeChoosenLocateWaypointIndex() {
        choosenLocateWaypointIndex = -1;
    }

    public Tour getTour() {
        return tour;
    }

    public Future saveTourToDb(final Context context) {
        // Currently does NOT handle additional waypoint pics (PicturePath / TourWpWithPicPaths)
        //Log.d("crashTest", "saveTourToDb()");
        return AppDatabase.databaseWriteExecutor.submit(new Runnable() {
            public void run() {
                //Log.d("crashTest", "run()");
                AppDatabase appDatabase = AppDatabase.getInstance(context);
                long tourId = appDatabase.tourDAO().insertTour(getTour());
                getTour().setTourId((int)tourId);
                LinkedList<TourWaypoint> tourWps = new LinkedList<>();
                for (int i = 0; i < tourWaypointList.size(); i++) {
                    TourWaypoint tourWaypoint = tourWaypointList.get(i).tourWaypoint;
                    tourWaypoint.setTourId((int)tourId);
                    tourWaypoint.setWpOrder(i);
                    tourWps.addLast(tourWaypoint);
                }
                long[] wpsIds = appDatabase.tourWaypointDAO().insertTourWps(tourWps);
                for (int i = 0; i < wpsIds.length; i++) {
                    tourWaypointList.get(i).tourWaypoint.setTourWpId((int)wpsIds[i]);
                }
                //List<Tour> toursWithTourWps = AppDatabase.getInstance(requireContext()).tourDAO().getTours();
                //List<TourWithWpWithPaths> toursWithTourWps = appDatabase.tourWaypointDAO().getToursWithTourWps();
                //Log.d("crashTest", Integer.toString(toursWithTourWps.size()));
            }
        });
    }

    /*
    public void notifyTourWpsChanged() {
        // In case of advanced operations on observed lists in the future, replace with dedicated methods to work on list within ViewModel.
        tourWaypointList.setValue(tourWaypointList.getValue());
    }
    */

    /*
    @Override
    public void onCleared() {
        super.onCleared();
        Log.d("crashTest", "CreateTourSharedViewModel.onCleared()");
    }
    */

}
