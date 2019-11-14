package com.hal9000.tourmania.ui.join_tour;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.hal9000.tourmania.R;

public class JoinTourFragment extends Fragment {

    private JoinTourViewModel joinTourViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        joinTourViewModel =
                ViewModelProviders.of(this).get(JoinTourViewModel.class);
        View root = inflater.inflate(R.layout.fragment_join_tour, container, false);
        final TextView textView = root.findViewById(R.id.text_join_tour);
        joinTourViewModel.getText().observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });
        return root;
    }
}