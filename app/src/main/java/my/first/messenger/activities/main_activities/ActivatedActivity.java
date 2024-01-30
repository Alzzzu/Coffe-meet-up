package my.first.messenger.activities.main_activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import my.first.messenger.R;

public class ActivatedActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_activated);
    }
}