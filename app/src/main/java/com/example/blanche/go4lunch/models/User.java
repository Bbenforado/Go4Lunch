package com.example.blanche.go4lunch.models;

import android.support.annotation.Nullable;

public class User {

    private String uid;
    private String username;
    private boolean hasChosenRestaurant;
    private String chosenRestaurant;
    @Nullable
    private String urlPicture;

    public User() {
    }

    public User(String uid, String username, String urlPicture, boolean hasChosenRestaurant, String chosenRestaurant) {
        this.uid = uid;
        this.username = username;
        this.urlPicture = urlPicture;
        this.hasChosenRestaurant = hasChosenRestaurant;
        this.chosenRestaurant = chosenRestaurant;
    }

    public User(String uid, String username, String urlPicture) {
        this.uid = uid;
        this.username = username;
        this.urlPicture = urlPicture;
    }

    public User(String uid, String username) {
        this.uid = uid;
        this.username = username;
    }

    // --- GETTERS ---
    public String getUid() {
        return uid;
    }

    public String getUsername() {
        return username;
    }

    public String getUrlPicture() {
        return urlPicture;
    }

    public String getChosenRestaurant() {
        return chosenRestaurant;
    }

    public boolean isHasChosenRestaurant() {
        return hasChosenRestaurant;
    }

    // --- SETTERS ---
    public void setUsername(String username) {
        this.username = username;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public void setUrlPicture(String urlPicture) {
        this.urlPicture = urlPicture;
    }
}
