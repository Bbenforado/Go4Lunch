package com.example.blanche.go4lunch.models;

import android.support.annotation.Nullable;

public class User {

    private String uid;
    private String username;
    private boolean hasChosenRestaurant;
    private String chosenRestaurant;
    private String chosenRestaurantAdress;
    private String chosenRestaurantWebsite;
    private String chosenRestaurantPhoneNumber;
    private String chosenRestaurantPhotoId;
    private String restaurantId;
    //private boolean isNotificationEnabled;
    private boolean hasEnableNotifications;

    public String getChosenRestaurantPhotoId() {
        return chosenRestaurantPhotoId;
    }

    @Nullable
    private String urlPicture;

    public User() {
    }



    public User(String uid, String username, String urlPicture, boolean hasChosenRestaurant, String chosenRestaurant, String chosenRestaurantAdress, String chosenRestaurantPhoneNumber, String chosenRestaurantWebsite) {
        this.uid = uid;
        this.username = username;
        this.urlPicture = urlPicture;
        this.hasChosenRestaurant = hasChosenRestaurant;
        this.chosenRestaurant = chosenRestaurant;
        this.chosenRestaurantAdress = chosenRestaurantAdress;
        this.chosenRestaurantPhoneNumber = chosenRestaurantPhoneNumber;
        this.chosenRestaurantWebsite = chosenRestaurantWebsite;
    }

    public User(String uid, String username, String urlPicture, boolean hasChosenRestaurant, String chosenRestaurant) {
        this.uid = uid;
        this.username = username;
        this.urlPicture = urlPicture;
        this.hasChosenRestaurant = hasChosenRestaurant;
        this.chosenRestaurant = chosenRestaurant;
    }

    public User(String uid, String username, String urlPicture, boolean isNotificationEnabled) {
        this.uid = uid;
        this.username = username;
        this.urlPicture = urlPicture;
        this.hasEnableNotifications = isNotificationEnabled;
    }

    public User(String uid, String username) {
        this.uid = uid;
        this.username = username;
    }

    // --- GETTERS ---

    public String getRestaurantId() {
        return restaurantId;
    }

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

    public String getChosenRestaurantAdress() {
        return chosenRestaurantAdress;
    }

    public String getChosenRestaurantWebsite() {
        return chosenRestaurantWebsite;
    }

    public String getChosenRestaurantPhoneNumber() {
        return chosenRestaurantPhoneNumber;
    }

    public boolean isHasEnableNotifications() {
        return hasEnableNotifications;
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
