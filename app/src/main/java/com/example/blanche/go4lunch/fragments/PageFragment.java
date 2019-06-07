package com.example.blanche.go4lunch.fragments;


import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.NonNull;
import com.google.android.material.navigation.NavigationView;

import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.cardview.widget.CardView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.blanche.go4lunch.BuildConfig;
import com.example.blanche.go4lunch.callbacks.MyCallback;
import com.example.blanche.go4lunch.R;
import com.example.blanche.go4lunch.activities.RestaurantDetailsActivity;
import com.example.blanche.go4lunch.api.RestaurantPlaceHelper;
import com.example.blanche.go4lunch.api.UserHelper;
import com.example.blanche.go4lunch.models.RestaurantObject;
import com.example.blanche.go4lunch.models.RestaurantsResults;
import com.example.blanche.go4lunch.utils.RestaurantStreams;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableObserver;
import static com.example.blanche.go4lunch.utils.Utils.disposeWhenDestroy;


/**
 * PageFragment display the map
 */
public class PageFragment extends BaseFragment implements GoogleMap.OnMarkerClickListener, OnMapReadyCallback {

    private GoogleMap map;
    private SharedPreferences preferences;
    private Disposable disposable;
    private List<RestaurantsResults> restaurantsResultsList;
    private static final int REQUEST_ID_ACCESS_COURSE_FINE_LOCATION = 100;
    private static final String APP_PREFERENCES = "appPreferences";
    private static final String RESTAURANT_ID = "idRestaurant";
    private static final String KEY_ACTIVITY = "keyActivity";
    private static final String CURRENT_USER_NAME = "currentUserName";
    private static final String CURRENT_USER_MAIL_ADRESS = "currentUserMailAdress";
    private static final String KEY_FOR_SEARCH = "keyForSearch";
    private Marker marker;
    private List<String> idList;
    private FirebaseFirestore firestoreRootRef;
    private CollectionReference itemsRef;
    private List<RestaurantsResults> restaurantsResultsListForSearch;
    private String apikey;

    //-----------------
    //BIND VIEWS
    //------------------
    @BindView(R.id.nav_view) NavigationView navigationView;
    @BindView(R.id.drawer_layout) DrawerLayout drawerLayout;
    @BindView(R.id.autocomplete_textview)
    AutoCompleteTextView autoCompleteTextView;
    @BindView(R.id.idCardView)
    CardView cardView;
    @BindView(R.id.clear_text_button)
    ImageButton clearTextButton;
    @BindView(R.id.bar)
    ProgressBar bar;


    //-----------------
    //CONSTRUCTOR
    //--------------------
    public PageFragment() {
        // Required empty public constructor
    }

    //-----------------------
    //
    //-------------------------
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_page, container, false);
        ButterKnife.bind(this, rootView);
        apikey = BuildConfig.ApiKey;
        setHasOptionsMenu(true);
        configureToolbar(rootView);

        preferences = this.getActivity().getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
        preferences.edit().putString(KEY_FOR_SEARCH, null).apply();

        configureNavigationView(navigationView, getActivity(), drawerLayout, getContext(), preferences);
        configureDrawerLayout(drawerLayout, getActivity());

        restaurantsResultsList = new ArrayList<>();

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        return rootView;
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

    @Override
    public void onResume() {
        super.onResume();
        updateMarkersForAListOfRestaurant(restaurantsResultsList);
        if (cardView.getVisibility() == View.VISIBLE) {
            cardView.setVisibility(View.GONE);
            autoCompleteTextView.getText().clear();
            clearTextButton.setVisibility(View.GONE);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        disposeWhenDestroy(disposable);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;

        map.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
            @Override
            public void onMapLoaded() {
                askPermissionsAndShowMyLocation();
            }
        });
        map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        map.getUiSettings().setZoomControlsEnabled(true);

        try {
            map.setMyLocationEnabled(true);
        }
        catch (SecurityException e) {
            e.printStackTrace();
            return;
        }
        map.setOnMarkerClickListener(this);
    }

    /**
     * if it s the marker of a restaurant, it displays the detail activity of this restaurant
     * @param marker marker clicked on the map
     * @return
     */
    @Override
    public boolean onMarkerClick(Marker marker) {
        int tag = (Integer) marker.getTag();
        //if this is the user marker
        if (tag == -1) {
            marker.setTitle(preferences.getString(CURRENT_USER_NAME, null));
            marker.setSnippet(preferences.getString(CURRENT_USER_MAIL_ADRESS, null));
        } else if (preferences.getString(KEY_FOR_SEARCH, null) != null) {
            if (preferences.getString(KEY_FOR_SEARCH, null).equals("search")) {
                String restaurantId = restaurantsResultsListForSearch.get(tag).getPlaceId();
                preferences.edit().putString(RESTAURANT_ID, restaurantId).apply();
                preferences.edit().putInt(KEY_ACTIVITY, 0).apply();
                launchDetailsActivity();
                preferences.edit().putString(KEY_FOR_SEARCH, null).apply();
            } else {
                String restaurantId = restaurantsResultsList.get(tag).getPlaceId();
                preferences.edit().putString(RESTAURANT_ID, restaurantId).apply();
                preferences.edit().putInt(KEY_ACTIVITY, 0).apply();
                launchDetailsActivity();
            }
        } else {
            String restaurantId = restaurantsResultsList.get(tag).getPlaceId();
            preferences.edit().putString(RESTAURANT_ID, restaurantId).apply();
            preferences.edit().putInt(KEY_ACTIVITY, 0).apply();
            launchDetailsActivity();
        }
        return true;
    }

    //--------------------
    //CONFIGURATION
    //----------------------
    /**
     * update the map with markers for restaurants that contains letters entered by user in search
     */
    private void configureMapForSearch() {
        map.clear();
        for (int i = 0; i < restaurantsResultsListForSearch.size(); i++) {
            Double lat = restaurantsResultsListForSearch.get(i).getGeometry().getLocation().getLat();
            Double longitude = restaurantsResultsListForSearch.get(i).getGeometry().getLocation().getLng();
            LatLng restaurantLocation = new LatLng(lat, longitude);

            marker = map.addMarker(new MarkerOptions()
                    .position(restaurantLocation)
                    .title(restaurantsResultsListForSearch.get(i).getName())
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_ic)));
            marker.setTag(i);
            preferences.edit().putString(KEY_FOR_SEARCH, "search").apply();
        }
    }

    //-----------------------
    //PERMISSIONS
    //--------------------------------
    private void askPermissionsAndShowMyLocation() {
        if (Build.VERSION.SDK_INT >= 23) {
            int accessCoarsePermission
                    = ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION);
            int accessFinePermission
                    = ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION);
            if (accessCoarsePermission != PackageManager.PERMISSION_GRANTED
            || accessFinePermission != PackageManager.PERMISSION_GRANTED) {
                String[] permissions = new String[] {
                        Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION};
                requestPermissions(permissions, REQUEST_ID_ACCESS_COURSE_FINE_LOCATION);
                return;
            }
        }
        showMyLocation();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_ID_ACCESS_COURSE_FINE_LOCATION: {
                if (grantResults.length > 1
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED
                &&grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(getContext(), getContext().getString(R.string.permission_granted), Toast.LENGTH_SHORT).show();
                    showMyLocation();
                } else {
                    Toast.makeText(getContext(), getContext().getString(R.string.permission_denied), Toast.LENGTH_SHORT).show();
                }
                break;
            }
        }
    }

    //------------------
    //LOCATION
    //----------------------------
    /**
     * get the user location and display markers (marker for the user and markers for the restaurants available) on the map
     */
    private void showMyLocation() {
        Location location = getUserLocation(getContext(), this, getActivity());
        //formatLocation(getUserLocation(getContext(), this, getActivity()));
        String userLocation = formatLocation(location);

        executeRequestForRestaurant(userLocation);

        Double userLat = getUserLocation(getContext(), this, getActivity()).getLatitude();
        Double userLng = getUserLocation(getContext(), this, getActivity()).getLongitude();

        LatLng latLng = new LatLng(userLat, userLng);
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 13));
        float zoomLevel = 16.0f;
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoomLevel));

        Marker marker;
        marker = map.addMarker(new MarkerOptions()
                .position(latLng)
                .title(getContext().getString(R.string.user_location_marker_title)));
        marker.setTag(-1);
        marker.showInfoWindow();

    }

    //------------------
    //REQUEST
    //----------------------
    /**
     * retrieve restaurants that are close to the users location
     * @param latlng location of the user format (latitude,longitude)
     */
    public void executeRequestForRestaurant(String latlng) {
        bar.setVisibility(View.VISIBLE);
        disposable =
                RestaurantStreams.streamFetchRestaurants(latlng, 1500, "restaurant", apikey)
                        .subscribeWith(new DisposableObserver<RestaurantObject>() {

                            @Override
                            public void onNext(RestaurantObject restaurantObject) {
                                Log.i("TAG", "on next");
                                //updateUI
                                updateUiWithRestaurants(restaurantObject.getResults());
                                for (int i = 0; i < restaurantObject.getResults().size(); i++) {
                                    //get restaurants, check uid if uid is not in database, create restaurants
                                    String id = restaurantObject.getResults().get(i).getPlaceId();
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
                                Log.e("TAG", "on error");
                            }

                            @Override
                            public void onComplete() {
                                Log.i("TAG", "on complete");
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

    //------------------------
    //UPDATE UI
    //------------------------
    /**
     * save restaurants found in a List
     * display markers for this list of restaurants
     * @param results restaurants found by the request (executeRequestForRestaurant)
     */
    private void updateUiWithRestaurants(List<RestaurantsResults> results) {
        restaurantsResultsList.clear();
        restaurantsResultsList.addAll(results);
        updateMarkersForAListOfRestaurant(restaurantsResultsList);
        bar.setVisibility(View.GONE);
    }

    /**
     * update the map with restaurants markers while the user is typing
     * @param charSequence letters typed by user
     */
    private void updateUiWhileUserIsTyping(CharSequence charSequence) {
        restaurantsResultsListForSearch = new ArrayList<>();
        for (int i = 0; i < restaurantsResultsList.size(); i++) {
            if (restaurantsResultsList.get(i).getName().toLowerCase().contains(charSequence)) {
                restaurantsResultsListForSearch.add(restaurantsResultsList.get(i));
            }
        }
        configureMapForSearch();
    }

    /**
     * add a marker on the map for every restaurant in the list
     * if someone has chosen the restaurant, the marker is displayed in another color
     * @param list
     */
    private void updateMarkersForAListOfRestaurant(List<RestaurantsResults> list) {
        for (int i = 0; i < list.size(); i++) {
            Double lat = list.get(i).getGeometry().getLocation().getLat();
            Double longitude = list.get(i).getGeometry().getLocation().getLng();
            LatLng restaurantLocation = new LatLng(lat, longitude);

            marker = map.addMarker(new MarkerOptions()
                    .position(restaurantLocation)
                    .title(list.get(i).getName())
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_ic)));
            marker.setTag(i);

            int finalI = i;
            UserHelper.getUsersCollection()
                    .whereEqualTo("restaurantId", list.get(i).getPlaceId())
                    .addSnapshotListener(new EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                            if (e != null) {
                                Log.w("TAG", "Listen failed", e);
                                return;
                            }
                            List<String> users = new ArrayList<>();
                            for (DocumentSnapshot doc : queryDocumentSnapshots) {
                                if (doc.get("restaurantId") != null) {
                                    users.add(doc.getString("chosenRestaurant"));
                                    marker = map.addMarker(new MarkerOptions()
                                            .position(restaurantLocation)
                                            .title(doc.getString("chosenRestaurant"))
                                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.food_and_restaurant)));
                                    marker.setTag(finalI);
                                }
                            }
                        }
                    });
        }
    }

    //-------------------------
    //METHODS THAT LAUNCH ACTIVITIES
    //---------------------------------
    private void launchDetailsActivity() {
        Intent yourLunchActivity = new Intent(getContext(), RestaurantDetailsActivity.class);
        startActivity(yourLunchActivity);
    }


    //-------------------
    //GET DATA
    //-------------------
    /**
     * get the list of id of restaurants already stored in database to check if the restaurants we got from the request is already stored or not
     * @param myCallback
     */
    /*private void readData(MyCallback myCallback) {
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
                    Log.e("TAG", "Error");
                }
            }
        });
    }*/
}
