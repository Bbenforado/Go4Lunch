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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.blanche.go4lunch.R;
import com.example.blanche.go4lunch.activities.RestaurantDetailsActivity;
import com.example.blanche.go4lunch.api.UserHelper;
import com.example.blanche.go4lunch.models.RestaurantObject;
import com.example.blanche.go4lunch.models.RestaurantsResults;
import com.example.blanche.go4lunch.utils.RestaurantStreams;
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
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.TypeFilter;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.firebase.auth.FirebaseAuth;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableObserver;


/**
 * A simple {@link Fragment} subclass.
 */
public class PageFragment extends Fragment implements GoogleMap.OnMarkerClickListener, OnMapReadyCallback, LocationListener {

    private GoogleMap map;
    SharedPreferences preferences;
    Disposable disposable;
    private List<RestaurantsResults> restaurantsResultsList;
    public static final int REQUEST_ID_ACCESS_COURSE_FINE_LOCATION = 100;
    public static final String LATITUDE_AND_LONGITUDE = "latitudeAndLongitude";
    public static final String APP_PREFERENCES = "appPreferences";
    public static final String RESTAURANT_NAME = "name";
    public static final String TYPE_OF_FOOD_AND_ADRESS = "typeAndAdress";
    public static final String KEY_ACTIVITY = "keyActivity";
    public static final String CURRENT_USER_NAME = "currentUserName";
    public static final String CURRENT_USER_MAIL_ADRESS = "currentUserMailAdress";

    public PageFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_page, container, false);
        restaurantsResultsList = new ArrayList<>();
        preferences = this.getActivity().getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
        preferences.edit().putString(LATITUDE_AND_LONGITUDE, null).apply();
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        return rootView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        disposeWhenDestroy();
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
            Toast.makeText(getContext(), "Show my location error: " + e.getMessage(),
                    Toast.LENGTH_LONG).show();
            e.printStackTrace();
            return;
        }

        map.setOnMarkerClickListener(this);

    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        int tag = (Integer) marker.getTag();
        if (tag == -1) {
            marker.setTitle(preferences.getString(CURRENT_USER_NAME, null));
            marker.setSnippet(preferences.getString(CURRENT_USER_MAIL_ADRESS, null));
        } else {
            String restaurantName = restaurantsResultsList.get(tag).getName();
            String restaurantAdress = restaurantsResultsList.get(tag).getVicinity();
            preferences.edit().putString(RESTAURANT_NAME, restaurantName).apply();
            preferences.edit().putString(TYPE_OF_FOOD_AND_ADRESS, restaurantAdress).apply();
            preferences.edit().putInt(KEY_ACTIVITY, 1).apply();
            launchDetailsActivity();
        }
        return false;
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
        LocationManager locationManager = (LocationManager)getActivity().getSystemService(Context.LOCATION_SERVICE);
        String locationProvider = getEnabledLocationProvider();
        System.out.println("location provider = " + locationProvider);
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

            executeHttpRequestForRestaurant(latLong);

            preferences.edit().putString(LATITUDE_AND_LONGITUDE, latLong).apply();

            map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 13));

            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(latLng)
                    .zoom(15)
                    .bearing(90)
                    .tilt(40)
                    .build();
            map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

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
        //executeHttpRequestForRestaurant(location.toString());
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
        disposable =
                RestaurantStreams.streamFetchRestaurants(latlng, 1500, "restaurant", "AIzaSyA6Jk5Xl1MbXbYcfWywZ0vwUY2Ux4KLta4")
                        .subscribeWith(new DisposableObserver<RestaurantObject>() {

                            @Override
                            public void onNext(RestaurantObject restaurantObject) {
                                Log.e("TAG", "on next");
                                //updateUI
                                updateUiWithRestaurants(restaurantObject.getResults());
                                System.out.println("size first fragment = " + restaurantObject.getResults().size());
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

    private void disposeWhenDestroy() {
        if(this.disposable != null && !this.disposable.isDisposed()) {
            this.disposable.dispose();
        }
    }

    //------------------------
    //UPDATE UI
    //------------------------
    private void updateUiWithRestaurants(List<RestaurantsResults> results) {
        restaurantsResultsList.clear();
        restaurantsResultsList.addAll(results);
        for (int i = 0; i < restaurantsResultsList.size(); i++) {
            Double lat = restaurantsResultsList.get(i).getGeometry().getLocation().getLat();
            Double longitude = restaurantsResultsList.get(i).getGeometry().getLocation().getLng();

            LatLng restaurantLocation = new LatLng(lat, longitude);
            Marker marker;
            marker = map.addMarker(new MarkerOptions()
                    .position(restaurantLocation)
                    .title(restaurantsResultsList.get(i).getName())
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_ic)));
            marker.setTag(i);
        }
    }

    private void launchDetailsActivity() {
        Intent yourLunchActivity = new Intent(getContext(), RestaurantDetailsActivity.class);
        startActivity(yourLunchActivity);
    }
}
