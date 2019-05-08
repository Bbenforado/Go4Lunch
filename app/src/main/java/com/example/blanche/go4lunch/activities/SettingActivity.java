package com.example.blanche.go4lunch.activities;

import android.content.SharedPreferences;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.blanche.go4lunch.R;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SettingActivity extends AppCompatActivity {

    public static final String APP_PREFERENCES = "appPreferences";
    public static final String CURRENT_USER_NAME = "currentUserName";
    public static final String CURRENT_USER_MAIL_ADRESS = "currentUserMailAdress";
    public static final String CURRENT_USER_URL_PICTURE = "currentUserUrlPicture";
    private SharedPreferences preferences;
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
        preferences = getSharedPreferences(APP_PREFERENCES, MODE_PRIVATE);

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
        String userName = preferences.getString(CURRENT_USER_NAME, null);
        String userMail = preferences.getString(CURRENT_USER_MAIL_ADRESS, null);
        String photoUrl = preferences.getString(CURRENT_USER_URL_PICTURE, null);

        userNameTextview.setText(userName);
        userMailTextview.setText(userMail);
        if (photoUrl != null) {
            Glide.with(this)
                    .load(photoUrl)
                    .apply(RequestOptions.circleCropTransform())
                    .into(imageView);
        }
    }

}
