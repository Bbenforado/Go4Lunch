package com.example.blanche.go4lunch.utils;

import com.example.blanche.go4lunch.models.RestaurantObject;

import io.reactivex.Observable;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface PlacesService {

    Retrofit retrofit = new Retrofit.Builder()
            .baseUrl("https://maps.googleapis.com")
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build();

    @GET("/maps/api/place/nearbysearch/json?")
    Observable <RestaurantObject> getRestaurants (@Query("location")String latitudeLongitude,
                                                  @Query("radius") int radius,
                                                  @Query("type") String type,
                                                  @Query("key") String apiKey);
}
