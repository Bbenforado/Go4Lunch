package com.example.blanche.go4lunch.adapters;

import android.content.res.Resources;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.RequestManager;
import com.bumptech.glide.request.RequestOptions;
import com.example.blanche.go4lunch.R;
import com.example.blanche.go4lunch.models.User;

import butterknife.BindView;
import butterknife.ButterKnife;

public class WorkmateViewHolder extends RecyclerView.ViewHolder {

    @BindView(R.id.fragment_page_item_image)
    ImageView imageView;
    @BindView(R.id.text)
    TextView textView;
    Resources res;

    public WorkmateViewHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
        res = itemView.getResources();
    }

    public void updateWithUsers(User user, RequestManager glide) {
        if (user.isHasChosenRestaurant()) {
            String finalString = user.getUsername() + " is eating at " + user.getChosenRestaurant();
            textView.setText(finalString);
        } else {
            String finalString = user.getUsername() + " hasn't decided yet";
            textView.setText(finalString);
        }
        if (user.getUrlPicture() != null) {
            glide.load(user.getUrlPicture())
                    .apply(RequestOptions.circleCropTransform())
                    .into(imageView);
        }
    }
}
