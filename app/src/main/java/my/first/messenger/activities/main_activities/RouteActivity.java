package my.first.messenger.activities.main_activities;

import static java.lang.Double.parseDouble;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.FirebaseFirestore;

import org.osmdroid.api.IMapController;
import org.osmdroid.bonuspack.routing.OSRMRoadManager;
import org.osmdroid.bonuspack.routing.Road;
import org.osmdroid.bonuspack.routing.RoadManager;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.util.ArrayList;

import javax.annotation.OverridingMethodsMustInvokeSuper;

import my.first.messenger.R;
import my.first.messenger.activities.fragments.AlertFragment;
import my.first.messenger.activities.fragments.SelectFragment;
import my.first.messenger.activities.models.User;
import my.first.messenger.activities.services.GPSTracker;
import my.first.messenger.activities.utils.Constants;
import my.first.messenger.activities.utils.PreferencesManager;
import my.first.messenger.databinding.ActivityRouteBinding;
import my.first.messenger.databinding.FragmentRouteBinding;

public class RouteActivity extends AppCompatActivity {
    private static ActivityRouteBinding binding;
    private static GeoPoint userLoc, coffeeshopLoc;
    private static final int PERMISSION_1 = 1;
    private PreferencesManager preferencesManager;
    private String id;
    private MapView map;
    private Boolean arrived = true;
    protected IMapController mapController;
    private FusedLocationProviderClient mFusedLocationClient;
    private MyLocationNewOverlay mLocationOverlay;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

          StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
         StrictMode.setThreadPolicy(policy);

        binding = ActivityRouteBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //Service
        // ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_1);
        // Intent intent = new Intent(this, GPSTracker.class);
        // Log.d("BUBA", "BABU");


        // startService(intent);

        // Classes
        preferencesManager = new PreferencesManager(getApplicationContext());

        init();
        settingMap();
        setListeners();
    }

    private void init() {
        // id = getIntent().getStringExtra(Constants.KEY_COFFEESHOP_ID);
        id = preferencesManager.getString(Constants.KEY_COFFEESHOP_ID);
        userLoc = new GeoPoint(parseDouble(preferencesManager.getString(Constants.KEY_USER_LATITUDE)), parseDouble(preferencesManager.getString(Constants.KEY_USER_LONGITUDE)));
    }

    private void settingMap() {
        Context ctx = getApplicationContext();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));
        map = binding.map;

        map.setMinZoomLevel(15.0);
        map.setTileSource(TileSourceFactory.MAPNIK);
        mapController = map.getController();
        mapController.setZoom(15);
        map.setBuiltInZoomControls(true);
        map.setMultiTouchControls(true);
        mapController.setCenter(userLoc);//new GeoPoint(parseDouble(preferencesManager.getString(Constants.KEY_USER_LATITUDE)), parseDouble(preferencesManager.getString(Constants.KEY_USER_LONGITUDE))));
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(ctx);
        this.mLocationOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(this), map);
        this.mLocationOverlay.enableMyLocation();
        this.mLocationOverlay.enableFollowLocation();
        map.getOverlays().add(this.mLocationOverlay);
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection(Constants.KEY_COLLECTION_COFFEE_SHOPS).document(id).get()
                .addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                double lng = task.getResult().getDouble("longitude");
                double lat = task.getResult().getDouble("latitude");
                coffeeshopLoc = new GeoPoint(lat, lng);

                Marker marker = new Marker(map);
                marker.setPosition(new GeoPoint(lat, lng));
                map.getOverlays().add(marker);
                marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                map.getOverlays().add(marker);
                marker.setIcon(getResources().getDrawable(R.mipmap.coffee_colour));
                marker.setTitle("here");
                     routing(lat, lng);

            }

        });

    }

    private void setListeners() {
        binding.cancel.setOnClickListener(v -> {
            preferencesManager.putBoolean(Constants.KEY_IS_GOING, false);

            FirebaseFirestore db = FirebaseFirestore.getInstance();

            db.collection("coffeeshops").document(id)
                    .update("activated", false);

            db.collection("coffeeshops").document(id)
                    .collection(Constants.KEY_COLLECTION_USERS)
                    .document(preferencesManager.getString(Constants.KEY_USER_ID)).delete();

            Intent i = new Intent(getApplicationContext(), MapActivity.class);
            startActivity(i);

        });

        binding.finish.setOnClickListener(v -> {
            // showNotification("bam","bum");

            getLocation();
            // AlertFragment alert = new AlertFragment();
            if (distance(coffeeshopLoc.getLatitude(), coffeeshopLoc.getLongitude(), parseDouble(preferencesManager.getString(Constants.KEY_USER_LATITUDE)), parseDouble(preferencesManager.getString(Constants.KEY_USER_LONGITUDE))) < 0.2) {
                FragmentManager manager = getSupportFragmentManager();
                //    alert.show(manager, "dialog");
                FirebaseFirestore db = FirebaseFirestore.getInstance();
                db.collection("coffeeshops").document(id)
                        .collection(Constants.KEY_COLLECTION_USERS)
                        .document(preferencesManager.getString(Constants.KEY_USER_ID)).update("status", "active");
                preferencesManager.putBoolean(Constants.KEY_IS_ACTIVATED, true);
                preferencesManager.putBoolean(Constants.KEY_IS_GOING, false);
                Intent i = new Intent(getApplicationContext(), ActivatedActivity.class);
                startActivity(i);
                finish();
            } else {
                makeToast("Чтобы завершить путь необходимо достигнуть точку");
            }
        });
        binding.bottomNavigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                if (item.getItemId()==R.id.profile){
                    Intent intent = new Intent( getApplicationContext(), ProfileActivity.class);
                    User user = new User();
                    user.id =  preferencesManager.getString(Constants.KEY_USER_ID);
                    user.about = preferencesManager.getString(Constants.KEY_ABOUT);
                    user.hobby =preferencesManager.getString(Constants.KEY_HOBBIES);
                    user.name =preferencesManager.getString(Constants.KEY_NAME);
                    user.age = preferencesManager.getString(Constants.KEY_AGE);
                    user.image =preferencesManager.getString(Constants.KEY_IMAGE);
                    intent.putExtra(Constants.KEY_USER, user);
                    startActivity(intent);
                    overridePendingTransition(0,0);
                    return true;
                }
                else if (item.getItemId()==R.id.map){
                    return true;
                }
                else if (item.getItemId()==R.id.chat){
                    startActivity(new Intent(getApplicationContext(), RecentConversationsActivity.class));
                    finish();
                    overridePendingTransition(0,0);
                    return true;
                }
                else {
                    return false;
                }
            }
        });
    }


    private void routing(double lat, double lng) {
        new Thread() {
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                      //  GeoPoint g = new GeoPoint(new GeoPoint(parseDouble(preferencesManager.getString(Constants.KEY_USER_LATITUDE)), parseDouble(preferencesManager.getString(Constants.KEY_USER_LONGITUDE))));
                        RoadManager roadManager = new OSRMRoadManager(getApplicationContext(), "coffeeshopperhere");
                        ArrayList<GeoPoint> waypoints = new ArrayList<GeoPoint>();
                        waypoints.add(userLoc);
                        waypoints.add(new GeoPoint(lat, lng));
                        ((OSRMRoadManager) roadManager).setMean(OSRMRoadManager.MEAN_BY_FOOT);

                        Road road = roadManager.getRoad(waypoints);
                        Polyline roadOverlay = RoadManager.buildRoadOverlay(road);
                        map.getOverlays().add(roadOverlay);
                        binding.progress.setVisibility(View.GONE);
                        binding.map.setVisibility(View.VISIBLE);
                    }
                });
            }
        }.start();
    }

    private void discard() {
        new Thread() {
            public void run() {
                try {
                    Thread.sleep(Constants.TIME_GAP);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            AlertFragment myDialogFragment = new AlertFragment();
                            FragmentManager manager = getSupportFragmentManager();
                            //myDialogFragment.show(manager, "dialog");

                            FragmentTransaction transaction = manager.beginTransaction();
                            myDialogFragment.show(transaction, "dialog");
                        }
                    });
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }.start();

    }

    public static double distance(double lat1, double lon1, double lat2, double lon2) {
        if ((lat1 == lat2) && (lon1 == lon2)) {
            return 0;
        } else {
            double theta = lon1 - lon2;
            double dist = Math.sin(Math.toRadians(lat1)) * Math.sin(Math.toRadians(lat2)) + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.cos(Math.toRadians(theta));
            dist = Math.acos(dist);
            dist = Math.toDegrees(dist);
            dist = dist * 60 * 1.1515;
            dist = dist * 1.609344;

            return (dist);
        }
    }


    private void makeToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    void showNotification(String title, String message) {
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
        PendingIntent pi = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(pi);
        mNotificationManager.notify(0, mBuilder.build());
    }

    private void getLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            mFusedLocationClient.getLastLocation().addOnCompleteListener(task -> {
                Location location = task.getResult();
                preferencesManager.putString(Constants.KEY_USER_LONGITUDE,location.getLongitude()+"");
                preferencesManager.putString(Constants.KEY_USER_LATITUDE,location.getLatitude()+"");});
        }
    }

    //
    private void deactivate(){
        FirebaseFirestore db = FirebaseFirestore.getInstance();


        db.collection("coffeeshops").document(id)
                .update("activated", false);

        db.collection("coffeeshops").document(id)
                .collection(Constants.KEY_COLLECTION_USERS)
                .document(preferencesManager.getString(Constants.KEY_USER_ID)).delete();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    //    deactivate();
    }
}
