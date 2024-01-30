package my.first.messenger.activities.fragments;

import static java.lang.Double.parseDouble;

import android.content.Context;
import android.os.Bundle;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.firestore.FirebaseFirestore;

import org.checkerframework.checker.units.qual.A;
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
import my.first.messenger.activities.main_activities.MapActivity;
import my.first.messenger.activities.utils.Constants;
import my.first.messenger.activities.utils.PreferencesManager;
import my.first.messenger.databinding.FragmentRouteBinding;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link RouteFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class RouteFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private GeoPoint userLoc, coffeeshopLoc;
    private FragmentRouteBinding binding;
    private MapView map;
    private Boolean arrived = true;
    protected IMapController mapController;
    private FusedLocationProviderClient mFusedLocationClient;
    private PreferencesManager preferencesManager;
    private MyLocationNewOverlay mLocationOverlay;



    // TODO: Rename and change types of parameters
    private String mParam1;
    private String id;

    public RouteFragment() {
        // Required empty public constructor
    }

    // TODO: Rename and change types and number of parameters
    public static RouteFragment newInstance(String id) {
        RouteFragment fragment = new RouteFragment();
        Bundle args = new Bundle();
        args.putString(Constants.KEY_COFFEESHOP_ID, id);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        if (getArguments() != null) {
            id = getArguments().getString(Constants.KEY_COFFEESHOP_ID);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentRouteBinding.inflate(inflater, container, false);
        preferencesManager = new PreferencesManager(getActivity());
        settingMap();
        setListeners();
        discard();
        return binding.getRoot();
    }

    private void settingMap(){
        Context ctx = getActivity();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));
        map =  binding.map;
        map.setMinZoomLevel(15.0);
        map.setTileSource(TileSourceFactory.MAPNIK);
        mapController = map.getController();
        mapController.setZoom(15);
        map.setBuiltInZoomControls(true);
        map.setMultiTouchControls(true);

        mapController.setCenter(new GeoPoint(parseDouble(preferencesManager.getString(Constants.KEY_USER_LATITUDE)), parseDouble(preferencesManager.getString(Constants.KEY_USER_LONGITUDE))));
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());
        this.mLocationOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(getActivity()),map);
        this.mLocationOverlay.enableMyLocation();
        map.getOverlays().add(this.mLocationOverlay);
        FirebaseFirestore database = FirebaseFirestore.getInstance();
         database.collection(Constants.KEY_COLLECTION_COFFEE_SHOPS).document(id).get().addOnCompleteListener(task -> {
          //  String currentUserId = preferenceManager.getString(Constants.KEY_USER_ID);
            if(task.isSuccessful()&&task.getResult()!=null){
                double lng = task.getResult().getDouble("longitude");
                double lat = task.getResult().getDouble("latitude");
                //  coffeeshopLoc = new GeoPoint(lng,lat);
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

            SelectFragment frag = new SelectFragment();
            getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container_view, frag).commit();

            arrived = false;

        });

        binding.finish.setOnClickListener(v->{
            AlertFragment alert = new AlertFragment();
          //  if(distance())
            FragmentManager manager = getActivity().getSupportFragmentManager();
            alert.show(manager, "dialog");
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("coffeeshops").document(id)
                    .collection(Constants.KEY_COLLECTION_USERS)
                    .document(preferencesManager.getString(Constants.KEY_USER_ID)).delete();
            SelectFragment frag = new SelectFragment();
            getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container_view, frag).commit();


            arrived = true;
        });
    }


    public void makeToast(String message) {
        Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
    }


    private void routing(double lat, double lng){
        new Thread(){
            public void run(){
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        GeoPoint g = new GeoPoint(new GeoPoint(parseDouble(preferencesManager.getString(Constants.KEY_USER_LATITUDE)), parseDouble(preferencesManager.getString(Constants.KEY_USER_LONGITUDE))));
                        makeToast(coffeeshopLoc+"");
                        RoadManager roadManager = new OSRMRoadManager(getActivity(), "coffeeshopperhere");
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
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            AlertFragment myDialogFragment = new AlertFragment();
                            FragmentManager manager = getActivity().getSupportFragmentManager();
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
}