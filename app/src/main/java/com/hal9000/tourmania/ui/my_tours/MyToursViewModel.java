package com.hal9000.tourmania.ui.my_tours;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class MyToursViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public MyToursViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is my_tours fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}