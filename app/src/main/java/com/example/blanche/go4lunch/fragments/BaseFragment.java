package com.example.blanche.go4lunch.fragments;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import androidx.annotation.NonNull;

import com.example.blanche.go4lunch.callbacks.MyCallback;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import androidx.fragment.app.Fragment;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.appcompat.widget.Toolbar;

import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.blanche.go4lunch.R;
import com.example.blanche.go4lunch.activities.ChatActivity;
import com.example.blanche.go4lunch.activities.RestaurantDetailsActivity;
import com.example.blanche.go4lunch.activities.SettingActivity;
import com.example.blanche.go4lunch.api.UserHelper;
import com.example.blanche.go4lunch.models.User;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.List;

import butterknife.BindView;

import static com.example.blanche.go4lunch.utils.Utils.SIGN_OUT_TASK;
import static com.example.blanche.go4lunch.utils.Utils.getCurrentUser;
import static com.example.blanche.go4lunch.utils.Utils.isCurrentUserLogged;
import static com.example.blanche.go4lunch.utils.Utils.updateUIAfterRESTRequestsCompleted;

/**
 * Basic fragment
 * each fragment of the activity extends BaseFragment
 */
public class BaseFragment extends Fragment implements LocationListener {

    public static final String KEY_ACTIVITY = "keyActivity";
    @BindView(R.id.toolbar) Toolbar toolbar;

    //------------------
    //CONFIGURATION
    //------------------------
    public void configureDrawerLayout(DrawerLayout drawerLayout, Activity activity) {
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(activity, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
    }

    public void configureToolbar(View view) {
        toolbar = view.findViewById(R.id.toolbar);
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        activity.setSupportActionBar(toolbar);
        ActionBar actionBar = activity.getSupportActionBar();
        actionBar.setTitle(R.string.toolbar_title_for_first_and_second_fragment);
    }

    public void displayUserInfoInNavigationDrawer(NavigationView navigationView, Context context) {
        View headerLayout = navigationView.getHeaderView(0);
        ImageView profilePictureImageview = headerLayout.findViewById(R.id.profile_picture);
        TextView userNameTextview = headerLayout.findViewById(R.id.user_name);
        TextView userMail = headerLayout.findViewById(R.id.user_mail);

        if (isCurrentUserLogged()) {
            if (getCurrentUser().getPhotoUrl() != null) {
                Glide.with(context)
                        .load(getCurrentUser().getPhotoUrl().toString())
                        .apply(RequestOptions.circleCropTransform())
                        .into(profilePictureImageview);
            }
            UserHelper.getUser(getCurrentUser().getUid()).addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    User currentUser = documentSnapshot.toObject(User.class);
                    userNameTextview.setText(currentUser.getUsername());
                }
            });
            userMail.setText(getCurrentUser().getEmail());
        }
    }

    public void configureNavigationView(NavigationView navigationView, Activity activity, DrawerLayout drawerLayout, Context context,
                                               SharedPreferences preferences) {
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                switch (id) {
                    case R.id.lunch:
                        checkIfCurrentUserChoseRestaurant(context, preferences);
                        break;
                    case R.id.settings:
                        launchSettingActivity(context);
                        break;
                    case R.id.log_out:
                        signOutUser(context, SIGN_OUT_TASK, activity);
                        break;
                    case R.id.chat:
                        launchChatActivity(context);
                    default:
                        break;
                }
                uncheckItemOfNavigationDrawer(navigationView);
                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            }
        });
        displayUserInfoInNavigationDrawer(navigationView, context);
    }

    //---------------
    //METHODS
    //---------------------
    public void checkIfCurrentUserChoseRestaurant(Context context, SharedPreferences preferences) {
        UserHelper.getUser(getCurrentUser().getUid()).addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                User currentUser = documentSnapshot.toObject(User.class);
                if (currentUser.isHasChosenRestaurant()) {
                    launchYourLunchActivity(preferences, context);
                } else {
                    Toast.makeText(context, context.getString(R.string.no_restaurant_chose), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void uncheckItemOfNavigationDrawer(NavigationView navigationView) {
        int size = navigationView.getMenu().size();
        for (int i = 0; i < size; i++) {
            navigationView.getMenu().getItem(i).setChecked(false);
        }
    }

    //----------------
    //ACTION
    //--------------------
    /**
     * dismiss the cardView with the autocompleteTextView if click on the cross and no text written
     * @param textView textView display for search
     * @param cardView
     */
    public void setClearTextButtonBehavior(AutoCompleteTextView textView, CardView cardView) {
        if (TextUtils.isEmpty(textView.getText())) {
            cardView.setVisibility(View.GONE);
        } else {
            textView.getText().clear();
        }
    }

    //---------------------
    //LAUNCH ACTIVITY
    //-----------------------
    public void launchYourLunchActivity(SharedPreferences preferences, Context context) {
        preferences.edit().putInt(KEY_ACTIVITY, 2).apply();
        Intent yourLunchActivity = new Intent(context, RestaurantDetailsActivity.class);
        context.startActivity(yourLunchActivity);
    }

    public void launchSettingActivity(Context context) {
        Intent settingActivity = new Intent(context, SettingActivity.class);
        context.startActivity(settingActivity);
    }

    public void launchChatActivity(Context context) {
        Intent chatActivity = new Intent(context, ChatActivity.class);
        context.startActivity(chatActivity);
    }

    //--------------------
    //AUTHENTICATION
    //-----------------------
    public void signOutUser(Context context, final int signOutTask, Activity activity) {
        AuthUI.getInstance()
                .signOut(context)
                .addOnSuccessListener(updateUIAfterRESTRequestsCompleted(signOutTask, activity));
    }

    //--------------------
    //LOCATION LISTENER
    //-----------------------
    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }

    //-----------------------
    //LOCATION
    //------------------------
    /**
     * get the current user location
     * @param context
     * @param listener
     * @param activity
     * @return the user Location
     */
    public static Location getUserLocation(Context context, LocationListener listener, Activity activity) {
        LocationManager locationManager = (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        String bestProvider = locationManager.getBestProvider(criteria, true);
        boolean enabled = locationManager.isProviderEnabled(bestProvider);
        if (!enabled) {
            Toast.makeText(context, context.getString(R.string.no_location_provider), Toast.LENGTH_SHORT).show();
        }

        final long MIN_TIME_BW_UPDATES = 1000;
        final float MIN_DISTANCE_CHANGE_FOR_UPDATES = 1;
        Location myLocation = null;
        try {
            locationManager.requestLocationUpdates(bestProvider, MIN_TIME_BW_UPDATES,
                    MIN_DISTANCE_CHANGE_FOR_UPDATES, listener);
            //myLocation = locationManager.getLastKnownLocation(bestProvider);
            myLocation = locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
        }
        catch (SecurityException e) {
            e.printStackTrace();
        }
        return myLocation;
    }

    /**
     * format a Location to a String format (latitude,longitude)
     * @param location location of the user
     * @return
     */
    public static String formatLocation(Location location) {
            return location.getLatitude() + "," + location.getLongitude();
    }

    //-----------------
    /**
     * get the list of id of restaurants already stored in database to check if the restaurants we got from the request is already stored or not
     * @param myCallback
     */
    public static void readData(MyCallback myCallback, CollectionReference itemsRef, List<String> idList) {
        itemsRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (DocumentSnapshot document : task.getResult()) {
                        String id = document.getString("uid");
                        idList.add(id);
                    }
                    myCallback.onCallback(idList);
                } else {
                    Log.e("TAG", "Error");
                }
            }
        });
    }
}
