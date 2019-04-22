package com.example.blanche.go4lunch.fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.blanche.go4lunch.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class SecondPageFragment extends Fragment {

    public static final String KEY_POSITION = "position";

    public SecondPageFragment() {
        // Required empty public constructor
    }

    public static SecondPageFragment newInstance(int position) {
        SecondPageFragment fragment = new SecondPageFragment();
        Bundle args = new Bundle();
        args.putInt(KEY_POSITION, position);
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_second_page, container, false);
    }

}
