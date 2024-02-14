package my.first.messenger.activities.main_activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.makeramen.roundedimageview.RoundedImageView;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;
import java.util.function.Consumer;

import my.first.messenger.R;
import my.first.messenger.activities.adapters.ImageAdapter;
import my.first.messenger.activities.listeners.ImageGalleryListener;
import my.first.messenger.activities.models.Image;
import my.first.messenger.activities.models.User;
import my.first.messenger.activities.utils.PreferencesManager;
import my.first.messenger.activities.utils.Constants;
import my.first.messenger.databinding.ActivityProfileBinding;

    public class ProfileActivity extends BaseActivity implements ImageGalleryListener {
    private ActivityProfileBinding binding;
    // private TextView name, age, hobby, about;
    private PreferencesManager preferencesManager;
    StorageReference storageReference;

    private User user;
    private ArrayList<Image> galleryImages;
    private ImageAdapter imageAdapter;

    private Boolean clicked;
    private Image image;
    private String encodedImage;
    BottomNavigationView bottomNavigationView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        init();
        loadUsersDetails();
        loadUserGallery();
        getToken();
        setListeners();
    }
    private void init(){
        preferencesManager = new PreferencesManager(getApplicationContext());
        binding.bottomNavigation.setSelectedItemId(R.id.profile);
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
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                makeToast(exception.getMessage());
            }
        });

        if (user.id.equals(preferencesManager.getString(Constants.KEY_USER_ID))){
            binding.editProfile.setVisibility(View.VISIBLE);
            binding.imageSignOut.setVisibility(View.VISIBLE);
            binding.bottomNavigation.setVisibility(View.VISIBLE);
            binding.imageBack.setVisibility(View.GONE);
            binding.buttonToText.setVisibility(View.GONE);
            binding.addImage.setVisibility(View.VISIBLE);
            binding.bottomNavigation.setVisibility(View.VISIBLE);
        }
    }
    private void getToken(){
        FirebaseMessaging.getInstance().getToken().addOnSuccessListener(this::updateToken);
    }
    private void updateToken(String token){

        preferencesManager.putString(Constants.KEY_FCM_TOKEN,token);

        FirebaseFirestore database  = FirebaseFirestore.getInstance();
        DocumentReference documentReference = database.collection(Constants.KEY_COLLECTION_USERS).document(
                preferencesManager.getString(Constants.KEY_USER_ID)
        );

        documentReference.update(Constants.KEY_FCM_TOKEN, token)
                .addOnSuccessListener(unused->makeToast("Token changed"))
                .addOnFailureListener(e -> makeToast("Failed token"));
    }

    private void signOut() {
        makeToast("Signing out...");
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection(Constants.KEY_COLLECTION_MEET_UP_OFFERS)
                .whereEqualTo(Constants.KEY_VISITED_ID, preferencesManager.getString(Constants.KEY_USER_ID))
                .get()
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()&&task.getResult()!=null) {

                        for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                            FirebaseFirestore.getInstance().collection(Constants.KEY_COLLECTION_MEET_UP_OFFERS).document(queryDocumentSnapshot.getId()).delete();
                        }
                        }
                });

        database.collection(Constants.KEY_COLLECTION_MEET_UP_OFFERS)
                .whereEqualTo(Constants.KEY_VISITOR_ID, preferencesManager.getString(Constants.KEY_USER_ID))
                .get()
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()&&task.getResult()!=null) {

                        for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                            FirebaseFirestore.getInstance().collection(Constants.KEY_COLLECTION_MEET_UP_OFFERS).document(queryDocumentSnapshot.getId()).delete();
                        }
                    }
                });

        // delete from coffeeshop
        if(preferencesManager.getBoolean(Constants.KEY_IS_ACTIVATED)||preferencesManager.getBoolean(Constants.KEY_IS_GOING)) {
            database.collection("coffeeshops").document(preferencesManager.getString(Constants.KEY_COFFEESHOP_ID))
                    .collection(Constants.KEY_COLLECTION_USERS)
                    .document(preferencesManager.getString(Constants.KEY_USER_ID)).delete();

            if (database.collection(Constants.KEY_COLLECTION_COFFEE_SHOPS)
                    .document(preferencesManager.getString(Constants.KEY_COFFEESHOP_ID))
                    .collection(Constants.KEY_COLLECTION_USERS).count().equals(0)){

            }

        }
        DocumentReference documentReference =
                database.collection(Constants.KEY_COLLECTION_USERS).document( preferencesManager.getString(Constants.KEY_USER_ID)
                );
        HashMap<String, Object> updates =  new HashMap<>();
        updates.put(Constants.KEY_FCM_TOKEN, FieldValue.delete());
        documentReference.update(updates)
                .addOnSuccessListener(unused -> {
                    preferencesManager.clear();
                    startActivity(new Intent(getApplicationContext(), LogIn.class));
                    finish();
                })
                .addOnFailureListener(e-> makeToast("Unable to sign out"));
    }
    private void setListeners() {

        //Signing out
        binding.imageSignOut.setOnClickListener(v -> signOut());
        binding.imageBack.setOnClickListener(v -> {startActivity(new Intent(getApplicationContext(), UsersActivity.class));
            finish();
        });

        //Editing user's profile
        binding.editProfile.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), EditProfileActivity.class );
            intent.putExtra(Constants.KEY_USER, user);
            startActivity(intent);
            finish();
        });

        //Redirecting to chatActivity
        binding.buttonToText.setOnClickListener(v ->{
            Intent intent = new Intent(getApplicationContext(), ChatActivity.class);
            intent.putExtra(Constants.KEY_USER, user);
            startActivity(intent);
            finish();}
        );

        // adding images
        binding.addImage.setOnClickListener(v->{
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            activityResultLauncher.launch(intent);

        });

        // hiding and showing information
        binding.showInfoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (clicked) {
                    binding.userInformation.setVisibility(View.VISIBLE);
                    binding.showInfoButton.setImageResource(R.drawable.wrap);
                    clicked = false;
                } else {
                    binding.userInformation.setVisibility(View.GONE);
                    binding.showInfoButton.setImageResource(R.drawable.unwrap);
                    clicked = true;

                }
            }
        });

        // Setting BottomNavigation
        binding.bottomNavigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                if (item.getItemId()==R.id.profile){
                    return true;
                }
                else if (item.getItemId()==R.id.map){
                    if (preferencesManager.getBoolean(Constants.KEY_IS_ACTIVATED)){
                        startActivity(new Intent(getApplicationContext(),ActivatedActivity.class));

                    }
                    else if (preferencesManager.getBoolean(Constants.KEY_IS_GOING)){
                        startActivity(new Intent(getApplicationContext(),RouteActivity.class));
                    }
                    else {
                        startActivity(new Intent(getApplicationContext(), UserLocationActivity.class));
                        overridePendingTransition(0, 0);
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


    private final ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
            if (result.getResultCode() == RESULT_OK) {
                if (result.getData() != null) {
                    image = new Image();
                    image.uri = result.getData().getData().toString();
                    image.name = UUID.randomUUID().toString();
                    //      Glide.with(getApplicationContext()).load(image).into(binding.userGallery);
                    //      Glide.with(getApplicationContext()).load(image).into(binding.profilePicture);

                    galleryImages.add(image);
                    imageAdapter.notifyDataSetChanged();
                    uploadImage(result.getData().getData(), image.name);//imageAdapter.getItemCount()+"");
                }
            } else {
                Toast.makeText(ProfileActivity.this, "Please select an image", Toast.LENGTH_SHORT).show();
            }
        }
    });


    private void uploadImage(Uri file, String name) {
        StorageReference ref = storageReference.child("images/"+ user.id+"/" + name);
        ref.putFile(file).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                makeToast("uploaded!");
                binding.progressProfileImage.setVisibility(View.GONE);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                makeToast(e.getMessage());
            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot) {
                //   progressIndicator.setMax(Math.toIntExact(taskSnapshot.getTotalByteCount()));
                //    progressIndicator.setProgress(Math.toIntExact(taskSnapshot.getBytesTransferred()));
            }
        });
    }
    private void loadUserGallery(){
        FirebaseStorage.getInstance().getReference().child("images/"+user.id+"/").listAll().addOnSuccessListener(new OnSuccessListener<ListResult>() {

            @Override
            public void onSuccess(ListResult listResult) {
                binding.userGallery.setAdapter(imageAdapter);
                listResult.getItems().forEach(new Consumer<StorageReference>() {
                    @Override
                    public void accept(StorageReference storageReference) {
                        Image image = new Image();
                        image.name = storageReference.getName();
                        storageReference.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                            @Override
                            public void onComplete(@NonNull Task<Uri> task) {
                                String url = "https://" + task.getResult().getEncodedAuthority() + task.getResult().getEncodedPath() + "?alt=media&token=" + task.getResult().getQueryParameters("token").get(0);
                                image.uri = url;
                                galleryImages.add(image);
                                imageAdapter.notifyDataSetChanged();
                            }
                        });

                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(ProfileActivity.this, "Failed to retrieve images", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void makeToast(String message){
        Toast.makeText(getApplicationContext(),message, Toast.LENGTH_SHORT).show();
    }
    public void deleteImage(String name){
        //if(user.id.equals(preferencesManager.getString(Constants.KEY_USER_ID))){
            FirebaseStorage.getInstance().getReference().child("images/" + user.id + "/" + name + "").delete();
            FirebaseStorage.getInstance().getReference().child("images/" + user.id + "/" + name + "").delete();
    }
    public void onImageGalleryClick(String url, int position){
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        intent.setDataAndType(Uri.parse(url), "image/*");
        startActivity(intent);
    }
}

