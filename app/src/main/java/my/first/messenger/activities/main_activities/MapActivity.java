package my.first.messenger.activities.main_activities;

import android.Manifest;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.work.BackoffPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.osmdroid.util.GeoPoint;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import my.first.messenger.R;
import my.first.messenger.activities.models.User;
import my.first.messenger.activities.services.BackgroundLocationWork;
import my.first.messenger.activities.services.LocationManager;
import my.first.messenger.activities.utils.Constants;
import my.first.messenger.activities.utils.PreferencesManager;
import my.first.messenger.databinding.ActivityMapBinding;



public class MapActivity extends FragmentActivity {

    ActivityMapBinding binding;
    FusedLocationProviderClient mFusedLocationClient;
    BottomNavigationView bottomNavigationView;
    // Идентификатор уведомления
    private static final int NOTIFY_ID = 101;

    // Идентификатор канала
    private static final int PERMISSION_1 = 1;

    private PreferencesManager preferencesManager;
    private String[] backgroundLocationPermissions =
            {Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION};
    private LocationManager locationManager;
    private WorkRequest backgroundWorkRequest;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_1);
        locationManager = LocationManager.getInstance(this);
        binding = ActivityMapBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.bottomNavigation.setSelectedItemId(R.id.map);
        init();
        setListeners();
   //     startLocationWork();
    }

    public void makeToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }
    public void init(){
        preferencesManager = new PreferencesManager(getApplicationContext());
            // FirebaseFirestore db = FirebaseFirestore.getInstance();
       //   String[] a = {"ТРЦ ‎Европейский, 1 этаж. Площадь Киевского вокзала, 2","ТРЦ ‎Европейский, 4 этаж. Площадь Киевского вокзала, 2","ТРЦ Ереван Плаза. Большая Тульская ул., д.13","ТРЦ Март, Большая Семеновская ул., 17А","ТРЦ Океания. Кутузовский просп., 57","ТРЦ Свиблово. Снежная ул., 27","ТЦ 'Цитрус'. Московская ул., 14А, Химки","Профсоюзная ул., 102, стр. 1","Большой Овчинниковский пер., 16","Барклая ул., 10","ТЦ «‎Варшавский». Варшавское., 87б, 3 этаж","Венёвская ул., 6","Головинское ш., 5, корп. 1","ТЦ «‎Город», 1 этаж. ш., Энтузиастов, 12, корп. 2","ТЦ «‎Город», 3 этаж. ш., Энтузиастов, 12, корп. 2","ТЦ «‎Город», 2 этаж. Рязанский просп., 2 корп. 2","Большая Тульская ул., 13","Большая Семёновская ул., 20","ТЦ «‎Калита». Новоясеневский просп., 7","Профсоюзная ул., 61А","ТЦ «‎Колумбус». Кировоградская ул., 13А","ТЦ Колумбус. Красного Маяка ул., 2Б","ТЦ «‎Круг», Старокачаловская ул., 5А","Тушинская ул., 16, стр. 2","ТЦ Ладья. Дубравная ул., 34/29","1-й Покровский пр., 5, Котельники","1-й Покровский пр., 1, Котельники","8-й микрорайон, Новокуркино, 4, Химки","ТЦ «‎Облака». Ореховый б-р., 22А","Киевское ш., 22-ой км, 4, стр. 1","Таганская ул., 2","Декабристов ул., 12","ТЦ «‎Штаер». Балаклавский просп., 5А, стр. 10","Кавказский бульвар, 17","ТЦ XL. Дмитровское ш., 89","ТЦ Ашан. Пролетарский просп., 30","ТЦ БУМ. Перерва ул., 43, корп. 1","ТЦ Капитолий. Большая Серпуховская ул., 45, Подольск","ТЦ «‎Колумбус». Кировоградская ул., 13А","ТЦ Маяк. Рязанский просп., 99А","ТЦ Метрополис, 1 этаж. Ленинградское ш. 16А стр. 4","ТЦ Метрополис, 2 этаж. Ленинградское ш. 16А стр. 4","ТЦ Метрополис, 3 этаж. Ленинградское ш. 16А стр. 4","ТЦ Речной, Фестивальная ул., 2Б","ТЦ Щелчок. Уральская ул., 1А","ул. Ленинская Слобода, д. 19","ул. Маросейка д 4\2 стр. 1","улица Баррикадная, дом 12/2, строение 3","Усачёва ул., 29, корп. 1","Хоромный тупик, 2/6","Цветной бул., 25, стр. 1","Цветной бул., 7, стр. 1","Цветной бульвар, 21с1","ЦДМ, Театральный пр., 5, стр. 1","Шаболовка ул., 34, стр. 3","Шаболовка ул., д. 30/12","ш. Энтузиастов, 31","Щепкина ул., 47, стр. 1","Южная ул., 2, Реутов","Ярцевская ул., 25А"};
        // for(String i:a){
          // HashMap<String, Object> b = new HashMap<>();
           //GeoPoint g = new GeoPoint(parseDouble(preferenceManager.getString(Constants.KEY_USER_LATITUDE)), parseDouble(preferenceManager.getString(Constants.KEY_USER_LONGITUDE)));
           // b.put("address","ул.Невзоровых 66А");
           // b.put("name", "PANDA");
           // b.put("longitude", g.getLongitude());
           // b.put("latitude", g.getLatitude());
           // db.collection("coffeeshops").add(b);

//     }
    }
    private void setListeners(){
        binding.bottomNavigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                if (item.getItemId()==R.id.profile){
                    Intent intent = new Intent( getApplicationContext(), ProfileActivity.class);
                    User user = new User();
                    user.id =  preferencesManager.getString(Constants.KEY_USER_ID);
                    user.about = preferencesManager.getString(Constants.KEY_ABOUT);
                    user.hobby = preferencesManager.getString(Constants.KEY_HOBBIES);
                    user.name = preferencesManager.getString(Constants.KEY_NAME);
                    user.age = preferencesManager.getString(Constants.KEY_AGE);
                    user.image = preferencesManager.getString(Constants.KEY_IMAGE);
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
    public GeoPoint getLocationFromAddress(String strAddress) {

        Geocoder coder = new Geocoder(this);
        List<Address> address;
        GeoPoint p1 = null;

        try {
            address = coder.getFromLocationName(strAddress, 5);
            if (address == null) {
                return null;
            }
            Address location = address.get(0);
            location.getLatitude();
            location.getLongitude();

            p1 = new GeoPoint((double) (location.getLatitude()),
                    (double) (location.getLongitude()));

            return p1;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
    private void startLocationWork() {
        backgroundWorkRequest = new OneTimeWorkRequest.Builder(BackgroundLocationWork.class)
                .addTag("LocationWork")
                .setBackoffCriteria(BackoffPolicy.LINEAR,
                        OneTimeWorkRequest.MIN_BACKOFF_MILLIS, TimeUnit.SECONDS)
                .build();
        WorkManager.getInstance(MapActivity.this).enqueue(backgroundWorkRequest);
    }

}