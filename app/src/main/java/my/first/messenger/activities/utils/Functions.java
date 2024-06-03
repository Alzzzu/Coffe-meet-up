package my.first.messenger.activities.utils;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class Functions {

    // Firebase Actions
    public static void deleteActivation(FirebaseFirestore database, PreferencesManager preferencesManager)  {
        preferencesManager.putBoolean(Constants.KEY_IS_ACTIVATED, false);

        database.collection(Constants.KEY_COLLECTION_COFFEE_SHOPS)
                .document(preferencesManager.getString(Constants.KEY_COFFEESHOP_ID))
                .collection(Constants.KEY_COLLECTION_USERS)
                .document(preferencesManager.getString(Constants.KEY_USER_ID)).delete();

        database.collection(Constants.KEY_COLLECTION_COFFEE_SHOPS)
                .document(preferencesManager.getString(Constants.KEY_COFFEESHOP_ID))
                .collection(Constants.KEY_COLLECTION_USERS)
                .get()
                .addOnCompleteListener(task ->{
                    if (task.getResult()!=null && task.isSuccessful()) {
                        boolean is_enough_users = false;
                        for (QueryDocumentSnapshot queryDocumentSnapshot: task.getResult()){
                            is_enough_users = true;
                            break;
                        }
                        if (!is_enough_users){
                            database.collection(Constants.KEY_COLLECTION_COFFEE_SHOPS).document(preferencesManager.getString(Constants.KEY_COFFEESHOP_ID))
                                    .update("activated", false);
                        }
                    }
        });
        database.collection(Constants.KEY_COLLECTION_MEET_UP_OFFERS).
                whereEqualTo(Constants.KEY_VISITED_ID, preferencesManager.getString(Constants.KEY_USER_ID))
                .get()
                .addOnCompleteListener(task ->{
                    if (task.getResult()!=null && task.isSuccessful()) {
                        for (QueryDocumentSnapshot queryDocumentSnapshot: task.getResult()){
                        database.collection(Constants.KEY_COLLECTION_MEET_UP_OFFERS)
                            .document(queryDocumentSnapshot.getId()).delete();
                        }
                    }
        });
        database.collection(Constants.KEY_COLLECTION_CHAT)
                .whereEqualTo(Constants.KEY_SENDER_ID, preferencesManager.getString(Constants.KEY_USER_ID))
                .get()
                .addOnCompleteListener(task->{
                    if (task.isSuccessful()&&task.getResult()!=null){
                        for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()){
                            if (queryDocumentSnapshot.getString("type").equals("location")){
                                database.collection(Constants.KEY_COLLECTION_CHAT)
                                    .document(queryDocumentSnapshot.getId())
                                    .delete();
                            }
                        }
                    }
                });
    }
    public static void deleteVisits(FirebaseFirestore database, PreferencesManager preferencesManager){
        preferencesManager.putBoolean(Constants.KEY_IS_VISITED, false);
        if (preferencesManager.getString(Constants.KEY_VISITED_ID).isEmpty()){
            database.collection(Constants.KEY_COLLECTION_VISITS)
                    .document(preferencesManager.getString(Constants.KEY_USER_ID))
                    .delete();
        }
        else {
            database.collection(Constants.KEY_COLLECTION_VISITS)
                    .document(preferencesManager.getString(Constants.KEY_VISITED_ID))
                    .delete();
        }
    }

    //Location
    public static double distance(double lat1, double lon1, double lat2, double lon2) {
        if ((lat1 == lat2) && (lon1 == lon2)) {
            return 0;
        } else {
            double theta = lon1 - lon2;
            double dist = Math.sin(Math.toRadians(lat1)) * Math.sin(Math.toRadians(lat2)) + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.cos(Math.toRadians(theta));
            dist = Math.acos(dist);
            dist = Math.toDegrees(dist);
            dist = dist * 60 * 1.1515;
            dist = dist * 1.609344;
            return (dist);
        }
    }
}
