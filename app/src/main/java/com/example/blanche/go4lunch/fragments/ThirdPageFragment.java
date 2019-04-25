package com.example.blanche.go4lunch.fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.blanche.go4lunch.R;
import com.example.blanche.go4lunch.adapters.RecyclerViewAdapter;
import com.example.blanche.go4lunch.adapters.RecyclerViewAdapterThirdFragment;
import com.example.blanche.go4lunch.utils.ItemClickSupport;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * A simple {@link Fragment} subclass.
 */
public class ThirdPageFragment extends Fragment {

    public static final String KEY_POSITION = "position";
    private static final String KEY_RESTAURANT = "restaurant";
    public static final String RESTAURANT_NAME = "name";
    Bundle bundle;
    private RecyclerViewAdapterThirdFragment adapter;

    @BindView(R.id.fragment_page_recycler_view)
    RecyclerView recyclerView;
    @BindView(R.id.fragment_page_swipe_container)
    SwipeRefreshLayout swipeRefreshLayout;

    public ThirdPageFragment() {
        // Required empty public constructor
    }

    public static ThirdPageFragment newInstance(int position) {
        ThirdPageFragment fragment = new ThirdPageFragment();
        Bundle args = new Bundle();
        args.putInt(KEY_POSITION, position);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View result = inflater.inflate(R.layout.fragment_third_page, container, false);
        ButterKnife.bind(this, result);
        configureRecyclerView();
        return result;
    }

    //--------------------
    //CONFIGURATION
    //-------------------
    private void configureRecyclerView() {
        //here we fetch an arrayList of objects restaurants and set the adapter to the
        //recycler view, something like:
        //restaurantResultsList = new ArrayList<>();
        adapter = new RecyclerViewAdapterThirdFragment();
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
    }

    private void configureSwipeRefreshLayout() {
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                //refresh the page, request the api
            }
        });
    }

    private void configureOnClickRecyclerView() {
        ItemClickSupport.addTo(recyclerView, R.layout.fragment_third_item)
                .setOnItemClickListener(new ItemClickSupport.OnItemClickListener() {
                    @Override
                    public void onItemClicked(RecyclerView recyclerView, int position, View v) {
                        //LAUNCH RESTAURANT ACTIVITY
                        //which displays picture of restaurant and some informations
                    }
                });
    }

}
