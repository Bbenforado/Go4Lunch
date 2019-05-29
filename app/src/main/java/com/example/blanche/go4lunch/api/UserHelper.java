package com.example.blanche.go4lunch.api;

import com.example.blanche.go4lunch.models.User;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;


public class UserHelper {

    private static final String COLLECTION_NAME = "users";

    // --- COLLECTION REFERENCE ---

    public static CollectionReference getUsersCollection(){
        return FirebaseFirestore.getInstance().collection(COLLECTION_NAME);
    }

    // --- CREATE ---

    public static Task<Void> createUser(String uid, String username, String urlPicture, boolean isNotificationEnabled) {
        User userToCreate = new User(uid, username, urlPicture, isNotificationEnabled);
        return UserHelper.getUsersCollection().document(uid).set(userToCreate);
    }

    // --- GET ---

    public static Task<DocumentSnapshot> getUser(String uid){
        return UserHelper.getUsersCollection().document(uid).get();
    }

    public static Query getAllUsers(){
        return UserHelper.getUsersCollection()
                .limit(50);
    }

    // --- UPDATE ---

    public static Task<Void> updateUsername(String username, String uid) {
        return UserHelper.getUsersCollection().document(uid).update("username", username);
    }

    public static Task<Void> updateUserChosenRestaurant(String uid, Boolean hasChosenRestaurant, String restaurantName, String adress, String number, String website, String photoId, String restaurantId) {
        return UserHelper.getUsersCollection().document(uid).update("hasChosenRestaurant", hasChosenRestaurant, "chosenRestaurant", restaurantName, "chosenRestaurantAdress", adress, "chosenRestaurantPhoneNumber", number, "chosenRestaurantWebsite", website, "chosenRestaurantPhotoId", photoId, "restaurantId", restaurantId);
    }

    public static Task<Void> updateNotificationBoolean(String uid, boolean isNotificationEnabled) {
        return UserHelper.getUsersCollection().document(uid).update("hasEnableNotifications", isNotificationEnabled);
    }

    // --- DELETE ---

    public static Task<Void> deleteUser(String uid) {
        return UserHelper.getUsersCollection().document(uid).delete();
    }
}
