package my.first.messenger.activities.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.HashMap;

import my.first.messenger.R;
import my.first.messenger.activities.main_activities.LogIn;
import my.first.messenger.activities.main_activities.MapActivity;
import my.first.messenger.activities.utils.Constants;
import my.first.messenger.activities.utils.PreferencesManager;

public class AlertFragment extends DialogFragment {
    private String type;
    private String ID;
    private PreferencesManager preferencesManager;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        type = getArguments().getString("type");
        ID = getArguments().getString("id");
        preferencesManager = new PreferencesManager(getActivity());
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        if (type.equals("CANCEL")) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle("Вы уверены, что хотите отменить активацию?")
                    .setIcon(R.drawable.logo)
                    .setPositiveButton("Да", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {

                            preferencesManager.putBoolean(Constants.KEY_IS_GOING, false);
                            preferencesManager.putString(Constants.KEY_VISITED_ID,"");
                            preferencesManager.putString(Constants.KEY_VISITOR_ID,"");

                            FirebaseFirestore database = FirebaseFirestore.getInstance();

                            database.collection(Constants.KEY_COLLECTION_COFFEE_SHOPS).document(ID)
                                    .update("activated", false);

                            database.collection(Constants.KEY_COLLECTION_COFFEE_SHOPS).document(ID)
                                    .collection(Constants.KEY_COLLECTION_USERS)
                                    .document(preferencesManager.getString(Constants.KEY_USER_ID)).delete();
                            database.collection(Constants.KEY_COLLECTION_VISITS)
                                    .whereEqualTo(Constants.KEY_VISITOR_ID, preferencesManager.getString(Constants.KEY_USER_ID))
                                    .get()
                                    .addOnCompleteListener(task->{
                                        for(QueryDocumentSnapshot queryDocumentSnapshot:task.getResult()){
                                            database.collection(Constants.KEY_COLLECTION_VISITS).document(queryDocumentSnapshot.getId()).delete();
                                        }
                                    });

                            Intent i = new Intent(getActivity(), MapActivity.class);
                            startActivity(i);
                        }
                    })
                    .setNegativeButton("Нет", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();


                        }
                    });
            return builder.create();
        }
        else if (type.equals("EXIT")) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle("Вы уверены, что хотите выйти?")
                    .setIcon(R.drawable.logo)
                    .setPositiveButton("Да", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {

                            FirebaseFirestore database = FirebaseFirestore.getInstance();
                            database.collection(Constants.KEY_COLLECTION_MEET_UP_OFFERS)
                                    .whereEqualTo(Constants.KEY_VISITED_ID, preferencesManager.getString(Constants.KEY_USER_ID))
                                    .get()
                                    .addOnCompleteListener(task -> {
                                        if (task.isSuccessful() && task.getResult() != null) {

                                            for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                                                FirebaseFirestore.getInstance().collection(Constants.KEY_COLLECTION_MEET_UP_OFFERS).document(queryDocumentSnapshot.getId()).delete();
                                            }
                                        }
                                    });

                            database.collection(Constants.KEY_COLLECTION_MEET_UP_OFFERS)
                                    .whereEqualTo(Constants.KEY_VISITOR_ID, preferencesManager.getString(Constants.KEY_USER_ID))
                                    .get()
                                    .addOnCompleteListener(task -> {
                                        if (task.isSuccessful() && task.getResult() != null) {

                                            for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                                                FirebaseFirestore.getInstance().collection(Constants.KEY_COLLECTION_MEET_UP_OFFERS).document(queryDocumentSnapshot.getId()).delete();
                                            }
                                        }
                                    });
                            DocumentReference documentReference =
                                    database.collection(Constants.KEY_COLLECTION_USERS).document( preferencesManager.getString(Constants.KEY_USER_ID)
                                    );
                            HashMap<String, Object> updates =  new HashMap<>();
                            updates.put(Constants.KEY_FCM_TOKEN, FieldValue.delete());
                            documentReference.update(updates)
                                    .addOnSuccessListener(unused -> {
                                        preferencesManager.clear();
                                        Intent intent = new Intent(getActivity(), LogIn.class);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(intent);
                                    });
                        }
                    })
                    .setNegativeButton("Нет", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();


                        }
                    });
            return builder.create();
        }
        else {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle("Вы уверены, что хотите отменить активацию?")
                    .setIcon(R.drawable.coffee_colour)
                    .setPositiveButton("Да", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    })
                    .setNegativeButton("Нет", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent(getActivity(), MapActivity.class);
                            startActivity(intent);
                            dialog.cancel();
                        }
                    });
            return builder.create();

        }
    }
}
