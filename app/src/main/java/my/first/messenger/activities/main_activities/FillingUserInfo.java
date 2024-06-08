package my.first.messenger.activities.main_activities;

import static java.lang.Integer.parseInt;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;

import my.first.messenger.R;
import my.first.messenger.activities.models.User;
import my.first.messenger.activities.utils.Constants;
import my.first.messenger.activities.utils.PreferencesManager;
import my.first.messenger.databinding.FillingProfileBinding;

public class FillingUserInfo extends AppCompatActivity {
    private PreferencesManager preferencesManager;
    private FillingProfileBinding binding;
    private StorageReference storageReference;
    private User user;
    private String encodedImage;
    private static Uri url;
    private String[] email_and_password;
    private static String gender;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = FillingProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        url = null;
        email_and_password = getIntent().getStringArrayExtra("email and password");
        preferencesManager = new PreferencesManager(getApplicationContext());
        user = new User();
        storageReference = FirebaseStorage.getInstance().getReference();
        getGender();
        binding.button.setOnClickListener(v -> {
            if(isValidFilling()){
                binding.progress.setVisibility(View.VISIBLE);
                binding.loading.setVisibility(View.VISIBLE);
                binding.whiteBackground.setVisibility(View.VISIBLE);
                binding.background.setVisibility(View.GONE);
                binding.photo.setVisibility(View.GONE);
                getGender();
                fillInfo();
            }
        });
        binding.layoutPicture.setOnClickListener(v -> {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                activityResultLauncher.launch(intent);
    });
    }
    private void fillInfo() {
       FirebaseFirestore database = FirebaseFirestore.getInstance();
       HashMap<String, Object> data = new HashMap<>();
       data.put(Constants.KEY_EMAIL, email_and_password[0]);
       data.put(Constants.KEY_PASSWORD, email_and_password[1]);
       data.put(Constants.KEY_NAME, binding.name.getText().toString());
       data.put(Constants.KEY_GENDER, gender);
       data.put(Constants.KEY_AGE, parseInt(binding.age.getText().toString()));
       data.put(Constants.KEY_ABOUT, binding.about.getText().toString());
       data.put(Constants.KEY_HOBBIES, binding.hobby.getText().toString());
       data.put(Constants.KEY_IMAGE, encodedImage);
       data.put(Constants.KEY_IS_SIGNED_IN, true);
       database.collection(Constants.KEY_COLLECTION_USERS).add(data)
               .addOnSuccessListener(documentReference -> {
                                    Toast.makeText(getApplicationContext(), "Data Inserted", Toast.LENGTH_SHORT).show();
                                    preferencesManager.putBoolean(Constants.KEY_IS_SIGNED_IN, true);
                                    preferencesManager.putString(Constants.KEY_USER_ID, documentReference.getId());
                                    preferencesManager.putString(Constants.KEY_NAME, binding.name.getText().toString());
                                    preferencesManager.putString(Constants.KEY_ABOUT, binding.about.getText().toString());
                                    preferencesManager.putString(Constants.KEY_GENDER, gender);
                                    preferencesManager.putString(Constants.KEY_HOBBIES, binding.hobby.getText().toString());
                                    preferencesManager.putString(Constants.KEY_IMAGE, encodedImage);
                                    preferencesManager.putString(Constants.KEY_AGE, binding.age.getText().toString());
                                    user.id = documentReference.getId();
                                    user.about = binding.about.getText().toString();
                                    user.hobby = binding.hobby.getText().toString();
                                    user.name =binding.name.getText().toString();
                                    user.image=encodedImage;
                                    user.age = binding.age.getText().toString();
                                    uploadImage(url,"0", documentReference.getId());
                                })
               .addOnFailureListener(exception -> {
                                    Toast.makeText(getApplicationContext(), exception.getMessage(), Toast.LENGTH_SHORT).show();
                                    });
               }

        private String encodeImage(Bitmap bitmap){
            int previewWidth = 150;
            int previewHeight = bitmap.getHeight() * previewWidth / bitmap.getWidth();
            Bitmap previewBitmap = Bitmap.createScaledBitmap(bitmap, previewWidth, previewHeight, false);
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            previewBitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
            byte[] bytes = byteArrayOutputStream.toByteArray();
            return Base64.encodeToString(bytes, Base64.DEFAULT);
        }

        private Boolean isValidFilling () {
            if (url==null) {
                makeToast("Пожалуйста, выберите фото");
                return false;
            }
            if (!binding.name.getText().toString().matches("\\w+")) {
                makeToast("Пожалуйста, используйте только буквы для записи имени");
                return false;
            }
            if (parseInt(binding.age.getText().toString()) < 18) {
                makeToast("Приложение доступно только для лиц старше 18 лет");
                return false;
            }
            if (binding.about.getText().toString().isEmpty() || binding.hobby.getText().toString().isEmpty()) {
                makeToast("Пожалуйста, заполните все поля");
                return false;
            }
            if (!binding.name.getText().toString().matches("\\w+")) {
                makeToast("Пожалуйста, используйте только буквы для имени");
                return false;
            }
            if (gender==null) {
                makeToast("Пожалуйста, отметьте свой пол.");
                return false;
            }
            return true;
        }

        private void getGender() {
            binding.gender.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup group, int checkedId) {
                    if (checkedId == R.id.M) {
                        gender = "M";
                    } else if (checkedId == R.id.F) {
                        gender = "F";
                    }
                }
            });
        }

    private final ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
            if (result.getResultCode() == RESULT_OK) {
                if (result.getData() != null) {
                    url = result.getData().getData();
                    Glide.with(getApplicationContext()).load(url).into(binding.profilePicture);
                    try {
                        InputStream inputStream = getContentResolver().openInputStream(url);
                        BitmapFactory.Options options = new BitmapFactory.Options();
                        options.inScaled = false;
                        options.inDither = false;
                        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                        Bitmap bitmap = BitmapFactory.decodeStream(inputStream, null, options);
                        binding.profilePicture.setImageBitmap(bitmap);
                        binding.addImageText.setVisibility(View.GONE);
                        encodedImage = encodeImage(bitmap);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                makeToast("Please select an image");
            }
        }
    });

    private void uploadImage(Uri file, String name, String id) {
        StorageReference ref = storageReference.child("images/" + id + "/" + name);
        ref.putFile(file).addOnSuccessListener(taskSnapshot -> {
            makeToast("uploaded!");
            Intent intent = new Intent(getApplicationContext(), ProfileActivity.class);
            intent.putExtra(Constants.KEY_USER, user);
            finish();
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }).addOnFailureListener(e -> makeToast(e.getMessage()));
    }

    public void makeToast(String message){
            Toast.makeText(getApplicationContext(),message, Toast.LENGTH_SHORT).show();
    }
}



