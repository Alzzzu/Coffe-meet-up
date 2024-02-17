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
import java.util.Map;

import my.first.messenger.R;
import my.first.messenger.activities.main_activities.RouteActivity;
import my.first.messenger.activities.models.Coffeeshop;
import my.first.messenger.activities.utils.Constants;
import my.first.messenger.activities.utils.PreferencesManager;
import my.first.messenger.databinding.FragmentCoffeeshopBinding;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link CoffeeshopFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CoffeeshopFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
  //  private static final String ARG_PARAM1 = "param1";
   // private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String address;
    private String name;
    private Coffeeshop coffeeshop;
    private String id;
    private FragmentCoffeeshopBinding binding;
    private PreferencesManager preferencesManager;

    public CoffeeshopFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param// param1 Parameter 1.
     * @param// param2 Parameter 2.
     * @return A new instance of fragment Coffeeshop_fragment.
     */
    // TODO: Rename and change types and number of parameters
    public static CoffeeshopFragment newInstance(String address, String name,String id) {
        CoffeeshopFragment fragment = new CoffeeshopFragment();
        Bundle args = new Bundle();
     //   args.putSerializable("coffeeshop", coffeeshop);
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
        //    coffeeshop = (Coffeeshop) getArguments().getSerializable("coffeeshop");
            preferencesManager = new PreferencesManager(getActivity());
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
            user.put(Constants.KEY_IMAGE,preferencesManager.getString(Constants.KEY_IMAGE));
            user.put(Constants.KEY_GENDER,preferencesManager.getString(Constants.KEY_GENDER));
            user.put(Constants.KEY_USER_ID,preferencesManager.getString(Constants.KEY_USER_ID));
            user.put(Constants.KEY_AGE,parseLong(preferencesManager.getString(Constants.KEY_AGE)));
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("coffeeshops").document(id)
                    .update("activated", true);
            db.collection("coffeeshops").document(id)
                    .collection(Constants.KEY_COLLECTION_USERS).document(preferencesManager.getString(Constants.KEY_USER_ID)).set(user);
            Intent intent = new Intent(getActivity(), RouteActivity.class);
            intent.putExtra(Constants.KEY_COFFEESHOP_ID,id);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            preferencesManager.putBoolean(Constants.KEY_IS_GOING,true);
            preferencesManager.putString(Constants.KEY_COFFEESHOP_ID,id);
            startActivity(intent);

            Bundle bundle = new Bundle();
            bundle.putString(Constants.KEY_COFFEESHOP_ID,id);
            RouteFragment frag = new RouteFragment();

            frag.setArguments(bundle);
            getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container_view, frag).commit();
        });
    }
}