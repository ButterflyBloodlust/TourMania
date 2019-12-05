package com.hal9000.tourmania.ui.create_tour;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

// Allows for scoping view model to multiple fragments within the same navigation group.
public class CreateTourSharedViewModelFactory implements ViewModelProvider.Factory {
    @Override
    @NonNull
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        return (T)new CreateTourSharedViewModel();
    }
}