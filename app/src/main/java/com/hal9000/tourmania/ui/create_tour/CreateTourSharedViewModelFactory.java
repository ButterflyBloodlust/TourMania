package com.hal9000.tourmania.ui.create_tour;

import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

public class CreateTourSharedViewModelFactory implements ViewModelProvider.Factory {
    @Override
    public <T extends ViewModel> T create(Class<T> modelClass) {
        return (T)new CreateTourSharedViewModel();
    }
}