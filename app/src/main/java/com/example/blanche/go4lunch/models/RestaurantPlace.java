package com.example.blanche.go4lunch.models;

import java.util.HashMap;
import java.util.List;

public class RestaurantPlace {

    int like;
    String uid;
    //HashMap<String, String > usersWhoLiked;
    List<String> usersWhoLiked;

    public RestaurantPlace() {

    }

    public RestaurantPlace(String uid) {
        this.uid = uid;
    }

    public RestaurantPlace(String uid, int like) {
        this.uid = uid;
        this.like = like;
    }

    public int getLike() {
        return like;
    }

    public String getUid() {
        return uid;
    }

    /*public HashMap<String, String> getUsersWhoLiked() {
        return usersWhoLiked;
    }*/

    public List<String> getUsersWhoLiked() {
        return usersWhoLiked;
    }
}
