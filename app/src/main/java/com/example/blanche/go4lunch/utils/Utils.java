package com.example.blanche.go4lunch.utils;

import android.app.Activity;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;

import android.widget.ImageView;
import androidx.appcompat.widget.Toolbar;

import com.example.blanche.go4lunch.R;
import com.example.blanche.go4lunch.api.RestaurantPlaceHelper;
import com.example.blanche.go4lunch.models.RestaurantPlace;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;

import io.reactivex.disposables.Disposable;

public class Utils {

    public static final int SIGN_OUT_TASK = 10;
    public static final int DELETE_USER_TASK = 20;

    //----------------------
    //GET USER INFO
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

    //-------------------
    //UPDATE INTERFACE
    //---------------------
    /**
     * get the rate of a restaurant and display the stars depending on that rate
     * @param id id of the restaurant
     * @param starOne image of star for rating
     * @param starTwo image of star for rating
     * @param starThree image of star for rating
     */
    public static void setStars(String id, ImageView starOne, ImageView starTwo, ImageView starThree) {
        starOne.setImageResource(R.drawable.ic_star);
        starTwo.setImageResource(R.drawable.ic_star);
        starThree.setImageResource(R.drawable.ic_star);

        RestaurantPlaceHelper.getRestaurantPlace(id).addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                RestaurantPlace restaurantPlace = documentSnapshot.toObject(RestaurantPlace.class);
                if (restaurantPlace.getLike() != 0) {
                    float like = restaurantPlace.getLike();
                    if (like == 0.5) {
                        starOne.setImageResource(R.drawable.ic_star_half_colored);
                    } else if (like >= 1) {
                        starOne.setImageResource(R.drawable.ic_colored_star);
                    }
                    if (like == 1.5) {
                        starTwo.setImageResource(R.drawable.ic_star_half_colored);
                    } else if (like >= 2){
                        starTwo.setImageResource(R.drawable.ic_colored_star);
                    }
                    if (like == 2.5) {
                        starThree.setImageResource(R.drawable.ic_star_half_colored);
                    } else if (like == 3) {
                        starThree.setImageResource(R.drawable.ic_colored_star);
                    }
                }
            }
        });
    }

    //---------------------------
    //ON SUCCESS LISTENER
    //------------------------------
    /**
     * Used in fragment for sign out
     * in settingActivity for deleting user task
     * @param origin
     * @param activity
     * @return
     */
    public static OnSuccessListener<Void> updateUIAfterRESTRequestsCompleted(final int origin, Activity activity) {
        return new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                switch (origin) {
                    case SIGN_OUT_TASK:
                        activity.finish();
                        break;
                    case DELETE_USER_TASK:
                        System.exit(0);
                        break;
                    default:
                        break;
                }
            }
        };
    }

    //-------------------
    //METHODS
    //---------------------
    /**
     * get the distance between two points in meters
     * @param lat_a lat of point a
     * @param lng_a lng of point a
     * @param lat_b lat of point b
     * @param lng_b lng of point b
     * @return the distance between two points in meters (double)
     */
    public static double meterDistanceBetweenPoints(float lat_a, float lng_a, float lat_b, float lng_b) {
        float pk = (float) (180.f/Math.PI);

        float a1 = lat_a / pk;
        float a2 = lng_a / pk;
        float b1 = lat_b / pk;
        float b2 = lng_b / pk;

        double t1 = Math.cos(a1) * Math.cos(a2) * Math.cos(b1) * Math.cos(b2);
        double t2 = Math.cos(a1) * Math.sin(a2) * Math.cos(b1) * Math.sin(b2);
        double t3 = Math.sin(a1) * Math.sin(b1);
        double tt = Math.acos(t1 + t2 + t3);

        return 6366000 * tt;
    }

    /**
     * get the distance between the user and a place, in meters, formatted to have no double
     * @param lat lat of place
     * @param lng lng of place
     * @param userLatlng user position
     * @return formatted distance between user and a place
     */
    public static String getDistance(double lat, double lng, String userLatlng) {
        if (userLatlng.contains(",")) {
            String[] values = userLatlng.split(",");
            double userLat = Double.parseDouble(values[0]);
            double userLng = Double.parseDouble(values[1]);
            float userL = (float) userLat;
            float userLg = (float) userLng;
            float restaurantLat = (float) lat;
            float restaurantLng = (float) lng;

            double restaurantLocation = meterDistanceBetweenPoints(userL, userLg, restaurantLat, restaurantLng);
            String distanceBetween = Double.toString(restaurantLocation);

            String[] meters = distanceBetween.split("\\.");

            return meters[0] + " m";
        } else {
            return userLatlng;
        }
    }

    /**
     * get the opening hours in a certain format
     * @param openingHoursSentence Sentence of opening hours
     * @param string day of week
     * @return formatted string of opening hours (remove the day of week at the beginning if it s there)
     */
    public static String getFormattedOpeningHours(String openingHoursSentence, String string) {
        String formattedOpeningHours = openingHoursSentence;
        if (formattedOpeningHours.startsWith(string)) {
            formattedOpeningHours = formattedOpeningHours.substring(string.length());
        }
        return formattedOpeningHours;
    }

    /**
     * if the name of the user is too long, we make it shorter it
     * @param username name of the user
     * @return name of the user shorter if it was too long
     */
    public static String verifyUsernameLength(String username) {
        if (username.length() > 10) {
            username = username.substring(0, 7) + ".";
        }
        return username;
    }
}
