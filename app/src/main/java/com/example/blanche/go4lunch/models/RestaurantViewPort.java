package com.example.blanche.go4lunch.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class RestaurantViewPort {

    @SerializedName("northeast")
    @Expose
    private RestaurantNorthEast northeast;
    @SerializedName("southwest")
    @Expose
    private RestaurantSouthWest southwest;

    public RestaurantNorthEast getNortheast() {
        return northeast;
    }

    public void setNortheast(RestaurantNorthEast northeast) {
        this.northeast = northeast;
    }

    public RestaurantSouthWest getSouthwest() {
        return southwest;
    }

    public void setSouthwest(RestaurantSouthWest southwest) {
        this.southwest = southwest;
    }
}
