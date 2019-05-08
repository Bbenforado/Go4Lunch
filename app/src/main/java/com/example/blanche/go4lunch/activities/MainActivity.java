package com.example.blanche.go4lunch.activities;

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
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.blanche.go4lunch.EssaiActivity;
import com.example.blanche.go4lunch.R;
import com.example.blanche.go4lunch.api.UserHelper;
import com.example.blanche.go4lunch.fragments.PageFragment;
import com.example.blanche.go4lunch.fragments.SecondPageFragment;
import com.example.blanche.go4lunch.fragments.ThirdPageFragment;
import com.example.blanche.go4lunch.models.User;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.ErrorCodes;
import com.firebase.ui.auth.IdpResponse;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    public static final int RC_SIGN_IN = 123;
    public static final int SIGN_OUT_TASK = 10;
    public static final int DELETE_USER_TASK = 20;
    public static final String KEY_FRAGMENT = "keyFragment";
    public static final String APP_PREFERENCES = "appPreferences";
    public static final String KEY_ACTIVITY = "keyActivity";
    public static final String CURRENT_USER_NAME = "currentUserName";
    public static final String CURRENT_USER_MAIL_ADRESS = "currentUserMailAdress";
    public static final String CURRENT_USER_URL_PICTURE = "currentUserUrlPicture";
    public static final String CURRENT_USER_UID = "currentUserUid";
    private List<User> userList;
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
        }
        else {
            preferences = getSharedPreferences(APP_PREFERENCES, MODE_PRIVATE);
            ButterKnife.bind(this);
            configureToolbar();
            preferences.edit().putString(KEY_FRAGMENT, null).apply();
            preferences.edit().putString(CURRENT_USER_UID, getCurrentUser().getUid()).apply();
            saveUserInformationsInPreferences();
            configureNavigationView();
            configureDrawerLayout();
            setListener();
            showFragment(new PageFragment());
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
        if (isCurrentUserLogged()) {
            //displayUserInfoInNavigationDrawer();
            if (preferences.getString(KEY_FRAGMENT, null) != null) {
                if (preferences.getString(KEY_FRAGMENT, null) == "first") {
                    showFragment(new PageFragment());
                } else if (preferences.getString(KEY_FRAGMENT, null).equals("second")) {
                    showFragment(new SecondPageFragment());
                } else if (preferences.getString(KEY_FRAGMENT, null) == "third") {
                    showFragment(new ThirdPageFragment());
                }
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        System.out.println("on stop main");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        System.out.println("on destroy main");
    }

    //----------------------
    //CONFIGURATION
    //----------------------------
    private void configureNavigationView() {
        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        displayUserInfoInNavigationDrawer();
    }

    private void displayUserInfoInNavigationDrawer() {
        //final View headerLayout = navigationView.getHeaderView(0);

        View headerLayout = navigationView.getHeaderView(0);
        ImageView profilePictureImageview = headerLayout.findViewById(R.id.profile_picture);
        TextView userNameTextview = headerLayout.findViewById(R.id.user_name);
        TextView userMail = headerLayout.findViewById(R.id.user_mail);
        if (isCurrentUserLogged()) {
            if (getCurrentUser().getPhotoUrl() != null) {
                Glide.with(this)
                        .load(this.getCurrentUser().getPhotoUrl().toString())
                        .apply(RequestOptions.circleCropTransform())
                        .into(profilePictureImageview);
            }
            userNameTextview.setText(getCurrentUser().getDisplayName());
            userMail.setText(getCurrentUser().getEmail());
        }
    }

    protected void configureToolbar() {
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();
        actionBar.setTitle(R.string.toolbar_title_for_first_and_second_fragment);
    }

    private void configureDrawerLayout() {
        drawerLayout = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
    }

    //---------------------------
    //ACTIONS
    //----------------------------


    private void setListener() {
        listener = new BottomNavigationView.OnNavigationItemSelectedListener() {

            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.navigation_map:
                        showFragment(new PageFragment());
                        actionBar.setTitle(R.string.toolbar_title_for_first_and_second_fragment);
                        preferences.edit().putString(KEY_FRAGMENT, "first").apply();
                        return true;
                    case R.id.navigation_list:
                        showFragment(new SecondPageFragment());
                        actionBar.setTitle(R.string.toolbar_title_for_first_and_second_fragment);
                        preferences.edit().putString(KEY_FRAGMENT, "second").apply();
                        return true;
                    case R.id.navigation_workmates:
                        showFragment(new ThirdPageFragment());
                        actionBar.setTitle(R.string.toolbar_title_for_third_fragment);
                        preferences.edit().putString(KEY_FRAGMENT, "third").apply();
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
                launchYourLunchActivity();
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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_toolbar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.search_item:
                return true;
                default:
                    return super.onOptionsItemSelected(item);
        }
    }

    private void showFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.content, fragment)
                .commit();
    }

    //--------------------
    //AUTHENTIFICATION METHODS
    //-----------------------------
    private void startSignInActivity() {
        startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setTheme(R.style.LoginTheme)
                        .setAvailableProviders(
                                Arrays.asList(
                                        new AuthUI.IdpConfig.GoogleBuilder().build(),
                                        new AuthUI.IdpConfig.FacebookBuilder().build()))
                        .setIsSmartLockEnabled(false, true)
                        .setLogo(R.drawable.ic_logo_lunch)
                        .build(),
                RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        handleResponseAfterSignIn(requestCode, resultCode, data);
    }

    private void handleResponseAfterSignIn(int requestCode, int resultCode, Intent data) {
        IdpResponse response = IdpResponse.fromResultIntent(data);

        if (requestCode == RC_SIGN_IN) {
            if (resultCode == RESULT_OK) {//SUCCESS
                //launch main activity
                Toast.makeText(this, R.string.succeed_auth_message, Toast.LENGTH_SHORT).show();
                createUserInFirestore();
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

    protected OnFailureListener onFailureListener(){
        return new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getApplicationContext(), "unknown error", Toast.LENGTH_LONG).show();
            }
        };
    }

    private void createUserInFirestore(){
        if (this.getCurrentUser() != null){

            String urlPicture = (this.getCurrentUser().getPhotoUrl() != null) ? this.getCurrentUser().getPhotoUrl().toString() : null;
            String username = this.getCurrentUser().getDisplayName();
            String uid = this.getCurrentUser().getUid();

            UserHelper.createUser(uid, username, urlPicture).addOnFailureListener(this.onFailureListener());

        }
    }

    @Nullable
    protected FirebaseUser getCurrentUser(){ return FirebaseAuth.getInstance().getCurrentUser(); }

    protected Boolean isCurrentUserLogged(){ return (this.getCurrentUser() != null); }

    private void signOutUser() {
        AuthUI.getInstance()
                .signOut(this)
                .addOnSuccessListener(this, this.updateUIAfterRESTRequestsCompleted(SIGN_OUT_TASK));
    }

    private OnSuccessListener<Void> updateUIAfterRESTRequestsCompleted(final int origin) {
        return new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                switch (origin) {
                    case SIGN_OUT_TASK:
                        finish();
                        //startSignInActivity();
                        break;
                    case DELETE_USER_TASK:
                        finish();
                        break;
                        default:
                            break;
                }
            }
        };
    }

    //------------------
    //METHODS TO LAUNCH ACTIVITIES
    //-----------------------------------
    private void launchSettingActivity() {
        Intent settingActivity = new Intent(this, SettingActivity.class);
        startActivity(settingActivity);
    }

    private void launchYourLunchActivity() {
        Intent yourLunchActivity = new Intent(this, RestaurantDetailsActivity.class);
        startActivity(yourLunchActivity);
        preferences.edit().putInt(KEY_ACTIVITY, 0).apply();
    }

    //------------------
    //SAVE DATA
    //------------------
    private void saveUserInformationsInPreferences() {
        preferences.edit().putString(CURRENT_USER_NAME, getCurrentUser().getDisplayName()).apply();
        preferences.edit().putString(CURRENT_USER_MAIL_ADRESS, getCurrentUser().getEmail()).apply();
        preferences.edit().putString(CURRENT_USER_URL_PICTURE, getCurrentUser().getPhotoUrl().toString()).apply();
    }
}