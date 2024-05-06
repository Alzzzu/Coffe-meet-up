package my.first.messenger.activities.fragments;


import static java.lang.Double.parseDouble;
import static my.first.messenger.activities.utils.Functions.distance;

import android.content.Context;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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

import java.util.ArrayList;

import my.first.messenger.R;
import my.first.messenger.activities.models.Coffeeshop;
import my.first.messenger.activities.utils.Constants;
import my.first.messenger.activities.utils.PreferencesManager;
import my.first.messenger.databinding.FragmentMapBinding;

public class MapFragment extends Fragment {
    protected IMapController mapController;
    MapView map =null;
    private ArrayList<Coffeeshop> coffeshops;
    private FusedLocationProviderClient mFusedLocationClient;
    private FragmentMapBinding binding;
    private MyLocationNewOverlay mLocationOverlay;
    private PreferencesManager preferencesManager;
    private final String FRAGMENT_TAG = "COFFEESHOP_FRAGMENT_TAG";



    public MapFragment() {

    }
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

        binding = FragmentMapBinding.inflate(inflater, container, false);
        init();
        getCoffeeshops();
        settingMap();

            for (Coffeeshop cf: coffeshops){
                setMarker(cf);
            }
        return binding.getRoot();
    }
    private void init(){
        preferencesManager = new PreferencesManager(getActivity());
        coffeshops = new ArrayList<>();
    }
    private void settingMap(){
        Context ctx = getActivity();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));
        map =  binding.map;
        map.setMinZoomLevel(15.0);

        map.setTileSource(TileSourceFactory.MAPNIK);
        mapController = map.getController();
        mapController.setZoom(15);
        map.setBuiltInZoomControls(false);
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
                .get()
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()&&task.getResult()!=null){

                        for(QueryDocumentSnapshot queryDocumentSnapshot:task.getResult()) {
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
        startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        map.getOverlays().add(startMarker);
        startMarker.setIcon(getResources().getDrawable(R.mipmap.map_icon_2));
        startMarker.setTitle(coffeeshop.name);

        startMarker.setOnMarkerClickListener(new Marker.OnMarkerClickListener() {
        @Override
        public boolean onMarkerClick(Marker marker, MapView mapView) {
            mapController.animateTo(coffeeshop.geoPoint);
            CoffeeshopFragment frag = new CoffeeshopFragment();
            Bundle bundle = new Bundle();
            bundle.putString(Constants.KEY_ADDRESS,coffeeshop.address);
            bundle.putString(Constants.KEY_NAME, coffeeshop.name);
            bundle.putString(Constants.KEY_COFFEESHOP_ID, coffeeshop.id);
            frag.setArguments(bundle);
            getActivity().getSupportFragmentManager().beginTransaction()
                    .setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out)
                    .add(R.id.fragment_container_view, frag, FRAGMENT_TAG)
                    .addToBackStack(null)
                    .commit();
            Fragment fragment = getActivity().getSupportFragmentManager().findFragmentByTag(FRAGMENT_TAG);
            if(fragment != null) {
                getActivity().getSupportFragmentManager().beginTransaction()
                        .setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out)
                        .remove(fragment).commit();
            }
            return true;
        }
    });
}
}