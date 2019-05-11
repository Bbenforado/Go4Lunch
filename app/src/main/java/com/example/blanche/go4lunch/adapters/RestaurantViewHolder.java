package com.example.blanche.go4lunch.adapters;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.request.RequestOptions;
import com.example.blanche.go4lunch.R;
import com.example.blanche.go4lunch.models.Restaurant;
import com.example.blanche.go4lunch.models.RestaurantInformationObject;
import com.example.blanche.go4lunch.models.RestaurantInformations;
import com.example.blanche.go4lunch.models.RestaurantsResults;
import com.example.blanche.go4lunch.utils.RestaurantStreams;

import java.util.Calendar;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableObserver;

public class RestaurantViewHolder extends RecyclerView.ViewHolder {

    @BindView(R.id.fragment_page_item_image)
    ImageView imageView;
    @BindView(R.id.restaurant_name)
    TextView textViewName;
    @BindView(R.id.distance) TextView distance;
    @BindView(R.id.typeAndAdress) TextView typeAndAdress;
    @BindView(R.id.workmates_images) ImageView workmatesImage;
    @BindView(R.id.number_of_workmates) TextView numberTextView;
    @BindView(R.id.horaires) TextView horairesTextView;



    public RestaurantViewHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }

    //WHEN USE NEARBY
    /*public void updateWithRestaurants(RestaurantsResults results, RequestManager glide) {
        textViewName.setText(results.getName());
        typeAndAdress.setText(results.getVicinity());
        String photoId = results.getPhotos().get(0).getPhotoReference();
        String url = "https://maps.googleapis.com/maps/api/place/photo?maxwidth=400&photoreference=" + photoId + "&key=AIzaSyA6Jk5Xl1MbXbYcfWywZ0vwUY2Ux4KLta4";
        glide.load(url).apply(RequestOptions.noTransformation()).into(imageView);
    }*/

    //WHEN USE NEARBY AND DETAILS API
    /*public void updateWithRestaurants (Restaurant restaurant, RequestManager glide) {
        textViewName.setText(restaurant.getName());
        typeAndAdress.setText(restaurant.getAdress());
        horairesTextView.setText(restaurant.getOpeningHours());
        String url = restaurant.getPictureUrl();
        String finalUrl = "https://maps.googleapis.com/maps/api/place/photo?maxwidth=400&photoreference=" + url + "&key=AIzaSyA6Jk5Xl1MbXbYcfWywZ0vwUY2Ux4KLta4";
        glide.load(finalUrl).apply(RequestOptions.noTransformation()).into(imageView);
    }*/

    public void update(RestaurantInformations restaurantInformations, RequestManager glide) {
        textViewName.setText(restaurantInformations.getName());
        typeAndAdress.setText(restaurantInformations.getVicinity());
        Calendar calendar = Calendar.getInstance();
        int day = calendar.get(Calendar.DAY_OF_WEEK);
        switch (day) {
            case Calendar.MONDAY:
                horairesTextView.setText(restaurantInformations.getOpeningHours().getWeekdayText().get(0));
                break;
            case Calendar.TUESDAY:
                horairesTextView.setText(restaurantInformations.getOpeningHours().getWeekdayText().get(1));
                break;
            case Calendar.WEDNESDAY:
                horairesTextView.setText(restaurantInformations.getOpeningHours().getWeekdayText().get(2));
                break;
            case Calendar.THURSDAY:
                horairesTextView.setText(restaurantInformations.getOpeningHours().getWeekdayText().get(3));
                break;
            case Calendar.FRIDAY:
                horairesTextView.setText(restaurantInformations.getOpeningHours().getWeekdayText().get(4));
                break;
            case Calendar.SATURDAY:
                horairesTextView.setText(restaurantInformations.getOpeningHours().getWeekdayText().get(5));
                break;
            case Calendar.SUNDAY:
                horairesTextView.setText(restaurantInformations.getOpeningHours().getWeekdayText().get(6));
                break;
            default:
                break;
        }
        String url = restaurantInformations.getPhotos().get(0).getPhotoReference();
        String finalUrl = "https://maps.googleapis.com/maps/api/place/photo?maxwidth=400&photoreference=" + url + "&key=AIzaSyA6Jk5Xl1MbXbYcfWywZ0vwUY2Ux4KLta4";
        glide.load(finalUrl).apply(RequestOptions.noTransformation()).into(imageView);
    }


}
