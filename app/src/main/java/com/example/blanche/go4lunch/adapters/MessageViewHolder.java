package com.example.blanche.go4lunch.adapters;

import android.graphics.drawable.Drawable;

import androidx.core.content.ContextCompat;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.view.View;

import com.bumptech.glide.RequestManager;
import com.bumptech.glide.request.RequestOptions;
import com.example.blanche.go4lunch.R;
import com.example.blanche.go4lunch.models.Message;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MessageViewHolder extends RecyclerView.ViewHolder {

    //-----------------------
    //BIND VIEWS
    //------------------------
    @BindView(R.id.activity_chat_item_root_view)
    RelativeLayout rootView;
    @BindView(R.id.activity_chat_item_profile_container)
    LinearLayout profileContainer;
    @BindView(R.id.activity_chat_item_profile_container_profile_image)
    ImageView imageViewProfile;
    @BindView(R.id.activity_chat_item_message_container)
    RelativeLayout messageContainer;
    @BindView(R.id.activity_chat_item_message_container_image_sent_cardview)
    CardView cardViewImageSent;
    @BindView(R.id.activity_chat_item_message_container_image_sent_cardview_image)
    ImageView imageViewSent;
    @BindView(R.id.activity_chat_item_message_container_text_message_container)
    LinearLayout textMessageContainer;
    @BindView(R.id.activity_chat_item_message_container_text_message_container_text_view)
    TextView textViewMessage;
    @BindView(R.id.activity_chat_item_message_container_text_view_date)
    TextView textViewDate;

    private Drawable drawableCurrentUser;
    private Drawable drawableRemoteUser;

    public MessageViewHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);

        drawableCurrentUser = ContextCompat.getDrawable(itemView.getContext(), R.drawable.bubble_style_user_sender);
        drawableRemoteUser = ContextCompat.getDrawable(itemView.getContext(), R.drawable.bubble_style);
    }

    public void updateWithMessage(Message message, String currentUserId, RequestManager glide) {
        // Check if current user is the sender
        Boolean isCurrentUser = message.getUserSender().getUid().equals(currentUserId);

        // Update message TextView
        this.textViewMessage.setText(message.getMessage());
        // Update date TextView
        String userNameAndDate = verifyUsernameLength(message.getUserSender().getUsername());
        if (message.getDateCreated() != null) {
            userNameAndDate = userNameAndDate + ", " + convertDateToHour(message.getDateCreated());
        }
        this.textViewDate.setText(userNameAndDate);

        // Update profile picture ImageView
        if (message.getUserSender().getUrlPicture() != null) {
            glide.load(message.getUserSender().getUrlPicture())
                    .apply(RequestOptions.circleCropTransform())
                    .into(imageViewProfile);
        } else {
            glide.load(R.drawable.ic_profile_recolored)
                    .apply(RequestOptions.circleCropTransform())
                    .into(imageViewProfile);
        }

        // Update image sent ImageView
        if (message.getUrlImage() != null) {
            glide.load(message.getUrlImage())
                    .into(imageViewSent);
            this.imageViewSent.setVisibility(View.VISIBLE);
            imageViewSent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    System.out.println("click");
                }
            });
        } else {
            this.imageViewSent.setVisibility(View.GONE);
        }

        //Update Message Bubble Color Background
        textMessageContainer.setBackground(isCurrentUser ? drawableCurrentUser : drawableRemoteUser);

        // Update all views alignment depending is current user or not
        this.updateDesignDependingUser(isCurrentUser);
    }

    private void updateDesignDependingUser(Boolean isSender) {
        // PROFILE CONTAINER
        RelativeLayout.LayoutParams paramsLayoutHeader = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        paramsLayoutHeader.addRule(isSender ? RelativeLayout.ALIGN_PARENT_RIGHT : RelativeLayout.ALIGN_PARENT_LEFT);
        this.profileContainer.setLayoutParams(paramsLayoutHeader);

        // MESSAGE CONTAINER
        RelativeLayout.LayoutParams paramsLayoutContent = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        paramsLayoutContent.addRule(isSender ? RelativeLayout.LEFT_OF : RelativeLayout.RIGHT_OF, R.id.activity_chat_item_profile_container);
        this.messageContainer.setLayoutParams(paramsLayoutContent);

        // CARDVIEW IMAGE SEND
        RelativeLayout.LayoutParams paramsImageView = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        paramsImageView.addRule(isSender ? RelativeLayout.ALIGN_LEFT : RelativeLayout.ALIGN_RIGHT, R.id.activity_chat_item_message_container_text_message_container);
        this.cardViewImageSent.setLayoutParams(paramsImageView);

        this.rootView.requestLayout();
    }

    // --------
    //
    //---------------

    private String convertDateToHour(Date date) {
        DateFormat dfTime = new SimpleDateFormat("HH:mm");
        return dfTime.format(date);
    }

    private String verifyUsernameLength(String username) {
        if (username.length() > 10) {
            username = username.substring(0, 7) + ".";
        }
        return username;
    }
}
