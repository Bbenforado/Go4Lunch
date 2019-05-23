package com.example.blanche.go4lunch.activities;

import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.blanche.go4lunch.BaseActivity;
import com.example.blanche.go4lunch.R;
import com.example.blanche.go4lunch.adapters.RecyclerViewAdapterDetails;
import com.example.blanche.go4lunch.adapters.RecyclerViewAdapterThirdFragment;
import com.example.blanche.go4lunch.api.RestaurantPlaceHelper;
import com.example.blanche.go4lunch.api.UserHelper;
import com.example.blanche.go4lunch.models.RestaurantInformationObject;
import com.example.blanche.go4lunch.models.RestaurantInformations;
import com.example.blanche.go4lunch.models.RestaurantPlace;
import com.example.blanche.go4lunch.models.User;
import com.example.blanche.go4lunch.utils.RestaurantStreams;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import javax.annotation.Nullable;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableObserver;

import static com.example.blanche.go4lunch.utils.Utils.disposeWhenDestroy;
import static com.example.blanche.go4lunch.utils.Utils.getCurrentUser;

public class RestaurantDetailsActivity extends BaseActivity {

    public static final String RESTAURANT_ID = "idRestaurant";
    public static final String KEY_ACTIVITY = "keyActivity";
    public static final String APP_PREFERENCES = "appPreferences";
    public static final String TIME_WHEN_SAVED = "time";
    public static final String RESTAURANT_WEBSITE_URL = "url";
    private int keyActivity;
    boolean isButtonClicked;
    private User currentUser;
    private String name;
    private String adress;
    private String photoId;
    private String website;
    private String phoneNumber;
    private String restaurantId;
    private SharedPreferences preferences;
    private Disposable disposable;
    private RecyclerViewAdapterThirdFragment adapter;
    private List<String> users;
    private boolean isOpen;

    //-------------------
    //BIND VIEWS
    //------------------
    @BindView(R.id.textview_hasnt_chose_yet) TextView textViewDidntChose;
    @BindView(R.id.details_page_recycler_view) RecyclerView recyclerView;
    @BindView(R.id.floating_action_button) FloatingActionButton button;
    @BindView(R.id.call_button) ImageButton callButton;
    @BindView(R.id.like_button) ImageButton likeButton;
    @BindView(R.id.website_button) ImageButton websiteButton;
    @BindView(R.id.restaurant_name) TextView restaurantName;
    @BindView(R.id.main_backdrop) ImageView imageView;
    @BindView(R.id.textview_for_recyclerview) TextView textView;
    @BindView(R.id.type_of_food_and_adress) TextView typeOfFoodAndAdress;
    @BindView(R.id.textview_call) TextView textviewCall;
    @BindView(R.id.textview_website) TextView textviewWebsite;
    @BindView(R.id.textview_like) TextView textviewLike;
    @BindView(R.id.bar) ProgressBar bar;
    @BindView(R.id.star_one) ImageView starOne;
    @BindView(R.id.star_two) ImageView starTwo;
    @BindView(R.id.star_three) ImageView starThree;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restaurant_details);
        ButterKnife.bind(this);
        System.out.println("on create details");
        //get the preferences
        preferences = getSharedPreferences(APP_PREFERENCES, MODE_PRIVATE);
        keyActivity = preferences.getInt(KEY_ACTIVITY, -1);

        //get the data from map fragment 0
        // from list fragment 1
        // or from navigation drawer 2
        if (keyActivity == 0) {
            restaurantId = preferences.getString(RESTAURANT_ID, null);
            requestForInformations(restaurantId);
        } else if (keyActivity == 2) {
            button.setVisibility(View.GONE);
            getCurrentUserDataFromFireBase();
        }else {
            restaurantId = getIntent().getExtras().getString(RESTAURANT_ID);
            requestForInformations(restaurantId);

        }
        //setStars(restaurantId, starOne, starTwo, starThree);
        //displayLikeButton();

        if (keyActivity == 0 || keyActivity == 1) {
            configureRecyclerView();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        preferences.edit().putInt(KEY_ACTIVITY, -1).apply();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        disposeWhenDestroy(disposable);
    }

    //-----------------------
    //CONFIGURATION
    //--------------------------------
    private void configureRecyclerView() {
        users = new ArrayList<>();
        Query query = UserHelper.getUsersCollection().whereEqualTo("restaurantId", restaurantId);
        query.addSnapshotListener(new EventListener<QuerySnapshot>() {
                                      @Override
                                      public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                                          if (e != null) {
                                              Log.w("TAG", "Listen failed", e);
                                              return;
                                          }

                                          for (DocumentSnapshot doc : queryDocumentSnapshots) {
                                              if (doc.get("restaurantId") != null) {
                                                      users.add(doc.getString("chosenRestaurant"));
                                                  System.out.println("it s here");
                                                      System.out.println("users in function = " + users);
                                                      textView.setVisibility(View.GONE);
                                              }
                                          }
                                      }
                                  });


        adapter = new RecyclerViewAdapterThirdFragment(generateOptionsForAdapter(query),
                        Glide.with(this));
        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                recyclerView.smoothScrollToPosition(adapter.getItemCount()); // Scroll to bottom on new messages
            }
        });
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    private FirestoreRecyclerOptions<User> generateOptionsForAdapter(Query query) {
        return new FirestoreRecyclerOptions.Builder<User>()
                .setQuery(query, User.class)
                .setLifecycleOwner(this)
                .build();
    }
    //--------------------------
    //REQUEST
    //-----------------------------
    private void requestForInformations(String placeId) {
        bar.setVisibility(View.VISIBLE);
        disposable =
                RestaurantStreams.streamFetchRestaurantInfos(placeId, "AIzaSyA6Jk5Xl1MbXbYcfWywZ0vwUY2Ux4KLta4")
                        .subscribeWith(new DisposableObserver<RestaurantInformationObject>() {

                            @Override
                            public void onNext(RestaurantInformationObject restaurantInformationObject) {
                                RestaurantInformations infos = restaurantInformationObject.getResult();
                                website = infos.getWebsite();
                                name = infos.getName();
                                adress = infos.getVicinity();
                                phoneNumber = infos.getFormattedPhoneNumber();
                                if (infos.getPhotos() != null) {
                                    photoId = infos.getPhotos().get(0).getPhotoReference();
                                }
                                isOpen = infos.getOpeningHours().getOpenNow();
                                displayRestaurantInformations();
                            }

                            @Override
                            public void onError(Throwable e) {

                            }

                            @Override
                            public void onComplete() {
                            }
                        });
    }
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
            //if (preferences.getInt(KEY_ACTIVITY, -1) != 0) {
                //we save at what time the user chose the restaurant
                saveTimeWhenChoseRestaurant();

                //we update the name of the restaurant in firebase
                UserHelper.updateUserChosenRestaurant(userUid, true, name, adress, phoneNumber, website, photoId, restaurantId);

                //we display toast message to user
                Toast.makeText(this, this.getString(R.string.toast_text_when_user_chose_restaurant) + name + " !", Toast.LENGTH_SHORT).show();
        } else {
            //unclick button
            //update the name of the restaurant in firebase
            UserHelper.updateUserChosenRestaurant(userUid, false, null, null, null, null, null, restaurantId);
            //change button color
            button.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorAccent)));
            //display message to user
            Toast.makeText(this, this.getString(R.string.toast_text_when_user_uncheck_chose_button), Toast.LENGTH_SHORT).show();
        }
    }

    @OnClick(R.id.call_button)
    public void callRestaurant(View v) {
        //get the restaurant number, and displays the call tool of the phone
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel:" + phoneNumber));
        startActivity(intent);
    }

    @OnClick(R.id.like_button)
    public void likeRestaurant(View v) {

        final AlertDialog.Builder dialog = new AlertDialog.Builder(this);

        LayoutInflater inflater = getLayoutInflater();
        View dialogLayout = inflater.inflate(R.layout.dialog_ratingbar, null);
        final RatingBar ratingBar = dialogLayout.findViewById(R.id.ratingBar);
        ratingBar.setNumStars(3);
        ratingBar.setStepSize(1);

        dialog.setMessage("How much?")
                .setView(dialogLayout)
                .setPositiveButton("Done", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Toast.makeText(v.getContext(), "Rating is " + ratingBar.getRating(), Toast.LENGTH_SHORT).show();
                        RestaurantPlaceHelper.getRestaurantPlace(restaurantId).addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                RestaurantPlace restaurantPlace = documentSnapshot.toObject(RestaurantPlace.class);

                                List<Map<String, Integer>> list = new ArrayList<>();

                                //if the list already exits
                                if (restaurantPlace.getUsersWhoLiked2() != null) {

                                    boolean contains = false;
                                    System.out.println("list is not empty");
                                    //we get the list
                                    list = restaurantPlace.getUsersWhoLiked2();
                                    //we get the size of the list
                                    int size = restaurantPlace.getUsersWhoLiked2().size();

                                    //boucle sur la taille
                                    for (int i = 0; i<size; i++) {
                                        System.out.println("boucle");
                                        System.out.println("bool = " + contains);
                                        //check if one entry of the list contains the users id
                                        if (restaurantPlace.getUsersWhoLiked2().get(i).containsKey(getCurrentUser().getUid())) {
                                            contains = true;
                                            System.out.println("it contains");
                                            Map<String, Integer> userRate = new HashMap<>();
                                            int rating = (int) ratingBar.getRating();
                                            userRate.put(getCurrentUser().getUid(), rating);

                                            //we get the entry with the users id and set the new value to it
                                            list.get(i).put(getCurrentUser().getUid(), rating);

                                            //we update the list with this new list
                                            RestaurantPlaceHelper.updateUserWhoLiked2(restaurantId, list).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    System.out.println("success");
                                                }
                                            });
                                        }
                                    }
                                    //if it contains
                                    //if (contains) {
                                        /*System.out.println("it contains");
                                        Map<String, Integer> userRate = new HashMap<>();
                                        int rating = (int) ratingBar.getRating();
                                        userRate.put(getCurrentUser().getUid(), rating);

                                        //we get the entry with the users id and set the new value to it
                                        list.get().put(getCurrentUser().getUid(), rating);

                                        //we update the list with this new list
                                        RestaurantPlaceHelper.updateUserWhoLiked2(restaurantId, list).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                System.out.println("success");
                                            }
                                        });*/
                                        //contains = false;
                                    //} else {
                                    if (!contains) {
                                        System.out.println("it doesn't contains");
                                        //we create a map
                                        Map<String, Integer> userRate = new HashMap<>();
                                        int rating = (int) ratingBar.getRating();
                                        userRate.put(getCurrentUser().getUid(), rating);
                                        //we add it to the list
                                        list.add(userRate);
                                        //we update the list with the new list
                                        RestaurantPlaceHelper.updateUserWhoLiked2(restaurantId, list).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                System.out.println("success");
                                            }
                                        });
                                    }
                                }
                                /*Map<List<String>, List<Integer>> mapLists = new HashMap<>();
                                List<String> usersId = new ArrayList<>();
                                List<Integer> ratings = new ArrayList<>();
                                if (restaurantPlace.getUsersWhoLiked2() != null) {
                                    mapLists = restaurantPlace.getUsersWhoLiked2();
                                }*/

                                //else if the list is empty
                                 else {
                                    System.out.println("list is empty");
                                    //we create the map
                                    Map<String, Integer> userRate = new HashMap<>();
                                    int rating = (int) ratingBar.getRating();
                                    userRate.put(getCurrentUser().getUid(), rating);
                                    //we add it to the list
                                    list.add(userRate);
                                    //we update the list with new list
                                    RestaurantPlaceHelper.updateUserWhoLiked2(restaurantId, list).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            System.out.println("success");
                                        }
                                    });
                                }
                            }
                        });
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }



    @OnClick(R.id.website_button)
    public void openRestaurantWebsite(View v) {
        //get the website of the restaurant and open it in a webview
        Bundle bundle = new Bundle();
        bundle.putString(RESTAURANT_WEBSITE_URL, website);
        Intent webviewActivity = new Intent(this, WebviewActivity.class);
        webviewActivity.putExtras(bundle);
        startActivity(webviewActivity);
    }

    //-------------------
    //UPDATE UI
    //------------------------
    private void displayRestaurantInformations() {
        bar.setVisibility(View.GONE);
        //if key activity == 0, it means this is coming from the navigation drawer
        if (name != null && adress != null && photoId != null) {
            String url = "https://maps.googleapis.com/maps/api/place/photo?maxwidth=400&photoreference=" + photoId + "&key=AIzaSyA6Jk5Xl1MbXbYcfWywZ0vwUY2Ux4KLta4";
            setRestaurantInformations(name, adress, url);
            displayButton(phoneNumber, callButton, textviewCall, R.drawable.ic_phone_disabled);
            displayButton(website, websiteButton, textviewWebsite, R.drawable.ic_website_disabled);
            if (keyActivity == 0 || keyActivity == 1) {
                getCurrentUserDataFromFireBase();
            }
        }
    }

    private void displayLikeButton() {
        RestaurantPlaceHelper.getRestaurantPlace(restaurantId).addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                RestaurantPlace restaurantPlace = documentSnapshot.toObject(RestaurantPlace.class);
                if (restaurantPlace.getUsersWhoLiked2() != null) {
                    if (!restaurantPlace.getUsersWhoLiked2().contains(getCurrentUser().getUid())) {
                        likeButton.setEnabled(true);
                        textviewLike.setTextColor(Color.parseColor("#000000"));
                        likeButton.setBackgroundResource(R.drawable.ic_like);
                    } else {
                        displayButton(null, likeButton, textviewLike, R.drawable.ic_like_disabled);
                    }
                }
            }
        });
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

    private void displayButton(String info, ImageButton imageButton, TextView textView, int drawable) {
        if (info == null) {
            imageButton.setEnabled(false);
            textView.setTextColor(Color.parseColor("#bcbcbc"));
            imageButton.setBackgroundResource(drawable);
        }
    }

    private void setRestaurantInformations(String name, String adress, String url) {
        restaurantName.setText(name);
        typeOfFoodAndAdress.setText(adress);
        Glide.with(this)
                .load(url)
                .apply(RequestOptions.noTransformation())
                .into(imageView);
    }

    //--------------------
    //GET DATA
    //----------------------
    private void getCurrentUserDataFromFireBase() {
        UserHelper.getUser(getCurrentUser().getUid()).addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                currentUser = documentSnapshot.toObject(User.class);
                if (keyActivity == 0 || keyActivity == 1) {
                    //if restaurant is open, we display the button, color depending on if user chose this place to eat or not
                    if (isOpen) {
                        if (currentUser.isHasChosenRestaurant()) {
                            displayColorButton(currentUser);
                        }
                    } else {
                        //if it s close, user can't chose the restaurant
                        button.setVisibility(View.GONE);
                    }
                } else if (keyActivity == 2) {
                    if (currentUser.isHasChosenRestaurant()) {
                        restaurantId = currentUser.getRestaurantId();
                        requestForInformations(currentUser.getRestaurantId());
                        configureRecyclerView();
                    }
                }
            }
        });
    }

    //----------------------
    //METHODS
    //--------------------------
    private void saveTimeWhenChoseRestaurant() {
        TimeZone timeZone = TimeZone.getDefault();
        long timeWhenSaved = Calendar.getInstance().getTimeInMillis();
        timeWhenSaved = timeWhenSaved + timeZone.getDSTSavings();
        preferences.edit().putLong(TIME_WHEN_SAVED, timeWhenSaved).apply();
    }
}
