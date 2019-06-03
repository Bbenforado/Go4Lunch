package com.example.blanche.go4lunch.adapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.request.RequestOptions;
import com.example.blanche.go4lunch.BuildConfig;
import com.example.blanche.go4lunch.R;
import com.example.blanche.go4lunch.api.UserHelper;
import com.example.blanche.go4lunch.models.RestaurantInformations;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import javax.annotation.Nullable;
import butterknife.BindView;
import butterknife.ButterKnife;

import static com.example.blanche.go4lunch.utils.Utils.getDistance;
import static com.example.blanche.go4lunch.utils.Utils.getFormattedOpeningHours;
import static com.example.blanche.go4lunch.utils.Utils.setStars;


public class RestaurantViewHolder extends RecyclerView.ViewHolder {

    //------------------------
    //BIND VIEWS
    //-------------------------
    @BindView(R.id.fragment_page_item_image)
    ImageView imageView;
    @BindView(R.id.restaurant_name)
    TextView textViewName;
    @BindView(R.id.distance) TextView distance;
    @BindView(R.id.typeAndAdress) TextView typeAndAdress;
    @BindView(R.id.workmates_images) ImageView workmatesImage;
    @BindView(R.id.number_of_workmates) TextView numberTextView;
    @BindView(R.id.horaires) TextView openingHoursTextView;
    @BindView(R.id.star_one) ImageView starOne;
    @BindView(R.id.star_two) ImageView starTwo;
    @BindView(R.id.star_three) ImageView starThree;

    //---------------
    private static final String LATITUDE_AND_LONGITUDE = "latitudeAndLongitude";
    private static final String APP_PREFERENCES = "appPreferences";
    private String userLatlng;
    private String apikey;

    public RestaurantViewHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
        SharedPreferences preferences = itemView.getContext().getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
        userLatlng = preferences.getString(LATITUDE_AND_LONGITUDE, null);
    }

    public void update(RestaurantInformations restaurantInformations, RequestManager glide) {
        textViewName.setText(restaurantInformations.getName());
        typeAndAdress.setText(restaurantInformations.getVicinity());
        Calendar calendar = Calendar.getInstance();
        openingHoursTextView.setTextColor(Color.parseColor("#808080"));
        if (restaurantInformations.getOpeningHours() != null) {
            if (restaurantInformations.getOpeningHours().getOpenNow()) {
                String opening = null;
                int day = calendar.get(Calendar.DAY_OF_WEEK);
                switch (day) {

                    case Calendar.MONDAY:
                        opening = restaurantInformations.getOpeningHours().getWeekdayText().get(0);
                        openingHoursTextView.setText(getFormattedOpeningHours(opening, "Monday: "));
                        break;
                    case Calendar.TUESDAY:
                        opening = restaurantInformations.getOpeningHours().getWeekdayText().get(1);
                        openingHoursTextView.setText(getFormattedOpeningHours(opening, "Tuesday: "));
                        break;
                    case Calendar.WEDNESDAY:
                        opening = restaurantInformations.getOpeningHours().getWeekdayText().get(2);
                        openingHoursTextView.setText(getFormattedOpeningHours(opening, "Wednesday: "));
                        break;
                    case Calendar.THURSDAY:
                        opening = restaurantInformations.getOpeningHours().getWeekdayText().get(3);
                        openingHoursTextView.setText(getFormattedOpeningHours(opening, "Thursday: "));
                        break;
                    case Calendar.FRIDAY:
                        opening = restaurantInformations.getOpeningHours().getWeekdayText().get(4);
                        openingHoursTextView.setText(getFormattedOpeningHours(opening, "Friday: "));
                        break;
                    case Calendar.SATURDAY:
                        opening = restaurantInformations.getOpeningHours().getWeekdayText().get(5);
                        openingHoursTextView.setText(getFormattedOpeningHours(opening, "Saturday: "));
                        break;
                    case Calendar.SUNDAY:
                        opening = restaurantInformations.getOpeningHours().getWeekdayText().get(6);
                        openingHoursTextView.setText(getFormattedOpeningHours(opening, "Sunday: "));
                        break;
                    default:
                        break;
                }
            } else if (!restaurantInformations.getOpeningHours().getOpenNow()){
                openingHoursTextView.setText(itemView.getContext().getString(R.string.restaurant_is_closed));
                openingHoursTextView.setTextColor(Color.parseColor("#ba0018"));
            }
        } else {
            openingHoursTextView.setText(R.string.opening_hours);
        }
        if (restaurantInformations.getPhotos() != null) {
            String url = restaurantInformations.getPhotos().get(0).getPhotoReference();
            apikey = BuildConfig.ApiKey;
            String finalUrl = "https://maps.googleapis.com/maps/api/place/photo?maxwidth=400&photoreference=" + url + "&key=" + apikey;
            glide.load(finalUrl).apply(RequestOptions.noTransformation()).into(imageView);
        }
        double lat = restaurantInformations.getGeometry().getLocation().getLat();
        double lng = restaurantInformations.getGeometry().getLocation().getLng();

        //setDistance(lat, lng, userLatlng, distance);
        distance.setText(getDistance(lat, lng, userLatlng));

        UserHelper.getUsersCollection()
                .whereEqualTo("restaurantId", restaurantInformations.getPlaceId())
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.w("TAG", "Listen failed", e);
                            return;
                        }
                        List<String> users = new ArrayList<>();
                        for (DocumentSnapshot doc : queryDocumentSnapshots) {
                            if (doc.get("restaurantId") != null) {
                                users.add(doc.getString("chosenRestaurant"));
                                numberTextView.setText("(" + users.size() + ")");

                            }
                        }
                    }
                });

        setStars(restaurantInformations.getPlaceId(), starOne, starTwo, starThree);

    }







}
