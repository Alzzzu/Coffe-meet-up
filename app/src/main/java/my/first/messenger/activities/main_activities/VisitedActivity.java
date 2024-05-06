package my.first.messenger.activities.main_activities;

import static java.lang.Double.parseDouble;
import static my.first.messenger.activities.utils.Functions.deleteVisits;
import static my.first.messenger.activities.utils.Functions.distance;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.bumptech.glide.Glide;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import my.first.messenger.R;
import my.first.messenger.activities.models.User;
import my.first.messenger.activities.utils.Constants;
import my.first.messenger.activities.utils.PreferencesManager;
import my.first.messenger.databinding.ActivityVisitedBinding;

public class VisitedActivity extends AppCompatActivity {
    private ActivityVisitedBinding binding;
    private FirebaseFirestore database;
    private PreferencesManager preferencesManager;
    private final String TAG= "VisitedActivityTAG";
    private StorageReference storageReference;
    private Query query;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationRequest locationRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityVisitedBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Log.d(TAG,"LOADED 1");

        init();
        loadUsersDetail();
        setListeners();
        locationUpdate();
        locationUpdates();
    }
    private void init(){

        database = FirebaseFirestore.getInstance();
        preferencesManager = new PreferencesManager(getApplicationContext());
        FirebaseApp.initializeApp(VisitedActivity.this);
        storageReference = FirebaseStorage.getInstance().getReference();
        query = database.collection(Constants.KEY_COLLECTION_VISITS)
                .whereEqualTo(Constants.KEY_VISITOR_ID, preferencesManager.getString(Constants.KEY_VISITOR_ID))
                .whereEqualTo(Constants.KEY_VISITED_ID, preferencesManager.getString(Constants.KEY_USER_ID));
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getApplicationContext());
    }
    private void locationUpdate(){
            query.addSnapshotListener(eventListener);
    }
    private void loadUsersDetail(){
        storageReference.child("images/"+preferencesManager.getString(Constants.KEY_VISITOR_ID)+"/0").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Glide.with(getApplicationContext()).load(uri).into(binding.visitorImage);
            }
        });

        query.get().addOnCompleteListener(task->{
           for(QueryDocumentSnapshot queryDocumentSnapshot: task.getResult()){
               binding.visitorName.setText(queryDocumentSnapshot.getString(Constants.KEY_VISITOR_NAME));
           }
        });
        Log.d(TAG,"LOADED");


    }
    private void setListeners(){

        // cancelling meeting

        binding.cancel.setOnClickListener(v->{

            preferencesManager.putBoolean(Constants.KEY_IS_VISITED, false);
            preferencesManager.putString(Constants.KEY_VISITOR_ID,"");
            preferencesManager.putString(Constants.KEY_COFFEESHOP_ID,"");

            deleteVisits(database, preferencesManager);
            Intent intent = new Intent(getApplicationContext(), MapActivity.class);
            startActivity(intent);
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
                    finish();

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
        Log.d(TAG,"LISTENERS STARTED");

    }
    private final EventListener<QuerySnapshot> eventListener = (value, error) -> {
        Log.d(TAG,"STARTED");

        if (error != null) {
            return;
        }

        if (value != null) {

            for(DocumentChange documentChange: value.getDocumentChanges()){

                if (documentChange.getType() == DocumentChange.Type.ADDED) {
                    Log.d(TAG,"ADDED");
                    if (documentChange.getDocument().getDouble("latitude")!=null){
                        double res = distance(documentChange.getDocument().getDouble("latitude"), documentChange.getDocument().getDouble("longitude"), parseDouble(preferencesManager.getString(Constants.KEY_USER_LATITUDE)), parseDouble(preferencesManager.getString(Constants.KEY_USER_LONGITUDE)));
                        binding.distance.setText((int) (res * 1000) + " м");
                    }

                }
                if (documentChange.getType() == DocumentChange.Type.MODIFIED){
                    Log.d(TAG,"MOD");
                     double res = distance(documentChange.getDocument().getDouble("latitude"), documentChange.getDocument().getDouble("longitude"), parseDouble(preferencesManager.getString(Constants.KEY_USER_LATITUDE)), parseDouble(preferencesManager.getString(Constants.KEY_USER_LONGITUDE)));
                     binding.distance.setText((int)(res*1000)+" м");

                }
                if (documentChange.getType() == DocumentChange.Type.REMOVED){
                    Log.d(TAG,"REMOVED");


                    preferencesManager.putBoolean(Constants.KEY_IS_VISITED, false);
                    preferencesManager.putBoolean(Constants.KEY_IS_ACTIVATED, false);
                    preferencesManager.putString(Constants.KEY_VISITED_ID,"");
                    preferencesManager.putString(Constants.KEY_VISITOR_ID,"");

               //     showNotification("Вас забулили!",documentChange.getDocument().getString(Constants.KEY_VISITOR_NAME)+ " отменил(а) встречу");

                    Intent intent = new Intent(getApplicationContext(), MapActivity.class);
                    startActivity(intent);
                    finish();

                }
            }
        }
    };

    // notifications
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
        Intent intent = new Intent(getApplicationContext(), RouteActivity.class);
        PendingIntent pi = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(pi);
        mNotificationManager.notify(0, mBuilder.build());
    }

    public void locationUpdates() {
        try {

            fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(VisitedActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                ActivityCompat.requestPermissions(VisitedActivity.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 2);
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
                                preferencesManager.putBoolean(Constants.KEY_IS_VISITED, false);
                                preferencesManager.putString(Constants.KEY_VISITOR_ID,"");
                                preferencesManager.putString(Constants.KEY_COFFEESHOP_ID,"");

                                database.collection(Constants.KEY_COLLECTION_VISITS)
                                        .whereEqualTo(Constants.KEY_VISITED_ID, preferencesManager.getString(Constants.KEY_USER_ID))
                                        .get()
                                        .addOnCompleteListener(task->{

                                            for(QueryDocumentSnapshot queryDocumentSnapshot:task.getResult()){
                                                database.collection(Constants.KEY_COLLECTION_VISITS).document(queryDocumentSnapshot.getId()).delete();
                                            }

                                            Intent intent = new Intent(getApplicationContext(), MapActivity.class);
                                            startActivity(intent);
                                            finish();

                                        });
                                    }

                            else{
                                Log.d(TAG, location.toString());
                            }
                        }
                    }}}
                    ;
            fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());

        }catch (Exception e){
            Log.e(TAG, e.getMessage());

        }

}
}