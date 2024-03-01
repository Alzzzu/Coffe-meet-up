package my.first.messenger.activities.services;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import com.google.firebase.firestore.FirebaseFirestore;

import java.lang.ref.PhantomReference;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import my.first.messenger.R;
import my.first.messenger.activities.main_activities.ProfileActivity;
import my.first.messenger.activities.main_activities.RecentConversationsActivity;
import my.first.messenger.activities.main_activities.RouteActivity;
import my.first.messenger.activities.main_activities.UserLocationActivity;
import my.first.messenger.activities.utils.Constants;
import my.first.messenger.activities.utils.PreferencesManager;

public class TimerService extends Service {
    private PreferencesManager preferencesManager;

    public TimerService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        // throw new UnsupportedOperationException("Not yet implemented");
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        preferencesManager = new PreferencesManager(getApplicationContext());
        Timer timer = new Timer();
        Log.d("SERVICETIMER", "here");


        timer.scheduleAtFixedRate(new TimerTask() {

            synchronized public void run() {
                if(preferencesManager.getBoolean(Constants.KEY_IS_GOING)){
                    showNotification();

                            if(preferencesManager.getBoolean(Constants.KEY_IS_GOING)) {
                                preferencesManager.putBoolean(Constants.KEY_IS_GOING,false);
                                FirebaseFirestore db = FirebaseFirestore.getInstance();

                                db.collection(Constants.KEY_COLLECTION_COFFEE_SHOPS).document(preferencesManager.getString(Constants.KEY_COFFEESHOP_ID))
                                        .update("activated", false);

                                db.collection(Constants.KEY_COLLECTION_COFFEE_SHOPS).document(preferencesManager.getString(Constants.KEY_COFFEESHOP_ID))
                                        .collection(Constants.KEY_COLLECTION_USERS)
                                        .document(preferencesManager.getString(Constants.KEY_USER_ID)).delete();
                                Intent dialogIntent = new Intent(getApplicationContext(), UserLocationActivity.class);
                                dialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(dialogIntent);

                            }
                            }


            }

        }, TimeUnit.MINUTES.toMillis(5), TimeUnit.MINUTES.toMillis(5));
        return super.onStartCommand(intent, flags, startId);


    }

    private void threader() {
        new Thread() {
            public void run() {
                try {

                    sleep(60*1000*6);//Constants.TIME_GAP);
                    Log.d("SERVICETIMER", "hure");
                    if(preferencesManager.getBoolean(Constants.KEY_IS_GOING)){
                    showNotification();
                    sleep(100000);
                        Log.d("SERVICETIMER", "hore");

                        if(preferencesManager.getBoolean(Constants.KEY_IS_GOING)){
                        showNotification();

                        preferencesManager.putBoolean(Constants.KEY_IS_GOING,false);
                        FirebaseFirestore db = FirebaseFirestore.getInstance();

                        db.collection(Constants.KEY_COLLECTION_COFFEE_SHOPS).document(preferencesManager.getString(Constants.KEY_COFFEESHOP_ID))
                                .update("activated", false);

                        db.collection(Constants.KEY_COLLECTION_COFFEE_SHOPS).document(preferencesManager.getString(Constants.KEY_COFFEESHOP_ID))
                                .collection(Constants.KEY_COLLECTION_USERS)
                                .document(preferencesManager.getString(Constants.KEY_USER_ID)).delete();
                        Intent dialogIntent = new Intent(getApplicationContext(), UserLocationActivity.class);
                        dialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(dialogIntent);
                        onDestroy();
                    }


                    }


                } catch (InterruptedException e) {
                    Log.d("TimerService", e.getMessage());
                }
            }
        }.start();
    }
private void showNotification(){
    NotificationManager mNotificationManager =
            (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                        if(android.os.Build.VERSION.SDK_INT >=android.os.Build.VERSION_CODES.O)

    {
        NotificationChannel channel = new NotificationChannel("YOUR_CHANNEL_ID",
                "YOUR_CHANNEL_NAME",
                NotificationManager.IMPORTANCE_DEFAULT);
        channel.setDescription("YOUR_NOTIFICATION_CHANNEL_DESCRIPTION");
        mNotificationManager.createNotificationChannel(channel);
    }

    NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(getApplicationContext(), "YOUR_CHANNEL_ID")
            .setSmallIcon(R.drawable.logo) // notification icon
            .setContentTitle("Прошло 15 минут!") // title for notification
            .setContentText("Вы уверены, что хотите продолжить путь?")// message for notification
            .setAutoCancel(true); // clear notification after click
    Intent intent = new Intent(getApplicationContext(), RouteActivity.class);
    PendingIntent pi = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                        mBuilder.setContentIntent(pi);
                        mNotificationManager.notify(0,mBuilder.build());

}
    private void makeToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }
}