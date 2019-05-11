package com.example.blanche.go4lunch.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class RestaurantInformationObject {
    @SerializedName("result")
    @Expose
    private RestaurantInformations result;
    @SerializedName("status")
    @Expose
    private String status;

    public RestaurantInformations getResult() {
        return result;
    }

    public void setResult(RestaurantInformations result) {
        this.result = result;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
