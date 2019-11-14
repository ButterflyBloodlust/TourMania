package com.hal9000.tourmania.ui.view_tour;

import androidx.lifecycle.ViewModelProviders;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.hal9000.tourmania.R;

public class ViewTourFragment extends Fragment {

    private ViewTourViewModel mViewModel;

    public static ViewTourFragment newInstance() {
        return new ViewTourFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_view_tour, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = ViewModelProviders.of(this).get(ViewTourViewModel.class);
        // TODO: Use the ViewModel
    }

}
