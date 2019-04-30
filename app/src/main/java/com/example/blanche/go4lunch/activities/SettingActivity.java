package com.example.blanche.go4lunch.activities;

import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.blanche.go4lunch.R;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SettingActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private ActionBar actionBar;
    @BindView(R.id.profile_picture_settings)
    ImageView imageView;
    @BindView(R.id.user_name_settings)
    TextView userNameTextview;
    @BindView(R.id.user_mail_settings)
    TextView userMailTextview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        ButterKnife.bind(this);
        configureToolbar();
        updateUIWithUserInfos();
    }

    private void configureToolbar() {
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();
        actionBar.setTitle(R.string.settings);
        actionBar.setDisplayHomeAsUpEnabled(true);
    }

    private void updateUIWithUserInfos() {
        /*userNameTextview.setText(getCurrentUser().getDisplayName());
        userMailTextview.setText(getCurrentUser().getEmail());*/
        /*if (getCurrentUser().getPhotoUrl() != null) {
            Glide.with(this)
                    .load(getCurrentUser().getPhotoUrl())
                    .apply(RequestOptions.circleCropTransform())
                    .into(imageView);
        }*/
    }

}
