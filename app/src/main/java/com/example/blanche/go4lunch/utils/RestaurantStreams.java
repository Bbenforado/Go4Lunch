package com.example.blanche.go4lunch.utils;

import com.example.blanche.go4lunch.models.RestaurantObject;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;


public class RestaurantStreams {

    private static PlacesService placesService = PlacesService.retrofit.create(PlacesService.class);


    public static Observable<RestaurantObject> streamFetchRestaurants(String latitudeLongitude, int radius, String type, String apiKey) {
        return placesService.getRestaurants(latitudeLongitude,radius,type, apiKey)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .timeout(10, TimeUnit.SECONDS);
    }
}
