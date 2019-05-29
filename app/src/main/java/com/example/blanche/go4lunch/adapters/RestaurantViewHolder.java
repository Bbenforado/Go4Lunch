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

                int day = calendar.get(Calendar.DAY_OF_WEEK);
                switch (day) {
                    case Calendar.MONDAY:
                        openingHoursTextView.setText(restaurantInformations.getOpeningHours().getWeekdayText().get(0));
                        break;
                    case Calendar.TUESDAY:
                        openingHoursTextView.setText(restaurantInformations.getOpeningHours().getWeekdayText().get(1));
                        break;
                    case Calendar.WEDNESDAY:
                        openingHoursTextView.setText(restaurantInformations.getOpeningHours().getWeekdayText().get(2));
                        break;
                    case Calendar.THURSDAY:
                        openingHoursTextView.setText(restaurantInformations.getOpeningHours().getWeekdayText().get(3));
                        break;
                    case Calendar.FRIDAY:
                        openingHoursTextView.setText(restaurantInformations.getOpeningHours().getWeekdayText().get(4));
                        break;
                    case Calendar.SATURDAY:
                        openingHoursTextView.setText(restaurantInformations.getOpeningHours().getWeekdayText().get(5));
                        break;
                    case Calendar.SUNDAY:
                        openingHoursTextView.setText(restaurantInformations.getOpeningHours().getWeekdayText().get(6));
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

        setDistance(lat, lng);

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
    private void setDistance(double lat, double lng) {
        String[] values = userLatlng.split(",");
        double userLat = Double.parseDouble(values[0]);
        double userLng = Double.parseDouble(values[1]);
        float userL = (float)userLat;
        float userLg = (float)userLng;
        float restaurantLat = (float)lat;
        float restaurantLng = (float)lng;

        double restaurantLocation = meterDistanceBetweenPoints(userL, userLg, restaurantLat, restaurantLng);
        String distanceBetween = Double.toString(restaurantLocation);

        String[] meters = distanceBetween.split("\\.");
        String finalString = meters[0] + " m";

        distance.setText(finalString);
    }

    private double meterDistanceBetweenPoints(float lat_a, float lng_a, float lat_b, float lng_b) {
        float pk = (float) (180.f/Math.PI);

        float a1 = lat_a / pk;
        float a2 = lng_a / pk;
        float b1 = lat_b / pk;
        float b2 = lng_b / pk;

        double t1 = Math.cos(a1) * Math.cos(a2) * Math.cos(b1) * Math.cos(b2);
        double t2 = Math.cos(a1) * Math.sin(a2) * Math.cos(b1) * Math.sin(b2);
        double t3 = Math.sin(a1) * Math.sin(b1);
        double tt = Math.acos(t1 + t2 + t3);

        return 6366000 * tt;
    }




}
