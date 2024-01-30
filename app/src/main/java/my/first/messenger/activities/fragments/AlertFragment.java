package my.first.messenger.activities.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import my.first.messenger.R;
import my.first.messenger.activities.main_activities.MapActivity;

public class AlertFragment extends DialogFragment {
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Прошло 15 минут")
                .setMessage("Вы собираетесь продолжить путь?")
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
