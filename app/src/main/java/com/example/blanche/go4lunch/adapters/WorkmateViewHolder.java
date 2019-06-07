package com.example.blanche.go4lunch.adapters;

import android.content.Context;
import android.graphics.Color;
import androidx.recyclerview.widget.RecyclerView;
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

    //-----------------
    //BIND VIEWS
    //--------------------
    @BindView(R.id.fragment_page_item_image)
    ImageView imageView;
    @BindView(R.id.text)
    TextView textView;

    public WorkmateViewHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }

    public void updateWithUsers(Context context, User user, RequestManager glide) {
        if (user.getUid().equals(getCurrentUser().getUid())) {
            String finalString = context.getString(R.string.toast_text_when_user_chose_restaurant) + " " + user.getChosenRestaurant();
            setUserInfo(user, finalString, context.getString(R.string.user_didnt_chose_restaurant), glide);
            } else {
            String userHasChose = user.getUsername() + " " + context.getString(R.string.workmate_has_chose) + " " + user.getChosenRestaurant();
            String userDidntChose = user.getUsername() + " " + context.getString(R.string.workmate_didnt_chose_yet);
            setUserInfo(user, userHasChose, userDidntChose, glide);
        }
    }

    /**
     * display information depending on if user has chose restaurant or not
     * @param user
     * @param userIsEatingAt string if user has chose where to eat
     * @param userHasntChoseYet string if user didn't chose yet
     * @param glide display image
     */
    private void setUserInfo(User user, String userIsEatingAt, String userHasntChoseYet, RequestManager glide) {
        if (user.isHasChosenRestaurant()) {
            textView.setText(userIsEatingAt);
        } else {
            textView.setText(userHasntChoseYet);
            textView.setTextColor(Color.parseColor("#bcbcbc"));
        }
        if (user.getUrlPicture() != null) {
            glide.load(user.getUrlPicture())
                    .apply(RequestOptions.circleCropTransform())
                    .into(imageView);
        }
    }
}
