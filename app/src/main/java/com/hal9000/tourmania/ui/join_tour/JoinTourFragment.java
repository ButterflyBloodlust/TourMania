package com.hal9000.tourmania.ui.join_tour;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.Navigation;

import com.hal9000.tourmania.R;
import com.hal9000.tourmania.ui.create_tour.CreateTourFragmentArgs;

public class JoinTourFragment extends Fragment {

    private JoinTourViewModel joinTourViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        joinTourViewModel = ViewModelProviders.of(this).get(JoinTourViewModel.class);
        View root = inflater.inflate(R.layout.fragment_join_tour, container, false);

        Button joinTourButton = root.findViewById(R.id.button_join_tour);
        joinTourButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Navigation.findNavController(requireView()).navigate(R.id.nav_nested_create_tour,
                        new CreateTourFragmentArgs.Builder().setTourServerId(
                                "5dee3c74121532341089d2dc").build().toBundle());
            }
        });

        return root;
    }
}