package com.example.blanche.go4lunch.activities;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.blanche.go4lunch.R;
import com.example.blanche.go4lunch.adapters.ChatAdapter;
import com.example.blanche.go4lunch.api.MessageHelper;
import com.example.blanche.go4lunch.api.UserHelper;
import com.example.blanche.go4lunch.fragments.ImageFragment;
import com.example.blanche.go4lunch.models.Message;
import com.example.blanche.go4lunch.models.User;
import com.example.blanche.go4lunch.utils.ItemClickSupport;
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
import io.reactivex.internal.operators.completable.CompletableObserveOn;
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
    private ImageFragment imageFragment;
    private Toolbar toolbar;
    int defaultColorR;
    int defaultColorG;
    int defaultColorB;
    int selectedColorRGB;
    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        ButterKnife.bind(this);
        preferences = getSharedPreferences(APP_PREFERENCES, MODE_PRIVATE);
        configureToolbar();
        this.configureRecyclerView(CHAT_NAME_ANDROID);
        this.getCurrentUserFromFirestore();
        displayChatColor();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // 2 - Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        this.handleResponse(requestCode, resultCode, data);
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
    @OnClick(R.id.activity_chat_send_button)
    public void onClickSendMessage() {
        // 1 - Check if text field is not empty and current user properly downloaded from Firestore
        if (!TextUtils.isEmpty(editTextMessage.getText()) && modelCurrentUser != null){

            if (imageViewPreview.getDrawable() == null) {
                // 2 - Create a new Message to Firestore
                MessageHelper.createMessageForChat(editTextMessage.getText().toString(), this.currentChatName, modelCurrentUser).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        System.out.println("error");
                    }
                });
                // 3 - Reset text field
                this.editTextMessage.setText("");
            } else {
                uploadPhotoInFireBaseAndSendMessage(editTextMessage.getText().toString());
                editTextMessage.setText("");
                imageViewPreview.setImageDrawable(null);
            }
        }
    }


    @OnClick(R.id.activity_chat_add_file_button)
    @AfterPermissionGranted(RC_IMAGE_PERMS)
    public void onClickAddFile() {
        chooseImageFromPhone();
    }




    private void showFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.content, imageFragment)
                .commit();
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
                        //String pathImageSavedInFirebase = taskSnapshot.getMetadata().getReference().getDownloadUrl().toString();

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

    // --------------------
    // UI
    // --------------------
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_toolbar_chat, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
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
                final ColorPicker cp = new ColorPicker(this, defaultColorR, defaultColorG, defaultColorB);

                cp.show();

                cp.enableAutoClose(); // Enable auto-dismiss for the dialog

                /* Set a new Listener called when user click "select" */
                cp.setCallback(new ColorPickerCallback() {
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





    private void configureRecyclerView(String chatName) {
        //Track current chat name
        this.currentChatName = chatName;
        //Configure Adapter & RecyclerView
        this.chatAdapter = new ChatAdapter(generateOptionsForAdapter(MessageHelper.getAllMessageForChat(this.currentChatName)), Glide.with(this), this, getCurrentUser().getUid());
        chatAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                recyclerView.smoothScrollToPosition(chatAdapter.getItemCount()); // Scroll to bottom on new messages
            }
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(this.chatAdapter);
    }

    // 6 - Create options for RecyclerView from a Query
    private FirestoreRecyclerOptions<Message> generateOptionsForAdapter(Query query) {
        return new FirestoreRecyclerOptions.Builder<Message>()
                .setQuery(query, Message.class)
                .setLifecycleOwner(this)
                .build();
    }

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

    // --------------------
    // CALLBACK
    // --------------------

    @Override
    public void onDataChanged() {
        // 7 - Show TextView in case RecyclerView is empty
        textViewRecyclerViewEmpty.setVisibility(this.chatAdapter.getItemCount() == 0 ? View.VISIBLE : View.GONE);
    }

    //------------------
    private void configureToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //get a support actionbar corresponding to this toolbar
        ActionBar actionBar = getSupportActionBar();
        //enable the up button
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle("Chat");
    }
}
