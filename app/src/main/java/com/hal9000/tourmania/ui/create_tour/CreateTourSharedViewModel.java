package com.hal9000.tourmania.ui.create_tour;

import android.util.Log;

import com.hal9000.tourmania.model.TourWaypoint;
import com.hal9000.tourmania.model.TourWpWithPicPaths;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class CreateTourSharedViewModel extends ViewModel {

    private ArrayList<TourWpWithPicPaths> tourWaypointList = new ArrayList<TourWpWithPicPaths>();

    public CreateTourSharedViewModel() {
        Log.d("crashTest", "CreateTourSharedViewModel.CreateTourSharedViewModel()");
    }

    public ArrayList<TourWpWithPicPaths> getTourWaypointList() {
        return tourWaypointList;
    }

    /*
    public void notifyTourWpsChanged() {
        // In case of advanced operations on observed lists in the future, replace with dedicated methods to work on list within ViewModel.
        tourWaypointList.setValue(tourWaypointList.getValue());
    }
    */


    @Override
    public void onCleared() {
        super.onCleared();
        Log.d("crashTest", "CreateTourSharedViewModel.onCleared()");
    }

}
