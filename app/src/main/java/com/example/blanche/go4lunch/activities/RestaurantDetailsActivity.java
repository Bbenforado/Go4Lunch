package com.example.blanche.go4lunch.activities;

import android.graphics.drawable.Drawable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.blanche.go4lunch.R;
import com.example.blanche.go4lunch.adapters.RecyclerViewAdapterDetails;
import com.example.blanche.go4lunch.adapters.RecyclerViewAdapterThirdFragment;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class RestaurantDetailsActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private ActionBar actionBar;
    private RecyclerViewAdapterDetails adapter;
    private boolean isButtonClicked;
    @BindView(R.id.details_page_recycler_view)
    RecyclerView recyclerView;
    @BindView(R.id.details_page_swipe_container)
    SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.i_eat_here_button)
    ImageButton button;
    @BindView(R.id.call_button) ImageButton callButton;
    @BindView(R.id.like_button) ImageButton likeButton;
    @BindView(R.id.website_button) ImageButton websiteButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restaurant_details);
        ButterKnife.bind(this);
        System.out.println("on create details");
        isButtonClicked = false;
        configureToolbar();
        configureRecyclerView();
    }

    //-----------------------
    //CONFIGURATION
    //--------------------------------

    protected void configureToolbar() {
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();
        actionBar.setTitle("Restaurant Details");
        actionBar.setDisplayHomeAsUpEnabled(true);
    }

    private void configureRecyclerView() {
        //here we fetch an arrayList of objects restaurants and set the adapter to the
        //recycler view, something like:
        //restaurantResultsList = new ArrayList<>();
        adapter = new RecyclerViewAdapterDetails();
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    private void configureSwipeRefreshLayout() {
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                //refresh the page, request the api
            }
        });
    }

    //--------------------------
    //ACTIONS
    //-----------------------------
    @OnClick(R.id.i_eat_here_button)
    public void chooseRestaurant(View view) {
        isButtonClicked = !isButtonClicked;
        if (isButtonClicked) {
            button.setBackgroundResource(R.drawable.ic_button_i_eat_here);
        } else {
            button.setBackgroundResource(R.drawable.ic_button_do_i_eat_here);
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

}
