package my.first.messenger.activities.main_activities;

import static android.widget.Toast.makeText;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;

import org.osmdroid.util.GeoPoint;

import java.util.HashMap;

import my.first.messenger.R;
import my.first.messenger.activities.utils.Constants;
import my.first.messenger.activities.utils.PreferencesManager;

public class UserLocationActivity extends AppCompatActivity {

    // initializing
    // FusedLocationProviderClient
    // object
    PreferencesManager preferencesManager;
    FusedLocationProviderClient mFusedLocationClient;

    // Initializing other items
    // from layout file
   // TextView latitudeTextView, longitTextView;
    int PERMISSION_ID = 44;
    Button butt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_location);
        preferencesManager = new PreferencesManager(this);

   //     latitudeTextView = findViewById(R.id.latTextView);
  //      longitTextView = findViewById(R.id.lonTextView);
        FirebaseFirestore database = FirebaseFirestore.getInstance();

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // method to get the location
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
     //   getLastLocation();
    }
    @Override
    public boolean onCreateOptionsMenu (Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId()==R.id.sign_out){

                Toast.makeText(this, "You clicked new group", Toast.LENGTH_SHORT).show();
        }
        else if (item.getItemId()==R.id.edit){
                Toast.makeText(this, "You clicked new groin", Toast.LENGTH_SHORT).show();
        }
        return true;
    }

    @SuppressLint("MissingPermission")
    private void getLastLocation() {
        // check if permissions are given
        if (checkPermissions()) {

            // check if location is enabled
            if (isLocationEnabled()) {

                // getting last
                // location from
                // FusedLocationClient
                // object
                mFusedLocationClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        Location location = task.getResult();
                        if (location == null) {
                            requestNewLocationData();
                        } else {
                            //latitudeTextView.setText(location.getLatitude() + "");
                            // longitTextView.setText(location.getLongitude() + "");
                            preferencesManager.putString(Constants.KEY_USER_LONGITUDE,location.getLongitude()+"");
                            preferencesManager.putString(Constants.KEY_USER_LATITUDE,location.getLatitude()+"");
                            Intent i = new Intent(getApplicationContext(), MapActivity.class);
                            double loc[] = {location.getLatitude(), location.getLongitude()};
                            i.putExtra("location", loc);
                            startActivity(i);
                        }
                    }
                });
            } else {
                makeText(this, "Please turn on" + " your location...", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        } else {
            // if permissions aren't available,
            // request for permissions
            requestPermissions();
        }
    }

    @SuppressLint("MissingPermission")
    private void requestNewLocationData() {

        // Initializing LocationRequest
        // object with appropriate methods
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(5);
        mLocationRequest.setFastestInterval(0);
        mLocationRequest.setNumUpdates(1);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
    }

    private LocationCallback mLocationCallback = new LocationCallback() {

        @Override
        public void onLocationResult(LocationResult locationResult) {
            Location mLastLocation = locationResult.getLastLocation();
            Intent i = new Intent(getApplicationContext(), MapActivity.class);
            double loc[] = {mLastLocation.getLatitude(), mLastLocation.getLongitude()};
            i.putExtra("location", loc);
            preferencesManager.putString(Constants.KEY_USER_LONGITUDE,mLastLocation.getLongitude()+"");
            preferencesManager.putString(Constants.KEY_USER_LATITUDE,mLastLocation.getLatitude()+"");

            startActivity(i);
        }
    };

    // method to check for permissions
    private boolean checkPermissions() {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;

        // If we want background location
        // on Android 10.0 and higher,
        // use:
        // ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    // method to request for permissions
    private void requestPermissions() {
        ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_ID);
    }

    // method to check
    // if location is enabled
    private boolean isLocationEnabled() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    // If everything is alright then
    @Override
    public void
    onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_ID) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLastLocation();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (checkPermissions()) {
            getLastLocation();
        }
    }
}
