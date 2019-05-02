package com.example.blanche.go4lunch.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.RequestManager;
import com.bumptech.glide.request.RequestOptions;
import com.example.blanche.go4lunch.R;
import com.example.blanche.go4lunch.models.RestaurantsResults;

import butterknife.BindView;
import butterknife.ButterKnife;

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

    public void updateWithRestaurants(RestaurantsResults results, RequestManager glide) {
        textViewName.setText(results.getName());
        typeAndAdress.setText(results.getVicinity());
        glide.load(results.getIcon()).apply(RequestOptions.noTransformation()).into(imageView);

    }
}
