package my.first.messenger.activities.main_activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import my.first.messenger.activities.models.User;
import my.first.messenger.activities.utils.Constants;
import my.first.messenger.activities.utils.PreferencesManager;
import my.first.messenger.databinding.ActivityEditProfileBinding;

public class EditProfileActivity extends AppCompatActivity {
    private ActivityEditProfileBinding binding;
    private String encodedImage;
    private Boolean isPictureUpdate = false;

    private User user;
    private Uri uri=null;
    private StorageReference storageReference;
    private PreferencesManager preferencesManager;
    private FirebaseFirestore database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        init();
        loadUserInfo();
        setListeners();
    }
    private void init(){
        user = (User) getIntent().getSerializableExtra(Constants.KEY_USER);
        preferencesManager = new PreferencesManager(getApplicationContext());
        storageReference = FirebaseStorage.getInstance().getReference();

    }
    private void loadUserInfo(){
        binding.name.setText(user.name);
        binding.hobby.setText(user.hobby);
        binding.about.setText(user.about);
        storageReference.child("images/"+user.id+"/0").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Glide.with(getApplicationContext()).load(uri).into(binding.profilePicture);
                Glide.with(getApplicationContext()).load(uri).into(binding.galleryImage);
                binding.progressProfileImage.setVisibility(View.GONE);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                makeToast(exception.getMessage());
            }
        });
    }
    private void setListeners(){
        binding.profilePicture.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            activityResultLauncher.launch(intent);
        });
        binding.done.setOnClickListener(v -> {
            if (isValidFilling()){
                binding.done.setOnClickListener(null);
                updateUserInfo();
                //if(updateUserInfo()){
      //          Intent intent = new Intent(getApplicationContext(), ProfileActivity.class );
       //         intent.putExtra(Constants.KEY_USER, user);
       //         startActivity(intent);
         //       finish();
                    //}
            }
        });
    }
    private void updateUserInfo(){
         if (!(uri==null)){
            uploadImageProfile(uri);
            user.image = encodedImage;
            isPictureUpdate = true;
         }

        user.name = binding.name.getText().toString();
        user.hobby = binding.hobby.getText().toString();
        user.about = binding.about.getText().toString();
        preferencesManager.putString(Constants.KEY_NAME, user.name);
        preferencesManager.putString(Constants.KEY_HOBBIES, user.hobby);
        preferencesManager.putString(Constants.KEY_ABOUT, user.about);
        preferencesManager.putString(Constants.KEY_IMAGE, user.image);
        database = FirebaseFirestore.getInstance();

        database.collection(Constants.KEY_COLLECTION_USERS).document(user.id)
                .update(Constants.KEY_NAME, user.name,
                        Constants.KEY_HOBBIES, user.hobby,
                        Constants.KEY_ABOUT, user.about,
                        Constants.KEY_IMAGE, user.image);
        if(isPictureUpdate){

        database.collection(Constants.KEY_COLLECTION_CONVERSATIONS).whereEqualTo(Constants.KEY_SENDER_ID, user.id).get()
                .addOnCompleteListener(task -> {
                    for (QueryDocumentSnapshot queryDocumentSnapshot: task.getResult()){
                        database.collection(Constants.KEY_COLLECTION_CONVERSATIONS).document(queryDocumentSnapshot.getId())
                                .update(Constants.KEY_SENDER_IMAGE, user.image);
                    }
                });

        database.collection(Constants.KEY_COLLECTION_CONVERSATIONS).whereEqualTo(Constants.KEY_RECEIVER_ID, user.id).get()
                    .addOnCompleteListener(task -> {
                        for (QueryDocumentSnapshot queryDocumentSnapshot: task.getResult()){
                            database.collection(Constants.KEY_COLLECTION_CONVERSATIONS).document(queryDocumentSnapshot.getId())
                                    .update(Constants.KEY_RECEIVER_IMAGE, user.image);
                        }
                    });
        }
        else{
            Intent intent = new Intent(getApplicationContext(), ProfileActivity.class );
            intent.putExtra(Constants.KEY_USER, user);
            startActivity(intent);
            finish();

        }

    }
    private Boolean isValidFilling(){
        // if(encodedImage.equals(null)){
         //   makeToast("Выберите фото");
          ///  return false;
        //}

        if (!binding.name.getText().toString().matches("\\w+")) {
            makeToast("Пожалуйста, используйте только буквы для записи имени");
            return false;
        }
        if (binding.about.getText().toString().isEmpty() || binding.hobby.getText().toString().isEmpty()) {
            makeToast("Пожалуйста, заполните все поля");
            return false;
        }
        return true;
    }
    private final ActivityResultLauncher<Intent> pickImage = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    if (result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        try {
                            InputStream inputStream = getContentResolver().openInputStream(imageUri);
                            BitmapFactory.Options options = new BitmapFactory.Options();
                             options.inScaled = false;
                             options.inDither = false;
                             options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                            Bitmap bitmap = BitmapFactory.decodeStream(inputStream, null, options);
                            binding.profilePicture.setImageBitmap(bitmap);
                            String encodedImage = encodeImage(bitmap);
                            byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
                            Bitmap new_bitmap = BitmapFactory.decodeByteArray(bytes, 0 ,bytes.length);
                            //here
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
    private String encodeImage(Bitmap bitmap){
        int previewWidth = 150;
        int previewHeight = bitmap.getHeight() * previewWidth / bitmap.getWidth();
        Bitmap previewBitmap = Bitmap.createScaledBitmap(bitmap, previewWidth, previewHeight, true);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        previewBitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        byte[] bytes = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(bytes, Base64.DEFAULT);
    }
    private final ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
            if (result.getResultCode() == RESULT_OK) {
                if (result.getData() != null) {
                          Glide.with(getApplicationContext()).load(result.getData().getData()).into(binding.galleryImage);
                          Glide.with(getApplicationContext()).load(result.getData().getData()).into(binding.profilePicture);
                          //uploadImageProfile(result.getData().getData());
                          uri = result.getData().getData();
                    try {
                        InputStream inputStream = getContentResolver().openInputStream(uri);
                        BitmapFactory.Options options = new BitmapFactory.Options();
                        options.inScaled = false;
                        options.inDither = false;
                        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                        Bitmap bitmap = BitmapFactory.decodeStream(inputStream, null, options);
                        binding.profilePicture.setImageBitmap(bitmap);
                        encodedImage = encodeImage(bitmap);
                       // byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
                        //Bitmap new_bitmap = BitmapFactory.decodeByteArray(bytes, 0 ,bytes.length);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }

                }
            }
        }
    });
    private void uploadImageProfile(Uri file) {
        StorageReference ref = storageReference.child("images/"+ user.id+"/" + "0");
        ref.putFile(file).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                //     makeToast("uploaded!");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                makeToast(e.getMessage());
            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot) {
                   binding.progress.setMax(Math.toIntExact(taskSnapshot.getTotalByteCount()));
                    binding.progress.setProgress(Math.toIntExact(taskSnapshot.getBytesTransferred()));
            }
        }).addOnCompleteListener(task -> {
            Intent intent = new Intent(getApplicationContext(), ProfileActivity.class );
            intent.putExtra(Constants.KEY_USER, user);
            startActivity(intent);
            finish();

        });

    }


    public void makeToast(String message){
        Toast.makeText(getApplicationContext(),message, Toast.LENGTH_SHORT).show();
    }
}