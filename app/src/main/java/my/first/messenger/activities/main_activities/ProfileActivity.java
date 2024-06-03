package my.first.messenger.activities.main_activities;

import static my.first.messenger.activities.utils.Functions.deleteActivation;
import static my.first.messenger.activities.utils.Functions.deleteVisits;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import java.util.UUID;

import my.first.messenger.R;
import my.first.messenger.activities.adapters.ImageAdapter;
import my.first.messenger.activities.listeners.ImageGalleryListener;
import my.first.messenger.activities.models.Image;
import my.first.messenger.activities.models.User;
import my.first.messenger.activities.utils.Constants;
import my.first.messenger.activities.utils.PreferencesManager;
import my.first.messenger.databinding.ActivityProfileBinding;

    public class ProfileActivity extends BaseActivity implements ImageGalleryListener {
    private ActivityProfileBinding binding;
    private PreferencesManager preferencesManager;
    StorageReference storageReference;
    private FirebaseFirestore database;
    private User user;
    private ArrayList<Image> galleryImages;
    private ImageAdapter imageAdapter;

    private Boolean clicked;
    private Image image;
    private final String TAG ="ProfileActivityTAG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        init();
        loadUsersDetails();
        loadUserGallery();
        getToken();
        setListeners();
    }

    private void init(){
        preferencesManager = new PreferencesManager(getApplicationContext());
        binding.bottomNavigation.setSelectedItemId(R.id.profile);
        database = FirebaseFirestore.getInstance();
        clicked = true;
        user = (User) getIntent().getSerializableExtra(Constants.KEY_USER);
        FirebaseApp.initializeApp(ProfileActivity.this);
        storageReference = FirebaseStorage.getInstance().getReference();
        galleryImages = new ArrayList<>();
        imageAdapter = new ImageAdapter(galleryImages, this, ProfileActivity.this);
    }

    private void loadUsersDetails(){
        user = (User) getIntent().getSerializableExtra(Constants.KEY_USER);
        binding.userName.setText(user.name);
        binding.userAge.setText("Возраст: "+user.age);
        binding.userHobby.setText("Хобби: "+user.hobby);
        binding.userAbout.setText(user.about);
        storageReference.child("images/"+user.id+"/0").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Glide.with(getApplicationContext()).load(uri).into(binding.profilePicture);
                binding.progressProfileImage.setVisibility(View.GONE);
            }
        }).addOnFailureListener(exception -> makeToast(exception.getMessage()));
    }

    private void getToken(){
        FirebaseMessaging.getInstance().getToken().addOnSuccessListener(this::updateToken);
        }

        @Override
        public boolean onCreateOptionsMenu (Menu menu) {
            getMenuInflater().inflate(R.menu.toolbar_menu, menu);
            return true;
        }

        @Override
        public boolean onOptionsItemSelected(@NonNull MenuItem item) {
            if(item.getItemId()==R.id.sign_out){
                 signOut();
            }
            else if (item.getItemId()==R.id.edit){
                Intent intent = new Intent(getApplicationContext(), EditProfileActivity.class );
                intent.putExtra(Constants.KEY_USER, user);
                startActivity(intent);
                finish();
            }
            return true;
        }

    private void updateToken(String token){
        preferencesManager.putString(Constants.KEY_FCM_TOKEN,token);
        DocumentReference documentReference = database.collection(Constants.KEY_COLLECTION_USERS).document(
                preferencesManager.getString(Constants.KEY_USER_ID)
        );
        documentReference.update(Constants.KEY_FCM_TOKEN, token)
                .addOnSuccessListener(unused-> Log.d(TAG, "Token Changed"))
                .addOnFailureListener(e -> makeToast("Failed token"));
    }

    private void setListeners() {
        binding.addImage.setOnClickListener(v->{
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            activityResultLauncher.launch(intent);
        });
        binding.showInfoButton.setOnClickListener(v -> {
            if (clicked) {
                Animation animation = AnimationUtils.loadAnimation(getApplicationContext(), android.R.anim.slide_in_left);
                binding.userInformation.setVisibility(View.VISIBLE);
                binding.userInformation.startAnimation(animation);
                binding.showInfoButton.setImageResource(R.drawable.wrap);
                clicked = false;
            } else {
                binding.showInfoButton.setImageResource(R.drawable.unwrap);
                binding.userInformation.setVisibility(View.GONE);
                clicked = true;
            }
        });
        binding.bottomNavigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if (item.getItemId()==R.id.profile){
                    return true;
                }
                else if (item.getItemId()==R.id.map){
                    database.collection(Constants.KEY_COLLECTION_VISITS).whereEqualTo(Constants.KEY_VISITED_ID,preferencesManager.getString(Constants.KEY_USER_ID))
                            .get().addOnCompleteListener(task->{
                                for(QueryDocumentSnapshot queryDocumentSnapshot:task.getResult()){
                                    preferencesManager.putBoolean(Constants.KEY_IS_ACTIVATED, false);
                                    preferencesManager.putBoolean(Constants.KEY_IS_VISITED, true);
                                    preferencesManager.putString(Constants.KEY_VISITOR_ID, queryDocumentSnapshot.getString(Constants.KEY_VISITOR_ID));
                                    startActivity(new Intent(getApplicationContext(),VisitedActivity.class));
                                }
                            });
                    if (preferencesManager.getBoolean(Constants.KEY_IS_ACTIVATED)){
                        startActivity(new Intent(getApplicationContext(),ActivatedActivity.class));
                        overridePendingTransition(0,0);
                    }
                    else if (preferencesManager.getBoolean(Constants.KEY_IS_GOING)){
                        startActivity(new Intent(getApplicationContext(),RouteActivity.class));
                        overridePendingTransition(0,0);
                    }
                    else if(preferencesManager.getBoolean(Constants.KEY_IS_VISITED)){
                        startActivity(new Intent(getApplicationContext(),VisitedActivity.class));
                        overridePendingTransition(0,0);
                    }
                    else {
                        startActivity(new Intent(getApplicationContext(), UserLocationActivity.class));
                        overridePendingTransition(0,0);
                    }
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

        private void signOut() {
            if (preferencesManager.getBoolean(Constants.KEY_IS_GOING)||preferencesManager.getBoolean(Constants.KEY_IS_ACTIVATED)||preferencesManager.getBoolean(Constants.KEY_IS_VISITED)){
                try {
                    deleteActivation(database, preferencesManager);
                    deleteVisits(database, preferencesManager);
                }
                catch (Exception e){
                    Log.d(TAG, Objects.requireNonNull(e.getMessage()));
                }
            }
            DocumentReference documentReference = database.collection(Constants.KEY_COLLECTION_USERS).document(preferencesManager.getString(Constants.KEY_USER_ID));
            HashMap<String, Object> updates = new HashMap<>();
            updates.put(Constants.KEY_FCM_TOKEN, FieldValue.delete());
            updates.put(Constants.KEY_AVAILABILITY, 0);
            updates.put(Constants.KEY_IS_SIGNED_IN, false);
            documentReference.update(updates)
                    .addOnSuccessListener(unused -> {
                        preferencesManager.putBoolean(Constants.KEY_IS_GOING, false);
                        preferencesManager.putBoolean(Constants.KEY_IS_ACTIVATED, false);
                        preferencesManager.putBoolean(Constants.KEY_IS_VISITED, false);
                        preferencesManager.putBoolean(Constants.KEY_IS_SIGNED_IN, false);
                        preferencesManager.putString(Constants.KEY_SEARCH_PURPOSE, "");
                        preferencesManager.putString(Constants.KEY_SEARCH_GENDER, "");
                        preferencesManager.putLong(Constants.KEY_SEARCH_MIN_AGE, 0);
                        preferencesManager.putLong(Constants.KEY_SEARCH_MAX_AGE, 0);
                        startActivity(new Intent(getApplicationContext(), LogIn.class));
                        finish();
                    })
                    .addOnFailureListener(e -> makeToast("Ошибка выхода"));
            }

    private final ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
            if (result.getResultCode() == RESULT_OK) {
                if (result.getData() != null) {
                    image = new Image();
                    image.uri = result.getData().getData().toString();
                    image.name = UUID.randomUUID().toString();
                    galleryImages.add(image);
                    imageAdapter.notifyDataSetChanged();
                    uploadImage(result.getData().getData(), image.name);
                }
            }
            else {
                makeToast("Пожалуйста, Выберите фото");
            }
        }
    });

    private void uploadImage(Uri file, String name) {
        StorageReference ref = storageReference.child("images/" + user.id + "/" + name);
        ref.putFile(file).addOnSuccessListener(taskSnapshot -> {
            makeToast("uploaded!");
            binding.progressProfileImage.setVisibility(View.GONE);
        }).addOnFailureListener(e -> makeToast(e.getMessage()));
    }

    private void loadUserGallery(){
        FirebaseStorage.getInstance().getReference().child("images/" + user.id + "/").listAll().addOnSuccessListener(new OnSuccessListener<ListResult>() {
            @Override
            public void onSuccess(ListResult listResult) {
                binding.userGallery.setAdapter(imageAdapter);
                listResult.getItems().forEach(storageReference -> {
                    Image image = new Image();
                    image.name = storageReference.getName();
                    storageReference.getDownloadUrl().addOnCompleteListener(task -> {
                        String url = "https://" + task.getResult().getEncodedAuthority() + task.getResult().getEncodedPath() + "?alt=media&token=" + task.getResult().getQueryParameters("token").get(0);
                        image.uri = url;
                        galleryImages.add(image);
                        imageAdapter.notifyDataSetChanged();
                    });
                });
            }
        }).addOnFailureListener(e -> makeToast("Неудалось извлечь файл"));
    }

    public void deleteImage(String name){
            FirebaseStorage.getInstance().getReference().child("images/" + user.id + "/" + name + "").delete();
    }

    public void onImageGalleryClick(String url, int position){
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        intent.setDataAndType(Uri.parse(url), "image/*");
        startActivity(intent);
    }

    private void makeToast(String message){
            Toast.makeText(getApplicationContext(),message, Toast.LENGTH_SHORT).show();
    }
}