package com.example.blanche.go4lunch.activities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.blanche.go4lunch.BaseActivity;
import com.example.blanche.go4lunch.R;
import com.example.blanche.go4lunch.api.UserHelper;
import com.example.blanche.go4lunch.models.User;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import static com.example.blanche.go4lunch.utils.Utils.getCurrentUser;

public class SettingActivity extends BaseActivity {

    public static final String APP_PREFERENCES = "appPreferences";
    public static final String SWITCH_BUTTON_STATE = "switchButtonState";
    private SharedPreferences preferences;
    private Toolbar toolbar;
    private ActionBar actionBar;
    @BindView(R.id.profile_picture_settings)
    ImageView imageView;
    @BindView(R.id.user_name_settings)
    TextView userNameTextview;
    @BindView(R.id.user_mail_settings)
    TextView userMailTextview;
    @BindView(R.id.button_delete_account)
    Button deleteButton;
    @BindView(R.id.button_username) Button changeUsernameButton;
    @BindView(R.id.switch_button)
    Switch switchButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        preferences = getSharedPreferences(APP_PREFERENCES, MODE_PRIVATE);

        ButterKnife.bind(this);
        configureToolbar();
        configureSwitchButton();
        updateUIWithUserInfos();
    }


    private void configureToolbar() {
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();
        actionBar.setTitle(R.string.settings);

    }

    //---------------------
    //ACTIONS
    //-----------------------
    private void configureSwitchButton() {
        displaySwitchButtonState();
        switchButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked) {
                    preferences.edit().putInt(SWITCH_BUTTON_STATE, 0).apply();
                    UserHelper.updateNotificationBoolean(getCurrentUser().getUid(), true);
                } else {
                    preferences.edit().putInt(SWITCH_BUTTON_STATE, 1).apply();
                    UserHelper.updateNotificationBoolean(getCurrentUser().getUid(), false);
                }
            }
        });
    }

    private void displaySwitchButtonState() {
        if (preferences.getInt(SWITCH_BUTTON_STATE, -1) == 0) {
            switchButton.setChecked(true);
        } else {
            switchButton.setChecked(false);
        }
    }

    @OnClick(R.id.button_delete_account)
    public void deleteUserInFirebase() {
        new AlertDialog.Builder(this)
                .setMessage("Are you sure you want to delete your account?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        System.out.println("user id = " + getCurrentUser().getUid());

                        UserHelper.deleteUser(getCurrentUser().getUid()).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                System.out.println("user deleted in database!");
                                deleteUser();
                            }
                        });
                    }
                })
                .setNegativeButton("No", null)
                .show();
    }

    @OnClick(R.id.button_username)
    public void changeUsername() {
        final EditText editText = new EditText(this);
        new AlertDialog.Builder(this)
                .setMessage("Enter your new name:")
                .setView(editText)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        userNameTextview.setText(editText.getText());
                        UserHelper.updateUsername(editText.getText().toString(), getCurrentUser().getUid()).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(getApplicationContext(), "Updated!", Toast.LENGTH_SHORT).show();

                            }
                        });
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    //----------------------
    //METHODS
    //-----------------------
    private void deleteUser(){
        if (getCurrentUser() != null) {
            AuthUI.getInstance()
                    .delete(this)
                    .addOnSuccessListener(this, updateUIAfterRESTRequestsCompleted(DELETE_USER_TASK));
        }
    }

    //------------------
    //UI
    //------------------------
    private void updateUIWithUserInfos() {
        String photoUrl = null;
        if (getCurrentUser().getPhotoUrl() != null) {
            photoUrl = getCurrentUser().getPhotoUrl().toString();
        }
        setTextForUsername();
        userMailTextview.setText(getCurrentUser().getEmail());
        if (photoUrl != null) {
            Glide.with(this)
                    .load(photoUrl)
                    .apply(RequestOptions.circleCropTransform())
                    .into(imageView);
        }
    }

    private void setTextForUsername() {
        UserHelper.getUser(getCurrentUser().getUid()).addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                User user = documentSnapshot.toObject(User.class);
                userNameTextview.setText(user.getUsername());
            }
        });
    }



}
