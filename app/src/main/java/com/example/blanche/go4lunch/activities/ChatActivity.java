package com.example.blanche.go4lunch.activities;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.provider.MediaStore;
import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.textfield.TextInputEditText;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;

import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.blanche.go4lunch.R;
import com.example.blanche.go4lunch.adapters.ChatAdapter;
import com.example.blanche.go4lunch.api.MessageHelper;
import com.example.blanche.go4lunch.api.UserHelper;
import com.example.blanche.go4lunch.models.Message;
import com.example.blanche.go4lunch.models.User;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.pes.androidmaterialcolorpickerdialog.ColorPicker;
import com.pes.androidmaterialcolorpickerdialog.ColorPickerCallback;

import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

import static com.example.blanche.go4lunch.utils.Utils.getCurrentUser;

public class ChatActivity extends AppCompatActivity implements ChatAdapter.Listener {

    //--------------------
    //BIND VIEWS
    //----------------------
    @BindView(R.id.activity_chat_recycler_view)
    RecyclerView recyclerView;
    @BindView(R.id.activity_chat_text_view_recycler_view_empty)
    TextView textViewRecyclerViewEmpty;
    @BindView(R.id.activity_chat_message_edit_text)
    TextInputEditText editTextMessage;
    @BindView(R.id.activity_chat_image_chosen_preview)
    ImageView imageViewPreview;
    @Nullable
    @BindView(R.id.activity_chat_item_message_container_image_sent_cardview)
    CardView cardView;
    @BindView(R.id.relative_layout_chat_global)
    RelativeLayout relativeLayout;

    //---------------------------
    //
    //------------------------------
    private ChatAdapter chatAdapter;
    @Nullable
    private User modelCurrentUser;
    private String currentChatName;
    private Uri uriImage;
    private static final String CHAT_NAME_ANDROID = "android";
    private static final String PERMS = Manifest.permission.READ_EXTERNAL_STORAGE;
    private static final int RC_IMAGE_PERMS = 100;
    public static final int RC_CHOOSE_PHOTO = 200;
    public static final String COLOR_FOR_CHAT_BACKGROUND_R = "color_r";
    public static final String COLOR_FOR_CHAT_BACKGROUND_G = "color_g";
    public static final String COLOR_FOR_CHAT_BACKGROUND_B = "color_b";
    public static final String COLOR_FOR_CHAT_BACKGROUND = "color";
    public static final String APP_PREFERENCES = "appPreferences";
    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        ButterKnife.bind(this);
        preferences = getSharedPreferences(APP_PREFERENCES, MODE_PRIVATE);
        configureToolbar();
        configureRecyclerView(CHAT_NAME_ANDROID);
        getCurrentUserFromFirestore();
        displayChatColor();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        this.handleResponse(requestCode, resultCode, data);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_toolbar_chat, menu);
        return super.onCreateOptionsMenu(menu);
    }

    //-----------------------
    //CONFIGURATION
    //-----------------------
    private void configureToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle("Chat");
    }

    private void configureRecyclerView(String chatName) {
        currentChatName = chatName;
        //Configure Adapter & RecyclerView
        chatAdapter = new ChatAdapter(generateOptionsForAdapter(MessageHelper.getAllMessageForChat(currentChatName)), Glide.with(this), this, getCurrentUser().getUid());
        chatAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                recyclerView.smoothScrollToPosition(chatAdapter.getItemCount()); // Scroll to bottom on new messages
            }
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(chatAdapter);
    }

    private void displayChatColor() {
        if (preferences.getInt(COLOR_FOR_CHAT_BACKGROUND, -1) != -1) {
            relativeLayout.setBackgroundColor(preferences.getInt(COLOR_FOR_CHAT_BACKGROUND, -1));
        } else {
            relativeLayout.setBackgroundColor(getResources().getColor(R.color.default_chat_color));
        }
    }

    // --------------------
    // ACTIONS
    // --------------------
    /**
     * display color picker dialog
     * that allows the user to choose a color for the background of the chat activity
     * @param item color picker icon
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int defaultColorR;
        int defaultColorG;
        int defaultColorB;
        switch (item.getItemId()) {
            case R.id.search_item:
                if (preferences.getInt(COLOR_FOR_CHAT_BACKGROUND_R,-1) != -1 &&
                        preferences.getInt(COLOR_FOR_CHAT_BACKGROUND_G, -1) != -1 &&
                        preferences.getInt(COLOR_FOR_CHAT_BACKGROUND_B, -1) != -1) {
                    defaultColorR = preferences.getInt(COLOR_FOR_CHAT_BACKGROUND_R, -1);
                    defaultColorG = preferences.getInt(COLOR_FOR_CHAT_BACKGROUND_G, -1);
                    defaultColorB = preferences.getInt(COLOR_FOR_CHAT_BACKGROUND_B, -1);
                } else {
                    defaultColorR = 234;
                    defaultColorG = 239;
                    defaultColorB = 242;
                }
                final ColorPicker colorPicker = new ColorPicker(this, defaultColorR, defaultColorG, defaultColorB);

                colorPicker.show();
                colorPicker.enableAutoClose();
                colorPicker.setCallback(new ColorPickerCallback() {
                    @Override
                    public void onColorChosen(@ColorInt int color) {
                        preferences.edit().putInt(COLOR_FOR_CHAT_BACKGROUND_R, Color.red(color)).apply();
                        preferences.edit().putInt(COLOR_FOR_CHAT_BACKGROUND_G, Color.green(color)).apply();
                        preferences.edit().putInt(COLOR_FOR_CHAT_BACKGROUND_B, Color.blue(color)).apply();
                        preferences.edit().putInt(COLOR_FOR_CHAT_BACKGROUND, color).apply();

                        relativeLayout.setBackgroundColor(color);

                        // If the auto-dismiss option is not enable (disabled as default) you have to manually dimiss the dialog
                        // cp.dismiss();
                    }
                });
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * send the message
     * user can send message with image or without
     */
    @OnClick(R.id.activity_chat_send_button)
    public void onClickSendMessage() {
        if (TextUtils.isEmpty(editTextMessage.getText()) && modelCurrentUser != null && imageViewPreview.getDrawable() != null) {
            Toast.makeText(this, getString(R.string.message_missing), Toast.LENGTH_SHORT).show();
        }
        //Check if text field is not empty and current user properly downloaded from Firestore
        if (!TextUtils.isEmpty(editTextMessage.getText()) && modelCurrentUser != null) {

            if (imageViewPreview.getDrawable() == null) {
                //Create a new Message to Firestore
                MessageHelper.createMessageForChat(editTextMessage.getText().toString(), this.currentChatName, modelCurrentUser).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        System.out.println("error");
                    }
                });
                //Reset text field
                this.editTextMessage.setText("");
            } else {
                uploadPhotoInFireBaseAndSendMessage(editTextMessage.getText().toString());
                editTextMessage.setText("");
                imageViewPreview.setImageDrawable(null);
            }
        }
    }

    /**
     * user can pick an image from the phone
     */
    @OnClick(R.id.activity_chat_add_file_button)
    @AfterPermissionGranted(RC_IMAGE_PERMS)
    public void onClickAddFile() {
        chooseImageFromPhone();
    }

    // --------------------
    // REST REQUESTS
    // --------------------
    private void getCurrentUserFromFirestore() {
        UserHelper.getUser(getCurrentUser().getUid()).addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                modelCurrentUser = documentSnapshot.toObject(User.class);
            }
        });
    }

    /**
     * store the image in firebase and create the message with image
     * @param message text written by user
     */
    private void uploadPhotoInFireBaseAndSendMessage (final String message) {
        //generate unique string
        String uuid = UUID.randomUUID().toString();

        StorageReference imageRef = FirebaseStorage.getInstance().getReference(uuid);
        imageRef.putFile(uriImage).addOnSuccessListener(this, new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Task<Uri> task = taskSnapshot.getMetadata().getReference().getDownloadUrl();
                task.addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        Uri uri = task.getResult();
                        String pathImageSavedInFirebase = uri.toString();

                        MessageHelper.createMessageWithImageForChat(pathImageSavedInFirebase, message, currentChatName, modelCurrentUser)
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        System.out.println("error");
                                    }
                                });
                    }
                });
            }
        });
    }

    private FirestoreRecyclerOptions<Message> generateOptionsForAdapter(Query query) {
        return new FirestoreRecyclerOptions.Builder<Message>()
                .setQuery(query, Message.class)
                .setLifecycleOwner(this)
                .build();
    }

    // --------------------
    //
    // -------------------
    private void handleResponse(int requestCode, int resultCode, Intent data) {
        if (requestCode == RC_CHOOSE_PHOTO) {
            if (resultCode == RESULT_OK) {
                uriImage = data.getData();
                Glide.with(this)
                        .load(uriImage)
                        .apply(RequestOptions.circleCropTransform())
                        .into(imageViewPreview);
            }
        }
    }

    private void chooseImageFromPhone() {
        if (!EasyPermissions.hasPermissions(this, PERMS)) {
            EasyPermissions.requestPermissions(this, getString(R.string.popup_title_permission_files_access),
                    RC_IMAGE_PERMS, PERMS);
            return;
        }
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, RC_CHOOSE_PHOTO);
    }

    @Override
    public void onDataChanged() {
        //Show TextView in case RecyclerView is empty
        textViewRecyclerViewEmpty.setVisibility(this.chatAdapter.getItemCount() == 0 ? View.VISIBLE : View.GONE);
    }

}
