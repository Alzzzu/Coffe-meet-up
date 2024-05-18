package my.first.messenger.activities.main_activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import my.first.messenger.activities.utils.Constants;
import my.first.messenger.activities.utils.PreferencesManager;
import my.first.messenger.databinding.ActivitySignUpBinding;

public class SignUp extends AppCompatActivity {
    private ActivitySignUpBinding binding;
    PreferencesManager preferencesManager;
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        binding = ActivitySignUpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferencesManager = new PreferencesManager(this);
        binding.finish.setOnClickListener(v->{
                if(isValidEmailAndPassword(binding.email.getText().toString(), binding.createPassword.getText().toString(), binding.confirmPassword.getText().toString())) {
                    checkIfEmailWasNotUsed(binding.email.getText().toString(), binding.createPassword.getText().toString());
                }
           });
    }
        private void checkIfEmailWasNotUsed(String user_email, String user_password){
            FirebaseFirestore database = FirebaseFirestore.getInstance();
            database.collection(Constants.KEY_COLLECTION_USERS)
                    .whereEqualTo(Constants.KEY_EMAIL, binding.email.getText().toString())
                    .get()
                    .addOnCompleteListener(task->{
                        if (task.isSuccessful() && task.getResult() != null && task.getResult().getDocuments().size() > 0) {
                            DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
                            makeToast("Эта почта уже была использована!");}
                        else {

                            Intent i = new Intent(SignUp.this, FillingUserInfo.class);
                            i.putExtra("email and password", new String[]{user_email,user_password});
                            startActivity(i);

                        }
                    });
                }
    private Boolean isValidEmailAndPassword(String email, String password, String confirmPasword){
        Boolean ans = false;
        if(!email.matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$")){
            makeToast("некорректный адрес почты");
            return false;
        }
        if(email.isEmpty() || password.isEmpty()){
            makeToast("пустой адрес почты или пароль");
            return false;
        }
        if(!password.equals(confirmPasword)) {
            makeToast("введенные пароли не совпадают");
            return false;
        }
        return true;
    }
    public void makeToast(String message){
        Toast.makeText(getApplicationContext(),message, Toast.LENGTH_SHORT).show();
    }

}

