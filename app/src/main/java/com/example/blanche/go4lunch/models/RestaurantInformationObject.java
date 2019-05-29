package com.example.blanche.go4lunch.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class RestaurantInformationObject {
    @SerializedName("result")
    @Expose
    private RestaurantInformations result;

    public RestaurantInformations getResult() {
        return result;
    }
}
