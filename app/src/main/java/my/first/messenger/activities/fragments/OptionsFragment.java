package my.first.messenger.activities.fragments;

import static java.lang.Integer.parseInt;
import static java.lang.Long.parseLong;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;

import my.first.messenger.R;
import my.first.messenger.activities.utils.Constants;
import my.first.messenger.activities.utils.PreferencesManager;
import my.first.messenger.databinding.FragmentOptionsBinding;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link OptionsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class OptionsFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private PreferencesManager preferencesManager;
    private FragmentOptionsBinding binding;

    public OptionsFragment() {
        // Required empty public constructor
        super(R.layout.fragment_options);
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param //param1 Parameter 1.
     * @param //param2 Parameter 2.
     * @return A new instance of fragment UsersFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static OptionsFragment newInstance(){//(String param1, String param2) {
        OptionsFragment fragment = new OptionsFragment();
        Bundle args = new Bundle();
        //args.putString(ARG_PARAM1, param1);
        //args.putString(ARG_PARAM2, param2);
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
        binding = FragmentOptionsBinding.inflate(inflater, container, false);
        init();
        loadSettings();
        setListeners();

        return binding.getRoot();

    }
    private void init(){
        preferencesManager = new PreferencesManager(getActivity());
    }
    private void loadSettings(){
        switch(preferencesManager.getString(Constants.KEY_SEARCH_GENDER)){
            case "M": binding.female.setChecked(true);
            break;
            case "F":binding.male.setChecked(true);
            break;
            case "BOTH":binding.male.setChecked(true);
            binding.female.setChecked(true);
            break;
            default: break;
        }
        binding.minAge.setText(preferencesManager.getLong(Constants.KEY_SEARCH_MIN_AGE)+"");
        binding.maxAge.setText(preferencesManager.getLong(Constants.KEY_SEARCH_MAX_AGE)+"");


    }
    private void setListeners(){
        binding.done.setOnClickListener(v ->{
            if (checkAge()) {
                if(preferencesManager.getBoolean(Constants.KEY_IS_ACTIVATED)){

                    ActivatedUsersFragment activatedUsers = new ActivatedUsersFragment();
                    getActivity().getSupportFragmentManager().beginTransaction()
                            .setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out)
                            .replace(R.id.fragment_container_view, activatedUsers)
                            .addToBackStack(null)
                            .commit();

                }
                else{
                UsersFragment users = new UsersFragment();
                getActivity().getSupportFragmentManager().beginTransaction()
                        .setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out)
                        .replace(R.id.fragment_container_view, users)
                        .addToBackStack(null)
                        .commit();
                }
            }
        });
        binding.female.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView,boolean isChecked) {
                switch (preferencesManager.getString(Constants.KEY_SEARCH_GENDER)){
                    case "F": preferencesManager.putString(Constants.KEY_SEARCH_GENDER, "BOTH");
                    break;
                    case "M": preferencesManager.putString(Constants.KEY_SEARCH_GENDER, null);
                    break;
                    case "": preferencesManager.putString(Constants.KEY_SEARCH_GENDER, "M");
                    break;
                    case "BOTH":preferencesManager.putString(Constants.KEY_SEARCH_GENDER,"F");
                    break;
                    default: break;
                }

                makeToast(preferencesManager.getString(Constants.KEY_SEARCH_GENDER));
            }
        }
        );
        binding.male.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView,boolean isChecked) {
                switch (preferencesManager.getString(Constants.KEY_SEARCH_GENDER)){
                    case "M": preferencesManager.putString(Constants.KEY_SEARCH_GENDER, "BOTH");
                        break;
                    case "F": preferencesManager.putString(Constants.KEY_SEARCH_GENDER, "");
                        break;
                    case "": preferencesManager.putString(Constants.KEY_SEARCH_GENDER, "F");
                        break;
                    case "BOTH":preferencesManager.putString(Constants.KEY_SEARCH_GENDER,"M");
                        break;
                    default: break;
                }



            }
        }
        );
        binding.coworking.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView,boolean isChecked) {
                preferencesManager.putBoolean(Constants.KEY_SEARCH_FOR_COWORKING, !preferencesManager.getBoolean(Constants.KEY_SEARCH_FOR_COWORKING));
            }
        }
        );
        binding.meeting.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView,boolean isChecked) {
                preferencesManager.putBoolean(Constants.KEY_SEARCH_FOR_MEETING, !preferencesManager.getBoolean(Constants.KEY_SEARCH_FOR_MEETING));
            }
        }
        );
    }
    private Boolean checkAge(){
        try {
            if (parseInt(binding.minAge.getText().toString()) <= parseInt(binding.maxAge.getText().toString())) {
                preferencesManager.putLong(Constants.KEY_SEARCH_MIN_AGE, parseLong(binding.minAge.getText().toString()));
                preferencesManager.putLong(Constants.KEY_SEARCH_MAX_AGE, parseLong(binding.maxAge.getText().toString()));
                return true;
            }
            makeToast("Минимальный возраст должен быть больше максимального");
            return false;
        }
        catch(NumberFormatException e){
            makeToast("Пожалуйста, введите возраст");
            return false;
        }
    }
    public void makeToast(String message){
        Toast.makeText(getActivity(),message, Toast.LENGTH_SHORT).show();
    }
}