package my.first.messenger.activities.services;

import static java.lang.Double.parseDouble;
import static my.first.messenger.activities.utils.Functions.distance;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import my.first.messenger.R;
import my.first.messenger.activities.main_activities.MapActivity;
import my.first.messenger.activities.utils.Constants;
import my.first.messenger.activities.utils.PreferencesManager;

public class TimerService extends Service {
    private PreferencesManager preferencesManager;
    private final String TAG = "TimerServiceTag";
    public static Boolean isServiceRunning;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationRequest locationRequest;

    public TimerService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        isServiceRunning = true;
        Log.d(TAG, "created");

    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        preferencesManager = new PreferencesManager(getApplicationContext());
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getApplicationContext());
        Timer timer = new Timer();
        Log.d("SERVICETIMER", "here");
        locationUpdates();



        timer.scheduleAtFixedRate(new TimerTask() {

            synchronized public void run() {
                Log.d("SERVICETIMER", "here2");

                showNotification();
        //        if(preferencesManager.getBoolean(Constants.KEY_IS_GOING)){
           //         showNotification();

//                            if(preferencesManager.getBoolean(Constants.KEY_IS_GOING)) {//        preferencesManager.putBoolean(Constants.KEY_IS_GOING,false);
                                FirebaseFirestore db = FirebaseFirestore.getInstance();

                               /* db.collection(Constants.KEY_COLLECTION_COFFEE_SHOPS).document(preferencesManager.getString(Constants.KEY_COFFEESHOP_ID))
                                        .update("activated", false);

                                db.collection(Constants.KEY_COLLECTION_COFFEE_SHOPS).document(preferencesManager.getString(Constants.KEY_COFFEESHOP_ID))
                                        .collection(Constants.KEY_COLLECTION_USERS)
                                        .document(preferencesManager.getString(Constants.KEY_USER_ID)).delete();
                               Intent dialogIntent = new Intent(getApplicationContext(), UserLocationActivity.class);
                               dialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(dialogIntent);
*/
                        //    }
              //  }
            }

        }, TimeUnit.MINUTES.toMillis(3), TimeUnit.MINUTES.toMillis(3));

        return super.onStartCommand(intent, flags, startId);

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

}
    private void makeToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }


    public void locationUpdates() {
        try {

            fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

            if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
              //  ActivityCompat.requestPermissions(getApplication(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
               // ActivityCompat.requestPermissions(getApplicationContext(), new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 2);
            }

            locationRequest = locationRequest.create();
            locationRequest.setInterval(100);
            locationRequest.setFastestInterval(50);
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

            LocationCallback locationCallback = new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {

                    if (locationResult != null) {
                        for (Location location : locationResult.getLocations()) {
                            if (0.5 < distance(location.getLatitude(), location.getLongitude(), parseDouble(preferencesManager.getString(Constants.KEY_COFFEESHOP_LATITUDE)), parseDouble(preferencesManager.getString(Constants.KEY_COFFEESHOP_LONGITUDE)))) {

                                Intent i = new Intent(getApplicationContext(), MapActivity.class);
                                startActivity(i);

                            }
                            else{
                                Log.d(TAG, location.toString());
                            }
                        }
                    }}}
                    ;
            fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());

        }catch (Exception e){
            Log.d(TAG, "bubu");

        }


    }

    @Override
    public void onDestroy() {
        isServiceRunning = false;
        stopForeground(true);

        // call MyReceiver which will restart this service via a worker


        super.onDestroy();
    }
}