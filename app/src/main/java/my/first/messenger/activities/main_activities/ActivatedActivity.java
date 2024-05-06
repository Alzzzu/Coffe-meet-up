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

import java.util.ArrayList;
import java.util.List;

import my.first.messenger.R;
import my.first.messenger.activities.adapters.UserAdapter;
import my.first.messenger.activities.listeners.OnSwipeTouchListener;
import my.first.messenger.activities.listeners.UsersListener;
import my.first.messenger.activities.models.User;
import my.first.messenger.activities.services.TimerService;
import my.first.messenger.activities.utils.Constants;
import my.first.messenger.activities.utils.PreferencesManager;
import my.first.messenger.databinding.ActivityActivatedBinding;

public class ActivatedActivity extends FragmentActivity implements UsersListener {
    private ActivityActivatedBinding binding;
    private PreferencesManager preferencesManager;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationRequest locationRequest;
    private FirebaseFirestore database;
    private final String TAG = "AZAZA";
    private String id;
    ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityActivatedBinding.inflate(getLayoutInflater());
        preferencesManager = new PreferencesManager(getApplicationContext());
        database = FirebaseFirestore.getInstance();
        startService(new Intent(getApplicationContext(), TimerService.class));
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
      //  Log.d(TAG, "startServiceViaWorker called");
       // String UNIQUE_WORK_NAME = "StartMyServiceViaWorker";
        //WorkManager workManager = WorkManager.getInstance(this);
       // PeriodicWorkRequest request =
        //        new PeriodicWorkRequest.Builder(
         //               MyWorker.class,
            ///            2,
          //              TimeUnit.MINUTES)
               //         .build();

        //workManager.enqueueUniquePeriodicWork(UNIQUE_WORK_NAME, ExistingPeriodicWorkPolicy.KEEP, request);
       // Intent intent = new Intent(getApplicationContext(), TimerService.class);
        //startService(intent);
        locationUpdates();
        setListeners();
    }
    private void init(){
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getApplicationContext());
        database = FirebaseFirestore.getInstance();
        id = preferencesManager.getString(Constants.KEY_COFFEESHOP_ID);
        Log.d(TAG, id);
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
    private void getActiveUsers(){
        FirebaseFirestore database= FirebaseFirestore.getInstance();
        database.collection(Constants.KEY_COLLECTION_COFFEE_SHOPS)
                .whereEqualTo("activated", true)
                .get()
                .addOnCompleteListener(task->{
                    if(task.isSuccessful()&&task.getResult()!=null) {
                        List<User> users = new ArrayList<>();


                        for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {

                            double lat = queryDocumentSnapshot.getDouble("latitude");
                            double lng = queryDocumentSnapshot.getDouble("longitude");
                            if(2>distance(parseDouble(preferencesManager.getString(Constants.KEY_USER_LATITUDE)),parseDouble(preferencesManager.getString(Constants.KEY_USER_LONGITUDE)),lat, lng)){///getLocationFromAddress(queryDocumentSnapshot.getString(Constants.KEY_ADDRESS)).getLatitude(), getLocationFromAddress(queryDocumentSnapshot.getString(Constants.KEY_ADDRESS)).getLongitude())) {
                                database.collection(Constants.KEY_COLLECTION_COFFEE_SHOPS)
                                    .document(queryDocumentSnapshot.getId())
                                    .collection(Constants.KEY_COLLECTION_USERS)
                                        .whereEqualTo("status", "active")
                                    .whereEqualTo(Constants.KEY_GENDER, "")//preferencesManager.getString(Constants.KEY_SEARCH_GENDER))
                                    .get()
                                    .addOnCompleteListener(task2 -> {
                                        if(task2.isSuccessful()&&task2.getResult()!=null){
                                            for(QueryDocumentSnapshot queryDocumentSnapshot2 : task2.getResult()){
                                                if (queryDocumentSnapshot2.getLong(Constants.KEY_AGE)<=preferencesManager.getLong(Constants.KEY_SEARCH_MAX_AGE)
                                                    &&queryDocumentSnapshot2.getLong(Constants.KEY_AGE)>=preferencesManager.getLong(Constants.KEY_SEARCH_MIN_AGE)
                                               && !queryDocumentSnapshot2.getString(Constants.KEY_GENDER).equals(preferencesManager.getString(Constants.KEY_SEARCH_GENDER))) {

                                                    User user = new User();
                                                user.id=queryDocumentSnapshot2.getString(Constants.KEY_USER_ID);
                                                user.name=queryDocumentSnapshot2.getString(Constants.KEY_NAME);
                                                user.image=queryDocumentSnapshot2.getString(Constants.KEY_IMAGE);
                                                user.age=queryDocumentSnapshot2.getLong(Constants.KEY_AGE).toString();
                                                users.add(user);
                                                }
                                            }
                                        }
                                        UserAdapter userAdapter = new UserAdapter(users, this);
                                        binding.usersRecycleView.setAdapter(userAdapter);
                                        binding.usersRecycleView.setVisibility(View.VISIBLE);
                                    });
                                if (users.size()>0){
                                    UserAdapter userAdapter = new UserAdapter(users, this);
                                    binding.usersRecycleView.setAdapter(userAdapter);
                                    binding.usersRecycleView.setVisibility(View.VISIBLE);


                                }

                            }
                        }
                }});

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

    private void getMeetingUsers(){
        FirebaseFirestore database= FirebaseFirestore.getInstance();
        database.collection(Constants.KEY_COLLECTION_MEET_UP_OFFERS)
                .whereEqualTo(Constants.KEY_VISITED_ID, preferencesManager.getString(Constants.KEY_USER_ID))
               // .whereNotEqualTo(Constants.KEY_GENDER,  preferencesManager.getString(Constants.KEY_SEARCH_GENDER))
                .get()
                  .addOnCompleteListener(task->{
                    if(task.isSuccessful()&&task.getResult()!=null) {

                        for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                            ArrayList<User> users = new ArrayList<>();
                                for( QueryDocumentSnapshot queryDocumentSnapshot2 : task.getResult()){
                                    User user = new User();
                                    user.id = queryDocumentSnapshot.getString(Constants.KEY_VISITOR_ID);
                                    user.name =queryDocumentSnapshot.getString(Constants.KEY_VISITOR_NAME);
                                    user.image =queryDocumentSnapshot.getString(Constants.KEY_VISITOR_IMAGE);
                                    users.add(user);
                                    }
                                    UserAdapter userAdapter = new UserAdapter(users, this);
                                    binding.usersRecycleView.setAdapter(userAdapter);
                                    binding.usersRecycleView.setVisibility(View.VISIBLE);
                            }
                        }
                });
    }
    private void getUsers(){
        FirebaseFirestore database= FirebaseFirestore.getInstance();
        database.collection(Constants.KEY_COLLECTION_USERS)
                .get()
                .addOnCompleteListener(task -> {
                    String currentUserId = preferencesManager.getString(Constants.KEY_USER_ID);
                    if(task.isSuccessful()&&task.getResult()!=null){
                        List<User> users = new ArrayList<>();
                        for(QueryDocumentSnapshot queryDocumentSnapshot:task.getResult()){
                            if(currentUserId.equals(queryDocumentSnapshot.getId())){
                                continue;
                            }
                            User user = new User();
                            user.name=queryDocumentSnapshot.getString(Constants.KEY_NAME);
                            user.age=(queryDocumentSnapshot.getLong(Constants.KEY_AGE)).toString();
                            user.image = queryDocumentSnapshot.getString(Constants.KEY_IMAGE);
                            user.about = queryDocumentSnapshot.getString(Constants.KEY_ABOUT);
                            user.hobby = queryDocumentSnapshot.getString(Constants.KEY_HOBBIES);
                            user.token = queryDocumentSnapshot.getString(Constants.KEY_FCM_TOKEN);
                            user.id = queryDocumentSnapshot.getId();
                            users.add(user);
                        }
                        if (users.size()>0){
                            UserAdapter userAdapter = new UserAdapter(users, this);
                            binding.usersRecycleView.setAdapter(userAdapter);
                            binding.usersRecycleView.setVisibility(View.VISIBLE);
                        }

                    }
                });
        }

    @Override
    public void onUserClick(User user) {
    }
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
                            if (0.1 < distance(location.getLatitude(), location.getLongitude(), parseDouble(preferencesManager.getString(Constants.KEY_COFFEESHOP_LATITUDE)), parseDouble(preferencesManager.getString(Constants.KEY_COFFEESHOP_LONGITUDE)))) {
                                deleteActivation(database, preferencesManager);
                            }
                            else{
                                Log.d(TAG, location.toString());
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