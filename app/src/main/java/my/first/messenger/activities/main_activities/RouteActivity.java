package my.first.messenger.activities.main_activities;

import static java.lang.Double.parseDouble;
import static my.first.messenger.activities.utils.Functions.distance;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.fragment.app.FragmentManager;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

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
import java.util.HashMap;

import my.first.messenger.R;
import my.first.messenger.activities.fragments.AlertFragment;
import my.first.messenger.activities.models.User;
import my.first.messenger.activities.utils.Constants;
import my.first.messenger.activities.utils.PreferencesManager;
import my.first.messenger.databinding.ActivityRouteBinding;

public class RouteActivity extends AppCompatActivity {
    private static ActivityRouteBinding binding;
    private static GeoPoint userLoc, coffeeshopLoc;
    private PreferencesManager preferencesManager;
    private String id;
    private MapView map;
    private Boolean arrived = true;
    protected IMapController mapController;
    private FusedLocationProviderClient mFusedLocationClient;
    FusedLocationProviderClient fusedLocationProviderClient;
    private LocationRequest locationRequest;
    private static final String TAG = "ROUTE_ACT";
    private FirebaseFirestore database;
    private MyLocationNewOverlay mLocationOverlay;
    private LocationCallback locationCallback;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        Log.d(TAG, "strivt avtivated");
        binding = ActivityRouteBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        // Classes
        preferencesManager = new PreferencesManager(getApplicationContext());
        init();
        Log.d(TAG, "initialized");
        locationUpdates();
        settingMap();
        Log.d(TAG, "map set");

        setListeners();

    }

    private void init() {
        id = preferencesManager.getString(Constants.KEY_COFFEESHOP_ID);
        Log.d(TAG, preferencesManager.getString(Constants.KEY_USER_ID));
        database = FirebaseFirestore.getInstance();
        userLoc = new GeoPoint(parseDouble(preferencesManager.getString(Constants.KEY_USER_LATITUDE)), parseDouble(preferencesManager.getString(Constants.KEY_USER_LONGITUDE)));
        if(!preferencesManager.getString(Constants.KEY_VISITED_ID).equals("")) {
            database.collection(Constants.KEY_COLLECTION_VISITS)
                    .whereEqualTo(Constants.KEY_VISITOR_ID, preferencesManager.getString(Constants.KEY_USER_ID))
                     .addSnapshotListener(eventListener);
        }
        if (!preferencesManager.getString(Constants.KEY_VISITED_ID).equals("")) {
            binding.finish.setVisibility(View.GONE);
        }
        }
    private final EventListener<QuerySnapshot> eventListener = (value, error) -> {

        if (error != null) {
            makeToast(error.getMessage());
        }

        if (value != null) {

            for(DocumentChange documentChange: value.getDocumentChanges()){

                if (documentChange.getType() == DocumentChange.Type.REMOVED){

                    preferencesManager.putBoolean(Constants.KEY_IS_GOING, false);
                    preferencesManager.putString(Constants.KEY_VISITED_ID, "");
                    preferencesManager.putString(Constants.KEY_VISITOR_ID, "");

                    showNotification("Вас забулили!",documentChange.getDocument().getString(Constants.KEY_VISITOR_NAME)+ " отменил(а) встречу");
                    Intent intent = new Intent(getApplicationContext(), MapActivity.class);
                    startActivity(intent);

                }
            }
        }
    };
    private void settingMap() {
        Context ctx = getApplicationContext();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));
        map = binding.map;
        map.setMinZoomLevel(15.0);
        map.setTileSource(TileSourceFactory.MAPNIK);
        mapController = map.getController();
        mapController.setZoom(15);
        map.setBuiltInZoomControls(false);
        map.setMultiTouchControls(true);
        mapController.setCenter(userLoc);//new GeoPoint(parseDouble(preferencesManager.getString(Constants.KEY_USER_LATITUDE)), parseDouble(preferencesManager.getString(Constants.KEY_USER_LONGITUDE))));
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(ctx);
        this.mLocationOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(this), map);
        this.mLocationOverlay.enableMyLocation();
        this.mLocationOverlay.enableFollowLocation();
        map.getOverlays().add(this.mLocationOverlay);
        Log.d(TAG,"MAP SET");
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
                        marker.setIcon(getResources().getDrawable(R.mipmap.map_icon_2));
                        marker.setTitle(task.getResult().getString(Constants.KEY_ADDRESS));

                        preferencesManager.putString(Constants.KEY_COFFEESHOP_LONGITUDE, lng+"");
                        preferencesManager.putString(Constants.KEY_COFFEESHOP_LATITUDE, lat+"");
                        try{
                        routing(lat, lng);
                        }
                        catch(Exception e){
                            makeToast(e.getMessage());
                        }

                    }

                });

    }

    private void setListeners() {
        binding.cancel.setOnClickListener(v -> {
            AlertFragment frag = new AlertFragment();
            Bundle bundle = new Bundle();
            bundle.putString("type", "CANCEL");
            bundle.putString("id", id);
            frag.setArguments(bundle);
            FragmentManager manager = getSupportFragmentManager();
            frag.show(manager, "dialog");

            if (0>1){
            }

        });

        binding.finish.setOnClickListener(v -> {
            // AlertFragment alert = new AlertFragment();
            Log.d(TAG,"starting check");
            if (distance(coffeeshopLoc.getLatitude(), coffeeshopLoc.getLongitude(), parseDouble(preferencesManager.getString(Constants.KEY_USER_LATITUDE)), parseDouble(preferencesManager.getString(Constants.KEY_USER_LONGITUDE))) < 0.2) {
                if(preferencesManager.getString(Constants.KEY_VISITED_ID).equals("")){
                preferencesManager.putBoolean(Constants.KEY_IS_GOING, false);
                Intent i = new Intent(getApplicationContext(), ActivatedActivity.class);
                startActivity(i);
                finish();
                }
            } else {
                makeToast("Чтобы завершить путь необходимо достигнуть точку");
            }
        });
        binding.bottomNavigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                if (item.getItemId() == R.id.profile) {
                    Intent intent = new Intent(getApplicationContext(), ProfileActivity.class);
                    User user = new User();
                    user.id = preferencesManager.getString(Constants.KEY_USER_ID);
                    user.about = preferencesManager.getString(Constants.KEY_ABOUT);
                    user.hobby = preferencesManager.getString(Constants.KEY_HOBBIES);
                    user.name = preferencesManager.getString(Constants.KEY_NAME);
                    user.age = preferencesManager.getString(Constants.KEY_AGE);
                    user.image = preferencesManager.getString(Constants.KEY_IMAGE);
                    intent.putExtra(Constants.KEY_USER, user);
                    startActivity(intent);
                    overridePendingTransition(0, 0);
                    return true;
                } else if (item.getItemId() == R.id.map) {
                    return true;
                } else if (item.getItemId() == R.id.chat) {
                    startActivity(new Intent(getApplicationContext(), RecentConversationsActivity.class));
                    finish();
                    overridePendingTransition(0, 0);
                    return true;
                } else {
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
                        Log.d(TAG,"ROUTE STARTED");

                        //  GeoPoint g = new GeoPoint(new GeoPoint(parseDouble(preferencesManager.getString(Constants.KEY_USER_LATITUDE)), parseDouble(preferencesManager.getString(Constants.KEY_USER_LONGITUDE))));
                        try {
                            RoadManager roadManager = new OSRMRoadManager(getApplicationContext(), "coffeeshopperhere");
                            if (roadManager == null) {
                                Log.e(TAG, "null");
                            } else {
                                Log.d(TAG, "ROUTE STARTED1");

                                ArrayList<GeoPoint> waypoints = new ArrayList<GeoPoint>();
                                waypoints.add(userLoc);
                                waypoints.add(new GeoPoint(lat, lng));
                                ((OSRMRoadManager) roadManager).setMean(OSRMRoadManager.MEAN_BY_FOOT);
                                Log.d(TAG, "ROUTE STARTED2");


                                Road road = roadManager.getRoad(waypoints);
                                Log.d(TAG, "ROUTE STARTED3");

                                Polyline roadOverlay = RoadManager.buildRoadOverlay(road);
                                roadOverlay.setColor(Color.rgb(157, 92, 52));
                                map.getOverlays().add(roadOverlay);
                                binding.progress.setVisibility(View.GONE);
                                binding.loading.setVisibility(View.GONE);
                                binding.cancel.setVisibility(View.VISIBLE);
                                binding.map.setVisibility(View.VISIBLE);
                            }
                        }
                        catch(Exception e){
                            Log.d(TAG, e.getMessage());

                        }

                    }
                });
            }
        }.start();
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
                .setSmallIcon(R.drawable.logo)
                .setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(true);
    }
    private void removing() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
    }

    public void locationUpdates(){
        try{
            fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(RouteActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                ActivityCompat.requestPermissions(RouteActivity.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 2);
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
                            HashMap<String, Object> updt = new HashMap<>();
                            updt.put("latitude", location.getLatitude());
                            updt.put("longitude", location.getLongitude());
                            if (0.05>distance(location.getLatitude(), location.getLongitude(),
                                    parseDouble(preferencesManager.getString(Constants.KEY_COFFEESHOP_LATITUDE)),
                                    parseDouble(preferencesManager.getString(Constants.KEY_COFFEESHOP_LONGITUDE))))
                            {
                                if (preferencesManager.getString(Constants.KEY_VISITED_ID).equals("")){
                                    database.collection(Constants.KEY_COLLECTION_COFFEE_SHOPS).document(id)
                                            .collection(Constants.KEY_COLLECTION_USERS)
                                            .document(preferencesManager.getString(Constants.KEY_USER_ID)).update("status", "active");

                                    preferencesManager.putBoolean(Constants.KEY_IS_ACTIVATED, true);
                                    preferencesManager.putBoolean(Constants.KEY_IS_GOING, false);
                                    fusedLocationProviderClient.removeLocationUpdates(this);
                                    startActivity( new Intent(getApplicationContext(), ActivatedActivity.class));
                                    finish();
                                }
                                else {
                                    showNotification("Поздравляем!", "Вы достигли точки, удачного кофепития)");
                                    preferencesManager.putBoolean(Constants.KEY_IS_GOING, false);
                                    preferencesManager.putString(Constants.KEY_VISITED_ID,"");
                                    preferencesManager.putString(Constants.KEY_VISITOR_ID,"");

                                    FirebaseFirestore database = FirebaseFirestore.getInstance();

                                    database.collection(Constants.KEY_COLLECTION_COFFEE_SHOPS).document(id)
                                            .update("activated", false);

                                    database.collection(Constants.KEY_COLLECTION_COFFEE_SHOPS).document(id)
                                            .collection(Constants.KEY_COLLECTION_USERS)
                                            .document(preferencesManager.getString(Constants.KEY_USER_ID)).delete();
                                    database.collection(Constants.KEY_COLLECTION_VISITS)
                                            .whereEqualTo(Constants.KEY_VISITOR_ID, preferencesManager.getString(Constants.KEY_USER_ID))
                                            .get()
                                            .addOnCompleteListener(task->{
                                                for(QueryDocumentSnapshot queryDocumentSnapshot:task.getResult()){
                                                    database.collection(Constants.KEY_COLLECTION_VISITS).document(queryDocumentSnapshot.getId()).delete();
                                                }
                                            });
                                    fusedLocationProviderClient.removeLocationUpdates(this);


                                    Intent i = new Intent(getApplicationContext(), MapActivity.class);
                                    startActivity(i);

                                }
                            }

                            preferencesManager.putString(Constants.KEY_USER_LATITUDE,location.getLatitude()+"");
                            preferencesManager.putString(Constants.KEY_USER_LONGITUDE,location.getLongitude()+"");

                            FirebaseFirestore database = FirebaseFirestore.getInstance();

                            if(!preferencesManager.getBoolean(Constants.KEY_IS_VISITED)){
                                // deleteVisits(database, preferencesManager);
                            }
                            else{
                                database.collection(Constants.KEY_COLLECTION_COFFEE_SHOPS)
                                        .document(preferencesManager.getString(Constants.KEY_COFFEESHOP_ID))
                                        .collection(Constants.KEY_COLLECTION_USERS)
                                        .document(preferencesManager.getString(Constants.KEY_USER_ID))
                                        .update(updt);
                            }
                        }
                    }
                    else{

                        preferencesManager.putBoolean(Constants.KEY_IS_GOING, false);
                        FirebaseFirestore db = FirebaseFirestore.getInstance();

                        db.collection(Constants.KEY_COLLECTION_COFFEE_SHOPS).document(id)
                                .update("activated", false);

                        db.collection(Constants.KEY_COLLECTION_COFFEE_SHOPS).document(id)
                                .collection(Constants.KEY_COLLECTION_USERS)
                                .document(preferencesManager.getString(Constants.KEY_USER_ID)).delete();

                        Intent i = new Intent(getApplicationContext(), MapActivity.class);
                        startActivity(i);

                    }
                }

            };

            fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
        }
        catch (Exception e){
            makeToast(e.getMessage());
        }
    }
}



