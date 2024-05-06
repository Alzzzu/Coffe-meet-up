package my.first.messenger.activities.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.work.ForegroundInfo;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import org.checkerframework.checker.nullness.qual.NonNull;

import my.first.messenger.R;

public class BackgroundLocationWork extends Worker {
    private final String TAG ="WORKER_TAG";
    private NotificationManager notificationManager;
    private Context context;
    int NOTIFICATION_ID = 1;
    String progress ="Starting progres...";
    private LocationManager locationManager;
    private IntentFilter localBroadcastIntentFilter;

    public BackgroundLocationWork(@NonNull @org.jetbrains.annotations.NotNull Context context,
                                  @NonNull @org.jetbrains.annotations.NotNull WorkerParameters workerParameters){
        super(context, workerParameters);
        this.context = context;
        notificationManager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);
        locationManager = LocationManager.getInstance(context);

        localBroadcastIntentFilter = new IntentFilter();
        localBroadcastIntentFilter.addAction("background_location");
        LocalBroadcastManager.getInstance(context).registerReceiver(
                backgroundLocationBroadCastReceiver,
                localBroadcastIntentFilter);

    }

    @androidx.annotation.NonNull
    @Override
    public Result doWork() {
        setForegroundAsync(showNotification(progress));
        while (true){
            if (1==2){
                break;
            }
            locationManager.startLocationUpdates();
            try {
                Thread.sleep(5000);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        return Result.success();
    }
    @NonNull
    private ForegroundInfo showNotification(String progress){
        return new ForegroundInfo(NOTIFICATION_ID, createNotification(progress));
    }
    private Notification createNotification(String progress){
        String CHANNEL_ID="100";
        String title = "Foreground Work";
        String cancel = "Cancel";

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            ((NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE)).createNotificationChannel(
                    new NotificationChannel(CHANNEL_ID, title,
                            NotificationManager.IMPORTANCE_HIGH)
            );
        }
        Notification notification = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setContentTitle(title)
                .setTicker(title)
                .setContentText(progress)
                .setSmallIcon(R.drawable.logo)
                .setOngoing(true)
                .setOnlyAlertOnce(true)
                .build();
        return notification;
    }

    private void updateNotification(String progress){
        Notification notification = createNotification(progress);
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFICATION_ID, notification);
    }
    BroadcastReceiver backgroundLocationBroadCastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "Broadcasted");
            progress = intent.getStringExtra("location");
            updateNotification(progress);
        }
    };
    @Override public void onStopped() {
        LocalBroadcastManager.getInstance(context).unregisterReceiver(backgroundLocationBroadCastReceiver);
        super.onStopped();
    }
}
