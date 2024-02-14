package my.first.messenger.activities.fragments;

import static android.app.Activity.RESULT_OK;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.FirebaseApp;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.UUID;
import java.util.function.Consumer;

import my.first.messenger.R;
import my.first.messenger.activities.adapters.ImageAdapter;
import my.first.messenger.activities.adapters.ImageDisplayAdapter;
import my.first.messenger.activities.listeners.ImageGalleryListener;
import my.first.messenger.activities.main_activities.ChatActivity;
import my.first.messenger.activities.main_activities.EditProfileActivity;
import my.first.messenger.activities.main_activities.ProfileActivity;
import my.first.messenger.activities.main_activities.RecentConversationsActivity;
import my.first.messenger.activities.main_activities.UserLocationActivity;
import my.first.messenger.activities.main_activities.UsersActivity;
import my.first.messenger.activities.models.Image;
import my.first.messenger.activities.models.User;
import my.first.messenger.activities.utils.Constants;
import my.first.messenger.activities.utils.PreferencesManager;
import my.first.messenger.databinding.ActivityProfileBinding;
import my.first.messenger.databinding.FragmentMapBinding;
import my.first.messenger.databinding.FragmentProfileBinding;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ProfileFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ProfileFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters

    private User user;
    private FragmentProfileBinding binding;
    // private TextView name, age, hobby, about;
    private PreferencesManager preferencesManager;
    StorageReference storageReference;
    private ArrayList<Image> galleryImages;
    private ImageDisplayAdapter imageAdapter;

    private Boolean clicked;
    private Image image;

    public ProfileFragment(){
        // Required empty public constructor
    }
    // TODO: Rename and change types and number of parameters
    public static ProfileFragment newInstance(User user) {
        ProfileFragment fragment = new ProfileFragment();
        Bundle args = new Bundle();
        args.putSerializable(Constants.KEY_USER, user);
        //  args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            user = (User) getArguments().getSerializable(Constants.KEY_USER);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        makeToast(user.name);
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
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                makeToast(exception.getMessage());
            }
        });
    }
    public void makeToast(String message){
        Toast.makeText(getActivity(),message, Toast.LENGTH_SHORT).show();
    }


    private void setListeners() {




        //Redirecting to chatActivity
        binding.buttonToText.setOnClickListener(v ->{
            Intent intent = new Intent(getActivity(), ChatActivity.class);
            intent.putExtra(Constants.KEY_USER, user);
            startActivity(intent);
           // finish();}
            }
        );


        //back
        binding.imageBack.setOnClickListener(v-> {
            getActivity().getSupportFragmentManager().beginTransaction().remove(this).commit();}

        );


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
                Toast.makeText(getActivity(), "Failed to retrieve images", Toast.LENGTH_SHORT).show();
            }
        });
    }
}