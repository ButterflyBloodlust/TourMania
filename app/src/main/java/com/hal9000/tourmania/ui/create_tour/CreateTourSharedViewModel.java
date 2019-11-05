package com.hal9000.tourmania.ui.create_tour;

import android.util.Log;

import com.hal9000.tourmania.model.TourWpWithPicPaths;

import java.util.ArrayList;

import androidx.lifecycle.ViewModel;

public class CreateTourSharedViewModel extends ViewModel {

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
