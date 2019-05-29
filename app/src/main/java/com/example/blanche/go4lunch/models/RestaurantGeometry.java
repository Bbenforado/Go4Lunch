package com.example.blanche.go4lunch.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class RestaurantGeometry {

    @SerializedName("location")
    @Expose
    private RestaurantLocation location;

    public RestaurantLocation getLocation() {
        return location;
    }
}
