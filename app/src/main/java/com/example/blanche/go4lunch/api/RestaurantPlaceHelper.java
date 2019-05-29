package com.example.blanche.go4lunch.api;

import com.example.blanche.go4lunch.models.RestaurantPlace;
import com.example.blanche.go4lunch.models.User;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.List;
import java.util.Map;

public class RestaurantPlaceHelper {

    private static final String COLLECTION_NAME = "restaurantPlaces";

    // --- COLLECTION REFERENCE ---

    public static CollectionReference getRestaurantPlaceCollection(){
        return FirebaseFirestore.getInstance().collection(COLLECTION_NAME);
    }

    // --- CREATE ---

    public static Task<Void> createRestaurantPlace(String uid) {
        System.out.println("created!");
        RestaurantPlace restaurantToCreate = new RestaurantPlace(uid);
        return RestaurantPlaceHelper.getRestaurantPlaceCollection().document(uid).set(restaurantToCreate);
    }

    // --- GET ---

    public static Task<DocumentSnapshot> getRestaurantPlace(String uid){
        return RestaurantPlaceHelper.getRestaurantPlaceCollection().document(uid).get();
    }

    public static Query getAllRestaurantPlaces(){
        return RestaurantPlaceHelper.getRestaurantPlaceCollection()
                .limit(50);
    }

    // --- UPDATE ---

    public static Task<Void> updateRestaurantLike(String uid, float like) {
        return RestaurantPlaceHelper.getRestaurantPlaceCollection().document(uid).update("like", like);

    }

    public static Task<Void> updateUserWhoLikeList(String uid, List<String> users) {
        return RestaurantPlaceHelper.getRestaurantPlaceCollection().document(uid).update("usersWhoLiked", users);
    }

    public static Task<Void> updateUserWhoLiked2(String uid, List<Map<String, Integer>> list) {
        return RestaurantPlaceHelper.getRestaurantPlaceCollection().document(uid).update("usersWhoLiked2", list);
    }
}
