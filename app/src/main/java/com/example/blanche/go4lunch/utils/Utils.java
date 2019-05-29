package com.example.blanche.go4lunch.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.blanche.go4lunch.R;
import com.example.blanche.go4lunch.activities.RestaurantDetailsActivity;
import com.example.blanche.go4lunch.activities.SettingActivity;
import com.example.blanche.go4lunch.api.RestaurantPlaceHelper;
import com.example.blanche.go4lunch.api.UserHelper;
import com.example.blanche.go4lunch.models.RestaurantPlace;
import com.example.blanche.go4lunch.models.User;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;

import io.reactivex.disposables.Disposable;

public class Utils {

    public static final int SIGN_OUT_TASK = 10;
    public static final int DELETE_USER_TASK = 20;
    //----------------------
    //GET USERS INFOS
    //----------------------
    @Nullable
    public static FirebaseUser getCurrentUser(){ return FirebaseAuth.getInstance().getCurrentUser(); }

    public static Boolean isCurrentUserLogged(){ return (getCurrentUser() != null); }

    //-----------------------
    //
    //--------------------------
    public static void disposeWhenDestroy(Disposable disposable) {
        if(disposable != null && !disposable.isDisposed()) {
            disposable.dispose();
        }
    }

    //--------------------
    //UPDATE INTERFACE
    //---------------------
    public static void setStars(String id, ImageView starOne, ImageView starTwo, ImageView starThree) {
        starOne.setVisibility(View.GONE);
        starTwo.setVisibility(View.GONE);
        starThree.setVisibility(View.GONE);

        RestaurantPlaceHelper.getRestaurantPlace(id).addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                RestaurantPlace restaurantPlace = documentSnapshot.toObject(RestaurantPlace.class);
                if (restaurantPlace.getLike() != 0) {
                    float like = restaurantPlace.getLike();
                    if (like >= 1) {
                        starOne.setVisibility(View.VISIBLE);
                    }
                    if (like >= 2) {
                        starTwo.setVisibility(View.VISIBLE);
                    }
                    if (like == 3) {
                        starThree.setVisibility(View.VISIBLE);
                    }
                }
            }
        });
    }


    public static OnSuccessListener<Void> updateUIAfterRESTRequestsCompleted(final int origin, Activity activity) {
        return new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                switch (origin) {
                    case SIGN_OUT_TASK:
                        activity.finish();
                        //startSignInActivity();
                        break;
                    case DELETE_USER_TASK:
                        //finishAffinity();
                        //BaseActivity.this.finish();
                        System.exit(0);
                        break;
                    default:
                        break;
                }
            }
        };
    }


}
