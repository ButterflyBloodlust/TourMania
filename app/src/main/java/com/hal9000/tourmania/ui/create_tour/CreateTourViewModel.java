package com.hal9000.tourmania.ui.create_tour;

import android.util.Log;

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

    /*
    @Override
    public void onCleared() {
        Log.d("crashTest", "CreateTourViewModel.onCleared()");
        super.onCleared();
    }
    */
}
