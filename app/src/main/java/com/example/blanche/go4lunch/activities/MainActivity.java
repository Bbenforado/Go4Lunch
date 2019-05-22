package com.example.blanche.go4lunch.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.blanche.go4lunch.models.User;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.TypeFilter;
import com.google.android.libraries.places.api.net.PlacesClient;



import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.blanche.go4lunch.BaseActivity;
import com.example.blanche.go4lunch.R;
import com.example.blanche.go4lunch.api.UserHelper;
import com.example.blanche.go4lunch.fragments.PageFragment;
import com.example.blanche.go4lunch.fragments.SecondPageFragment;
import com.example.blanche.go4lunch.fragments.ThirdPageFragment;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.ErrorCodes;
import com.firebase.ui.auth.IdpResponse;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

import butterknife.BindView;
import butterknife.ButterKnife;
import static com.example.blanche.go4lunch.utils.Utils.getCurrentUser;
import static com.example.blanche.go4lunch.utils.Utils.isCurrentUserLogged;

public class MainActivity extends BaseActivity implements NavigationView.OnNavigationItemSelectedListener {

    public static final int RC_SIGN_IN = 123;
    public static final int SIGN_OUT_TASK = 10;
    public static final int DELETE_USER_TASK = 20;
    public static final String KEY_FRAGMENT = "keyFragment";
    public static final String APP_PREFERENCES = "appPreferences";
    public static final String KEY_ACTIVITY = "keyActivity";
    public static final String CURRENT_USER_UID = "currentUserUid";
    public static final long DAY_IN_MILLIS = 24*60*60*1000;
    public static final String TIME_WHEN_SAVED = "time";

    private SharedPreferences preferences;
    @BindView(R.id.navigation)
    BottomNavigationView bottomNavigationView;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Toolbar toolbar;
    private ActionBar actionBar;
    private BottomNavigationView.OnNavigationItemSelectedListener listener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //we will have to see if a user if already connected, then displays or not
        if (!isCurrentUserLogged()) {
            startSignInActivity();
        } else {
            configureActivity();
        }
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        /*if (isCurrentUserLogged()) {
            if (preferences.getString(KEY_FRAGMENT, null) != null) {
                if (preferences.getString(KEY_FRAGMENT, null) == "first") {
                    showFragment(new PageFragment());
                } else if (preferences.getString(KEY_FRAGMENT, null).equals("second")) {
                    showFragment(new SecondPageFragment());
                } else if (preferences.getString(KEY_FRAGMENT, null) == "third") {
                    showFragment(new ThirdPageFragment());
                }
            }
        }*/
    }

    @Override
    protected void onResume() {
        System.out.println("on resume");
        super.onResume();
        if (isCurrentUserLogged()) {
            updateUiWhenResuming();
        }
    }

    //----------------------
    //CONFIGURATION
    //----------------------------
    private void configureActivity() {
        // Initialize Places.
        Places.initialize(getApplicationContext(), "AIzaSyA6Jk5Xl1MbXbYcfWywZ0vwUY2Ux4KLta4");
        // Create a new Places client instance.
        PlacesClient placesClient = Places.createClient(this);

        preferences = getSharedPreferences(APP_PREFERENCES, MODE_PRIVATE);
        ButterKnife.bind(this);
        configureToolbar();
        checkTime();
        //preferences.edit().putString(KEY_FRAGMENT, null).apply();
        preferences.edit().putString(CURRENT_USER_UID, getCurrentUser().getUid()).apply();
        configureNavigationView();
        configureDrawerLayout();
        setListener();

        showFragment(new PageFragment());
    }

    protected void configureToolbar() {
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();
        actionBar.setTitle(R.string.toolbar_title_for_first_and_second_fragment);
    }
    private void configureNavigationView() {
        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        displayUserInfoInNavigationDrawer();
    }

    private void displayUserInfoInNavigationDrawer() {
        View headerLayout = navigationView.getHeaderView(0);
        ImageView profilePictureImageview = headerLayout.findViewById(R.id.profile_picture);
        TextView userNameTextview = headerLayout.findViewById(R.id.user_name);
        TextView userMail = headerLayout.findViewById(R.id.user_mail);
        if (isCurrentUserLogged()) {
            if (getCurrentUser().getPhotoUrl() != null) {
                Glide.with(this)
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

    private void configureDrawerLayout() {
        System.out.println("conf drawer layout");
        drawerLayout = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
    }

    //---------------------------
    //ACTIONS
    //---------------------------
    private void setListener() {
        listener = new BottomNavigationView.OnNavigationItemSelectedListener() {

            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.navigation_map:
                        showFragment(new PageFragment());
                        actionBar.setTitle(R.string.toolbar_title_for_first_and_second_fragment);
                        //preferences.edit().putString(KEY_FRAGMENT, "first").apply();
                        return true;
                    case R.id.navigation_list:
                        showFragment(new SecondPageFragment());
                        actionBar.setTitle(R.string.toolbar_title_for_first_and_second_fragment);
                        //preferences.edit().putString(KEY_FRAGMENT, "second").apply();
                        return true;
                    case R.id.navigation_workmates:
                        showFragment(new ThirdPageFragment());
                        actionBar.setTitle(R.string.toolbar_title_for_third_fragment);
                        //preferences.edit().putString(KEY_FRAGMENT, "third").apply();
                        return true;
                }
                return false;
            }
        };
        bottomNavigationView.setOnNavigationItemSelectedListener(listener);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.lunch:
                checkIfCurrentUserChoseRestaurant();
                break;
            case R.id.settings:
                //OPEN SETTING ACTIVITY WITH DELETE ACCOUNT BUTTON
                launchSettingActivity();
                break;
            case R.id.log_out:
                signOutUser();
                break;
            default:
                break;
        }
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    //-----------------------
    //METHODS
    //--------------------------
    private void showFragment(Fragment fragment) {
        System.out.println("show frag");
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.content, fragment)
                .commit();
    }

    //--------------------
    //AUTHENTICATION METHODS
    //-----------------------------
    private void startSignInActivity() {
        System.out.println("ok here");
        startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setTheme(R.style.LoginTheme)
                        .setAvailableProviders(
                                Arrays.asList(
                                        new AuthUI.IdpConfig.EmailBuilder().build(),
                                        new AuthUI.IdpConfig.GoogleBuilder().build(),
                                        new AuthUI.IdpConfig.FacebookBuilder().build()))
                        .setIsSmartLockEnabled(false, true)
                        .setLogo(R.drawable.ic_logo_lunch)
                        .build(),
                RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        System.out.println("request here = " + requestCode);
        System.out.println("result here = " + resultCode);
        handleResponseAfterSignIn(requestCode, resultCode, data);
        handleResponseAfterAutocompleteSearch(requestCode, resultCode, data);
    }

    private void handleResponseAfterSignIn(int requestCode, int resultCode, Intent data) {
        System.out.println("request code = " + requestCode);
        if (requestCode == RC_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);
            System.out.println("result = " + resultCode);
            if (resultCode == RESULT_OK) {//SUCCESS
                //launch main activity
                Toast.makeText(this, R.string.succeed_auth_message, Toast.LENGTH_SHORT).show();
                createUserInFirestore();
                configureActivity();
            } else {
                if (response == null) {

                    Toast.makeText(this, R.string.error_auth_message, Toast.LENGTH_SHORT).show();
                } else if(response.getError().getErrorCode() == ErrorCodes.NO_NETWORK) {
                    Toast.makeText(this, R.string.no_internet_auth_message, Toast.LENGTH_SHORT).show();
                } else if (response.getError().getErrorCode() == ErrorCodes.UNKNOWN_ERROR) {
                    Toast.makeText(this, R.string.unknown_error_auth_message, Toast.LENGTH_SHORT).show();
                }
            }
        }
    }



    private void createUserInFirestore(){
        System.out.println("we are coming here");
        if (getCurrentUser() != null){

            String urlPicture = (getCurrentUser().getPhotoUrl() != null) ? getCurrentUser().getPhotoUrl().toString() : null;
            String username = getCurrentUser().getDisplayName();
            String uid = getCurrentUser().getUid();

            UserHelper.createUser(uid, username, urlPicture).addOnFailureListener(this.onFailureListener());

        }
    }

    private void signOutUser() {
        AuthUI.getInstance()
                .signOut(this)
                .addOnSuccessListener(this, this.updateUIAfterRESTRequestsCompleted(SIGN_OUT_TASK));
    }
    //------------------
    //METHODS TO LAUNCH ACTIVITIES
    //-----------------------------------
    private void launchSettingActivity() {
        Intent settingActivity = new Intent(this, SettingActivity.class);
        startActivity(settingActivity);
    }

    private void launchYourLunchActivity() {
        preferences.edit().putInt(KEY_ACTIVITY, 2).apply();
        Intent yourLunchActivity = new Intent(this, RestaurantDetailsActivity.class);
        startActivity(yourLunchActivity);
    }

    //------------------
    //METHODS
    //------------------
    private void checkTime() {
        System.out.println("check time");
        //retrieve the date saved when user saved place he s going to eat
        long timeWhenSaved = preferences.getLong(TIME_WHEN_SAVED, 0);

        if (timeWhenSaved != 0) {
            //get the current time
            long currentTime = Calendar.getInstance().getTimeInMillis();
            TimeZone timeZone = TimeZone.getDefault();
            currentTime = currentTime+timeZone.getDSTSavings();
            //get the time when we saved at midnight (at the beginning of the day)
            long timeRemain = timeWhenSaved % DAY_IN_MILLIS;
            long atStartOfTheDay = timeWhenSaved - timeRemain;
            //see how many days passed between current time and last day we saved restaurant(at midnight)
            long timeBetween = currentTime - atStartOfTheDay;
            if(timeBetween >= DAY_IN_MILLIS) {
                UserHelper.updateUserChosenRestaurant(getCurrentUser().getUid(), false, null, null, null, null, null, null);
            }
        }
    }

    private void checkIfCurrentUserChoseRestaurant() {
        UserHelper.getUser(getCurrentUser().getUid()).addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                User currentUser = documentSnapshot.toObject(User.class);
                if (currentUser.isHasChosenRestaurant()) {
                    launchYourLunchActivity();
                } else {
                    Toast.makeText(getApplicationContext(), "You haven't chose where you are going to eat yet! Check some restaurants and make a choice :)", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void updateUiWhenResuming() {
        View headerLayout = navigationView.getHeaderView(0);
        TextView userNameTextview = headerLayout.findViewById(R.id.user_name);
            UserHelper.getUser(getCurrentUser().getUid()).addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    User currentUser = documentSnapshot.toObject(User.class);
                    userNameTextview.setText(currentUser.getUsername());
                }
            });
    }
}