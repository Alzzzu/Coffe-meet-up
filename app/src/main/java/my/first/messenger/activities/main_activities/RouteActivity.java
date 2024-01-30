package my.first.messenger.activities.main_activities;

import static java.lang.Double.parseDouble;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
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
    private ActivityRouteBinding binding;
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
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_1);
        Intent intent = new Intent(this, GPSTracker.class);
        startService(intent);

        // Classes
        preferencesManager = new PreferencesManager(getApplicationContext());

        init();
        settingMap();
        setListeners();
  //      threader();
    }

    private void init(){
        id = getIntent().getStringExtra(Constants.KEY_COFFEESHOP_ID);
        userLoc = new GeoPoint(parseDouble(preferencesManager.getString(Constants.KEY_USER_LATITUDE)), parseDouble(preferencesManager.getString(Constants.KEY_USER_LONGITUDE)));
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection(Constants.KEY_COLLECTION_COFFEE_SHOPS).document(id).get().addOnCompleteListener(task -> {
            //  String currentUserId = preferenceManager.getString(Constants.KEY_USER_ID);
        });

    }
    private void setPoint(double lat, double lng){
        coffeeshopLoc = new GeoPoint(lat, lng);
    }
    private void settingMap(){
        Context ctx = getApplicationContext();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));
        map =  binding.map;


        map.setMinZoomLevel(15.0);
        map.setTileSource(TileSourceFactory.MAPNIK);
        mapController = map.getController();
        mapController.setZoom(15);
        map.setBuiltInZoomControls(true);
        map.setMultiTouchControls(true);
        mapController.setCenter(userLoc);//new GeoPoint(parseDouble(preferencesManager.getString(Constants.KEY_USER_LATITUDE)), parseDouble(preferencesManager.getString(Constants.KEY_USER_LONGITUDE))));
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(ctx);
        this.mLocationOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(this),map);
        this.mLocationOverlay.enableMyLocation();
        this.mLocationOverlay.enableFollowLocation();
        map.getOverlays().add(this.mLocationOverlay);
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection(Constants.KEY_COLLECTION_COFFEE_SHOPS).document(id).get().addOnCompleteListener(task -> {
            //  String currentUserId = preferenceManager.getString(Constants.KEY_USER_ID);
            if(task.isSuccessful()&&task.getResult()!=null){
                double lng = task.getResult().getDouble("longitude");
                double lat = task.getResult().getDouble("latitude");
                coffeeshopLoc = new GeoPoint(lng,lat);

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

    private void setListeners(){
        binding.cancel.setOnClickListener(v->{

            FirebaseFirestore db = FirebaseFirestore.getInstance();


            db.collection("coffeeshops").document(id)
                    .update("activated", false);

            db.collection("coffeeshops").document(id)
                    .collection(Constants.KEY_COLLECTION_USERS)
                    .document(preferencesManager.getString(Constants.KEY_USER_ID)).delete();

           Intent i = new Intent(getApplicationContext(), MapActivity.class);
           startActivity(i);

        });

        binding.finish.setOnClickListener(v->{
            AlertFragment alert = new AlertFragment();
            makeToast(distance(coffeeshopLoc.getLatitude(), parseDouble(preferencesManager.getString(Constants.KEY_USER_LATITUDE)), coffeeshopLoc.getLongitude(), parseDouble(preferencesManager.getString(Constants.KEY_USER_LONGITUDE)))+"");
              if(distance(coffeeshopLoc.getLatitude(), parseDouble(preferencesManager.getString(Constants.KEY_USER_LATITUDE)), coffeeshopLoc.getLongitude(), parseDouble(preferencesManager.getString(Constants.KEY_USER_LONGITUDE)))<1){
                  FragmentManager manager = getSupportFragmentManager();
                  alert.show(manager, "dialog");
                  FirebaseFirestore db = FirebaseFirestore.getInstance();
                  //db.collection("coffeeshops").document(id)
                    //      .collection(Constants.KEY_COLLECTION_USERS)
                      //    .document(preferencesManager.getString(Constants.KEY_USER_ID)).delete();
              }
              else{
                  makeToast("Чтобы завершить путь необходимо достигнуть точку");
              }
           // Intent i = new Intent(getApplicationContext(), MapActivity.class);
           // startActivity(i);


        });
    }




    private void routing(double lat, double lng){
        new Thread(){
            public void run(){
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        GeoPoint g = new GeoPoint(new GeoPoint(parseDouble(preferencesManager.getString(Constants.KEY_USER_LATITUDE)), parseDouble(preferencesManager.getString(Constants.KEY_USER_LONGITUDE))));
                        RoadManager roadManager = new OSRMRoadManager(getApplicationContext(), "coffeeshopperhere");
                        ArrayList<GeoPoint> waypoints = new ArrayList<GeoPoint>();
                        waypoints.add(g);
                        waypoints.add( new GeoPoint(lat, lng));
                        ((OSRMRoadManager)roadManager).setMean(OSRMRoadManager.MEAN_BY_FOOT);

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
    private void discard () {
        new Thread(){
            public void run(){
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

    public static double distance(double lat1,
                                  double lat2, double lon1,
                                  double lon2)
    {

        // The math module contains a function
        // named toRadians which converts from
        // degrees to radians.
        lon1 = Math.toRadians(lon1);
        lon2 = Math.toRadians(lon2);
        lat1 = Math.toRadians(lat1);
        lat2 = Math.toRadians(lat2);

        // Haversine formula
        double dlon = lon2 - lon1;
        double dlat = lat2 - lat1;
        double a = Math.pow(Math.sin(dlat / 2), 2)
                + Math.cos(lat1) * Math.cos(lat2)
                * Math.pow(Math.sin(dlon / 2),2);

        double c = 2 * Math.asin(Math.sqrt(a));

        // Radius of earth in kilometers. Use 3956
        // for miles
        double r = 6371;

        // calculate the result
        return(c);
    }

    private  void makeToast(String message){
        Toast.makeText(getApplicationContext(),message, Toast.LENGTH_SHORT).show();
    }

}