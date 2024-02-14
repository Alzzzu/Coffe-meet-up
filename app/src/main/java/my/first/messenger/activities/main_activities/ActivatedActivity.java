package my.first.messenger.activities.main_activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.checkerframework.checker.units.qual.A;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import my.first.messenger.R;
import my.first.messenger.activities.adapters.UserAdapter;
import my.first.messenger.activities.listeners.UsersListener;
import my.first.messenger.activities.models.ChatMessage;
import my.first.messenger.activities.models.User;
import my.first.messenger.activities.utils.Constants;
import my.first.messenger.activities.utils.PreferencesManager;
import my.first.messenger.databinding.ActivityActivatedBinding;

public class ActivatedActivity extends FragmentActivity implements UsersListener {
    private ActivityActivatedBinding binding;

    private PreferencesManager preferencesManager;
    private ArrayList<User> users;
    private UserAdapter userAdapter;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityActivatedBinding.inflate(getLayoutInflater());
        preferencesManager = new PreferencesManager(getApplicationContext());
        ArrayList<User> users = new ArrayList<>();
        userAdapter = new UserAdapter(users, this);

        setContentView(binding.getRoot());
        //getUsers();
        //getActiveUsers();
        getMeetingUsers();
        setListeners();
    }
    private void setListeners(){
        binding.bottomNavigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                if (item.getItemId()==R.id.profile){
                    Intent intent = new Intent( getApplicationContext(), ProfileActivity.class);
                    User user = new User();
                    user.id =  preferencesManager.getString(Constants.KEY_USER_ID);
                    user.about = preferencesManager.getString(Constants.KEY_ABOUT);
                    user.hobby =preferencesManager.getString(Constants.KEY_HOBBIES);
                    user.name =preferencesManager.getString(Constants.KEY_NAME);
                    user.age = preferencesManager.getString(Constants.KEY_AGE);
                    user.image =preferencesManager.getString(Constants.KEY_IMAGE);
                    intent.putExtra(Constants.KEY_USER, user);
                    startActivity(intent);
                    overridePendingTransition(0,0);
                    return true;
                }
                else if (item.getItemId()==R.id.map){
                    return true;
                }
                else if (item.getItemId()==R.id.chat){
                    startActivity(new Intent(getApplicationContext(), RecentConversationsActivity.class));
                    finish();
                    overridePendingTransition(0,0);
                    return true;
                }
                else {
                    return false;
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
                                            List<User> users = new ArrayList<>();

                                            for( QueryDocumentSnapshot queryDocumentSnapshot2 : task2.getResult()){
                                                User user = new User();
                                                user.name =queryDocumentSnapshot2.getString(Constants.KEY_NAME);
                                                user.image =queryDocumentSnapshot2.getString(Constants.KEY_IMAGE);
                                                users.add(user);
                                        }
                                            UserAdapter userAdapter = new UserAdapter(users, this);
                                            binding.usersRecycleView.setAdapter(userAdapter);
                                            binding.usersRecycleView.setVisibility(View.VISIBLE);
                                    }
                        });
                        }
                    }});
    }
    private final EventListener<QuerySnapshot> eventListener = (value, error) -> {
        if (error != null) {
            return;
        }
        if (value != null) {
            int count = users.size();
            for (DocumentChange documentChange : value.getDocumentChanges()) {
                if (documentChange.getType() == DocumentChange.Type.ADDED) {
                    User user = new User();
                    user.name =documentChange.getDocument().getString(Constants.KEY_VISITOR_NAME);
                    user.image =documentChange.getDocument().getString(Constants.KEY_VISITOR_IMAGE);
                    users.add(user);
                }
                if (documentChange.getType() == DocumentChange.Type.REMOVED){
                    int position=-1;
                    for( User user: users){
                        if (user.id.equals(documentChange.getDocument().getString(Constants.KEY_VISITED_ID))){
                            position = users.indexOf(user);
                            break;
                        }
                    }
                    if(position>-1){
                        users.remove(position);
                        userAdapter.notifyItemRemoved(position);
                    }

            }
    }
            if (count==0){
                userAdapter.notifyDataSetChanged();
            }
            else{
                userAdapter.notifyItemRangeInserted(users.size(), users.size());
                binding.usersRecycleView.smoothScrollToPosition(users.size()-1);
            }
            binding.usersRecycleView.setVisibility(View.VISIBLE);
        }
        };

    private void getMeetingUsers(){
        FirebaseFirestore database= FirebaseFirestore.getInstance();
        database.collection(Constants.KEY_COLLECTION_MEET_UP_OFFERS)
                .whereEqualTo(Constants.KEY_VISITED_ID, preferencesManager.getString(Constants.KEY_USER_ID))
                .whereNotEqualTo(Constants.KEY_GENDER,  preferencesManager.getString(Constants.KEY_SEARCH_GENDER))
                .get()
                  .addOnCompleteListener(task->{
                    if(task.isSuccessful()&&task.getResult()!=null) {

                        for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                            ArrayList<User> users = new ArrayList<>();
                                            for( QueryDocumentSnapshot queryDocumentSnapshot2 : task.getResult()){
                                                User user = new User();
                                                user.id = queryDocumentSnapshot.getString(Constants.KEY_VISITOR_ID);
                                                user.name =queryDocumentSnapshot.getString(Constants.KEY_VISITOR_NAME);
                                                user.image =queryDocumentSnapshot.getString(Constants.KEY_VISITOR_IMAGE);
                                                users.add(user);
                                            }
                                            UserAdapter userAdapter = new UserAdapter(users, this);
                                            binding.usersRecycleView.setAdapter(userAdapter);
                                            binding.usersRecycleView.setVisibility(View.VISIBLE);
                        }
                    }
                });
    }
    private void getUsers(){
        FirebaseFirestore database= FirebaseFirestore.getInstance();
        database.collection(Constants.KEY_COLLECTION_USERS)
                .get()
                .addOnCompleteListener(task -> {
                    String currentUserId = preferencesManager.getString(Constants.KEY_USER_ID);
                    if(task.isSuccessful()&&task.getResult()!=null){
                        List<User> users = new ArrayList<>();
                        for(QueryDocumentSnapshot queryDocumentSnapshot:task.getResult()){
                            if(currentUserId.equals(queryDocumentSnapshot.getId())){
                                continue;
                            }
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
                        if (users.size()>0){
                            UserAdapter userAdapter = new UserAdapter(users, this);
                            binding.usersRecycleView.setAdapter(userAdapter);
                            binding.usersRecycleView.setVisibility(View.VISIBLE);
                        }

                    }
                });
}

    @Override
    public void onUserClick(User user) {

    }
    private  void makeToast(String message){
        Toast.makeText(getApplicationContext(),message, Toast.LENGTH_SHORT).show();
    }
}