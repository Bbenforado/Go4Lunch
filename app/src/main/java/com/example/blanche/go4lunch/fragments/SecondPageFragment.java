package com.example.blanche.go4lunch.fragments;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.bumptech.glide.Glide;
import com.example.blanche.go4lunch.R;
import com.example.blanche.go4lunch.activities.RestaurantDetailsActivity;
import com.example.blanche.go4lunch.adapters.RecyclerViewAdapter;
import com.example.blanche.go4lunch.models.OpeningHours;
import com.example.blanche.go4lunch.models.Restaurant;
import com.example.blanche.go4lunch.models.RestaurantInformationObject;
import com.example.blanche.go4lunch.models.RestaurantInformations;
import com.example.blanche.go4lunch.models.RestaurantObject;
import com.example.blanche.go4lunch.models.RestaurantsResults;
import com.example.blanche.go4lunch.utils.ItemClickSupport;
import com.example.blanche.go4lunch.utils.RestaurantStreams;

import java.sql.SQLOutput;
import java.util.ArrayList;
import java.util.Calendar;
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
    public static final String KEY_ACTIVITY = "keyActivity";
    public static final String RESTAURANT_NAME = "name";
    public static final String TYPE_OF_FOOD_AND_ADRESS = "typeAndAdress";
    public static final String RESTAURANT_PHONE_NUMBER = "number";
    public static final String RESTAURANT_WEBSITE = "website";
    public static final String RESTAURANT_PHOTO = "photo";
    public static final String LATITUDE_AND_LONGITUDE = "latitudeAndLongitude";
    public static final String APP_PREFERENCES = "appPreferences";
    public static final String CLOSING_HOURS = "closingHours";
    String coordinates;
    String closingHours;
    String distance;
    String pictureUrl;
    String name;
    String adress;
    SharedPreferences preferences;
    private Disposable disposable;
    private List<RestaurantsResults> restaurantsResultsList;
    private List<Restaurant> restaurantList;
    private List<RestaurantInformations> restaurantInformationsList;
    private RecyclerViewAdapter adapter;
    @BindView(R.id.bar)
    ProgressBar bar;
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
        View result = inflater.inflate(R.layout.fragment_second_page, container, false);
        ButterKnife.bind(this, result);
        preferences = this.getActivity().getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
        coordinates = preferences.getString(LATITUDE_AND_LONGITUDE, null);

        configureRecyclerView();
        //this.restaurantsResultsList = new ArrayList<>();
       // this.restaurantList = new ArrayList<>();
       // executeHttpRequestForRestaurant(coordinates);
        request(coordinates);
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
        System.out.println("enter in configure");

        //this.restaurantsResultsList = new ArrayList<>();
        this.restaurantInformationsList = new ArrayList<>();
        this.restaurantList = new ArrayList<>();
        this.adapter = new RecyclerViewAdapter(this.restaurantInformationsList, Glide.with(this));
        //this.adapter = new RecyclerViewAdapter(this.restaurantList, Glide.with(this));
        //this.adapter = new RecyclerViewAdapter(this.restaurantsResultsList, Glide.with(this));
        this.recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        System.out.println("exit configure");
    }

    private void configureSwipeRefreshLayout() {
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                //refresh the page, request the api
                //executeHttpRequestForRestaurant(coordinates);
                request(coordinates);
                bar.setVisibility(View.GONE);
            }
        });
    }

    private void configureOnClickRecyclerView() {
        ItemClickSupport.addTo(recyclerView, R.layout.fragment_second_item)
                .setOnItemClickListener(new ItemClickSupport.OnItemClickListener() {
                    @Override
                    public void onItemClicked(RecyclerView recyclerView, int position, View v) {
                        //put in bundle informations about restaurant
                        preferences.edit().putInt(KEY_ACTIVITY, 1).apply();
                        Bundle bundle = new Bundle();
                        bundle.putString(RESTAURANT_NAME, restaurantInformationsList.get(position).getName());
                        bundle.putString(TYPE_OF_FOOD_AND_ADRESS, restaurantInformationsList.get(position).getVicinity());
                        bundle.putString(RESTAURANT_PHOTO, restaurantInformationsList.get(position).getPhotos().get(0).getPhotoReference());
                        bundle.putString(RESTAURANT_PHONE_NUMBER, restaurantInformationsList.get(position).getFormattedPhoneNumber());
                        bundle.putString(RESTAURANT_WEBSITE, restaurantInformationsList.get(position).getWebsite());
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
    private void request(String latlng) {
        updateUiWhenStartingRequest();
        disposable =
                RestaurantStreams.streamFetchPlaceInfo(latlng, 1500, "restaurant", "AIzaSyA6Jk5Xl1MbXbYcfWywZ0vwUY2Ux4KLta4")
                        .subscribeWith(new DisposableObserver<List<RestaurantInformations>>() {

                            @Override
                            public void onNext(List<RestaurantInformations> restaurantInformationsList) {
                                updateList(restaurantInformationsList);
                            }

                            @Override
                            public void onError(Throwable e) {

                            }

                            @Override
                            public void onComplete() {

                            }
                        });
    }


  /*  public void executeHttpRequestForRestaurant(String latlng) {
        System.out.println("request 1");

        disposable =
                RestaurantStreams.streamFetchRestaurants(latlng, 1500, "restaurant", "AIzaSyA6Jk5Xl1MbXbYcfWywZ0vwUY2Ux4KLta4")
                .subscribeWith(new DisposableObserver<RestaurantObject>() {

                    @Override
                    public void onNext(RestaurantObject restaurantObject) {
                        Log.e("TAG", "on next first request");
                        //updateUI
                        for (int i=0; i<restaurantObject.getResults().size(); i++) {
                           // executeRequestForRestaurantInformations(restaurantObject.getResults().get(i).getPlaceId());
                            name = restaurantObject.getResults().get(i).getName();
                            adress = restaurantObject.getResults().get(i).getVicinity();
                            pictureUrl = restaurantObject.getResults().get(i).getPhotos().get(0).getPhotoReference();
                            System.out.println("closing hours = " + closingHours);
                            System.out.println("name = " + name);
                            System.out.println("adress = " + adress);
                        }
                        //restaurantList = createRestaurantsList(restaurantObject.getResults());
                        //updateUiWithRestaurants(restaurantObject.getResults());
                        //adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e("TAG", "on error");
                    }

                    @Override
                    public void onComplete() {
                        Log.e("TAG", "on complete first request");


                    }
                });
        System.out.println("exit request 1");
    }*/

    /*public void executeRequestForRestaurantInformations(final String id) {
        System.out.println("enter in request");
            disposable =
                    RestaurantStreams.streamFetchRestaurantInfos(id, "AIzaSyA6Jk5Xl1MbXbYcfWywZ0vwUY2Ux4KLta4")
                            .subscribeWith(new DisposableObserver<RestaurantInformationObject>() {
                                @Override
                                public void onNext(RestaurantInformationObject restaurantInformationObject) {
                                    Log.e("TAG", "on next");
                                    RestaurantInformations info = restaurantInformationObject.getResult();
                                    OpeningHours openingHours = info.getOpeningHours();
                                    List<String> openingHoursWeekday = openingHours.getWeekdayText();
                                    Calendar calendar = Calendar.getInstance();
                                    int day = calendar.get(Calendar.DAY_OF_WEEK);
                                    switch (day) {
                                        case Calendar.MONDAY:
                                            closingHours = openingHoursWeekday.get(0);
                                            break;
                                        case Calendar.TUESDAY:
                                            closingHours = openingHoursWeekday.get(1);
                                            break;
                                        case Calendar.WEDNESDAY:
                                            closingHours = openingHoursWeekday.get(2);
                                            break;
                                        case Calendar.THURSDAY:
                                            closingHours = openingHoursWeekday.get(3);
                                            break;
                                        case Calendar.FRIDAY:
                                            closingHours = openingHoursWeekday.get(4);
                                            break;
                                        case Calendar.SATURDAY:
                                            closingHours = openingHoursWeekday.get(5);
                                            break;
                                        case Calendar.SUNDAY:
                                            closingHours = openingHoursWeekday.get(6);
                                            break;
                                        default:
                                            break;
                                    }
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

        System.out.println("exit of request");
    }*/

    //-----------------------
    //UPDATE UI
    //-----------------------
    private void disposeWhenDestroy() {
        if(this.disposable != null && !this.disposable.isDisposed()) {
            this.disposable.dispose();
        }
    }

    private void updateUiWhenStartingRequest() {
        bar.setVisibility(View.VISIBLE);
    }

    /*private void updateUiWithRestaurants(List<RestaurantsResults> results) {
        System.out.println("enter update ui");
        swipeRefreshLayout.setRefreshing(false);
        restaurantsResultsList.clear();
        restaurantsResultsList.addAll(results);
        //adapter.notifyDataSetChanged();
        System.out.println("exit update ui");
        *//*restaurantList = createRestaurantsList(this.restaurantsResultsList);
        configureRecyclerView();*//*
    }*/

    private void updateList(List<RestaurantInformations> results) {

        swipeRefreshLayout.setRefreshing(false);
        restaurantInformationsList.clear();
        restaurantInformationsList.addAll(results);
        bar.setVisibility(View.GONE);
        adapter.notifyDataSetChanged();
    }

    /*private List<Restaurant> createRestaurantsList(List<RestaurantsResults> results) {
        System.out.println("enter create resto list");
        List<Restaurant> list = new ArrayList<>();
        for (int i = 0; i < results.size(); i++) {
            name = results.get(i).getName();
            adress = results.get(i).getVicinity();
            pictureUrl = results.get(i).getPhotos().get(0).getPhotoReference();
            //Restaurant restaurant = executeRequestForRestaurantInformations(results.get(i).getPlaceId(), name, adress, pictureUrl);
           // list.add(restaurant);
            System.out.println("list size = " + list.size());
            *//*System.out.println("rest name = " + restaurant.getName());
            System.out.println("resto adress = " + restaurant.getAdress());
            System.out.println("rest horaires = " + restaurant.getOpeningHours());*//*
        }

        System.out.println("exit create resto list");
        System.out.println("list size = " + list.size());
        return list;
    }*/


    /*private void createAndAddRestaurantToList(String name, String adress, String closingHours, String distance, String pictureUrl) {
        Restaurant restaurant = new Restaurant(name, adress, closingHours, distance, pictureUrl);
        //restaurantList.add(restaurant);
    }


    private void updateUiWithDouble(RestaurantInformations restaurantInformations) {
        System.out.println("infos = " + restaurantInformations.getName() + " = " + restaurantInformations.getWebsite());
    }*/
}
