package com.example.blanche.go4lunch.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import androidx.annotation.NonNull;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import androidx.cardview.widget.CardView;
import android.view.MenuItem;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import com.example.blanche.go4lunch.R;
import com.example.blanche.go4lunch.api.UserHelper;
import com.example.blanche.go4lunch.fragments.PageFragment;
import com.example.blanche.go4lunch.fragments.SecondPageFragment;
import com.example.blanche.go4lunch.fragments.ThirdPageFragment;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.ErrorCodes;
import com.firebase.ui.auth.IdpResponse;
import java.util.Arrays;
import java.util.Calendar;
import java.util.TimeZone;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.example.blanche.go4lunch.utils.Utils.getCurrentUser;
import static com.example.blanche.go4lunch.utils.Utils.isCurrentUserLogged;

public class MainActivity extends BaseActivity {

    public static final int RC_SIGN_IN = 123;
    public static final String KEY_FRAGMENT = "keyFragment";
    public static final String APP_PREFERENCES = "appPreferences";
    public static final String CURRENT_USER_UID = "currentUserUid";
    public static final long DAY_IN_MILLIS = 24 * 60 * 60 * 1000;
    public static final String TIME_WHEN_SAVED = "time";
    private PageFragment pageFragment;
    private SecondPageFragment secondPageFragment;
    private ThirdPageFragment thirdPageFragment;
    private SharedPreferences preferences;

    //------------------
    //BIND VIEW
    //--------------------
    @BindView(R.id.navigation)
    BottomNavigationView bottomNavigationView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //we will have to see if a user if already connected, then displays or not
        if (!isCurrentUserLogged()) {
            startSignInActivity();
        } else {
            //check if internet is available before to display fragments
            if (isNetworkAvailable()) {
                configureActivity();
            } else {
                setContentView(R.layout.layout_no_internet);
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (isNetworkAvailable()) {
            AutoCompleteTextView autoCompleteTextView = findViewById(R.id.autocomplete_textview);
            CardView cardView = findViewById(R.id.idCardView);
            DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);

            if (pageFragment.isVisible() || secondPageFragment.isVisible()) {
                if (cardView.getVisibility() == View.VISIBLE) {
                    cardView.setVisibility(View.GONE);
                    autoCompleteTextView.getText().clear();
                } else if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START);
                } else {
                    super.onBackPressed();
                }
            } else if (thirdPageFragment.isVisible()) {
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START);
                } else {
                    super.onBackPressed();
                }
            }
        } else {
            super.onBackPressed();
        }
    }

    //----------------------
    //CONFIGURATION
    //----------------------------
    private void configureActivity() {
        ButterKnife.bind(this);
        preferences = getSharedPreferences(APP_PREFERENCES, MODE_PRIVATE);
        checkTime();
        preferences.edit().putString(KEY_FRAGMENT, null).apply();
        preferences.edit().putString(CURRENT_USER_UID, getCurrentUser().getUid()).apply();
        setListener();
        secondPageFragment = new SecondPageFragment();
        thirdPageFragment = new ThirdPageFragment();
        pageFragment = new PageFragment();
        showFragment(pageFragment);
    }

    //---------------------------
    //ACTIONS
    //---------------------------
    private void setListener() {
        BottomNavigationView.OnNavigationItemSelectedListener listener = new BottomNavigationView.OnNavigationItemSelectedListener() {

            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.navigation_map:
                        pageFragment = new PageFragment();
                        showFragment(pageFragment);
                        preferences.edit().putString(KEY_FRAGMENT, "first").apply();
                        return true;
                    case R.id.navigation_list:
                        secondPageFragment = new SecondPageFragment();
                        showFragment(secondPageFragment);
                        preferences.edit().putString(KEY_FRAGMENT, "second").apply();
                        return true;
                    case R.id.navigation_workmates:
                        thirdPageFragment = new ThirdPageFragment();
                        showFragment(thirdPageFragment);
                        return true;
                }
                return false;
            }
        };
        bottomNavigationView.setOnNavigationItemSelectedListener(listener);
    }

    //--------------------
    //AUTHENTICATION METHODS
    //-----------------------------
    private void startSignInActivity() {
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
        handleResponseAfterSignIn(requestCode, resultCode, data);
    }

    private void handleResponseAfterSignIn(int requestCode, int resultCode, Intent data) {
        if (requestCode == RC_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);
            if (resultCode == RESULT_OK) {//SUCCESS
                //launch main activity
                Toast.makeText(this, R.string.succeed_auth_message, Toast.LENGTH_SHORT).show();
                createUserInFirestore();
                configureActivity();
            } else {
                if (response == null) {
                    finish();
                } else if(response.getError().getErrorCode() == ErrorCodes.NO_NETWORK) {
                    Toast.makeText(this, R.string.no_internet_auth_message, Toast.LENGTH_SHORT).show();
                } else if (response.getError().getErrorCode() == ErrorCodes.UNKNOWN_ERROR) {
                    Toast.makeText(this, R.string.unknown_error_auth_message, Toast.LENGTH_SHORT).show();
                }
            }
        }
    }


    /**
     * creates a new user in firebase
     */
    private void createUserInFirestore(){
        if (getCurrentUser() != null){
            String urlPicture = (getCurrentUser().getPhotoUrl() != null) ? getCurrentUser().getPhotoUrl().toString() : null;
            String username = getCurrentUser().getDisplayName();
            String uid = getCurrentUser().getUid();
            UserHelper.createUser(uid, username, urlPicture, true).addOnFailureListener(this.onFailureListener());
        }
    }

    //------------------
    //METHODS
    //------------------
    private void showFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.content, fragment)
                .commit();
    }

    /**
     * verify if there is a internet connection
     * @return
     */
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    /**
     * check if it s a new day since last time the user chose a restaurant.
     * If it's a new day, we delete the saved place.
     */
    private void checkTime() {
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
}