package my.first.messenger.activities.fragments;

import static android.app.Activity.RESULT_OK;
import static java.lang.Long.parseLong;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;
import java.util.function.Consumer;

import my.first.messenger.R;
import my.first.messenger.activities.adapters.ImageDisplayAdapter;
import my.first.messenger.activities.listeners.OnSwipeTouchListener;
import my.first.messenger.activities.main_activities.ChatActivity;
import my.first.messenger.activities.models.Image;
import my.first.messenger.activities.models.User;
import my.first.messenger.activities.utils.Constants;
import my.first.messenger.activities.utils.PreferencesManager;
import my.first.messenger.databinding.FragmentProfileBinding;



public class ProfileFragment extends Fragment {
    private User user;
    private FragmentProfileBinding binding;
    private PreferencesManager preferencesManager;
    StorageReference storageReference;
    private ArrayList<Image> galleryImages;
    private ImageDisplayAdapter imageAdapter;

    private Boolean clicked;
    private boolean type;
    private Image image;

    public ProfileFragment(){
    }
    public static ProfileFragment newInstance(User user, Boolean type) {
        ProfileFragment fragment = new ProfileFragment();
        Bundle args = new Bundle();
        args.putSerializable(Constants.KEY_USER, user);
        fragment.setArguments(args);
        return fragment;
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            user = (User) getArguments().getSerializable(Constants.KEY_USER);
            type = getArguments().getBoolean("visitor");
        }
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        init();
        loadUsersDetails();
        loadUserGallery();
        setListeners();

        return binding.getRoot();
    }
    private void init(){
        preferencesManager = new PreferencesManager(getActivity());
        clicked = true;
        FirebaseApp.initializeApp(getActivity());
        storageReference = FirebaseStorage.getInstance().getReference();
        galleryImages = new ArrayList<>();
        imageAdapter = new ImageDisplayAdapter(galleryImages, getActivity());
        binding.view.setOnTouchListener(new OnSwipeTouchListener(getActivity()) {
            public void onSwipeRight() {
                cancel();
            }
        });

    }
    private void cancel(){
        getActivity().getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(android.R.anim.slide_in_left,
                        android.R.anim.slide_out_right).remove(this).commit();
    }
    private void loadUsersDetails(){
        binding.userName.setText(user.name);
        binding.userAge.setText("Возраст: "+user.age);
        binding.userHobby.setText("Хобби: "+user.hobby);
        binding.userAbout.setText(user.about);
        storageReference.child("images/"+user.id+"/0").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Glide.with(getActivity()).load(uri).into(binding.profilePicture);
                binding.progressProfileImage.setVisibility(View.GONE);
            }
        }).addOnFailureListener(exception -> makeToast(exception.getMessage()));
    }
    public void makeToast(String message){
        Toast.makeText(getActivity(),message, Toast.LENGTH_SHORT).show();
    }
    private void setListeners() {
        binding.buttonToText.setOnClickListener(v ->{
            if (!preferencesManager.getBoolean(Constants.KEY_IS_ACTIVATED)){
                FirebaseFirestore database = FirebaseFirestore.getInstance();
                database.collection(Constants.KEY_COLLECTION_MEET_UP_OFFERS).whereEqualTo(Constants.KEY_VISITOR_ID, preferencesManager.getString(Constants.KEY_USER_ID))
                    .get()
                    .addOnCompleteListener(task->{
                        int count=0;
                        for (QueryDocumentSnapshot queryDocumentSnapshot: task.getResult()){
                            count+=1;
                        }
                        if(count==0){
                            HashMap<String, Object> updt = new HashMap<>();
                            updt.put(Constants.KEY_VISITED_IMAGE, user.image);
                            updt.put(Constants.KEY_VISITOR_IMAGE, preferencesManager.getString(Constants.KEY_IMAGE));
                            updt.put(Constants.KEY_VISITED_ID, user.id);
                            updt.put(Constants.KEY_AGE, parseLong(preferencesManager.getString(Constants.KEY_AGE)));
                            updt.put(Constants.KEY_GENDER, preferencesManager.getString(Constants.KEY_GENDER));
                            updt.put(Constants.KEY_SEARCH_PURPOSE, preferencesManager.getString(Constants.KEY_SEARCH_PURPOSE));
                            updt.put(Constants.KEY_VISITOR_ID, preferencesManager.getString(Constants.KEY_USER_ID));
                            updt.put(Constants.KEY_VISITED_NAME, user.name);
                            updt.put(Constants.KEY_VISITOR_NAME, preferencesManager.getString(Constants.KEY_NAME));
                            database.collection(Constants.KEY_COLLECTION_MEET_UP_OFFERS).add(updt);
                            makeToast("запрос отправлен");
                        }
                    }
                );
                }
            else{
                preferencesManager.putString(Constants.KEY_VISITOR_ID, user.id);
                Intent intent = new Intent(getActivity(), ChatActivity.class);
                intent.putExtra(Constants.KEY_USER, user);
                startActivity(intent);
            }
        });
        binding.imageBack.setOnClickListener(v-> {
            getActivity().getSupportFragmentManager().beginTransaction()
                    .setCustomAnimations(android.R.anim.slide_in_left,
                    android.R.anim.slide_out_right).remove(this).commit();}
            );
        binding.showInfoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (clicked) {
                    Animation animation = AnimationUtils.loadAnimation(getActivity(), android.R.anim.slide_in_left);
                    binding.userInformation.setVisibility(View.VISIBLE);
                    binding.userInformation.startAnimation(animation);
                    binding.showInfoButton.setImageResource(R.drawable.wrap);
                    clicked = false;
                } else {
                    binding.userInformation.setVisibility(View.GONE);
                    binding.showInfoButton.setImageResource(R.drawable.unwrap);
                    clicked = true;
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
                    galleryImages.add(image);
                    imageAdapter.notifyDataSetChanged();
                    uploadImage(result.getData().getData(), image.name);//imageAdapter.getItemCount()+"");
                }
            } else {
                Toast.makeText(getActivity(), "Please select an image", Toast.LENGTH_SHORT).show();
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
        }).addOnFailureListener(e -> makeToast(e.getMessage()));
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
        }).addOnFailureListener(e -> makeToast("Неудалось извлечь файл"));
    }
}