package com.example.blanche.go4lunch.models;

import afu.org.checkerframework.checker.nullness.qual.Nullable;

public class User {

    private String uid;
    private String username;
    private boolean hasChosenRestaurant;
    @Nullable
    private String urlPicture;

    public User() {
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
