package com.example.blanche.go4lunch.fragments;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.blanche.go4lunch.R;
import com.example.blanche.go4lunch.activities.RestaurantDetailsActivity;
import com.example.blanche.go4lunch.activities.SettingActivity;
import com.example.blanche.go4lunch.api.UserHelper;
import com.example.blanche.go4lunch.models.User;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;

import static com.example.blanche.go4lunch.utils.Utils.SIGN_OUT_TASK;
import static com.example.blanche.go4lunch.utils.Utils.getCurrentUser;
import static com.example.blanche.go4lunch.utils.Utils.isCurrentUserLogged;
import static com.example.blanche.go4lunch.utils.Utils.updateUIAfterRESTRequestsCompleted;

public class BaseFragment extends Fragment {


    public static final String KEY_ACTIVITY = "keyActivity";

    public void configureDrawerLayout(DrawerLayout drawerLayout, Toolbar toolbar, Activity activity) {
        //drawerLayout = activity.findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(activity, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
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
                                               SharedPreferences preferences, final String key) {
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                switch (id) {
                    case R.id.lunch:
                        checkIfCurrentUserChoseRestaurant(context, preferences, key);
                        break;
                    case R.id.settings:
                        //OPEN SETTING ACTIVITY WITH DELETE ACCOUNT BUTTON
                        launchSettingActivity(context);
                        break;
                    case R.id.log_out:
                        System.out.println("context = " + context + ", activity = " + activity);
                        signOutUser(context, SIGN_OUT_TASK, activity);
                        break;
                    default:
                        break;
                }
                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            }
        });
        displayUserInfoInNavigationDrawer(navigationView, context);
    }


    public void checkIfCurrentUserChoseRestaurant(Context context, SharedPreferences preferences, final String key) {
        UserHelper.getUser(getCurrentUser().getUid()).addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                User currentUser = documentSnapshot.toObject(User.class);
                if (currentUser.isHasChosenRestaurant()) {
                    launchYourLunchActivity(preferences, key, context);
                } else {
                    Toast.makeText(context, "You haven't chose where you are going to eat yet! Check some restaurants and make a choice :)", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void launchYourLunchActivity(SharedPreferences preferences, final String key, Context context) {
        System.out.println("come here?");
        preferences.edit().putInt(KEY_ACTIVITY, 2).apply();
        Intent yourLunchActivity = new Intent(context, RestaurantDetailsActivity.class);
        context.startActivity(yourLunchActivity);
    }

    public void launchSettingActivity(Context context) {
        Intent settingActivity = new Intent(context, SettingActivity.class);
        context.startActivity(settingActivity);
    }

    public void signOutUser(Context context, final int signOutTask, Activity activity) {
        System.out.println("hello");
        AuthUI.getInstance()
                .signOut(context)
                .addOnSuccessListener(updateUIAfterRESTRequestsCompleted(signOutTask, activity));
    }




}
