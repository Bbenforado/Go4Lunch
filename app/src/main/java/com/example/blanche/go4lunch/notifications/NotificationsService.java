package com.example.blanche.go4lunch.notifications;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import android.util.Log;

import com.example.blanche.go4lunch.callbacks.MyCallback;
import com.example.blanche.go4lunch.R;
import com.example.blanche.go4lunch.activities.MainActivity;
import com.example.blanche.go4lunch.api.UserHelper;
import com.example.blanche.go4lunch.models.User;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import static com.example.blanche.go4lunch.utils.Utils.getCurrentUser;

public class NotificationsService extends FirebaseMessagingService {

    private final int NOTIFICATION_ID = 007;
    private final String NOTIFICATION_TAG = "FIREBASEOC";
    private String restaurantId;
    private List<String> userNames;

    /**
     * notifications are sent at 12:00
     * if application is in front, notification displays to the user where he s going to eat, with who and info about where he s going
     * if application is in back, notification displays to the user a generic message that asks him to open app and check where he s going to eat
     * @param remoteMessage message from notification from firebase
     */
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        String messageBody = getString(R.string.toast_text_when_user_chose_restaurant);
            UserHelper.getUser(getCurrentUser().getUid()).addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    User user = documentSnapshot.toObject(User.class);
                    if (user.isHasChosenRestaurant()) {
                        userNames = new ArrayList<>();
                        String restaurant = user.getChosenRestaurant();
                        String adress = user.getChosenRestaurantAdress();
                        restaurantId = user.getRestaurantId();
                        readData(new MyCallback() {
                            @Override
                            public void onCallback(List<String> list) {
                                String finalMessage;
                                if (userNames.size() > 0) {
                                    String listOfUsers = list.toString().replaceAll("[\\[\\]]", "");
                                    finalMessage = messageBody + " " + restaurant + ", " + adress + ", " + listOfUsers + getString(R.string.end_of_sentence_notif_workmates);
                                } else {
                                    finalMessage = messageBody + " " + restaurant + ", " + adress;
                                }
                                sendNotification(finalMessage);
                            }
                        });
                    } else {
                        sendNotification(getApplicationContext().getString(R.string.you_didnt_chose_restaurant));
                    }
                }
            });
    }

    private void sendNotification(String message) {
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);

        NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
        inboxStyle.setBigContentTitle(getApplicationContext().getString(R.string.notif_title));
        inboxStyle.addLine(message);

        String channelId = "fcm_default_channel";

        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.ic_lunch)
                .setContentTitle("Go4Lunch")
                .setAutoCancel(true)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setContentIntent(pendingIntent)
                .setStyle(inboxStyle);

        NotificationManager notificationManager = (NotificationManager)
                getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence channelName = "Message provenant de firebase";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(channelId, channelName, importance);
            notificationManager.createNotificationChannel(channel);

            notificationManager.notify(NOTIFICATION_TAG, NOTIFICATION_ID, notificationBuilder.build());
        }
    }

    private void readData(MyCallback myCallback) {
        Query query = UserHelper.getUsersCollection().whereEqualTo("restaurantId", restaurantId);
        query.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w("TAG", "Listen failed", e);
                    return;
                }
                for (DocumentSnapshot doc : queryDocumentSnapshots) {
                    if (doc.get("restaurantId") != null) {
                        userNames.add(doc.getString("username"));
                    }
                    myCallback.onCallback(userNames);
                }
            }
        });
    }
}
