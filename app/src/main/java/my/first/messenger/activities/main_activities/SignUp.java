package my.first.messenger.activities.main_activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import my.first.messenger.R;
import my.first.messenger.activities.utils.PreferencesManager;
import my.first.messenger.activities.utils.Constants;
import my.first.messenger.databinding.ActivitySignUpBinding;

public class SignUp extends AppCompatActivity {
    private EditText email, password, confirmedPassword;
    //private static final String reg_email = "^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$";
    private Button finish;
    private ActivitySignUpBinding binding;
    PreferencesManager preferencesManager;
    private TextView tv;
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        email = findViewById(R.id.editTextTextEmailAddress);
        password = findViewById(R.id.create_password);
        confirmedPassword = findViewById(R.id.confirm_password);
        finish = findViewById(R.id.button2);
        preferencesManager = new PreferencesManager(this);
        finish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isValidEmailAndPassword(email.getText().toString(), password.getText().toString(), confirmedPassword.getText().toString())){
                checkIfEmailWasNotUsed(email.getText().toString(), password.getText().toString());
                }
            }

        });
}
        private void checkIfEmailWasNotUsed(String user_email, String user_password){
            FirebaseFirestore database = FirebaseFirestore.getInstance();
            database.collection(Constants.KEY_COLLECTION_USERS)
                    .whereEqualTo(Constants.KEY_EMAIL, email.getText().toString())
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

