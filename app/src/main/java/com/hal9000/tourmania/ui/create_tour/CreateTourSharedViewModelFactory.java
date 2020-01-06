package com.hal9000.tourmania.ui.create_tour;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

// Allows for scoping view model to multiple fragments within the same navigation group.
public class CreateTourSharedViewModelFactory extends ViewModelProvider.AndroidViewModelFactory {
    @NonNull
    private final Application application;

    public CreateTourSharedViewModelFactory(@NonNull Application application) {
        super(application);
        this.application = application;
    }

    @Override
    @NonNull
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        return (T)new CreateTourSharedViewModel(application);
    }
}