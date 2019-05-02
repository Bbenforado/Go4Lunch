package com.example.blanche.go4lunch.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class RestaurantOpeningHours {

    @SerializedName("open_now")
    @Expose
    private Boolean openNow;

    public Boolean getOpenNow() {
        return openNow;
    }

    public void setOpenNow(Boolean openNow) {
        this.openNow = openNow;
    }
}
