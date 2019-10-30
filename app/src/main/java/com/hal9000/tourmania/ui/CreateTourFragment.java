package com.hal9000.tourmania.ui;

import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.hal9000.tourmania.R;

public class CreateTourFragment extends Fragment {

    private CreateTourViewModel createTourViewModel;

    public static CreateTourFragment newInstance() {
        return new CreateTourFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        createTourViewModel =
                ViewModelProviders.of(this).get(CreateTourViewModel.class);
        View root = inflater.inflate(R.layout.fragment_create_tour, container, false);
        final TextView textView = root.findViewById(R.id.text_create_tour);
        createTourViewModel.getText().observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });
        return root;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        createTourViewModel = ViewModelProviders.of(this).get(CreateTourViewModel.class);
        // TODO: Use the ViewModel
    }



}
