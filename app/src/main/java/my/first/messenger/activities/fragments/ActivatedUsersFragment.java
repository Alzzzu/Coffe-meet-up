package my.first.messenger.activities.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

import my.first.messenger.R;
import my.first.messenger.activities.adapters.UserAdapter;
import my.first.messenger.activities.listeners.UsersListener;
import my.first.messenger.activities.models.User;
import my.first.messenger.activities.utils.Constants;
import my.first.messenger.activities.utils.PreferencesManager;
import my.first.messenger.databinding.FragmentActivatedUsersBinding;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ActivatedUsersFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ActivatedUsersFragment extends Fragment implements UsersListener {


    private FragmentActivatedUsersBinding binding;
    private PreferencesManager preferencesManager;

    public ActivatedUsersFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ActivatedUsersFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ActivatedUsersFragment newInstance(String param1, String param2) {
        ActivatedUsersFragment fragment = new ActivatedUsersFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        preferencesManager = new PreferencesManager(getActivity());
        binding = FragmentActivatedUsersBinding.inflate(inflater, container, false);
        getMeetingUsers();
        setListeners();
        return binding.getRoot();
    }
    private void getMeetingUsers(){
        FirebaseFirestore database= FirebaseFirestore.getInstance();
        database.collection(Constants.KEY_COLLECTION_MEET_UP_OFFERS)
                .whereEqualTo(Constants.KEY_VISITED_ID, preferencesManager.getString(Constants.KEY_USER_ID))
             //   .whereNotEqualTo(Constants.KEY_GENDER,  preferencesManager.getString(Constants.KEY_SEARCH_GENDER))

                .get()
                .addOnCompleteListener(task->{
                    if(task.isSuccessful()&&task.getResult()!=null) {
                        ArrayList<User> users = new ArrayList<>();
                        for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                            if(queryDocumentSnapshot.getLong(Constants.KEY_AGE)>=preferencesManager.getLong(Constants.KEY_SEARCH_MIN_AGE)
                            &&queryDocumentSnapshot.getLong(Constants.KEY_AGE)<=preferencesManager.getLong(Constants.KEY_SEARCH_MAX_AGE)
                            &&!queryDocumentSnapshot.getString(Constants.KEY_GENDER).equals(preferencesManager.getString(Constants.KEY_SEARCH_GENDER))){
                                User user = new User();
                                user.id = queryDocumentSnapshot.getString(Constants.KEY_VISITOR_ID);
                                user.name =queryDocumentSnapshot.getString(Constants.KEY_VISITOR_NAME);
                                user.image =queryDocumentSnapshot.getString(Constants.KEY_VISITOR_IMAGE);
                                users.add(user);
                            }
                        }
                        UserAdapter userAdapter = new UserAdapter(users, this);
                        binding.usersRecycleView.setAdapter(userAdapter);
                        binding.usersRecycleView.setVisibility(View.VISIBLE);
                    }
                });
    }
    private void setListeners(){
        binding.back.setOnClickListener(v->{
            OptionsFragment options = new OptionsFragment();
            getActivity().getSupportFragmentManager().beginTransaction()
                    .setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out)
                    .replace(R.id.fragment_container_view, options)
                    .addToBackStack(null)
                    .commit();
        });
        binding.refresh.setOnClickListener(v->{
            getMeetingUsers();
        });
    }
    @Override
    public void onUserClick(User user) {
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection(Constants.KEY_COLLECTION_USERS)
                .document(user.id)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        DocumentSnapshot documentSnapshot = task.getResult();
                        user.id = documentSnapshot.getId();
                        user.about = documentSnapshot.getString(Constants.KEY_ABOUT);
                        user.hobby = documentSnapshot.getString(Constants.KEY_HOBBIES);
                        user.name = documentSnapshot.getString(Constants.KEY_NAME);
                        user.gender = documentSnapshot.getString(Constants.KEY_GENDER);
                        user.age = documentSnapshot.getLong(Constants.KEY_AGE).toString();
                        user.image = documentSnapshot.getString(Constants.KEY_IMAGE);
                        Bundle bundle = new Bundle();
                        bundle.putSerializable(Constants.KEY_USER, user);
                        ProfileFragment frag = new ProfileFragment();
                        frag.setArguments(bundle);
                        getActivity().getSupportFragmentManager().beginTransaction().add(R.id.fragment_container_view, frag).commit();

                    }
                });
    }
}