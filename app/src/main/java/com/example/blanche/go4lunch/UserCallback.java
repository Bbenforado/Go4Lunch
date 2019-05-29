package com.example.blanche.go4lunch;

import com.example.blanche.go4lunch.models.User;

import java.util.List;

public interface UserCallback {

    void onCallback(List<User> list);
}
