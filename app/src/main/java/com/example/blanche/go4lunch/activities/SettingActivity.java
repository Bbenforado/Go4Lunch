package com.example.blanche.go4lunch.activities;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.blanche.go4lunch.R;
import com.example.blanche.go4lunch.api.UserHelper;
import com.example.blanche.go4lunch.models.User;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.messaging.FirebaseMessaging;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.example.blanche.go4lunch.utils.Utils.DELETE_USER_TASK;
import static com.example.blanche.go4lunch.utils.Utils.getCurrentUser;
import static com.example.blanche.go4lunch.utils.Utils.updateUIAfterRESTRequestsCompleted;

public class SettingActivity extends BaseActivity {

    public static final String APP_PREFERENCES = "appPreferences";
    public static final String SWITCH_BUTTON_STATE = "switchButtonState";
    private SharedPreferences preferences;
    //----------------------
    //BIND VIEWS
    //----------------------
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

    //--------------------
    //CONFIGURATION
    //--------------------
    private void configureToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(R.string.settings);

    }

    private void configureSwitchButton() {
        displaySwitchButtonState();
        switchButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked) {
                    preferences.edit().putInt(SWITCH_BUTTON_STATE, 0).apply();

                    FirebaseMessaging.getInstance().subscribeToTopic("restaurant")
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    String message = getApplicationContext().getString(R.string.enable_notif_toast_message);
                                    if (!task.isSuccessful()) {
                                        message = getApplicationContext().getString(R.string.error_occurred);
                                    }
                                    Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
                                }
                            });

                } else {
                    preferences.edit().putInt(SWITCH_BUTTON_STATE, 1).apply();
                    FirebaseMessaging.getInstance().unsubscribeFromTopic("restaurant")
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    String message = getApplicationContext().getString(R.string.disable_notif_toast_message);
                                    if (!task.isSuccessful()) {
                                        message = getApplicationContext().getString(R.string.error_occurred);
                                    }
                                    Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
                                }
                            });
                }
            }
        });
    }

    //---------------------
    //ACTIONS
    //-----------------------
    @OnClick(R.id.button_delete_account)
    public void deleteUserInFirebase() {
        new AlertDialog.Builder(this)
                .setMessage(R.string.dialog_delete_account)
                .setPositiveButton(R.string.dialog_positiv_button, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        UserHelper.deleteUser(getCurrentUser().getUid()).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Log.i("TAG", "user deleted in database!");
                                deleteUser();
                            }
                        });
                    }
                })
                .setNegativeButton(R.string.dialog_negativ_button, null)
                .show();
    }

    @OnClick(R.id.button_username)
    public void changeUsername() {
        final EditText editText = new EditText(this);
        new AlertDialog.Builder(this)
                .setMessage(R.string.dialog_change_username)
                .setView(editText)
                .setPositiveButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        userNameTextview.setText(editText.getText());
                        UserHelper.updateUsername(editText.getText().toString(), getCurrentUser().getUid()).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(getApplicationContext(), R.string.username_updated, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                })
                .setNegativeButton(R.string.dialog_cancel, null)
                .show();
    }

    //----------------------
    //METHODS
    //-----------------------
    private void deleteUser(){
        if (getCurrentUser() != null) {
            AuthUI.getInstance()
                    .delete(this)
                    .addOnSuccessListener(this, updateUIAfterRESTRequestsCompleted(DELETE_USER_TASK, this));
        }
    }

    //------------------
    //UI
    //------------------------
    /**
     * display the button s state, depending on if user enabled notifications or not
     */
    private void displaySwitchButtonState() {
        if (preferences.getInt(SWITCH_BUTTON_STATE, -1) == 0) {
            switchButton.setChecked(true);
        } else {
            switchButton.setChecked(false);
        }
    }

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
