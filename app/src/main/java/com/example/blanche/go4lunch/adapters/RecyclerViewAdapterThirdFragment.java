package com.example.blanche.go4lunch.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.blanche.go4lunch.R;

public class RecyclerViewAdapterThirdFragment extends RecyclerView.Adapter<WorkmateViewHolder> {

    public RecyclerViewAdapterThirdFragment() {

    }

    @NonNull
    @Override
    public WorkmateViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        Context context = viewGroup.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.fragment_third_item, viewGroup, false);
        return new WorkmateViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WorkmateViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 5;
    }
}
