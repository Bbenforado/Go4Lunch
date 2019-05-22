package com.example.blanche.go4lunch.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.RequestManager;
import com.example.blanche.go4lunch.R;
import com.example.blanche.go4lunch.models.Restaurant;
import com.example.blanche.go4lunch.models.RestaurantInformations;
import com.example.blanche.go4lunch.models.RestaurantsResults;

import java.util.List;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RestaurantViewHolder> {

    /*private List<RestaurantsResults> restaurantsResultsList;
    private List<Restaurant> restaurantList;*/
    private List<RestaurantInformations> restaurantInformationsList;
    private RequestManager glide;

    //WHEN USE NEARBY
    /*public RecyclerViewAdapter(List<RestaurantsResults> restaurantsResultsList, RequestManager glide) {
        this.restaurantsResultsList = restaurantsResultsList;
        this.glide = glide;
    }*/

    //WHEN USE NEARBY AND DETAILS API
    /*public RecyclerViewAdapter (List<Restaurant> restaurantList, RequestManager glide) {
        this.restaurantList = restaurantList;
        this.glide = glide;
    }*/

    public RecyclerViewAdapter(List<RestaurantInformations> restaurantInformationsList, RequestManager glide) {
        this.restaurantInformationsList = restaurantInformationsList;
        this.glide = glide;
    }

    @NonNull
    @Override
    public RestaurantViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        Context context = viewGroup.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.fragment_second_item, viewGroup, false);
        return new RestaurantViewHolder(view);
    }
    @Override
    public void onBindViewHolder(@NonNull RestaurantViewHolder holder, int position) {
        holder.update(this.restaurantInformationsList.get(position), this.glide);
    }

    @Override
    public int getItemCount() {
        return restaurantInformationsList.size();
    }

    public RestaurantInformations getRestaurant(int position) {
        return this.restaurantInformationsList.get(position);
    }
}
