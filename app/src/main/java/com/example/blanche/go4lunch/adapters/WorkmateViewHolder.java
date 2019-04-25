package com.example.blanche.go4lunch.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.blanche.go4lunch.R;

import butterknife.BindView;
import butterknife.ButterKnife;

public class WorkmateViewHolder extends RecyclerView.ViewHolder {

    @BindView(R.id.fragment_page_item_image)
    ImageView imageView;
    @BindView(R.id.text)
    TextView textView;

    public WorkmateViewHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }
}
