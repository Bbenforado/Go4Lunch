package com.example.blanche.go4lunch.utils;

import android.content.Context;
import android.support.annotation.Nullable;

import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Utils {



    @Nullable
    public static FirebaseUser getCurrentUser(){ return FirebaseAuth.getInstance().getCurrentUser(); }

    public static Boolean isCurrentUserLogged(){ return (getCurrentUser() != null); }


}
