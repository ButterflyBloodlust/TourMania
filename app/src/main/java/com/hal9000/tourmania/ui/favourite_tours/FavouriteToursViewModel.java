package com.hal9000.tourmania.ui.favourite_tours;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class FavouriteToursViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public FavouriteToursViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is favourite_tours fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}