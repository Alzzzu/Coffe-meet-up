package my.first.messenger.activities.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.Date;
import java.util.HashMap;

import my.first.messenger.R;
import my.first.messenger.activities.main_activities.MapActivity;
import my.first.messenger.activities.utils.Constants;
import my.first.messenger.activities.utils.PreferencesManager;

public class AlertFragment extends DialogFragment {
    private String type;
    private String ID;
    private PreferencesManager preferencesManager;
    private FirebaseFirestore database;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        type = getArguments().getString("type");
        ID = getArguments().getString("id");
        preferencesManager = new PreferencesManager(getActivity());
        database = FirebaseFirestore.getInstance();
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
                            HashMap<String, Object> message = new HashMap<>();
                            message.put("type", "info");
                            message.put(Constants.KEY_SENDER_ID, preferencesManager.getString(Constants.KEY_USER_ID));
                            message.put(Constants.KEY_RECEIVER_ID, preferencesManager.getString(Constants.KEY_VISITED_ID));
                            message.put(Constants.KEY_MESSAGE, "Пользователь отменил встречу");
                            message.put(Constants.KEY_TIMESTAMP, new Date());
                            database.collection(Constants.KEY_COLLECTION_CHAT).add(message);
                            preferencesManager.putString(Constants.KEY_VISITED_ID,"");
                            preferencesManager.putString(Constants.KEY_VISITOR_ID,"");
                            Intent i = new Intent(getActivity(), MapActivity.class);
                            startActivity(i);
                        }
                    })
                    .setNegativeButton("Нет", (dialog, which) -> dialog.cancel());
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
