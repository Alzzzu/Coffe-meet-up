package my.first.messenger.activities.services;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import my.first.messenger.activities.utils.Constants;
import my.first.messenger.activities.utils.PreferencesManager;


public class LocationManager {
    private final String TAG = "LoCManager_tag";
    private PreferencesManager preferencesManager;

    private static LocationManager instance = null;
    private Context context;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private static int REQUEST_CHECK_SETTINGS = 200;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    Intent backgroundLocationIntent = new Intent("background_location");
    StringBuilder stringBuilder = new StringBuilder();
    private Activity activity;
    private LocationManager(){

    }
    public static LocationManager getInstance(Context context) {
        if(instance == null){
            instance = new LocationManager();
        }
    instance.init(context);
    return instance;
    }
    private void init(Context context) {
        preferencesManager = new PreferencesManager(context);
        this.context = context;
        this.fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context);
        if (context instanceof Activity) {
            activity = (Activity) context;
        }

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    if (location != null) {

                        preferencesManager.putString(Constants.KEY_USER_LATITUDE, location.getLatitude()+"");
                        preferencesManager.putString(Constants.KEY_USER_LONGITUDE, location.getLongitude()+"");
                        Log.d(TAG,location.toString());
                        stringBuilder.setLength(0);
                        stringBuilder.append("lat " + location.getLatitude() +
                                "lon " + location.getLongitude());
                        backgroundLocationIntent.putExtra("location", stringBuilder.toString());
                        LocalBroadcastManager.getInstance(context).sendBroadcast(backgroundLocationIntent);

                    }
                }

            }
        };

        createLocationRequest();
    }

        protected void createLocationRequest() {
            locationRequest =LocationRequest.create();
            locationRequest.setInterval(10000);
            locationRequest.setFastestInterval(5000);
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            LocationSettingsRequest.Builder builder =
                    new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);
            SettingsClient client = LocationServices.getSettingsClient(context);
            Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());
            task.addOnSuccessListener(new OnSuccessListener<LocationSettingsResponse>() {
                @Override
                public void onSuccess(LocationSettingsResponse locationSettingsResponse) {

                }
            });
            task.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    try {
                        ResolvableApiException resolvable = (ResolvableApiException)  e;
                        resolvable.startResolutionForResult(activity,
                                REQUEST_CHECK_SETTINGS);

                    } catch (Exception exception){
                    }
                }
            });


        }
        public void startLocationUpdates(){
        if(ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
        != PackageManager.PERMISSION_GRANTED
        && ActivityCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            return;
        }
        fusedLocationProviderClient.requestLocationUpdates(locationRequest,
                locationCallback,
                Looper.getMainLooper());
        }
        public void stopLocationUpdates(){
        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
        }



}
