package com.example.blanche.go4lunch.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.blanche.go4lunch.R;

import butterknife.BindView;
import butterknife.ButterKnife;

public class DetailsViewHolder extends RecyclerView.ViewHolder {

    @BindView(R.id.details_page_item_image)
    ImageView imageView;
    @BindView(R.id.details_text)
    TextView textView;


    public DetailsViewHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }
}
