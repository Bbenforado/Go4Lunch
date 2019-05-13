package com.example.blanche.go4lunch.activities;

import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.blanche.go4lunch.BaseActivity;
import com.example.blanche.go4lunch.R;
import com.example.blanche.go4lunch.adapters.RecyclerViewAdapterDetails;
import com.example.blanche.go4lunch.api.UserHelper;
import com.example.blanche.go4lunch.models.User;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.Calendar;
import java.util.TimeZone;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class RestaurantDetailsActivity extends BaseActivity {

    public static final String RESTAURANT_NAME = "name";
    public static final String TYPE_OF_FOOD_AND_ADRESS = "typeAndAdress";
    public static final String RESTAURANT_PHOTO = "photo";
    public static final String RESTAURANT_PHONE_NUMBER = "number";
    public static final String RESTAURANT_WEBSITE = "website";
    public static final String KEY_ACTIVITY = "keyActivity";
    public static final String APP_PREFERENCES = "appPreferences";
    public static final String TIME_WHEN_SAVED = "time";
    private int keyActivity;
    boolean isButtonClicked;
    private User currentUser;
    private String name;
    private String adress;
    private String photoId;
    private String website;
    private String phoneNumber;
    private SharedPreferences preferences;
    private Toolbar toolbar;
    private ActionBar actionBar;
    private RecyclerViewAdapterDetails adapter;
    @BindView(R.id.details_page_recycler_view)
    RecyclerView recyclerView;
    /*@BindView(R.id.details_page_swipe_container)
    SwipeRefreshLayout swipeRefreshLayout;*/
    @BindView(R.id.floating_action_button)
    FloatingActionButton button;
    @BindView(R.id.call_button) ImageButton callButton;
    @BindView(R.id.like_button) ImageButton likeButton;
    @BindView(R.id.website_button) ImageButton websiteButton;
    @BindView(R.id.restaurant_name)
    TextView restaurantName;
    @BindView(R.id.main_backdrop)
    ImageView imageView;
    @BindView(R.id.type_of_food_and_adress) TextView typeOfFoodAndAdress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restaurant_details);
        ButterKnife.bind(this);
        System.out.println("on create details");
        preferences = getSharedPreferences(APP_PREFERENCES, MODE_PRIVATE);

        keyActivity = preferences.getInt(KEY_ACTIVITY, -1);
        getCurrentUserDataFromFireBase();
        //get the data from your lunch (first fragment) [0] or from second fragment [1], or from navigation drawer
        if (keyActivity == 0) {
            name = preferences.getString(RESTAURANT_NAME, null);
            adress = preferences.getString(TYPE_OF_FOOD_AND_ADRESS, null);
            photoId = preferences.getString(RESTAURANT_PHOTO, null);
            System.out.println("name = " + name + " adress = " + adress + " photo = " + photoId);
        } else if(keyActivity == 1) {
            name = getIntent().getExtras().getString(RESTAURANT_NAME);
            adress = getIntent().getExtras().getString(TYPE_OF_FOOD_AND_ADRESS);
            photoId = getIntent().getExtras().getString(RESTAURANT_PHOTO);
            website = getIntent().getExtras().getString(RESTAURANT_WEBSITE);
            phoneNumber = getIntent().getExtras().getString(RESTAURANT_PHONE_NUMBER);
        }


        //configureToolbar();
        if (keyActivity == 0 || keyActivity == 1) {
            displayRestaurantInformations();
        }

        configureRecyclerView();
    }

    @Override
    protected void onStop() {
        super.onStop();
        preferences.edit().putInt(KEY_ACTIVITY, -1).apply();
    }

    //-----------------------
    //CONFIGURATION
    //--------------------------------

    /*protected void configureToolbar() {
        //toolbar = findViewById(R.id.toolbar);
        toolbar = findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();
        int keyActivity = preferences.getInt(KEY_ACTIVITY, -1);
        String name = null;
        if (keyActivity == 0) {
        } else if(keyActivity == 1) {
            name = preferences.getString(RESTAURANT_NAME, null);
        } else {
            name = getIntent().getExtras().getString(RESTAURANT_NAME);
        }

        actionBar.setTitle(name);
        actionBar.setDisplayHomeAsUpEnabled(true);
    }*/

    private void configureRecyclerView() {
        //here we fetch an arrayList of objects restaurants and set the adapter to the
        //recycler view, something like:
        adapter = new RecyclerViewAdapterDetails();
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

   /* private void configureSwipeRefreshLayout() {
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                //refresh the page, request the api
            }
        });
    }*/

    //--------------------------
    //ACTIONS
    //-----------------------------
    @OnClick(R.id.floating_action_button)
    public void saveRestaurant() {
        isButtonClicked = !isButtonClicked;
        String userUid = getCurrentUser().getUid();
        //if true
        if (isButtonClicked) {
            //change the color of the button
            button.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorPrimaryDark)));

            //if it s from the second fragment
            if (preferences.getInt(KEY_ACTIVITY, -1) != 0) {
                //we save at what time the user chose the restaurant
                saveTimeWhenChoseRestaurant();

                //we update the name of the restaurant in firebase
                UserHelper.updateUserChosenRestaurant(userUid, true, name, adress, phoneNumber, website, photoId);

                //we display toast message to user
                Toast.makeText(this, "You are going to eat at " + name + " !", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "not implemented yet", Toast.LENGTH_SHORT).show();
            }
        } else {
            //unclick button
            if (preferences.getInt(KEY_ACTIVITY, -1) != 0) {
                //update the name of the restaurant in firebase
                UserHelper.updateUserChosenRestaurant(userUid, false, null, null, null, null, null);
                //change button color
                button.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorAccent)));
                //display message to user
                Toast.makeText(this, "Want to eat somewhere else?", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "not implemented yet", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @OnClick(R.id.call_button)
    public void callRestaurant(View v) {
        //get the restaurant number, and displays the call tool of the phone
        Toast.makeText(this, "Not implemented yet, but soon, you'll be able to call the restaurant now! :)", Toast.LENGTH_LONG).show();
    }

    @OnClick(R.id.like_button)
    public void likeRestaurant(View v) {
        //add +1 to the restaurant and save it in firebase
        Toast.makeText(this, "Not implemented yet, but soon you'll be able to like the restaurant! <3", Toast.LENGTH_LONG).show();
    }

    @OnClick(R.id.website_button)
    public void openRestaurantWebsite(View v) {
        //get the website of the restaurant and open it in a webview
        Toast.makeText(this, "Not implemented yet, but soon you'll be able to visit restaurant website! :)", Toast.LENGTH_LONG).show();
    }

    //-------------------
    //UPDATE UI
    //------------------------
    private void displayRestaurantInformations() {
        //if key activity == 0, it means this is coming from the navigation drawer
        System.out.println("coming here?");
        if (name != null && adress != null && photoId != null) {
            System.out.println("and here?");
            String url = "https://maps.googleapis.com/maps/api/place/photo?maxwidth=400&photoreference=" + photoId + "&key=AIzaSyA6Jk5Xl1MbXbYcfWywZ0vwUY2Ux4KLta4";
            setRestaurantInformations(name, adress, url);
        }
    }

    private void displayColorButton(User user) {
        if (user.getChosenRestaurant() != null) {
            if (name.equals(user.getChosenRestaurant())) {
                button.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorPrimaryDark)));
                isButtonClicked = true;
            } else {
                button.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorAccent)));
                isButtonClicked = false;
            }
        }
    }

    private void getCurrentUserDataFromFireBase() {
        UserHelper.getUser(getCurrentUser().getUid()).addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                currentUser = documentSnapshot.toObject(User.class);
                if (keyActivity == 0 || keyActivity == 1) {
                    if (currentUser.isHasChosenRestaurant()) {
                        displayColorButton(currentUser);

                    }
                } else if (keyActivity == 2) {
                    if (currentUser.isHasChosenRestaurant()) {
                        button.setVisibility(View.GONE);
                        name = currentUser.getChosenRestaurant();
                        adress = currentUser.getChosenRestaurantAdress();
                        photoId = currentUser.getChosenRestaurantPhotoId();
                        System.out.println("name = " + name + " adress = " + adress + " photo = " + photoId);
                        displayRestaurantInformations();
                    }
                }
            }
        });
    }

    private void saveTimeWhenChoseRestaurant() {
        TimeZone timeZone = TimeZone.getDefault();
        long timeWhenSaved = Calendar.getInstance().getTimeInMillis();
        timeWhenSaved = timeWhenSaved + timeZone.getDSTSavings();
        preferences.edit().putLong(TIME_WHEN_SAVED, timeWhenSaved).apply();
    }



    private void setRestaurantInformations(String name, String adress, String url) {
        restaurantName.setText(name);
        typeOfFoodAndAdress.setText(adress);
        Glide.with(this)
                .load(url)
                .apply(RequestOptions.noTransformation())
                .into(imageView);

    }

}
