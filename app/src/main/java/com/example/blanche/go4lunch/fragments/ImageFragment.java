package com.example.blanche.go4lunch.fragments;


import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.blanche.go4lunch.R;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ImageFragment extends Fragment {

    @BindView(R.id.imageView)
    public ImageView imageView;

    public ImageFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_image, container, false);
        ButterKnife.bind(this, view);
        // Inflate the layout for this fragment
        return view;

    }

}
