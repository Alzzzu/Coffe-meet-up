package my.first.messenger.activities.services;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import my.first.messenger.activities.utils.Constants;
import my.first.messenger.activities.utils.PreferencesManager;

public class GPSTracker extends Service implements LocationListener {

    private LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
    private PreferencesManager preferencesManager;
    final String LOG_TAG = "myLogs";
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(LOG_TAG, "YUY");

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            LocationManager locationManager =
                    (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 1, this);
            Location location =  locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            preferencesManager.putString(Constants.KEY_USER_LATITUDE, location.getLatitude()+"");
            preferencesManager.putString(Constants.KEY_USER_LONGITUDE,location.getLongitude()+"");
        }

       // return Service.START_STICKY;
        return START_NOT_STICKY;
    }
    @SuppressLint("MissingPermission")
    @Override
    public void onCreate() {
        Log.d(LOG_TAG, "YUY");
      //  preferencesManager = new PreferencesManager(getApplicationContext());
    }

    @Override
    public void onDestroy() {

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onLocationChanged(Location location) {
        preferencesManager.putString(Constants.KEY_USER_LATITUDE, location.getLatitude()+"");
        preferencesManager.putString(Constants.KEY_USER_LONGITUDE,location.getLongitude()+"");

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }
}