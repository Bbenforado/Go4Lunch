package com.example.blanche.go4lunch.adapters;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
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

import static com.example.blanche.go4lunch.utils.Utils.getCurrentUser;

public class WorkmateViewHolder extends RecyclerView.ViewHolder {

    @BindView(R.id.fragment_page_item_image)
    ImageView imageView;
    @BindView(R.id.text)
    TextView textView;
    private Resources res;

    public WorkmateViewHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
        res = itemView.getResources();
    }

    public void updateWithUsers(Context context, User user, RequestManager glide) {
        if (user.getUid().equals(getCurrentUser().getUid())) {
            setUserInfo(user,context.getString(R.string.toast_text_when_user_chose_restaurant), context.getString(R.string.user_didnt_chose_restaurant), glide);
        } else {
            String userHasChose = user.getUsername() + " " + context.getString(R.string.workmate_has_chose) + " " + user.getChosenRestaurant();
            String userDidntChose = user.getUsername() + " " + context.getString(R.string.workmate_didnt_chose_yet);
            setUserInfo(user, userHasChose, userDidntChose, glide);
        }
    }

    private void setUserInfo(User user, String userIsEatingAt, String userHasntChoseYet, RequestManager glide) {
        if (user.isHasChosenRestaurant()) {
            String finalString = userIsEatingAt + " " + user.getChosenRestaurant();
            textView.setText(finalString);
        } else {
            String finalString = userHasntChoseYet;
            textView.setText(finalString);
            textView.setTextColor(Color.parseColor("#bcbcbc"));
        }
        if (user.getUrlPicture() != null) {
            glide.load(user.getUrlPicture())
                    .apply(RequestOptions.circleCropTransform())
                    .into(imageView);
        }
    }
}
