package com.example.blanche.go4lunch.fragments;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.google.android.material.navigation.NavigationView;

import androidx.drawerlayout.widget.DrawerLayout;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import android.widget.ProgressBar;


import com.bumptech.glide.Glide;
import com.example.blanche.go4lunch.BuildConfig;
import com.example.blanche.go4lunch.callbacks.MyCallback;
import com.example.blanche.go4lunch.R;
import com.example.blanche.go4lunch.activities.RestaurantDetailsActivity;
import com.example.blanche.go4lunch.adapters.RecyclerViewAdapter;
import com.example.blanche.go4lunch.api.RestaurantPlaceHelper;
import com.example.blanche.go4lunch.models.RestaurantInformations;
import com.example.blanche.go4lunch.utils.ItemClickSupport;
import com.example.blanche.go4lunch.utils.RestaurantStreams;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableObserver;

import static com.example.blanche.go4lunch.utils.Utils.disposeWhenDestroy;


/**
 * SecondPageFragment displays the list of restaurants
 */
public class SecondPageFragment extends BaseFragment {

    private static final String KEY_POSITION = "position";
    private static final String KEY_ACTIVITY = "keyActivity";
    private static final String RESTAURANT_ID = "idRestaurant";
    private static final String APP_PREFERENCES = "appPreferences";
    private static final String KEY_SEARCH = "keySearch";
    private static final String LATITUDE_AND_LONGITUDE = "latitudeAndLongitude";
    private List<RestaurantInformations> restaurantInformationsListForSearch;
    private String coordinates;
    private SharedPreferences preferences;
    private Disposable disposable;
    private List<RestaurantInformations> restaurantInformationsList;
    private RecyclerViewAdapter adapter;
    private List<String> namesList;
    private List<String> idList;
    private FirebaseFirestore firestoreRootRef;
    private CollectionReference itemsRef;
    private String apikey;

    //-----------------------
    //BIND VIEWS
    //------------------------
    @BindView(R.id.bar)
    ProgressBar bar;
    @BindView(R.id.fragment_second_page_recycler_view)
    RecyclerView recyclerView;
    @BindView(R.id.fragment_second_page_swipe_container)
    SwipeRefreshLayout swipeRefreshLayout;
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


    //-------------------
    //CONSTRUCTOR
    //----------------------
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

    //---------------------
    //
    //---------------------
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View result = inflater.inflate(R.layout.fragment_second_page, container, false);
        ButterKnife.bind(this, result);
        apikey = BuildConfig.ApiKey;
        preferences = this.getActivity().getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
        coordinates = formatLocation(getUserLocation(getContext(), this, getActivity()));
        preferences.edit().putString(LATITUDE_AND_LONGITUDE, coordinates).apply();

        setHasOptionsMenu(true);
        configureToolbar(result);

        configureNavigationView(navigationView, getActivity(), drawerLayout, getContext(), preferences);
        configureDrawerLayout(drawerLayout, getActivity());

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
                        //put in bundle information about restaurant
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
        setClearTextButtonBehavior(autoCompleteTextView, cardView);
    }

    //---------------------
    //REQUEST
    //-------------------------
    /**
     * Request to find restaurants available close to the user
     * @param latlng position of the user
     */
    private void request(String latlng) {
        updateUiWhenStartingRequest();
        disposable =
                RestaurantStreams.streamFetchPlaceInfo(latlng, 1500, "restaurant", apikey)
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
                                    }, itemsRef, idList);
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
    /**
     * update recycler view with search results
     */
    private void configureRecyclerViewForSearch() {
        this.adapter = new RecyclerViewAdapter(this.restaurantInformationsListForSearch, Glide.with(this));
        this.recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        preferences.edit().putString(KEY_SEARCH, "search").apply();
    }

    /**
     * show progress bar
     */
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

    //-------------------------------------
    //LAUNCH ACTIVITIES
    //--------------------------------------
    private void launchRestaurantDetailsActivity(Bundle bundle) {
        Intent restaurantDetailActivity = new Intent(getContext(), RestaurantDetailsActivity.class);
        restaurantDetailActivity.putExtras(bundle);
        startActivity(restaurantDetailActivity);
    }
}
