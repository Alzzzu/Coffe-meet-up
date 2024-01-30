package my.first.messenger.activities.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

import my.first.messenger.R;
import my.first.messenger.activities.adapters.UserAdapter;
import my.first.messenger.activities.listeners.UsersListener;
import my.first.messenger.activities.main_activities.ProfileActivity;
import my.first.messenger.activities.models.User;
import my.first.messenger.activities.utils.Constants;
import my.first.messenger.activities.utils.PreferencesManager;
import my.first.messenger.databinding.FragmentUsersBinding;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link UsersFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class UsersFragment extends Fragment implements UsersListener {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private FragmentUsersBinding binding;
    private PreferencesManager preferenceManager;
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public UsersFragment() {
        // Required empty public constructor
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
    public static UsersFragment newInstance(){//String param1, String param2) {
        UsersFragment fragment = new UsersFragment();
        Bundle args = new Bundle();
    //    args.putString(ARG_PARAM1, param1);
      //  args.putString(ARG_PARAM2, param2);
      //  fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentUsersBinding.inflate(inflater, container, false);
        init();
        getUsers();
        setListeners();


        //here data must be an instance of the class MarsDataProvider
        return binding.getRoot();
    }
    private void init(){
        preferenceManager = new PreferencesManager(getActivity());
    }
    private void setListeners(){
        binding.imageBack.setOnClickListener(v->{
            OptionsFragment options = new OptionsFragment();
            getActivity().getSupportFragmentManager().beginTransaction()
                    .setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out)
                    .replace(R.id.fragment_container_view, options)
                    .addToBackStack(null)
                    .commit();
        });
    }
    private void getUsers(){
       // makeToast(preferenceManager.getLong(Constants.KEY_SEARCH_MIN_AGE)+"");
       // makeToast(preferenceManager.getLong(Constants.KEY_SEARCH_MAX_AGE)+"");

        FirebaseFirestore database= FirebaseFirestore.getInstance();
        database.collection(Constants.KEY_COLLECTION_USERS)
                .whereNotEqualTo(Constants.KEY_GENDER,  preferenceManager.getString(Constants.KEY_SEARCH_GENDER))
                .get()
                .addOnCompleteListener(task -> {
                    String currentUserId = preferenceManager.getString(Constants.KEY_USER_ID);
                    if(task.isSuccessful()&&task.getResult()!=null){
                        List<User> users = new ArrayList<>();
                        for(QueryDocumentSnapshot queryDocumentSnapshot:task.getResult()){
                            if(currentUserId.equals(queryDocumentSnapshot.getId())){
                                continue;
                            }
                            if (queryDocumentSnapshot.getLong(Constants.KEY_AGE)<=preferenceManager.getLong(Constants.KEY_SEARCH_MAX_AGE)&&queryDocumentSnapshot.getLong(Constants.KEY_AGE)>=preferenceManager.getLong(Constants.KEY_SEARCH_MIN_AGE)){
                            User user = new User();
                            user.name=queryDocumentSnapshot.getString(Constants.KEY_NAME);
                            user.age=(queryDocumentSnapshot.getLong(Constants.KEY_AGE)).toString();
                            user.image = queryDocumentSnapshot.getString(Constants.KEY_IMAGE);
                            user.about = queryDocumentSnapshot.getString(Constants.KEY_ABOUT);
                            user.hobby = queryDocumentSnapshot.getString(Constants.KEY_HOBBIES);
                            user.token = queryDocumentSnapshot.getString(Constants.KEY_FCM_TOKEN);
                            user.id = queryDocumentSnapshot.getId();
                            users.add(user);
                            }
                        }
                        if (users.size()>0){
                            UserAdapter userAdapter = new UserAdapter(users, this::onUserClick);
                            binding.usersRecycleView.setAdapter(userAdapter);
                            binding.usersRecycleView.setVisibility(View.VISIBLE);
                        }
                        else{
                            makeToast("no users found");
                        }
                    }
                });

    }
    @Override
    public void onUserClick(User user){
        Intent intent = new Intent(getActivity(), ProfileActivity.class);
        intent.putExtra(Constants.KEY_USER, user);
        startActivity(intent);
    }
    public void makeToast(String message){
        Toast.makeText(getActivity(),message, Toast.LENGTH_SHORT).show();
    }
}