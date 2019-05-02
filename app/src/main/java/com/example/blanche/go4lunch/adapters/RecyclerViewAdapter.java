package com.example.blanche.go4lunch.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.RequestManager;
import com.example.blanche.go4lunch.R;
import com.example.blanche.go4lunch.models.RestaurantsResults;

import java.util.List;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RestaurantViewHolder> {

    private List<RestaurantsResults> restaurantsResultsList;
    private RequestManager glide;

    public RecyclerViewAdapter(List<RestaurantsResults> restaurantsResultsList, RequestManager glide) {
        this.restaurantsResultsList = restaurantsResultsList;
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
       holder.updateWithRestaurants(this.restaurantsResultsList.get(position), this.glide);
    }

    @Override
    public int getItemCount() {
        return restaurantsResultsList.size();
    }

    public RestaurantsResults getRestaurant(int position) {
        return this.restaurantsResultsList.get(position);
    }
}
