package com.example.blanche.go4lunch.fragments;


import android.support.v4.view.GravityCompat;
import android.support.v7.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;


import com.bumptech.glide.Glide;
import com.example.blanche.go4lunch.MyCallback;
import com.example.blanche.go4lunch.R;
import com.example.blanche.go4lunch.activities.RestaurantDetailsActivity;
import com.example.blanche.go4lunch.adapters.RecyclerViewAdapter;
import com.example.blanche.go4lunch.api.RestaurantPlaceHelper;
import com.example.blanche.go4lunch.models.OpeningHours;
import com.example.blanche.go4lunch.models.Restaurant;
import com.example.blanche.go4lunch.models.RestaurantInformationObject;
import com.example.blanche.go4lunch.models.RestaurantInformations;
import com.example.blanche.go4lunch.models.RestaurantObject;
import com.example.blanche.go4lunch.models.RestaurantsResults;
import com.example.blanche.go4lunch.utils.ItemClickSupport;
import com.example.blanche.go4lunch.utils.RestaurantStreams;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.sql.SQLOutput;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import javax.annotation.Nullable;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableObserver;

import static com.example.blanche.go4lunch.utils.Utils.disposeWhenDestroy;


/**
 * A simple {@link Fragment} subclass.
 */
public class SecondPageFragment extends BaseFragment {

    public static final String KEY_POSITION = "position";
    public static final String KEY_ACTIVITY = "keyActivity";
    public static final String RESTAURANT_ID = "idRestaurant";
    public static final String LATITUDE_AND_LONGITUDE = "latitudeAndLongitude";
    public static final String APP_PREFERENCES = "appPreferences";
    private static final String KEY_SEARCH = "keySearch";
    List<RestaurantInformations> restaurantInformationsListForSearch;
    private String coordinates;
    SharedPreferences preferences;
    private Disposable disposable;
    private List<RestaurantsResults> restaurantsResultsList;
    private List<Restaurant> restaurantList;
    private List<RestaurantInformations> restaurantInformationsList;
    private RecyclerViewAdapter adapter;
    List<String> namesList;
    List<String> idList;
    FirebaseFirestore firestoreRootRef;
    CollectionReference itemsRef;
    @BindView(R.id.bar)
    ProgressBar bar;
    @BindView(R.id.fragment_second_page_recycler_view)
    RecyclerView recyclerView;
    @BindView(R.id.fragment_second_page_swipe_container)
    SwipeRefreshLayout swipeRefreshLayout;
    private Toolbar toolbar;
    private ActionBar actionBar;
    @BindView(R.id.nav_view)
    NavigationView navigationView;
    @BindView(R.id.drawer_layout)
    DrawerLayout drawerLayout;
    @BindView(R.id.autocomplete_textview)
    AutoCompleteTextView autoCompleteTextView;
    @BindView(R.id.clear_text_button)
    ImageButton clearTextButton;
    @BindView(R.id.idCardView)
    CardView cardView;


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

        setHasOptionsMenu(true);
        toolbar = result.findViewById(R.id.toolbar);
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        activity.setSupportActionBar(toolbar);
        actionBar = activity.getSupportActionBar();
        actionBar.setTitle(R.string.toolbar_title_for_first_and_second_fragment);

        System.out.println("on create key = " + preferences.getString(KEY_SEARCH, null));

        configureNavigationView(navigationView, getActivity(), drawerLayout, getContext(), preferences, KEY_ACTIVITY);
        configureDrawerLayout(drawerLayout, toolbar, getActivity());

        configureRecyclerView();
        request(coordinates);
        configureOnClickRecyclerView();
        configureSwipeRefreshLayout();

        return result;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        disposeWhenDestroy(disposable);
        System.out.println("on destroy fragment");
    }

    @Override
    public void onResume() {
        super.onResume();
        if (cardView.getVisibility() == View.VISIBLE) {
            cardView.setVisibility(View.GONE);
            autoCompleteTextView.getText().clear();
            clearTextButton.setVisibility(View.GONE);
        }
        preferences.edit().putString(KEY_SEARCH, null).apply();

        this.adapter = new RecyclerViewAdapter(this.restaurantInformationsList, Glide.with(this));
        this.recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_toolbar, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.search_item:
                //autoCompleteTextView.setVisibility(View.VISIBLE);
                cardView.setVisibility(View.VISIBLE);
                autoCompleteTextView.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                    }

                    @Override
                    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                        if (autoCompleteTextView.getText() != null) {
                            clearTextButton.setVisibility(View.VISIBLE);
                        } else {
                            clearTextButton.setVisibility(View.GONE);
                        }
                        updateUiWhileUserIsTyping(charSequence);
                    }

                    @Override
                    public void afterTextChanged(Editable editable) {

                    }
                });

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    //--------------------
    //CONFIGURATION
    //-------------------
    private void configureRecyclerView() {
        this.restaurantInformationsList = new ArrayList<>();
        this.restaurantList = new ArrayList<>();
        this.adapter = new RecyclerViewAdapter(this.restaurantInformationsList, Glide.with(this));
        this.recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
    }

    private void configureSwipeRefreshLayout() {
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                //refresh the page, request the api
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
                        if (preferences.getString(KEY_SEARCH, null) != null) {

                            if (preferences.getString(KEY_SEARCH, null).equals("search")) {
                                Bundle bundle = new Bundle();
                                bundle.putString(RESTAURANT_ID, restaurantInformationsListForSearch.get(position).getPlaceId());
                                launchRestaurantDetailsActivity(bundle);
                                preferences.edit().putString(KEY_SEARCH, null).apply();
                            }
                        } else {
                            preferences.edit().putInt(KEY_ACTIVITY, 1).apply();
                            Bundle bundle = new Bundle();
                            bundle.putString(RESTAURANT_ID, restaurantInformationsList.get(position).getPlaceId());
                            launchRestaurantDetailsActivity(bundle);
                        }
                    }
                });
    }

    //-------------------------
    //ACTIONS
    //--------------------------
    @OnClick(R.id.clear_text_button)
    public void clearText() {
        autoCompleteTextView.getText().clear();
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
                                namesList = new ArrayList<>();
                                for (int i = 0; i < restaurantInformationsList.size(); i++) {
                                    namesList.add(restaurantInformationsList.get(i).getName());
                                    //get restaurants, check uid if uid is not in database, create restaurants

                                    String id = restaurantInformationsList.get(i).getPlaceId();

                                    idList = new ArrayList<>();

                                    firestoreRootRef = FirebaseFirestore.getInstance();
                                    itemsRef = firestoreRootRef.collection("restaurantPlaces");

                                    readData(new MyCallback() {
                                        @Override
                                        public void onCallback(List<String> list) {
                                            if (!list.contains(id)) {
                                                RestaurantPlaceHelper.createRestaurantPlace(id);
                                            }
                                        }
                                    });
                                }
                            }

                            @Override
                            public void onError(Throwable e) {

                            }

                            @Override
                            public void onComplete() {

                            }
                        });
    }


    //-----------------------
    //UPDATE UI
    //-----------------------
    private void updateUiWhenStartingRequest() {
        bar.setVisibility(View.VISIBLE);
    }

    private void updateList(List<RestaurantInformations> results) {
        swipeRefreshLayout.setRefreshing(false);
        restaurantInformationsList.clear();
        restaurantInformationsList.addAll(results);
        bar.setVisibility(View.GONE);
        adapter.notifyDataSetChanged();
    }

    private void updateUiWhileUserIsTyping(CharSequence charSequence) {
        restaurantInformationsListForSearch = new ArrayList<>();
        for (int i = 0; i < restaurantInformationsList.size(); i++) {
            if (restaurantInformationsList.get(i).getName().toLowerCase().contains(charSequence)) {
                restaurantInformationsListForSearch.add(restaurantInformationsList.get(i));
            }
        }
        configureRecyclerViewForSearch();
    }

    private void configureRecyclerViewForSearch() {
        this.adapter = new RecyclerViewAdapter(this.restaurantInformationsListForSearch, Glide.with(this));
        this.recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        preferences.edit().putString(KEY_SEARCH, "search").apply();
    }

    //-------------------------------------
    //LAUNCH ACTIVITIES
    //--------------------------------------
    private void launchRestaurantDetailsActivity(Bundle bundle) {
        Intent restaurantDetailActivity = new Intent(getContext(), RestaurantDetailsActivity.class);
        restaurantDetailActivity.putExtras(bundle);
        startActivity(restaurantDetailActivity);
    }

    //-----------------------------------------

    private void readData(MyCallback myCallback) {
        itemsRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (DocumentSnapshot document : task.getResult()) {
                        String id = document.getString("uid");
                        idList.add(id);
                    }
                    myCallback.onCallback(idList);
                } else {
                    Log.d("TAG", "Error");
                }
            }
        });
    }
}
