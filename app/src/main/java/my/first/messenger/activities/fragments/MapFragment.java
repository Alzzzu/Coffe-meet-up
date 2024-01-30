package my.first.messenger.activities.fragments;


import static java.lang.Double.parseDouble;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import my.first.messenger.R;
import my.first.messenger.activities.models.Coffeeshop;
import my.first.messenger.activities.utils.Constants;
import my.first.messenger.activities.utils.PreferencesManager;
import my.first.messenger.databinding.FragmentMapBinding;

public class MapFragment extends Fragment {
    protected IMapController mapController;
    int PERMISSION_ID = 44;
    MapView map =null;
    private ArrayList<Coffeeshop> coffeshops;
    private FusedLocationProviderClient mFusedLocationClient;
    private FragmentMapBinding binding;
    private MyLocationNewOverlay mLocationOverlay;
    private PreferencesManager preferencesManager;



    public MapFragment() {

    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param //param1 Parameter 1.
     * @param //param2 Parameter 2.
     * @return A new instance of fragment MapFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static MapFragment newInstance() {
        MapFragment fragment = new MapFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
       // binding = FragmentMapBinding.inflate(inflater, container, false);

        binding = FragmentMapBinding.inflate(inflater, container, false);
        init();
        getCoffeeshops();
        settingMap();

            for (Coffeeshop cf: coffeshops){
                setMarker(cf);
            }
        makeToast(coffeshops.size()+"");

        return binding.getRoot();
    }
    private void init(){
        preferencesManager = new PreferencesManager(getActivity());
        coffeshops = new ArrayList<>();
    }
    private void settingMap(){
        Context ctx = getActivity();
       // Configuration.getInstance().setUserAgentValue(getPackageName());
        int counter = 0;
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));
        map =  binding.map;
        map.setMinZoomLevel(13.0);

        map.setTileSource(TileSourceFactory.MAPNIK);
        mapController = map.getController();
       // makeToast(preferencesManager.getString(Constants.KEY_USER_LONGITUDE)+" "+preferencesManager.getString(Constants.KEY_USER_LATITUDE));
        mapController.setZoom(15);
        map.setBuiltInZoomControls(true);
        map.setMultiTouchControls(true);
        mapController.setCenter(new GeoPoint(parseDouble(preferencesManager.getString(Constants.KEY_USER_LATITUDE)), parseDouble(preferencesManager.getString(Constants.KEY_USER_LONGITUDE))));
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());
        this.mLocationOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(getActivity()),map);
        this.mLocationOverlay.enableMyLocation();
        map.getOverlays().add(this.mLocationOverlay);

    }
    private void getCoffeeshops(){
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection(Constants.KEY_COLLECTION_COFFEE_SHOPS)
                //.whereEqualTo(Constants.KEY_ADDRESS, "Электродная улица, 2, стр. 1")
                .get()
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()&&task.getResult()!=null){

                        for(QueryDocumentSnapshot queryDocumentSnapshot:task.getResult()) {
                           // if (queryDocumentSnapshot.getString(Constants.KEY_ADDRESS).equals("Авиамоторная ул., 14")){
                            //    break;
                           // }
                            double lat = queryDocumentSnapshot.getDouble("latitude");
                            double lng = queryDocumentSnapshot.getDouble("longitude");
                            if(2>distance(parseDouble(preferencesManager.getString(Constants.KEY_USER_LATITUDE)),parseDouble(preferencesManager.getString(Constants.KEY_USER_LONGITUDE)),lat, lng)){///getLocationFromAddress(queryDocumentSnapshot.getString(Constants.KEY_ADDRESS)).getLatitude(), getLocationFromAddress(queryDocumentSnapshot.getString(Constants.KEY_ADDRESS)).getLongitude())) {
                                Coffeeshop coffeeshop = new Coffeeshop();
                                coffeeshop.name = queryDocumentSnapshot.getString(Constants.KEY_NAME);
                                coffeeshop.address = queryDocumentSnapshot.getString(Constants.KEY_ADDRESS);
                                coffeeshop.id = queryDocumentSnapshot.getId();
                                coffeeshop.geoPoint = new GeoPoint(lat, lng);//getLocationFromAddress(queryDocumentSnapshot.getString(Constants.KEY_ADDRESS)));
                                coffeshops.add(coffeeshop);
                                setMarker(coffeeshop);
                            }
                        }

                    }
                });

    }
    private void setMarker(Coffeeshop coffeeshop){
        Marker startMarker = new Marker(map);
        startMarker.setPosition(coffeeshop.geoPoint);
        //getLocationFromAddress(coffeeshop.address));//queryDocumentSnapshot.getString(Constants.KEY_ADDRESS)));//queryDocumentSnapshot.getGeoPoint(Constants.KEY_GEOPOINT).getLatitude(), queryDocumentSnapshot.getGeoPoint(Constants.KEY_GEOPOINT).getLongitude()));
        startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        map.getOverlays().add(startMarker);
        startMarker.setIcon(getResources().getDrawable(R.mipmap.coffee_colour));
        startMarker.setTitle(coffeeshop.name);
       // String name = coffeeshop.name;
       // String address = coffeeshop.address;

        startMarker.setOnMarkerClickListener(new Marker.OnMarkerClickListener() {
        @Override
        public boolean onMarkerClick(Marker marker, MapView mapView) {
            CoffeeshopFragment frag = new CoffeeshopFragment();
            Bundle bundle = new Bundle();
            bundle.putString(Constants.KEY_ADDRESS,coffeeshop.address);
            bundle.putString(Constants.KEY_NAME, coffeeshop.name);
            bundle.putString(Constants.KEY_COFFEESHOP_ID, coffeeshop.id);
            frag.setArguments(bundle);
            getActivity().getSupportFragmentManager().beginTransaction()
                    .setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out)
                    .add(R.id.fragment_container_view, frag)
                    .addToBackStack(null)
                    .commit();
            return true;
        }
    });
}


    public GeoPoint getLocationFromAddress(String strAddress) {

        Geocoder coder = new Geocoder(getActivity());
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
    private static double distance(double lat1, double lon1, double lat2, double lon2) {
        if ((lat1 == lat2) && (lon1 == lon2)) {
            return 0;
        }
        else {
            double theta = lon1 - lon2;
            double dist = Math.sin(Math.toRadians(lat1)) * Math.sin(Math.toRadians(lat2)) + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.cos(Math.toRadians(theta));
            dist = Math.acos(dist);
            dist = Math.toDegrees(dist);
            dist = dist * 60 * 1.1515;
            dist = dist * 1.609344;
            return (dist);
        }
    }


    public void makeToast(String message) {
        Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
    }


}