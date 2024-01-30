package my.first.messenger.activities.main_activities;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import my.first.messenger.activities.models.ChatMessage;
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
    }

}