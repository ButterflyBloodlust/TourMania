package com.hal9000.tourmania.ui.tour_guides;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class TourGuidesViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public TourGuidesViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is tour_guides fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}