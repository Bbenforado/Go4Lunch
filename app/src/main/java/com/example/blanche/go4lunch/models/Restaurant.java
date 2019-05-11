package com.example.blanche.go4lunch.models;

public class Restaurant {

    private String name;
    private String adress;
    private String openingHours;
    private String distance;
    private String pictureUrl;
    private String website;


    public Restaurant(String name, String adress, String openingHours, String distance, String pictureUrl) {
        this.name = name;
        this.adress = adress;
        this.openingHours = openingHours;
        this.distance = distance;
        this.pictureUrl = pictureUrl;
    }

    public String getAdress() {
        return adress;
    }

    public String getOpeningHours() {
        return openingHours;
    }

    public String getDistance() {
        return distance;
    }

    public String getPictureUrl() {
        return pictureUrl;
    }

    public String getName() {
        return name;
    }

    public String getWebsite() {
        return website;
    }
}
