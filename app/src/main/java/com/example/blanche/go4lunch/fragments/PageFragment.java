package com.example.blanche.go4lunch.fragments;


import android.Manifest;
import android.support.v7.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
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
import android.support.v7.widget.Toolbar;
import com.example.blanche.go4lunch.MyCallback;
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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.Gson;

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
 * A simple {@link Fragment} subclass.
 */
public class PageFragment extends BaseFragment implements GoogleMap.OnMarkerClickListener, OnMapReadyCallback, LocationListener {

    private GoogleMap map;
    @BindView(R.id.bar)
    ProgressBar bar;
    SharedPreferences preferences;
    Disposable disposable;
    private List<RestaurantsResults> restaurantsResultsList;
    public static final int REQUEST_ID_ACCESS_COURSE_FINE_LOCATION = 100;
    public static final String LATITUDE_AND_LONGITUDE = "latitudeAndLongitude";
    public static final String APP_PREFERENCES = "appPreferences";
    public static final String RESTAURANT_ID = "idRestaurant";
    public static final String KEY_ACTIVITY = "keyActivity";
    public static final String RESTAURANT_NAMES_LIST_MAP_FRAG = "namesMapFrag";
    public static final String CURRENT_USER_NAME = "currentUserName";
    public static final String CURRENT_USER_MAIL_ADRESS = "currentUserMailAdress";
    public static final String KEY_FOR_SEARCH = "keyForSearch";
    private DatabaseReference database;
    private ValueEventListener listener;
    private Marker marker;
    private List<String> namesList;
    @BindView(R.id.nav_view) NavigationView navigationView;
    @BindView(R.id.drawer_layout) DrawerLayout drawerLayout;
    @BindView(R.id.autocomplete_textview)
    AutoCompleteTextView autoCompleteTextView;
    @BindView(R.id.idCardView)
    CardView cardView;
    @BindView(R.id.clear_text_button)
    ImageButton clearTextButton;
    List<String> idList;
    FirebaseFirestore firestoreRootRef;
    CollectionReference itemsRef;
    List<RestaurantsResults> restaurantsResultsListForSearch;

    public PageFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        System.out.println("on create map");
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_page, container, false);
        ButterKnife.bind(this, rootView);
        setHasOptionsMenu(true);
        Toolbar toolbar = rootView.findViewById(R.id.toolbar);
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        activity.setSupportActionBar(toolbar);
        ActionBar actionBar = activity.getSupportActionBar();
        actionBar.setTitle(R.string.toolbar_title_for_first_and_second_fragment);

        preferences = this.getActivity().getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
        preferences.edit().putString(LATITUDE_AND_LONGITUDE, null).apply();

        configureNavigationView(navigationView, getActivity(), drawerLayout, getContext(), preferences, KEY_ACTIVITY);
        configureDrawerLayout(drawerLayout, toolbar, getActivity());

        database = FirebaseDatabase.getInstance().getReference();


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



    private void updateUiWhileUserIsTyping(CharSequence charSequence) {
        restaurantsResultsListForSearch = new ArrayList<>();
        for (int i = 0; i < restaurantsResultsList.size(); i++) {
            if (restaurantsResultsList.get(i).getName().toLowerCase().contains(charSequence)) {
                restaurantsResultsListForSearch.add(restaurantsResultsList.get(i));
            }
        }
        configureMapForSearch();
    }

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

    @Override
    public void onResume() {
        System.out.println("on resume map");
        super.onResume();
        updateMarkersForAListOfRestaurant(restaurantsResultsList);
        /*if (autoCompleteTextView.getVisibility() == View.VISIBLE) {
            autoCompleteTextView.setVisibility(View.GONE);*/
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
        System.out.println("on map ready");
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
            Toast.makeText(getContext(), "Show my location error: " + e.getMessage(),
                    Toast.LENGTH_LONG).show();
            e.printStackTrace();
            return;
        }

        map.setOnMarkerClickListener(this);

    }

    public void backPressed() {
        Toast.makeText(getContext(), "first", Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        System.out.println("on marker clicked");
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
                    Toast.makeText(getContext(), "Permission granted! :)", Toast.LENGTH_SHORT).show();
                    showMyLocation();
                } else {
                    Toast.makeText(getContext(), "Permission denied! :(", Toast.LENGTH_SHORT).show();
                }
                break;
            }
        }
    }

    private String getEnabledLocationProvider() {
        LocationManager locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        String bestProvider = locationManager.getBestProvider(criteria, true);
        boolean enabled = locationManager.isProviderEnabled(bestProvider);
        if (!enabled) {
            Toast.makeText(getContext(), "No location provider enabled!", Toast.LENGTH_SHORT).show();
            return null;
        }
        return bestProvider;
    }

    //------------------
    //SHOW LOCATION
    //----------------------------
    private void showMyLocation() {
        System.out.println("show my location");
        LocationManager locationManager = (LocationManager)getActivity().getSystemService(Context.LOCATION_SERVICE);
        String locationProvider = getEnabledLocationProvider();
        if (locationProvider == null) {
            return;
        }
        final long MIN_TIME_BW_UPDATES = 1000;
        final float MIN_DISTANCE_CHANGE_FOR_UPDATES = 1;
        Location myLocation = null;
        try {
            locationManager.requestLocationUpdates(locationProvider, MIN_TIME_BW_UPDATES,
                    MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
            myLocation = locationManager.getLastKnownLocation(locationProvider);
        }
        catch (SecurityException e) {
            Toast.makeText(getContext(), "Show my location error: " + e.getMessage(),
                    Toast.LENGTH_LONG).show();
            e.printStackTrace();
            return;
        }

        if (myLocation != null) {
            LatLng latLng = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());

            String latLong = myLocation.getLatitude() + "," + myLocation.getLongitude();
            System.out.println("position = " + latLong);

            executeHttpRequestForRestaurant(latLong);

            preferences.edit().putString(LATITUDE_AND_LONGITUDE, latLong).apply();

            map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 13));

            /*CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(latLng)
                    .zoom(15)
                    .bearing(90)
                    .tilt(40)
                    .build();
            map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));*/
            float zoomLevel = 16.0f;
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoomLevel));

            Marker marker;
            marker = map.addMarker(new MarkerOptions()
                    .position(latLng)
                    .title("You are here"));
            marker.setTag(-1);
            marker.showInfoWindow();

            /*MarkerOptions option = new MarkerOptions();
            option.title("You are here");
            option.position(latLng);
            Marker currentMarker = map.addMarker(option);
            currentMarker.showInfoWindow();*/

        } else {
            Toast.makeText(getContext(), "Location not found!:(", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onLocationChanged(Location location) {
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }


    //------------------
    //HTTP REQUEST
    //----------------------
    public void executeHttpRequestForRestaurant(String latlng) {
        bar.setVisibility(View.VISIBLE);
        disposable =
                RestaurantStreams.streamFetchRestaurants(latlng, 1500, "restaurant", "AIzaSyA6Jk5Xl1MbXbYcfWywZ0vwUY2Ux4KLta4")
                        .subscribeWith(new DisposableObserver<RestaurantObject>() {

                            @Override
                            public void onNext(RestaurantObject restaurantObject) {
                                Log.e("TAG", "on next");
                                //updateUI
                                updateUiWithRestaurants(restaurantObject.getResults());
                                //namesList = new ArrayList<>();
                                for (int i = 0; i < restaurantObject.getResults().size(); i++) {
                                    //get restaurants, check uid if uid is not in database, create restaurants
                                    String id = restaurantObject.getResults().get(i).getPlaceId();


                                    //namesList.add(restaurantObject.getResults().get(i).getName());


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
                                Log.e("TAG", "on error");
                            }

                            @Override
                            public void onComplete() {
                                Log.e("TAG", "on complete");
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

    //------------------------
    //UPDATE UI
    //------------------------
    private void updateUiWithRestaurants(List<RestaurantsResults> results) {
        restaurantsResultsList.clear();
        restaurantsResultsList.addAll(results);
        updateMarkersForAListOfRestaurant(restaurantsResultsList);
        bar.setVisibility(View.GONE);
    }


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
