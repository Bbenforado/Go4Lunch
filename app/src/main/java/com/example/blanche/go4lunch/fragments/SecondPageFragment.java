package com.example.blanche.go4lunch.fragments;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.LocationListener;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.example.blanche.go4lunch.R;
import com.example.blanche.go4lunch.activities.RestaurantDetailsActivity;
import com.example.blanche.go4lunch.adapters.RecyclerViewAdapter;
import com.example.blanche.go4lunch.models.RestaurantObject;
import com.example.blanche.go4lunch.models.RestaurantsResults;
import com.example.blanche.go4lunch.utils.ItemClickSupport;
import com.example.blanche.go4lunch.utils.RestaurantStreams;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableObserver;

/**
 * A simple {@link Fragment} subclass.
 */
public class SecondPageFragment extends Fragment {

    public static final String KEY_POSITION = "position";
    public static final String RESTAURANT_NAME = "name";
    public static final String TYPE_OF_FOOD_AND_ADRESS = "typeAndAdress";
    public static final String LATITUDE_AND_LONGITUDE = "latitudeAndLongitude";
    public static final String APP_PREFERENCES = "appPreferences";
    String coordinates;
    SharedPreferences preferences;
    private Disposable disposable;
    private List<RestaurantsResults> restaurantsResultsList;
    private RecyclerViewAdapter adapter;

    @BindView(R.id.fragment_second_page_recycler_view)
    RecyclerView recyclerView;
    @BindView(R.id.fragment_second_page_swipe_container)
    SwipeRefreshLayout swipeRefreshLayout;

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
        System.out.println("on create fragment");
        View result = inflater.inflate(R.layout.fragment_second_page, container, false);
        ButterKnife.bind(this, result);
        preferences = this.getActivity().getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
        coordinates = preferences.getString(LATITUDE_AND_LONGITUDE, null);
        System.out.println("coordinates = " + coordinates);
        executeHttpRequestForRestaurant(coordinates);
        configureRecyclerView();
        configureOnClickRecyclerView();
        configureSwipeRefreshLayout();
        return result;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        disposeWhenDestroy();
        System.out.println("on destroy fragment");
    }

    //--------------------
    //CONFIGURATION
    //-------------------
    private void configureRecyclerView() {
        //here we fetch an arrayList of objects restaurants and set the adapter to the
        //recycler view, something like:
        restaurantsResultsList = new ArrayList<>();
        adapter = new RecyclerViewAdapter(this.restaurantsResultsList, Glide.with(this));
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
    }

    private void configureSwipeRefreshLayout() {
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                //refresh the page, request the api
                executeHttpRequestForRestaurant(coordinates);
            }
        });
    }

    private void configureOnClickRecyclerView() {
        ItemClickSupport.addTo(recyclerView, R.layout.fragment_second_item)
                .setOnItemClickListener(new ItemClickSupport.OnItemClickListener() {
                    @Override
                    public void onItemClicked(RecyclerView recyclerView, int position, View v) {
                        //put in bundle informations about restaurant
                        Bundle bundle = new Bundle();
                        bundle.putString(RESTAURANT_NAME, restaurantsResultsList.get(position).getName());
                        bundle.putString(TYPE_OF_FOOD_AND_ADRESS, restaurantsResultsList.get(position).getVicinity());
                        launchRestaurantDetailsActivity(bundle);


                    }
                });
    }

    private void launchRestaurantDetailsActivity(Bundle bundle) {
        Intent restaurantDetailActivity = new Intent(getContext(), RestaurantDetailsActivity.class);
        restaurantDetailActivity.putExtras(bundle);
        startActivity(restaurantDetailActivity);
    }

    //---------------------
    //HTTP REQUEST
    //-------------------------
    public void executeHttpRequestForRestaurant(String latlng) {
        disposable =
                RestaurantStreams.streamFetchRestaurants(latlng, 1500, "restaurant", "AIzaSyAolE90HXhEuYkd1kR0AEGly1uq8eyNig8")
                .subscribeWith(new DisposableObserver<RestaurantObject>() {

                    @Override
                    public void onNext(RestaurantObject restaurantObject) {
                        Log.e("TAG", "on next");
                        System.out.println("size request = " + restaurantObject.getResults().size());
                        //updateUI
                        updateUiWithRestaurants(restaurantObject.getResults());
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e("TAG", "on error");
                    }

                    @Override
                    public void onComplete() {
                        Log.e("TAG", "on complete");
                    }
                });
    }

    //-----------------------
    //UPDATE UI
    //-----------------------
    private void disposeWhenDestroy() {
        if(this.disposable != null && !this.disposable.isDisposed()) {
            this.disposable.dispose();
        }
    }

    private void updateUiWithRestaurants(List<RestaurantsResults> results) {
        System.out.println("coming here?");
        System.out.println("size update = " + results.size());
        restaurantsResultsList.clear();
        restaurantsResultsList.addAll(results);
        adapter.notifyDataSetChanged();
    }

}
