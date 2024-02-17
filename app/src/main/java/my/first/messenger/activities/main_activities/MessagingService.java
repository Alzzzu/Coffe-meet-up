package my.first.messenger.activities.main_activities;


import static android.app.job.JobInfo.PRIORITY_DEFAULT;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Random;

import my.first.messenger.R;
import my.first.messenger.activities.models.ChatMessage;
import my.first.messenger.activities.models.User;
import my.first.messenger.activities.utils.Constants;
import my.first.messenger.activities.utils.PreferencesManager;

public class MessagingService extends FirebaseMessagingService {
    private PreferencesManager preferencesManager;

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        Log.d("FCM", "token: " + token);

    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage message) {
        super.onMessageReceived(message);
        Log.d("FCM", "message: " + message.getNotification().getBody());
        User user = new User();
        user.id = message.getData().get(Constants.KEY_USER_ID);
        user.name = message.getData().get(Constants.KEY_NAME);
        user.token = message.getData().get(Constants.KEY_FCM_TOKEN);
        showNotification(user.name, "aea");
        int notificationId = new Random().nextInt();
        String channel_id = "chat_message";
        Intent intent = new Intent(this, ChatActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra(Constants.KEY_USER, user);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,  PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channel_id);
        builder.setSmallIcon(R.drawable.ic_back);
        builder.setContentTitle(user.name);
        builder.setContentText(message.getData().get(Constants.KEY_MESSAGE));
        builder.setStyle(
                new NotificationCompat.BigTextStyle().bigText(
                        message.getData().get(Constants.KEY_MESSAGE)
                )
        );
        builder.setPriority(NotificationCompat.PRIORITY_DEFAULT);
        builder.setContentIntent(pendingIntent);
        builder.setAutoCancel(true);

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O) {
            CharSequence channelName = "Chat Messages";
            String channelDescription = "This Channel is used for chat message notifications";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(channel_id, channelName, importance);
            channel.setDescription(channelDescription);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);

        }
        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
   //         // TODO: Consider calling
             // ActivityCompat#requestPermissions
                    //here to request the missing permissions, and then overriding
              // public void onRequestPermissionsResult(int requestCode, String[] permissions,
             //                                         int[] grantResults)
            //// to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

        notificationManagerCompat.notify(notificationId, builder.build());


    }
    void showNotification(String title, String message) {
            PendingIntent pi=PendingIntent.getActivity(this,0, new Intent(this,MainActivity.class), 0);
            Notification notification = new NotificationCompat.Builder(this)
                    .setSmallIcon(R.drawable.ic_back)
                    .setContentTitle("TechEduTricks")
                    .setContentText(message)
                    .setContentIntent(pi)
                    .setAutoCancel(true)
                    .setDefaults(Notification.FLAG_ONLY_ALERT_ONCE)
                    .setDefaults(Notification.DEFAULT_SOUND)
                    .setDefaults(Notification.DEFAULT_VIBRATE)
                    .build();
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(0, notification);



        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("YOUR_CHANNEL_ID",
                    "YOUR_CHANNEL_NAME",
                    NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("YOUR_NOTIFICATION_CHANNEL_DESCRIPTION");
            mNotificationManager.createNotificationChannel(channel);
        }
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(getApplicationContext(), "YOUR_CHANNEL_ID")
                .setSmallIcon(R.mipmap.ic_launcher) // notification icon
                .setContentTitle(title) // title for notification
                .setContentText(message)// message for notification
                .setAutoCancel(true); // clear notification after click
        Intent intent = new Intent(getApplicationContext(), RouteActivity.class);
        PendingIntent pir = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(pir);
        mNotificationManager.notify(0, mBuilder.build());
    }

}