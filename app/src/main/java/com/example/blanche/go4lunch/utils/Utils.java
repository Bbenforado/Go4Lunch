package com.example.blanche.go4lunch.utils;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.example.blanche.go4lunch.MyCallback;
import com.example.blanche.go4lunch.api.RestaurantPlaceHelper;
import com.example.blanche.go4lunch.models.RestaurantPlace;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import io.reactivex.disposables.Disposable;

public class Utils {



    @Nullable
    public static FirebaseUser getCurrentUser(){ return FirebaseAuth.getInstance().getCurrentUser(); }

    public static Boolean isCurrentUserLogged(){ return (getCurrentUser() != null); }

    public static void disposeWhenDestroy(Disposable disposable) {
        if(disposable != null && !disposable.isDisposed()) {
            disposable.dispose();
        }
    }
    public static void setStars(String id, ImageView starOne, ImageView starTwo, ImageView starThree) {
        System.out.println("set stars");
        RestaurantPlaceHelper.getRestaurantPlace(id).addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                RestaurantPlace restaurantPlace = documentSnapshot.toObject(RestaurantPlace.class);
                int like = restaurantPlace.getLike();
                if (like >= 10) {
                    starOne.setVisibility(View.VISIBLE);
                }
                if (like >= 20) {
                    starTwo.setVisibility(View.VISIBLE);
                }
                if (like >= 30) {
                    starThree.setVisibility(View.VISIBLE);
                }
            }
        });
    }
}
