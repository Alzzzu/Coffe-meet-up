package my.first.messenger.activities.main_activities;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import my.first.messenger.activities.models.User;
import my.first.messenger.activities.utils.Constants;
import my.first.messenger.activities.utils.PreferencesManager;
import my.first.messenger.databinding.LoginBinding;

public class LogIn extends AppCompatActivity {
    private User user;;
    private LoginBinding binding;
    private PreferencesManager preferencesManager;
    private FirebaseFirestore database;
    private final String TAG ="LogIn";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferencesManager = new PreferencesManager(getApplicationContext());
        user = new User();
        database = FirebaseFirestore.getInstance();
        checkIfSignedIn();
        binding = LoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setListeners();
    }
    private void setListeners(){
        binding.login.setOnClickListener(v-> {
            if (checkConnection()) {
                checkUser(binding.email.getText().toString(), binding.password.getText().toString());
            }
        });
        binding.register.setOnClickListener(v-> {
            Intent i = new Intent(LogIn.this, SignUp.class);
            startActivity(i);
        });
    }
    private void checkIfSignedIn() {
        if (preferencesManager.getBoolean(Constants.KEY_IS_SIGNED_IN)) {
            Intent i = new Intent(getApplicationContext(), ProfileActivity.class);
            user.id = preferencesManager.getString(Constants.KEY_USER_ID);
            user.about = preferencesManager.getString(Constants.KEY_ABOUT);
            user.hobby = preferencesManager.getString(Constants.KEY_HOBBIES);
            user.name = preferencesManager.getString(Constants.KEY_NAME);
            user.age = preferencesManager.getString(Constants.KEY_AGE);
            user.gender = preferencesManager.getString(Constants.KEY_GENDER);
            user.image = preferencesManager.getString(Constants.KEY_IMAGE);
            database.collection(Constants.KEY_COLLECTION_VISITS).whereEqualTo(Constants.KEY_VISITED_ID, user.id)
                    .get().addOnCompleteListener(task -> {
                        for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                            preferencesManager.putBoolean(Constants.KEY_IS_ACTIVATED, false);
                            preferencesManager.putBoolean(Constants.KEY_IS_VISITED, true);
                            preferencesManager.putString(Constants.KEY_VISITOR_ID, queryDocumentSnapshot.getString(Constants.KEY_VISITOR_ID));
                        }
                    });
            i.putExtra(Constants.KEY_USER, user);
            startActivity(i);
            finish();
        }
    }

    private void checkUser(String user_email, String user_password) {
        database.collection(Constants.KEY_COLLECTION_USERS)
                .whereEqualTo(Constants.KEY_EMAIL, binding.email.getText().toString())
                .whereEqualTo(Constants.KEY_PASSWORD, binding.password.getText().toString())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null && task.getResult().getDocuments().size() > 0) {
                        DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
                        if (Boolean.FALSE.equals(documentSnapshot.getBoolean(Constants.KEY_IS_SIGNED_IN))){
                            database.collection(Constants.KEY_COLLECTION_USERS).document(task.getResult().getDocuments().get(0).getId()).update(Constants.KEY_IS_SIGNED_IN, true);
                            preferencesManager.putBoolean(Constants.KEY_IS_SIGNED_IN, true);
                            preferencesManager.putString(Constants.KEY_USER_ID, documentSnapshot.getId());
                            preferencesManager.putString(Constants.KEY_NAME, documentSnapshot.getString(Constants.KEY_NAME));
                            preferencesManager.putString(Constants.KEY_AGE, (documentSnapshot.getLong(Constants.KEY_AGE)+""));
                            preferencesManager.putString(Constants.KEY_HOBBIES, documentSnapshot.getString(Constants.KEY_HOBBIES));
                            preferencesManager.putString(Constants.KEY_ABOUT, documentSnapshot.getString(Constants.KEY_ABOUT));
                            preferencesManager.putString(Constants.KEY_GENDER, documentSnapshot.getString(Constants.KEY_GENDER));
                            preferencesManager.putString(Constants.KEY_IMAGE, documentSnapshot.getString(Constants.KEY_IMAGE));
                            user.id =  documentSnapshot.getId();
                            user.about = documentSnapshot.getString(Constants.KEY_ABOUT);
                            user.hobby =documentSnapshot.getString(Constants.KEY_HOBBIES);
                            user.name =documentSnapshot.getString(Constants.KEY_NAME);
                            user.gender =documentSnapshot.getString(Constants.KEY_GENDER);
                            user.age = documentSnapshot.getLong(Constants.KEY_AGE).toString();
                            user.image =documentSnapshot.getString(Constants.KEY_IMAGE);
                            Intent intent = new Intent(getApplicationContext(), ProfileActivity.class);
                            intent.putExtra(Constants.KEY_USER, user);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                        }
                        else {
                            binding.incorrect.setVisibility(View.VISIBLE);
                            binding.incorrect.setText("Этот аккаунт уже в сети");
                        }
                    }
                    else {
                        makeToast("Не можем найти пользователя");
                        binding.incorrect.setVisibility(View.VISIBLE);
                    }
                });
        }

    private Boolean checkConnection(){
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager != null ? connectivityManager.getActiveNetworkInfo() : null;
        if (activeNetworkInfo != null && activeNetworkInfo.isConnected()){
            return true;
        }
        else{
            binding.incorrect.setText("Нет подключения к сети");
            binding.incorrect.setVisibility(View.VISIBLE);
            return false;
        }
    }
    public void makeToast(String message){
        Toast.makeText(getApplicationContext(),message, Toast.LENGTH_SHORT).show();
    }
}