package com.hal9000.tourmania.ui;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class CreateTourViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public CreateTourViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is create tour fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}
