package com.example.blanche.go4lunch.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class RestaurantGeometry {

    @SerializedName("location")
    @Expose
    private RestaurantLocation location;
    @SerializedName("viewport")
    @Expose
    private RestaurantViewPort viewport;

    public RestaurantLocation getLocation() {
        return location;
    }

    public void setLocation(RestaurantLocation location) {
        this.location = location;
    }

    public RestaurantViewPort getViewport() {
        return viewport;
    }

    public void setViewport(RestaurantViewPort viewport) {
        this.viewport = viewport;
    }
}
