package com.example.blanche.go4lunch.fragments;


import android.Manifest;
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
import android.renderscript.Double4;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.blanche.go4lunch.MyCallback;
import com.example.blanche.go4lunch.R;
import com.example.blanche.go4lunch.activities.RestaurantDetailsActivity;
import com.example.blanche.go4lunch.api.RestaurantPlaceHelper;
import com.example.blanche.go4lunch.api.UserHelper;
import com.example.blanche.go4lunch.models.RestaurantObject;
import com.example.blanche.go4lunch.models.RestaurantsResults;
import com.example.blanche.go4lunch.utils.RestaurantStreams;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.TypeFilter;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.annotation.Nullable;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableObserver;

import static com.example.blanche.go4lunch.utils.Utils.disposeWhenDestroy;


/**
 * A simple {@link Fragment} subclass.
 */
public class PageFragment extends Fragment implements GoogleMap.OnMarkerClickListener, OnMapReadyCallback, LocationListener {

    private GoogleMap map;
    @BindView(R.id.bar)
    ProgressBar bar;
    SharedPreferences preferences;
    Disposable disposable;
    List<String> restaurantsNames;
    private List<RestaurantsResults> restaurantsResultsList;

    public static final int REQUEST_ID_ACCESS_COURSE_FINE_LOCATION = 100;
    public static final String LATITUDE_AND_LONGITUDE = "latitudeAndLongitude";
    public static final String APP_PREFERENCES = "appPreferences";
    public static final String RESTAURANT_NAME = "name";
    public static final String TYPE_OF_FOOD_AND_ADRESS = "typeAndAdress";
    public static final String RESTAURANT_ID = "idRestaurant";
    public static final String RESTAURANT_PHONE_NUMBER = "number";
    public static final String RESTAURANT_WEBSITE = "website";
    public static final String RESTAURANT_PHOTO = "photo";
    public static final String KEY_ACTIVITY = "keyActivity";
    public static final String KEY = "key";
    public static final String CURRENT_USER_NAME = "currentUserName";
    public static final String CURRENT_USER_MAIL_ADRESS = "currentUserMailAdress";
    private DatabaseReference database;
    private ValueEventListener listener;
    private Marker marker;
    private String latLong;
    private boolean isAlreadySavedInDatabase;

    List<String> idList;
    FirebaseFirestore firestoreRootRef;
    CollectionReference itemsRef;

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
        database = FirebaseDatabase.getInstance().getReference();


        restaurantsResultsList = new ArrayList<>();
        preferences = this.getActivity().getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
        preferences.edit().putString(LATITUDE_AND_LONGITUDE, null).apply();
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        return rootView;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        System.out.println("on destroy map");
    }

    @Override
    public void onStop() {
        super.onStop();
        System.out.println("on stop map");
    }

    @Override
    public void onStart() {
        super.onStart();
        System.out.println("on start map");
    }

    @Override
    public void onResume() {
        System.out.println("on resume map");
        super.onResume();
        updateMarkersForAListOfRestaurant(restaurantsResultsList);
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

    @Override
    public boolean onMarkerClick(Marker marker) {
        System.out.println("on marker clicked");
        int tag = (Integer) marker.getTag();
        //if this is the user marker
        if (tag == -1) {
            marker.setTitle(preferences.getString(CURRENT_USER_NAME, null));
            marker.setSnippet(preferences.getString(CURRENT_USER_MAIL_ADRESS, null));
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

            latLong = myLocation.getLatitude() + "," + myLocation.getLongitude();
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



    //------------------------
    //UPDATE UI
    //------------------------
    private void updateUiWithRestaurants(List<RestaurantsResults> results) {
        System.out.println("    and here");
        restaurantsResultsList.clear();
        restaurantsResultsList.addAll(results);

        //restaurantsNames = new ArrayList<>();
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

    /*public void updateMap(LatLng latLng) {
        System.out.println("coming here??");
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 13));
        float zoomLevel = 16.0f;
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoomLevel));

        Marker marker;
        marker = map.addMarker(new MarkerOptions()
                .position(latLng)
                .title("You are here"));
        marker.setTag(-1);
        marker.showInfoWindow();
    }*/





}
