package my.first.messenger.activities.main_activities;

import static java.lang.Double.parseDouble;
import static my.first.messenger.activities.utils.Functions.deleteActivation;
import static my.first.messenger.activities.utils.Functions.distance;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

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

import my.first.messenger.R;
import my.first.messenger.activities.listeners.OnSwipeTouchListener;
import my.first.messenger.activities.models.User;
import my.first.messenger.activities.utils.Constants;
import my.first.messenger.activities.utils.PreferencesManager;
import my.first.messenger.databinding.ActivityActivatedBinding;

public class ActivatedActivity extends FragmentActivity  {
    private ActivityActivatedBinding binding;
    private PreferencesManager preferencesManager;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationRequest locationRequest;
    private FirebaseFirestore database;
    private final String TAG = "ActivatedActivityTAG";
    private String id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityActivatedBinding.inflate(getLayoutInflater());
        preferencesManager = new PreferencesManager(getApplicationContext());
        database = FirebaseFirestore.getInstance();

        FirebaseFirestore.getInstance().collection(Constants.KEY_COLLECTION_VISITS).whereEqualTo(Constants.KEY_VISITED_ID,preferencesManager.getString(Constants.KEY_USER_ID))
                .get().addOnCompleteListener(task->{
                    for(QueryDocumentSnapshot queryDocumentSnapshot:task.getResult()){
                        preferencesManager.putBoolean(Constants.KEY_IS_ACTIVATED, false);
                        preferencesManager.putBoolean(Constants.KEY_IS_VISITED, true);
                        preferencesManager.putString(Constants.KEY_VISITOR_ID, queryDocumentSnapshot.getString(Constants.KEY_VISITOR_ID));
                    }
                });
        database.collection(Constants.KEY_COLLECTION_VISITS)
                .whereEqualTo(Constants.KEY_VISITED_ID, preferencesManager.getString(Constants.KEY_USER_ID))
                .addSnapshotListener(eventListener);
        setContentView(binding.getRoot());
        init();
        locationUpdates();
        setListeners();
    }
    private void init(){
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getApplicationContext());
        database = FirebaseFirestore.getInstance();
        id = preferencesManager.getString(Constants.KEY_COFFEESHOP_ID);
    }
    private void setListeners(){
        binding.getRoot().getRootView().setOnTouchListener(new OnSwipeTouchListener(ActivatedActivity.this) {
            public void onSwipeBottom() {
                binding.cancel.setVisibility(View.VISIBLE);
                binding.cancel.animate().translationY(10);
            }
            public void onSwipeTop() {
                binding.cancel.animate().translationY(-10);
                binding.cancel.setVisibility(View.GONE);
            }
        });
        binding.cancel.setOnClickListener(v->{
            try{
                deleteActivation(database, preferencesManager);
            }
            catch(Exception e){
                Log.e(TAG, e.getMessage());
            }

            Intent i = new Intent(getApplicationContext(), MapActivity.class);
            startActivity(i);
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
    private final EventListener<QuerySnapshot> eventListener = (value, error) -> {
        if (error != null) {
            return;
        }
        if (value != null) {
            for (DocumentChange documentChange : value.getDocumentChanges()) {
                if (documentChange.getType() == DocumentChange.Type.ADDED) {
                    preferencesManager.putBoolean(Constants.KEY_IS_ACTIVATED, false);
                    preferencesManager.putBoolean(Constants.KEY_IS_VISITED, true);
                    preferencesManager.putString(Constants.KEY_VISITOR_ID, documentChange.getDocument().getString(Constants.KEY_VISITOR_ID));
                    deleteActivation(database, preferencesManager);
                    startActivity(new Intent(getApplicationContext(), VisitedActivity.class));
                    finish();
                }

            }
        }
    };
    public void locationUpdates() {
        try {
            fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(ActivatedActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                ActivityCompat.requestPermissions(ActivatedActivity.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 2);
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
                            try{
                            if (0.1 < distance(location.getLatitude(), location.getLongitude(), parseDouble(preferencesManager.getString(Constants.KEY_COFFEESHOP_LATITUDE)), parseDouble(preferencesManager.getString(Constants.KEY_COFFEESHOP_LONGITUDE)))) {
                                fusedLocationProviderClient.removeLocationUpdates(this);
                                deleteActivation(database, preferencesManager);
                                startActivity(new Intent(getApplicationContext(), MapActivity.class));
                                finish();
                            }
                            else{
                                Log.d(TAG, location.toString());
                            }
                            }
                            catch (Exception e){
                                Log.wtf(TAG,e.getMessage());
                            }
                        }
                    }
                }
            };
            fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
            }
        catch (Exception e){
            Log.e(TAG, e.getMessage());
        }
    }
}