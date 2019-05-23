package com.example.blanche.go4lunch.utils;

import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ImageView;
import com.example.blanche.go4lunch.api.RestaurantPlaceHelper;
import com.example.blanche.go4lunch.models.RestaurantPlace;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;

import io.reactivex.disposables.Disposable;

public class Utils {


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
    /*public static void setStars(String id, ImageView starOne, ImageView starTwo, ImageView starThree) {
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
    }*/


}
