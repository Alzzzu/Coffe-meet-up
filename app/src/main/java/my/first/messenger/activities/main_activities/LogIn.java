package my.first.messenger.activities.main_activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import my.first.messenger.R;
import my.first.messenger.activities.models.User;
import my.first.messenger.activities.utils.PreferencesManager;
import my.first.messenger.activities.utils.Constants;
import my.first.messenger.databinding.LoginBinding;

public class LogIn extends AppCompatActivity {
    private User user;;
    private LoginBinding binding;
    private PreferencesManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // initializing classes
        preferenceManager = new PreferencesManager(getApplicationContext());
        user = new User();
        preferenceManager.putBoolean(Constants.KEY_IS_GOING, false);

        // checking if user is logged in
    //    if(preferenceManager.getBoolean(Constants.KEY_IS_GOING)) {
     //       Intent i  = new Intent(getApplicationContext(), RouteActivity.class);
      //      startActivity(i);
      //      finish();

        // }

          if(preferenceManager.getBoolean(Constants.KEY_IS_SIGNED_IN)) {
            Intent i = new Intent(getApplicationContext(), ProfileActivity.class);
              user.id =  preferenceManager.getString(Constants.KEY_USER_ID);
              user.about = preferenceManager.getString(Constants.KEY_ABOUT);
              user.hobby =preferenceManager.getString(Constants.KEY_HOBBIES);
              user.name =preferenceManager.getString(Constants.KEY_NAME);
              user.age = preferenceManager.getString(Constants.KEY_AGE);
              user.image =preferenceManager.getString(Constants.KEY_IMAGE);
            i.putExtra(Constants.KEY_USER, user);
            startActivity(i);
            finish();
        }

        binding = LoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setListeners();

    }

        private void setListeners(){

        // log in button pressed
        binding.login.setOnClickListener(v-> {
                checkUser(binding.email.getText().toString(), binding.password.getText().toString());
        });

        // register button pressed
        binding.register.setOnClickListener(v-> {

                Intent i = new Intent(LogIn.this, SignUp.class);
                startActivity(i);

        });
    }

    // check if user exist in database
    private void checkUser(String user_email, String user_password) {
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection(Constants.KEY_COLLECTION_USERS)
                .whereEqualTo(Constants.KEY_EMAIL, binding.email.getText().toString())
                .whereEqualTo(Constants.KEY_PASSWORD, binding.password.getText().toString())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null && task.getResult().getDocuments().size() > 0) {
                        DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
                        preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN, true);
                        preferenceManager.putString(Constants.KEY_USER_ID, documentSnapshot.getId());
                        preferenceManager.putString(Constants.KEY_NAME, documentSnapshot.getString(Constants.KEY_NAME));
                        preferenceManager.putString(Constants.KEY_AGE, ((documentSnapshot.getLong(Constants.KEY_AGE)).toString()));
                        preferenceManager.putString(Constants.KEY_HOBBIES, documentSnapshot.getString(Constants.KEY_HOBBIES));
                        preferenceManager.putString(Constants.KEY_ABOUT, documentSnapshot.getString(Constants.KEY_ABOUT));
                        preferenceManager.putString(Constants.KEY_IMAGE, documentSnapshot.getString(Constants.KEY_IMAGE));
                        user.id =  documentSnapshot.getId();
                        user.about = documentSnapshot.getString(Constants.KEY_ABOUT);
                        user.hobby =documentSnapshot.getString(Constants.KEY_HOBBIES);
                        user.name =documentSnapshot.getString(Constants.KEY_NAME);
                        user.age = documentSnapshot.getLong(Constants.KEY_AGE).toString();
                        user.image =documentSnapshot.getString(Constants.KEY_IMAGE);
                        Intent intent = new Intent(getApplicationContext(), ProfileActivity.class);
                        intent.putExtra(Constants.KEY_USER, user);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    }
                    else {
                        makeToast("Не можем найти пользователя");
                        binding.incorrect.setVisibility(View.VISIBLE);
                    }
                });
    }
    public void makeToast(String message){
        Toast.makeText(getApplicationContext(),message, Toast.LENGTH_SHORT).show();
    }

}