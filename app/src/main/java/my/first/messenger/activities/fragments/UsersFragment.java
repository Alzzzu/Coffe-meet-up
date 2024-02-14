package my.first.messenger.activities.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
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
    private FragmentUsersBinding binding;
    private PreferencesManager preferenceManager;

    // TODO: Rename and change types of parameters

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
    public static UsersFragment newInstance(){
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
    private void getActiveUsers(){
        FirebaseFirestore database= FirebaseFirestore.getInstance();
        database.collection(Constants.KEY_COLLECTION_COFFEE_SHOPS)
                .whereEqualTo("activated", true)
                .get()
                .addOnCompleteListener(task->{
                    if(task.isSuccessful()&&task.getResult()!=null) {

                        for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                            database.collection(Constants.KEY_COLLECTION_COFFEE_SHOPS)
                                    .document(queryDocumentSnapshot.getId())
                                    .collection(Constants.KEY_COLLECTION_USERS)
                                    .get()
                                    .addOnCompleteListener(task2 -> {
                                        if(task2.isSuccessful()&&task2.getResult()!=null){
                                            makeToast("here");
                                            List<User> users = new ArrayList<>();

                                            for( QueryDocumentSnapshot queryDocumentSnapshot2 : task2.getResult()){
                                                User user = new User();
                                                user.name =queryDocumentSnapshot2.getString(Constants.KEY_NAME);
                                                user.image =queryDocumentSnapshot2.getString(Constants.KEY_NAME);
                                                ///   user.image="byHHic3bEjQ6zHbFnblHIg==";
                                                user.id=queryDocumentSnapshot2.getId();
                                                users.add(user);
                                            }
                                            makeToast(users.size()+"");
                                            UserAdapter userAdapter = new UserAdapter(users, this);
                                            binding.usersRecycleView.setAdapter(userAdapter);
                                            binding.usersRecycleView.setVisibility(View.VISIBLE);
                                        }
                                    });
                        }
                    }});
    }
    @Override
    public void onUserClick(User user){
        Intent intent = new Intent(getActivity(), ProfileActivity.class);
        intent.putExtra(Constants.KEY_USER, user);
        Bundle bundle = new Bundle();
      //  FirebaseFirestore database = FirebaseFirestore.getInstance();
      //  database.collection(Constants.KEY_USER).document(user.id).get()
        //        .addOnCompleteListener(task->{
          //          DocumentSnapshot documentSnapshot = task.getResult();
           //         user.about = documentSnapshot.getString(Constants.KEY_ABOUT);
            //        user.hobby = documentSnapshot.getString(Constants.KEY_HOBBIES);
             //       user.age = documentSnapshot.getLong(Constants.KEY_AGE).toString();
             //   });
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        HashMap<String, Object> updt = new HashMap<>();
        updt.put(Constants.KEY_VISITED_IMAGE, user.image);
        updt.put(Constants.KEY_VISITOR_IMAGE, preferenceManager.getString(Constants.KEY_IMAGE));
        updt.put(Constants.KEY_VISITED_ID, user.id);
        updt.put(Constants.KEY_VISITOR_ID, preferenceManager.getString(Constants.KEY_USER_ID));
        updt.put(Constants.KEY_VISITED_NAME, user.name);
        updt.put(Constants.KEY_VISITOR_NAME, preferenceManager.getString(Constants.KEY_NAME));
        database.collection(Constants.KEY_COLLECTION_MEET_UP_OFFERS).add(updt);
        bundle.putSerializable(Constants.KEY_USER, user);
        ProfileFragment frag = new ProfileFragment();
        frag.setArguments(bundle);
        getActivity().getSupportFragmentManager().beginTransaction().add(R.id.fragment_container_view, frag).commit();

        // startActivity(intent);
    }
    public void makeToast(String message){
        Toast.makeText(getActivity(),message, Toast.LENGTH_SHORT).show();
    }
}