package my.first.messenger.activities.fragments;

import static java.lang.Double.parseDouble;
import static my.first.messenger.activities.utils.Functions.distance;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.google.firebase.firestore.DocumentSnapshot;
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

public class UsersFragment extends Fragment implements UsersListener {

    private FragmentUsersBinding binding;
    private PreferencesManager preferenceManager;
    private List<User> users;
    private UserAdapter userAdapter;
    private static final String TAG = "UsersFragmentLog";
    public UsersFragment() {
    }
    public static UsersFragment newInstance(){
        UsersFragment fragment = new UsersFragment();
        Bundle args = new Bundle();
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
        getActiveUsers();
        setListeners();
        return binding.getRoot();
    }
    private void init(){
        users = new ArrayList<>();
        preferenceManager = new PreferencesManager(getActivity());
        userAdapter = new UserAdapter(users, this);
    }
    private void setListeners(){
        binding.swipeRefreshLayout.setOnRefreshListener(this::getActiveUsers);
          binding.imageBack.setOnClickListener(v->{
            OptionsFragment options = new OptionsFragment();
            getActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container_view, options)
                .addToBackStack(null)
                .commit();
        });
    }
    private void getActiveUsers(){
        FirebaseFirestore database= FirebaseFirestore.getInstance();
        users.clear();
        userAdapter = new UserAdapter(users, this);
        Log.d(TAG, "function started");
        database.collection(Constants.KEY_COLLECTION_COFFEE_SHOPS)
                .whereEqualTo("activated", true)
                .get()
                .addOnCompleteListener(task->{
                    if(task.isSuccessful()&&task.getResult()!=null&&task.getResult().size()!=0) {
                        Log.d(TAG, "first listener completed");
                        int coffeeShopCounter = 0;
                        for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                            double lat = queryDocumentSnapshot.getDouble("latitude");
                            double lng = queryDocumentSnapshot.getDouble("longitude");
                            Log.d(TAG, "second listener completed");
                            if(1>distance(parseDouble(preferenceManager.getString(Constants.KEY_USER_LATITUDE)),parseDouble(preferenceManager.getString(Constants.KEY_USER_LONGITUDE)),lat, lng)){
                                coffeeShopCounter +=1;
                                database.collection(Constants.KEY_COLLECTION_COFFEE_SHOPS)
                                    .document(queryDocumentSnapshot.getId())
                                    .collection(Constants.KEY_COLLECTION_USERS)
                                    .whereEqualTo("status", "active")
                                    .get()
                                    .addOnCompleteListener(task2 -> {
                                        if(task2.isSuccessful()&&task2.getResult()!=null){
                                            for(QueryDocumentSnapshot queryDocumentSnapshot2 : task2.getResult()){
                                                if (queryDocumentSnapshot2.getLong(Constants.KEY_AGE)<=preferenceManager.getLong(Constants.KEY_SEARCH_MAX_AGE)
                                                    &&queryDocumentSnapshot2.getLong(Constants.KEY_AGE)>=preferenceManager.getLong(Constants.KEY_SEARCH_MIN_AGE)
                                                    && !queryDocumentSnapshot2.getString(Constants.KEY_GENDER).equals(preferenceManager.getString(Constants.KEY_SEARCH_GENDER))) {
                                                    User user = new User();
                                                    user.id=queryDocumentSnapshot2.getString(Constants.KEY_USER_ID);
                                                    user.name=queryDocumentSnapshot2.getString(Constants.KEY_NAME);
                                                    user.image=queryDocumentSnapshot2.getString(Constants.KEY_IMAGE);
                                                    user.age=queryDocumentSnapshot2.getLong(Constants.KEY_AGE).toString();
                                                    users.add(user);
                                                    userAdapter.notifyDataSetChanged();
                                                }
                                            }
                                            if (users.size()>0) {
                                                userAdapter = new UserAdapter(users, this);
                                                binding.usersRecycleView.setAdapter(userAdapter);
                                                binding.usersRecycleView.setVisibility(View.VISIBLE);
                                                binding.swipeRefreshLayout.setRefreshing(false);
                                                binding.progress.setVisibility(View.GONE);
                                                binding.loading.setVisibility(View.GONE);
                                                binding.noUsers.setVisibility(View.GONE);
                                            }
                                            else{
                                                binding.noUsers.setVisibility(View.VISIBLE);
                                                binding.loading.setVisibility(View.VISIBLE);
                                                binding.progress.setVisibility(View.GONE);
                                                binding.swipeRefreshLayout.setRefreshing(false);
                                                binding.loading.setText("Пользователи по запросу не найдены");
                                            }
                                        }
                                    });
                                }
                             }
                        if (coffeeShopCounter==0){
                            binding.noUsers.setVisibility(View.VISIBLE);
                            binding.loading.setVisibility(View.VISIBLE);
                            binding.progress.setVisibility(View.GONE);
                            binding.swipeRefreshLayout.setRefreshing(false);
                            binding.loading.setText("Пользователи по запросу не найдены");
                        }
                    }
                    else{
                        Log.d(TAG, "no result");
                        binding.noUsers.setVisibility(View.VISIBLE);
                        binding.loading.setVisibility(View.VISIBLE);
                        binding.progress.setVisibility(View.GONE);
                        binding.swipeRefreshLayout.setRefreshing(false);
                        binding.loading.setText("Пользователи по запросу не найдены");
                    }
                });
        }
    @Override
    public void onUserClick(User user){
        Intent intent = new Intent(getActivity(), ProfileActivity.class);
        intent.putExtra(Constants.KEY_USER, user);
        Bundle bundle = new Bundle();
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection(Constants.KEY_COLLECTION_USERS).document(user.id).get()
            .addOnCompleteListener(task->{
                DocumentSnapshot documentSnapshot = task.getResult();
                user.about = documentSnapshot.getString(Constants.KEY_ABOUT);
                user.hobby = documentSnapshot.getString(Constants.KEY_HOBBIES);
                user.age = documentSnapshot.getLong(Constants.KEY_AGE).toString();
                bundle.putSerializable(Constants.KEY_USER, user);
                bundle.putBoolean("visitor", true);
                ProfileFragment frag = new ProfileFragment();
                frag.setArguments(bundle);
                getActivity().getSupportFragmentManager().beginTransaction()
                        .setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out)
                        .add(R.id.fragment_container_view, frag).commit();
            });
        }
    public void makeToast(String message){
        Toast.makeText(getActivity(),message, Toast.LENGTH_SHORT).show();
    }
}