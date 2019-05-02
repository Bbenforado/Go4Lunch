package com.example.blanche.go4lunch.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class RestaurantObject {

    @SerializedName("results")
    @Expose
    private List<RestaurantsResults> results = null;

    public List<RestaurantsResults> getResults() {
        return results;
    }

    public void setResults(List<RestaurantsResults> results) {
        this.results = results;
    }
}
