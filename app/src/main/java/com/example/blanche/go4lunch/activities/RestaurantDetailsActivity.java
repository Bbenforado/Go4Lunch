package com.example.blanche.go4lunch.activities;

import android.content.SharedPreferences;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import com.example.blanche.go4lunch.R;

public class RestaurantDetailsActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private ActionBar actionBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restaurant_details);
        System.out.println("on create details");
        configureToolbar();
    }

    protected void configureToolbar() {
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();
        actionBar.setTitle("Restaurant Details");
        actionBar.setDisplayHomeAsUpEnabled(true);
    }
}
