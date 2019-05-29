package com.example.blanche.go4lunch.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class RestaurantInformations {

    @SerializedName("place_id")
    @Expose
    private String placeId;
    @SerializedName("formatted_phone_number")
    @Expose
    private String formattedPhoneNumber;
    @SerializedName("name")
    @Expose
    private String name;
    @SerializedName("opening_hours")
    @Expose
    private OpeningHours openingHours;
    @SerializedName("photos")
    @Expose
    private List<Photo> photos = null;
    @SerializedName("rating")
    @Expose
    private Double rating;
    @SerializedName("website")
    @Expose
    private String website;
    @SerializedName("vicinity")
    @Expose
    private String vicinity;
    @SerializedName("geometry")
    @Expose
    private Geometry geometry;

    public String getPlaceId() {
        return placeId;
    }

    public String getFormattedPhoneNumber() {
        return formattedPhoneNumber;
    }
    public String getVicinity() {
        return vicinity;
    }

    public Geometry getGeometry() {
        return geometry;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public OpeningHours getOpeningHours() {
        return openingHours;
    }

    public List<Photo> getPhotos() {
        return photos;
    }
    public String getWebsite() {
        return website;
    }
}
