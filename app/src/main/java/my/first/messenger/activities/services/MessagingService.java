package my.first.messenger.activities.services;

import android.app.PendingIntent;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Random;

import my.first.messenger.activities.main_activities.ChatActivity;
import my.first.messenger.activities.models.User;
import my.first.messenger.activities.utils.Constants;
import my.first.messenger.activities.utils.PreferencesManager;

public class MessagingService extends FirebaseMessagingService{
    private PreferencesManager preferencesManager;
    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        Log.d("FCM","token: "+token);

    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage message) {
        super.onMessageReceived(message);
        Log.d("FCM", "message: "+message.getNotification().getBody());
        User user = new User();
        user.id = message.getData().get(Constants.KEY_USER_ID);
        user.name= message.getData().get(Constants.KEY_NAME);
        user.token =message.getData().get(Constants.KEY_FCM_TOKEN);
        int notificationId = new Random().nextInt();
        String channelId = "chat_message";
        Intent intent = new Intent( this, ChatActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra(Constants.KEY_USER, user);
        PendingIntent pendingIntent= PendingIntent.getActivity( this, 8, intent, 0);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId);
    }

}
