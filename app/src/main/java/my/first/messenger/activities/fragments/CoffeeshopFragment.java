package my.first.messenger.activities.fragments;

import static java.lang.Long.parseLong;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;

import my.first.messenger.activities.main_activities.RouteActivity;
import my.first.messenger.activities.models.Coffeeshop;
import my.first.messenger.activities.utils.Constants;
import my.first.messenger.activities.utils.PreferencesManager;
import my.first.messenger.databinding.FragmentCoffeeshopBinding;

public class CoffeeshopFragment extends Fragment {
    private String address;
    private String name;
    private Coffeeshop coffeeshop;
    private String id;
    private FragmentCoffeeshopBinding binding;
    private PreferencesManager preferencesManager;
    private FirebaseFirestore database;

    public CoffeeshopFragment() {
    }
    public static CoffeeshopFragment newInstance(String address, String name,String id) {
        CoffeeshopFragment fragment = new CoffeeshopFragment();
        Bundle args = new Bundle();
        args.putString(Constants.KEY_ADDRESS, address);
        args.putString(Constants.KEY_NAME, name);
        args.putString(Constants.KEY_COFFEESHOP_ID, id);

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            address = getArguments().getString(Constants.KEY_ADDRESS);
            name = getArguments().getString(Constants.KEY_NAME);
            id = getArguments().getString(Constants.KEY_COFFEESHOP_ID);
            preferencesManager = new PreferencesManager(getActivity());
            database = FirebaseFirestore.getInstance();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentCoffeeshopBinding.inflate(inflater, container, false);
        binding.name.setText(name);
        binding.address.setText(address);
        setListeners();
        return binding.getRoot();
    }
    public void setListeners(){
        binding.imageBack.setOnClickListener(v->{
            getActivity().getSupportFragmentManager().beginTransaction().remove(this).commit();
        });
        binding.route.setOnClickListener(v->{
            HashMap<String, Boolean> updt= new HashMap<>();
            updt.put("activated", true);
            HashMap<String, Object> user = new HashMap<>();
            user.put("status","going");
            user.put(Constants.KEY_NAME,preferencesManager.getString(Constants.KEY_NAME));
            user.put(Constants.KEY_USER_ID,preferencesManager.getString(Constants.KEY_USER_ID));
            user.put(Constants.KEY_IMAGE,preferencesManager.getString(Constants.KEY_IMAGE));
            user.put(Constants.KEY_GENDER,preferencesManager.getString(Constants.KEY_GENDER));
            user.put(Constants.KEY_USER_ID,preferencesManager.getString(Constants.KEY_USER_ID));
            user.put(Constants.KEY_AGE,parseLong(preferencesManager.getString(Constants.KEY_AGE)));

            database.collection("coffeeshops").document(id)
                    .update("activated", true);
            database.collection("coffeeshops").document(id)
                    .collection(Constants.KEY_COLLECTION_USERS).document(preferencesManager.getString(Constants.KEY_USER_ID)).set(user);
            Intent intent = new Intent(getActivity(), RouteActivity.class);
            intent.putExtra(Constants.KEY_COFFEESHOP_ID,id);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            preferencesManager.putBoolean(Constants.KEY_IS_GOING,true);
            preferencesManager.putString(Constants.KEY_COFFEESHOP_ID,id);
            startActivity(intent);
        });
    }
}