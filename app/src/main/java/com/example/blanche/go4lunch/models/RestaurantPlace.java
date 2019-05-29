package com.example.blanche.go4lunch.models;

import android.widget.LinearLayout;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RestaurantPlace {

    int maxLike;
    float like;
    String uid;
    List<String> usersWhoLiked;
    List<Map<String, Integer> > usersWhoLiked2;
    //Map<List<String>, List<Integer>> usersWhoLiked2;


    public RestaurantPlace() {

    }

    public RestaurantPlace(String uid) {
        this.uid = uid;
    }

    public RestaurantPlace(String uid, int like) {
        this.uid = uid;
        this.like = like;
    }

    public float getLike() {
        return like;
    }

    public String getUid() {
        return uid;
    }


    public List<String> getUsersWhoLiked() {
        return usersWhoLiked;
    }

    public List<Map<String, Integer>> getUsersWhoLiked2() {
        return usersWhoLiked2;
    }

    /*public Map<List<String>, List<Integer>> getUsersWhoLiked2() {
        return usersWhoLiked2;
    }*/

    public int getMaxLike() {
        return maxLike;
    }
}
