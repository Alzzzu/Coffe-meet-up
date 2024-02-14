package my.first.messenger.activities.main_activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.icu.text.Edits;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.JsonArray;
import com.google.gson.JsonIOException;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import my.first.messenger.R;
import my.first.messenger.activities.adapters.ChatAdapter;
import my.first.messenger.activities.adapters.ImageAdapter;
import my.first.messenger.activities.listeners.ChatMessageListener;
import my.first.messenger.activities.models.ChatMessage;
import my.first.messenger.activities.models.User;
import my.first.messenger.activities.network.ApiClient;
import my.first.messenger.activities.network.ApiService;
import my.first.messenger.activities.utils.Constants;
import my.first.messenger.activities.utils.PreferencesManager;
import my.first.messenger.databinding.ActivityChatBinding;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatActivity extends BaseActivity implements ChatMessageListener {
    private ActivityChatBinding binding;
    private User receiverUser;
    private List<ChatMessage> chatMessages;
    private ChatAdapter chatAdapter;
    private PreferencesManager preferencesManager;
    private FirebaseFirestore database;
    private String conversationId = null;
    private Boolean isReceiverAvailable = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setListeners();
        loadReceiverDetail();
        init();
        listenMessages();
    }
    private void init(){
        preferencesManager = new PreferencesManager(getApplicationContext());
        chatMessages = new ArrayList<>();
        chatAdapter = new ChatAdapter(
                chatMessages,
                preferencesManager.getString(Constants.KEY_USER_ID), this
        );
        binding.chatRecycleView.setAdapter(chatAdapter);
        database = FirebaseFirestore.getInstance();
    }


    private void sendNotification(String messageBody){
        ApiClient.getClient().create(ApiService.class).sendMessage(
                Constants.getRemoteMsgHeaders(),
                messageBody
        ).enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                if (response.isSuccessful()){
                    try{
                        if (response.body() !=null){
                            JSONObject responseJson  = new JSONObject(response.body());
                            JSONArray result = responseJson.getJSONArray("results");
                            if(responseJson.getInt("failure")==1){
                                JSONObject error =  (JSONObject) result.get(0);
                                makeToast(error.getString("error"));
                                return;
                            }
                        }
                    }
                    catch(JSONException e){
                        makeToast(e.getMessage());
                    }
                    makeToast("notification sent");
                }
                    else{
                        makeToast("error "+response.code());

                }

            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                makeToast(t.getMessage());

            }
        });

    }

    private void sendMessage(){
        HashMap<String, Object> message = new HashMap<>();
        message.put(Constants.KEY_SENDER_ID,preferencesManager.getString(Constants.KEY_USER_ID));
        message.put(Constants.KEY_RECEIVER_ID, receiverUser.id);
        message.put(Constants.KEY_MESSAGE, binding.inputMessage.getText().toString());
        message.put(Constants.KEY_TIMESTAMP, new Date());
        database.collection(Constants.KEY_COLLECTION_CHAT).add(message);
        if (conversationId != null){
            updateConversation(binding.inputMessage.getText().toString());
        }
        else {
            HashMap<String, Object> conversation = new HashMap<>();
            conversation.put(Constants.KEY_SENDER_ID, preferencesManager.getString(Constants.KEY_USER_ID));
            conversation.put(Constants.KEY_SENDER_NAME, preferencesManager.getString(Constants.KEY_NAME));
            conversation.put(Constants.KEY_SENDER_IMAGE, preferencesManager.getString(Constants.KEY_IMAGE));
            conversation.put(Constants.KEY_RECEIVER_ID, receiverUser.id);
            conversation.put(Constants.KEY_RECEIVER_NAME, receiverUser.name);
            conversation.put(Constants.KEY_RECEIVER_IMAGE, receiverUser.image);
            conversation.put(Constants.KEY_LAST_MESSAGE,binding.inputMessage.getText().toString());
            conversation.put(Constants.KEY_TIMESTAMP, new Date());
            addConversation(conversation);
        }
        if(!isReceiverAvailable){
            try{
            JSONArray tokens = new JSONArray();
            tokens.put(receiverUser.token);

            JSONObject data = new JSONObject();
            data.put(Constants.KEY_USER_ID,preferencesManager.getString(Constants.KEY_USER_ID));
            data.put(Constants.KEY_NAME, preferencesManager.getString(Constants.KEY_NAME));
            data.put(Constants.KEY_FCM_TOKEN, preferencesManager.getString(Constants.KEY_FCM_TOKEN));


            data.put(Constants.KEY_MESSAGE, binding.inputMessage.getText().toString());
            JSONObject body = new JSONObject();
            body.put(Constants.REMOTE_MSG_DATA, data);
            body.put(Constants.REMOTE_MSG_REGISTRATION_IDS, tokens);
            sendNotification(body.toString());
            }
            catch (JSONException e) {
                makeToast(e.getMessage());
            }
        }

        binding.inputMessage.setText(null);
    }


    private void listenMessages(){
            database.collection(Constants.KEY_COLLECTION_CHAT)
        .whereEqualTo(Constants.KEY_SENDER_ID, preferencesManager.getString(Constants.KEY_USER_ID))
        .whereEqualTo(Constants.KEY_RECEIVER_ID, receiverUser.id)
        .addSnapshotListener(eventListener);
        database.collection(Constants.KEY_COLLECTION_CHAT)
                .whereEqualTo(Constants.KEY_SENDER_ID, receiverUser.id)
                .whereEqualTo(Constants.KEY_RECEIVER_ID, preferencesManager.getString(Constants.KEY_USER_ID))
                .addSnapshotListener(eventListener);
    }


    private final EventListener<QuerySnapshot> eventListener = (value, error) -> {
        if (error != null) {
            return;
        }
        if (value != null) {
            int count = chatMessages.size();
            for (DocumentChange documentChange : value.getDocumentChanges()) {
                if (documentChange.getType() == DocumentChange.Type.ADDED) {
                    ChatMessage chatMessage = new ChatMessage();
                    chatMessage.senderId = documentChange.getDocument().getString(Constants.KEY_SENDER_ID);
                    chatMessage.receiverId = documentChange.getDocument().getString(Constants.KEY_RECEIVER_ID);
                    chatMessage.message = documentChange.getDocument().getString(Constants.KEY_MESSAGE);
                    chatMessage.dateTime = getReadableDateTime(documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP));
                    chatMessage.dateObject = documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP);
                    chatMessage.id =documentChange.getDocument().getId();
                    chatMessage.clicked = false;
                    chatMessages.add(chatMessage);
                }
                if (documentChange.getType() == DocumentChange.Type.REMOVED){
                    int position=-1;
                    for( ChatMessage message: chatMessages){
                        if (message.id.equals(documentChange.getDocument().getId())){
                            position = chatMessages.indexOf(message);
                            break;
                        }
                    }
                    if(position>-1){
                    chatMessages.remove(position);
                    chatAdapter.notifyItemRemoved(position);
                    }
                }
                if (documentChange.getType() == DocumentChange.Type.MODIFIED){
                    ChatMessage chatMessage = new ChatMessage();
                    chatMessage.senderId = documentChange.getDocument().getString(Constants.KEY_SENDER_ID);
                    chatMessage.receiverId = documentChange.getDocument().getString(Constants.KEY_RECEIVER_ID);
                    chatMessage.message = documentChange.getDocument().getString(Constants.KEY_MESSAGE);
                    chatMessage.dateTime = getReadableDateTime(documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP));
                    chatMessage.dateObject = documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP);
                    chatMessage.id =documentChange.getDocument().getId();
                    chatMessage.clicked = false;
                    int position =-1;
                    for( ChatMessage message: chatMessages){
                        if (message.id.equals(documentChange.getDocument().getId())){
                            position = chatMessages.indexOf(message);
                            break;
                        }
                    }
                    if(position>-1){
                        chatMessages.set(position, chatMessage);
                        chatAdapter.notifyItemChanged(position);
                    }

                }
            }

            Collections.sort(chatMessages, (obj1, obj2)-> obj1.dateObject.compareTo(obj2.dateObject));
            if (count==0){
                chatAdapter.notifyDataSetChanged();
            }
            else{
                chatAdapter.notifyItemRangeInserted(chatMessages.size(), chatMessages.size());
                binding.chatRecycleView.smoothScrollToPosition(chatMessages.size()-1);
            }
            binding.chatRecycleView.setVisibility(View.VISIBLE);
        }
        if (conversationId == null){
            checkForConversation();
        }
    };


    private void loadReceiverDetail(){
        receiverUser = (User) getIntent().getSerializableExtra(Constants.KEY_USER);
        binding.textName.setText(receiverUser.name);
    }


    private void setListeners() {
        binding.imageBack.setOnClickListener(v -> startActivity(new Intent(getApplicationContext(), RecentConversationsActivity.class)));
        binding.layoutSend.setOnClickListener(v -> sendMessage());
    }


    private void listenAvailabilityOfReceiver() {
        database.collection(Constants.KEY_COLLECTION_USERS).document(
                receiverUser.id
        ).addSnapshotListener(ChatActivity.this, (value, error) -> {
            if(error != null) {
                return;
            }
            if(value != null) {
                if(value.getLong(Constants.KEY_AVAILABILITY) != null) {
                    int availability = Objects.requireNonNull(
                            value.getLong(Constants.KEY_AVAILABILITY)
                    ).intValue();
                    isReceiverAvailable = availability == 1;
                }
                receiverUser.token = value.getString(Constants.KEY_FCM_TOKEN);

            }
            if (isReceiverAvailable){
                binding.userStatus.setImageResource(R.drawable.coffee_colour);;
            } else {
                binding.userStatus.setImageResource(R.drawable.coffee_colorless);;
            }
        });
    }


    private String getReadableDateTime(Date date){
        return new SimpleDateFormat("MMMM dd, yyyy - hh:mm a", Locale.getDefault()).format(date);
    }


    private void addConversation(HashMap<String,Object> conversation){
        database.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
                .add(conversation)
                .addOnSuccessListener(documentReference -> conversationId=documentReference.getId());
    }


    private void updateConversation(String message){
        DocumentReference documentReference = database.collection(Constants.KEY_COLLECTION_CONVERSATIONS).document(conversationId);
        documentReference.update(
                Constants.KEY_LAST_MESSAGE, message,
                Constants.KEY_TIMESTAMP, new Date()
        );
    }


    private void updateMessage(ChatMessage message){
        DocumentReference documentReference = database.collection(Constants.KEY_COLLECTION_CHAT).document(message.id);
        documentReference.update(
                Constants.KEY_MESSAGE, message.message
        );
    }


    //
    private void checkForConversation(){
        //if(chatMessages.size()!=0){
            checkForConversationRemotely(
                    preferencesManager.getString(Constants.KEY_USER_ID),//key sender id
                    receiverUser.id
            );
            checkForConversationRemotely(
                    receiverUser.id,
                    preferencesManager.getString(Constants.KEY_USER_ID)
            );
        }


    //
    private void checkForConversationRemotely(String senderId, String receiverId){
        database.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
                .whereEqualTo(Constants.KEY_SENDER_ID,senderId)
                .whereEqualTo(Constants.KEY_RECEIVER_ID, receiverId)
                .get()
                .addOnCompleteListener(conversationCompleteListener);
    }


    //
    private final OnCompleteListener<QuerySnapshot> conversationCompleteListener = task -> {
        if(task.isSuccessful() && task.getResult() != null && task.getResult().getDocuments().size() > 0){
            DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
            conversationId = documentSnapshot.getId();
        }
    };


    // Actions with message


    // deleting message
    private void deleteMessage(ChatMessage message, int position){
        database.collection(Constants.KEY_COLLECTION_CHAT).document(message.id).delete();
        chatAdapter.removeAt(position);
        if (position>0) {
            updateConversation(chatMessages.get(position-1).message);
        }
        else{
            updateConversation(null);
        }
        binding.actionsWithMessage.setVisibility(View.GONE);
    }


    // editing message
    private void editMessage(ChatMessage message, int position){
        binding.inputMessage.setText(message.message);
        binding.layoutSend.setVisibility(View.INVISIBLE);
        binding.layoutEdit.setVisibility(View.VISIBLE);
        binding.layoutEdit.setOnClickListener(v -> {
            message.message= binding.inputMessage.getText().toString();
            chatAdapter.editAt(position, message);
            binding.layoutSend.setVisibility(View.VISIBLE);
            binding.layoutEdit.setVisibility(View.GONE);
            binding.actionsWithMessage.setVisibility(View.GONE);
            binding.inputMessage.setText(null);
            updateMessage(message);
            updateConversation(message.message);
        });
    }



    @Override
    public void onMessageClick(ChatMessage message, int position){
        if (!message.clicked) {
            binding.actionsWithMessage.setVisibility(View.VISIBLE);
            binding.deleteMessage.setOnClickListener(v -> deleteMessage(message, position));
            binding.editMessage.setOnClickListener(v -> editMessage(message, position));
        }
        else{

            binding.actionsWithMessage.setVisibility(View.GONE);
        }
    }


    private void makeToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }


    @Override
    protected void onResume(){
        super.onResume();
        listenAvailabilityOfReceiver();
    }
}