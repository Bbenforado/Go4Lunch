package com.example.blanche.go4lunch.utils;

import com.example.blanche.go4lunch.models.RestaurantInformationObject;
import com.example.blanche.go4lunch.models.RestaurantInformations;
import com.example.blanche.go4lunch.models.RestaurantObject;

import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;


public class RestaurantStreams {

    private static PlacesService placesService = PlacesService.retrofit.create(PlacesService.class);


    //get places around the user
    public static Observable<RestaurantObject> streamFetchRestaurants(String latitudeLongitude, int radius, String type, String apiKey) {
        return placesService.getRestaurants(latitudeLongitude,radius,type, apiKey)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .timeout(10, TimeUnit.SECONDS);
    }

    //get information about place
    public static Observable<RestaurantInformationObject> streamFetchRestaurantInfos(String id, String apikey) {
        return placesService.getRestaurantInfo(id, apikey)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .timeout(10, TimeUnit.SECONDS);
    }

    //get places around and information about each place found
    public static  Observable<List<RestaurantInformations>> streamFetchPlaceInfo(String latlng, int radius, String type, String key){
        return placesService.getRestaurants(latlng, radius, type, key)
                .flatMapIterable(RestaurantObject::getResults)
                .flatMap(result -> placesService.getRestaurantInfo(result.getPlaceId(), key))
                .map(RestaurantInformationObject::getResult)
                .toList()
                .toObservable()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .timeout(10, TimeUnit.SECONDS);
    }

}
