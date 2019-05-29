package com.example.blanche.go4lunch.models;

import java.util.List;
import java.util.Map;

public class RestaurantPlace {

    private float like;
    private String uid;
    private List<Map<String, Integer> > usersWhoLiked2;


    public RestaurantPlace() {

    }

    public RestaurantPlace(String uid) {
        this.uid = uid;
    }


    public float getLike() {
        return like;
    }

    public String getUid() {
        return uid;
    }

    public List<Map<String, Integer>> getUsersWhoLiked2() {
        return usersWhoLiked2;
    }

}
