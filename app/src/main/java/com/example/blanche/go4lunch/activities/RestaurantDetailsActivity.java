package com.example.blanche.go4lunch.activities;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.net.Uri;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import android.os.Bundle;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.blanche.go4lunch.BuildConfig;
import com.example.blanche.go4lunch.R;
import com.example.blanche.go4lunch.adapters.RecyclerViewAdapterThirdFragment;
import com.example.blanche.go4lunch.api.RestaurantPlaceHelper;
import com.example.blanche.go4lunch.api.UserHelper;
import com.example.blanche.go4lunch.models.RestaurantInformationObject;
import com.example.blanche.go4lunch.models.RestaurantInformations;
import com.example.blanche.go4lunch.models.RestaurantPlace;
import com.example.blanche.go4lunch.models.User;
import com.example.blanche.go4lunch.utils.RestaurantStreams;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
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
import static com.example.blanche.go4lunch.utils.Utils.setStars;

public class RestaurantDetailsActivity extends BaseActivity {

    public static final String RESTAURANT_ID = "idRestaurant";
    public static final String KEY_ACTIVITY = "keyActivity";
    public static final String APP_PREFERENCES = "appPreferences";
    public static final String TIME_WHEN_SAVED = "time";
    public static final String REST_ID = "restId";
    private int keyActivity;
    private boolean isButtonClicked;
    private User currentUser;
    private String name;
    private String address;
    private String photoId;
    private String website;
    private String phoneNumber;
    private String restaurantId;
    private SharedPreferences preferences;
    private Disposable disposable;
    private RecyclerViewAdapterThirdFragment adapter;
    private List<String> users;
    private boolean isOpen;
    private String apikey;

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


    @SuppressLint("RestrictedApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restaurant_details);
        ButterKnife.bind(this);
        apikey = BuildConfig.ApiKey;
        //get the preferences
        preferences = getSharedPreferences(APP_PREFERENCES, MODE_PRIVATE);
        keyActivity = preferences.getInt(KEY_ACTIVITY, -1);

        //get the data from map fragment 0
        // from list fragment 1
        // or from navigation drawer 2
        if (keyActivity == 0) {
            restaurantId = preferences.getString(RESTAURANT_ID, null);
            requestForInformations(restaurantId);
            setStars(restaurantId, starOne, starTwo, starThree);
        } else if (keyActivity == 2) {
            button.setVisibility(View.GONE);
            getCurrentUserDataFromFireBase();
        }else if (keyActivity == 3) {
            restaurantId = getIntent().getExtras().getString(REST_ID);
            requestForInformations(restaurantId);
            setStars(restaurantId, starOne, starTwo, starThree);
        } else {
            restaurantId = getIntent().getExtras().getString(RESTAURANT_ID);
            requestForInformations(restaurantId);
            setStars(restaurantId, starOne, starTwo, starThree);
        }

        if (keyActivity == 0 || keyActivity == 1 || keyActivity == 3) {
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
                                                  users = new ArrayList<>();
                                                      users.add(doc.getString("chosenRestaurant"));
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
    /**
     * get information about the restaurant
     * @param placeId the id of the restaurant
     */
    private void requestForInformations(String placeId) {
        bar.setVisibility(View.VISIBLE);
        disposable =
                RestaurantStreams.streamFetchRestaurantInfos(placeId, apikey)
                        .subscribeWith(new DisposableObserver<RestaurantInformationObject>() {

                            @Override
                            public void onNext(RestaurantInformationObject restaurantInformationObject) {
                                RestaurantInformations infos = restaurantInformationObject.getResult();
                                website = infos.getWebsite();
                                name = infos.getName();
                                address = infos.getVicinity();
                                phoneNumber = infos.getFormattedPhoneNumber();
                                if (infos.getPhotos() != null) {
                                    photoId = infos.getPhotos().get(0).getPhotoReference();
                                }
                                if (infos.getOpeningHours() != null) {
                                    if (infos.getOpeningHours().getOpenNow() != null) {
                                        isOpen = infos.getOpeningHours().getOpenNow();
                                    }
                                }
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
        if (isButtonClicked) {
            //change the color of the button
            button.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorPrimaryDark)));
            //we save at what time the user chose the restaurant
            saveTimeWhenChoseRestaurant();
            UserHelper.updateUserChosenRestaurant(userUid, true, name, address, phoneNumber, website, photoId, restaurantId);
            Toast.makeText(this, this.getString(R.string.toast_text_when_user_chose_restaurant) + name + " !", Toast.LENGTH_SHORT).show();
        } else {
            //unclick button
            UserHelper.updateUserChosenRestaurant(userUid, false, null, null, null, null, null, null);
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
        displayDialog();
    }

    @OnClick(R.id.website_button)
    public void openRestaurantWebsite(View v) {
        if (!website.startsWith("http://") && !website.startsWith("https://")) {
            website = "http://" + website;
        }
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(website));
        startActivity(browserIntent);
    }

    //-------------------
    //UPDATE UI
    //------------------------
    /**
     * display a dialog with a rating bar
     * save the user s rate
     * display the new rate
     */
    private void displayDialog() {
        final AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogLayout = inflater.inflate(R.layout.dialog_ratingbar, null);
        final RatingBar ratingBar = dialogLayout.findViewById(R.id.ratingBar);
        ratingBar.setNumStars(3);
        ratingBar.setStepSize(1);

        dialog.setMessage(getString(R.string.rate_restaurant))
                .setView(dialogLayout)
                .setPositiveButton(getString(R.string.dialog_ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        RestaurantPlaceHelper.getRestaurantPlace(restaurantId).addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                RestaurantPlace restaurantPlace = documentSnapshot.toObject(RestaurantPlace.class);

                                List<Map<String, Integer>> list = new ArrayList<>();
                                List<Integer> listOfRates = new ArrayList<>();
                                //we get the rate given by user
                                int rating = (int) ratingBar.getRating();

                                //if the list already exits
                                if (restaurantPlace.getUsersWhoLiked2() != null) {
                                    boolean contains = false;
                                    //we get the list
                                    list = restaurantPlace.getUsersWhoLiked2();
                                    //we get the size of the list
                                    int size = restaurantPlace.getUsersWhoLiked2().size();

                                    //loop on the size
                                    for (int i = 0; i<size; i++) {
                                        //check if one entry of the list contains the users id
                                        if (restaurantPlace.getUsersWhoLiked2().get(i).containsKey(getCurrentUser().getUid())) {
                                            contains = true;
                                            //we get the entry with the users id and set the new value to it
                                            list.get(i).put(getCurrentUser().getUid(), rating);

                                            //we update the list with this new list
                                            RestaurantPlaceHelper.updateUserWhoLiked2(restaurantId, list).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    Log.i("TAG", "Success");
                                                }
                                            });
                                        }
                                    }
                                    //if the user never rated the restaurant
                                    if (!contains) {
                                        saveUserRateAndUpdateDatabase(rating, list);
                                    }
                                    //else if the list is empty
                                } else {
                                    saveUserRateAndUpdateDatabase(rating, list);
                                }
                                //get the list of rates
                                int size = list.size();
                                for (int i = 0; i<size; i++) {
                                    Map<String, Integer> map = list.get(i);
                                    for (Map.Entry<String, Integer> entry : map.entrySet()) {
                                        listOfRates.add(entry.getValue());
                                    }
                                }
                                //calculate the final rate and save it here
                                float finalRate = getFinalRate(listOfRates);
                                RestaurantPlaceHelper.updateRestaurantLike(restaurantId, finalRate).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Log.i("TAG", "UPDATED!");
                                        setStars(restaurantId, starOne, starTwo, starThree);
                                    }
                                });
                            }
                        });
                    }
                })
                .setNegativeButton(getString(R.string.dialog_cancel), null)
                .show();
    }

    private void displayRestaurantInformations() {
        bar.setVisibility(View.GONE);
        if (name != null && address != null && photoId != null) {
            String url = "https://maps.googleapis.com/maps/api/place/photo?maxwidth=400&photoreference=" + photoId + "&key=" + apikey;
            setRestaurantInformations(name, address, url);
            displayButton(phoneNumber, callButton, textviewCall, R.drawable.ic_phone_disabled);
            displayButton(website, websiteButton, textviewWebsite, R.drawable.ic_website_disabled);
            if (keyActivity == 0 || keyActivity == 1 || keyActivity == 3) {
                getCurrentUserDataFromFireBase();
            }
        }
    }

    /**
     * display the color of the choice button depending on user's data
     * @param user the current user
     */
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

    /**
     * set and enable the button depending on information we ve got about the restaurant
     * @param info number or website of the restaurant
     * @param imageButton the image button for call or website
     * @param textView textView below the imageButton
     * @param drawable the drawable for call or website button
     */
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
    /**
     * get data from the current user
     */
    private void getCurrentUserDataFromFireBase() {
        UserHelper.getUser(getCurrentUser().getUid()).addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @SuppressLint("RestrictedApi")
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                currentUser = documentSnapshot.toObject(User.class);

                if (keyActivity == 0 || keyActivity == 1 || keyActivity == 3) {
                    //if restaurant is open, we display the button, color depending on if user chose this place to eat or not
                    if (isOpen) {
                        if (currentUser.isHasChosenRestaurant()) {
                            displayColorButton(currentUser);
                        }
                    } else {
                        //if it s close, user can't chose the restaurant, so we don't display the button
                        button.setVisibility(View.GONE);
                    }
                } else if (keyActivity == 2) {
                    if (currentUser.isHasChosenRestaurant()) {
                        restaurantId = currentUser.getRestaurantId();
                        requestForInformations(currentUser.getRestaurantId());
                        configureRecyclerView();
                        setStars(restaurantId, starOne, starTwo, starThree);
                    }
                }
            }
        });
    }

    //----------------------
    //METHODS
    //--------------------------
    /**
     * add the current user's rate to the restaurant's list of rates
     * @param rating rate given by the current user
     * @param list the list of map that contains id of user who rated and the rate he gave
     */
    private void saveUserRateAndUpdateDatabase(int rating, List<Map<String, Integer>> list) {
        //we create the map
        Map<String, Integer> userRate = new HashMap<>();
        //int rating = (int) ratingBar.getRating();
        userRate.put(getCurrentUser().getUid(), rating);
        //we add it to the list
        list.add(userRate);
        //we update the list with new list
        RestaurantPlaceHelper.updateUserWhoLiked2(restaurantId, list).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.i("TAG", "Success");
            }
        });
    }

    /**
     * get the total rate for a restaurant
     * @param listRates the list of rates given by users
     * @return the final rate
     */
    private float getFinalRate(List<Integer> listRates) {
        float result = 0;
        for (int j = 0; j<listRates.size(); j++) {
            result = result + listRates.get(j);
        }
        float finalRate = result/listRates.size();
        return finalRate;
    }

    /**
     * save when the user chose a restaurant
     */
    private void saveTimeWhenChoseRestaurant() {
        TimeZone timeZone = TimeZone.getDefault();
        long timeWhenSaved = Calendar.getInstance().getTimeInMillis();
        timeWhenSaved = timeWhenSaved + timeZone.getDSTSavings();
        preferences.edit().putLong(TIME_WHEN_SAVED, timeWhenSaved).apply();
    }
}
