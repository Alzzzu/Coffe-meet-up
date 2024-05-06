package my.first.messenger.activities.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import my.first.messenger.R;
import my.first.messenger.activities.main_activities.MapActivity;
import my.first.messenger.activities.main_activities.RouteActivity;
import my.first.messenger.databinding.FragmentSelectBinding;

public class SelectFragment extends Fragment {
    FragmentSelectBinding binding;

    public SelectFragment() {
        // Required empty public constructor
    }
    public static SelectFragment newInstance(String param1, String param2) {
        SelectFragment fragment = new SelectFragment();
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
        binding = FragmentSelectBinding.inflate(inflater,container,false);
        setListeners();
        return binding.getRoot();
    }
    private void setListeners(){

        binding.coffeeShopSearch.setOnClickListener(v->{
            getActivity().getSupportFragmentManager().beginTransaction()
                    .setReorderingAllowed(true)
                    .add(R.id.fragment_container_view, MapFragment.class, null)
                    .commit();
        });

        binding.usersSearch.setOnClickListener(v->{
            getActivity().getSupportFragmentManager().beginTransaction()
                    .setReorderingAllowed(true)
                    .add(R.id.fragment_container_view, OptionsFragment.class, null)
                    .commit();
            //replace?
        });
    }
}