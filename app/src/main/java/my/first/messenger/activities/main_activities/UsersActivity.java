package my.first.messenger.activities.main_activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import my.first.messenger.activities.adapters.UserAdapter;
import my.first.messenger.activities.listeners.UsersListener;
import my.first.messenger.activities.models.User;
import my.first.messenger.activities.utils.Constants;
import my.first.messenger.activities.utils.PreferencesManager;
import my.first.messenger.databinding.ActivityUsersBinding;

public class UsersActivity extends AppCompatActivity implements UsersListener {
    private ActivityUsersBinding binding;
    private PreferencesManager preferencesManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUsersBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferencesManager = new PreferencesManager(getApplicationContext());
        setOnListeners();
        getUsers();

    }
    private void setOnListeners(){
        binding.imageBack.setOnClickListener(v -> onBackPressed());
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
                        else{
                            makeToast("no users found");
                        }
                    }
                });

    }
    public void makeToast(String message){
        Toast.makeText(getApplicationContext(),message, Toast.LENGTH_SHORT).show();
    }
    @Override
    public void onUserClick(User user){
        Intent intent = new Intent(getApplicationContext(), ProfileActivity.class);
        intent.putExtra(Constants.KEY_USER, user);
     //   startActivity(intent);
    //    finish();
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        HashMap<String, Object> updt = new HashMap<>();
        updt.put(Constants.KEY_VISITED_IMAGE, user.name);
        updt.put(Constants.KEY_VISITOR_IMAGE, preferencesManager.getString(Constants.KEY_NAME));
        updt.put(Constants.KEY_VISITED_ID, user.id);
        updt.put(Constants.KEY_VISITOR_ID, preferencesManager.getString(Constants.KEY_USER_ID));
        updt.put(Constants.KEY_VISITED_NAME, user.image);
        updt.put(Constants.KEY_VISITOR_NAME, preferencesManager.getString(Constants.KEY_IMAGE));
        database.collection(Constants.KEY_COLLECTION_MEET_UP_OFFERS).add(updt);
    }
}