package my.first.messenger.activities.main_activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.messaging.FirebaseMessaging;

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
    private Boolean isAdded = false;
    private final String TAG ="ChatActivityTAG";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseMessaging.getInstance().setAutoInitEnabled(true);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        init();
        setListeners();
        loadReceiverDetail();
        try{
        listenMessages();
        }
        catch (Exception e){
            Log.e(TAG, e.getMessage());
        }
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
                    Log.d(TAG,"notification sent");
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

    private void sendLocation(){
        HashMap<String, Object> message = new HashMap<>();
        message.put("type","location");
        message.put(Constants.KEY_MESSAGE, "LOCATION ☕");
        message.put(Constants.KEY_SENDER_ID,preferencesManager.getString(Constants.KEY_USER_ID));
        message.put(Constants.KEY_RECEIVER_ID, receiverUser.id);
        message.put(Constants.KEY_COFFEESHOP_ID, preferencesManager.getString(Constants.KEY_COFFEESHOP_ID));//binding.inputMessage.getText().toString());
        message.put(Constants.KEY_TIMESTAMP, new Date());
        database.collection(Constants.KEY_COLLECTION_CHAT).add(message);
        binding.layoutSendLocation.setVisibility(View.GONE);
        if (conversationId != null) {
            updateConversation(Objects.equals(binding.inputMessage.getText().toString(), "")?"LOCATION ☕":binding.inputMessage.getText().toString());
        } else {
            HashMap<String, Object> conversation = new HashMap<>();
            conversation.put(Constants.KEY_SENDER_ID, preferencesManager.getString(Constants.KEY_USER_ID));
            conversation.put(Constants.KEY_SENDER_NAME, preferencesManager.getString(Constants.KEY_NAME));
            conversation.put(Constants.KEY_SENDER_IMAGE, preferencesManager.getString(Constants.KEY_IMAGE));
            conversation.put(Constants.KEY_RECEIVER_ID, receiverUser.id);
            conversation.put(Constants.KEY_RECEIVER_NAME, receiverUser.name);
            conversation.put(Constants.KEY_RECEIVER_IMAGE, receiverUser.image);
            conversation.put(Constants.KEY_LAST_MESSAGE, binding.inputMessage.getText().toString());
            conversation.put(Constants.KEY_TIMESTAMP, new Date());
            addConversation(conversation);
        }
    }

    private void sendMessage(){
        if(!binding.inputMessage.getText().toString().isEmpty()&&!binding.inputMessage.getText().toString().matches("\\s*")) {
            HashMap<String, Object> message = new HashMap<>();
            message.put("type", "message");
            message.put(Constants.KEY_SENDER_ID, preferencesManager.getString(Constants.KEY_USER_ID));
            message.put(Constants.KEY_RECEIVER_ID, receiverUser.id);
            message.put(Constants.KEY_MESSAGE, binding.inputMessage.getText().toString());
            message.put(Constants.KEY_TIMESTAMP, new Date());
            database.collection(Constants.KEY_COLLECTION_CHAT).add(message);
            if (conversationId != null) {
                updateConversation(binding.inputMessage.getText().toString());
            } else {
                HashMap<String, Object> conversation = new HashMap<>();
                conversation.put(Constants.KEY_SENDER_ID, preferencesManager.getString(Constants.KEY_USER_ID));
                conversation.put(Constants.KEY_SENDER_NAME, preferencesManager.getString(Constants.KEY_NAME));
                conversation.put(Constants.KEY_SENDER_IMAGE, preferencesManager.getString(Constants.KEY_IMAGE));
                conversation.put(Constants.KEY_RECEIVER_ID, receiverUser.id);
                conversation.put(Constants.KEY_RECEIVER_NAME, receiverUser.name);
                conversation.put(Constants.KEY_RECEIVER_IMAGE, receiverUser.image);
                conversation.put(Constants.KEY_LAST_MESSAGE, binding.inputMessage.getText().toString());
                conversation.put(Constants.KEY_TIMESTAMP, new Date());
                addConversation(conversation);
            }
            if (!isReceiverAvailable) {
                try {
                    JSONArray tokens = new JSONArray();
                    tokens.put(receiverUser.token);
                    JSONObject data = new JSONObject();
                    data.put(Constants.KEY_USER_ID, preferencesManager.getString(Constants.KEY_USER_ID));
                    data.put(Constants.KEY_NAME, preferencesManager.getString(Constants.KEY_NAME));
                    data.put(Constants.KEY_FCM_TOKEN, preferencesManager.getString(Constants.KEY_FCM_TOKEN));
                    data.put(Constants.KEY_MESSAGE, binding.inputMessage.getText().toString());
                    JSONObject body = new JSONObject();
                    body.put(Constants.REMOTE_MSG_DATA, data);
                    body.put(Constants.REMOTE_MSG_REGISTRATION_IDS, tokens);
                    sendNotification(body.toString());
                } catch (JSONException e) {
                    makeToast(e.getMessage());
                }
            }
        }
        binding.inputMessage.setText(null);
    }

    private void listenMessages(){
        database.collection(Constants.KEY_COLLECTION_CHAT)
                .whereEqualTo(Constants.KEY_SENDER_ID, receiverUser.id)
                .whereEqualTo(Constants.KEY_RECEIVER_ID, preferencesManager.getString(Constants.KEY_USER_ID))
                .addSnapshotListener(eventListener);

        database.collection(Constants.KEY_COLLECTION_CHAT)
                .whereEqualTo(Constants.KEY_SENDER_ID, preferencesManager.getString(Constants.KEY_USER_ID))
                .whereEqualTo(Constants.KEY_RECEIVER_ID, receiverUser.id)
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
                    try{
                    if (!documentChange.getDocument().getString("type").equals("location")){
                        ChatMessage chatMessage = new ChatMessage();
                        chatMessage.senderId = documentChange.getDocument().getString(Constants.KEY_SENDER_ID);
                        chatMessage.receiverId = documentChange.getDocument().getString(Constants.KEY_RECEIVER_ID);
                        chatMessage.message = documentChange.getDocument().getString(Constants.KEY_MESSAGE);
                        chatMessage.dateTime = getReadableDateTime(documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP));
                        chatMessage.dateObject = documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP);
                        chatMessage.id =documentChange.getDocument().getId();
                        chatMessage.type =documentChange.getDocument().getString("type");
                        chatMessage.clicked = false;
                        chatMessages.add(chatMessage);
                  }
                    else{
                        ChatMessage chatMessage = new ChatMessage();
                        chatMessage.senderId = documentChange.getDocument().getString(Constants.KEY_SENDER_ID);
                        chatMessage.receiverId = documentChange.getDocument().getString(Constants.KEY_RECEIVER_ID);
                        chatMessage.coffeeshopId = documentChange.getDocument().getString(Constants.KEY_COFFEESHOP_ID);
                        chatMessage.dateTime = getReadableDateTime(documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP));
                        chatMessage.dateObject = documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP);
                        chatMessage.id =documentChange.getDocument().getId();
                        chatMessage.type ="location";
                        chatMessage.message = "LOCATION ☕";
                        chatMessages.add(chatMessage);
                    }
                    }
                    catch(Exception e){
                        Log.e(TAG, e.getMessage());
                    }
                    isAdded = true;
                }
                if (documentChange.getType() == DocumentChange.Type.REMOVED){
                    int position;
                    for( ChatMessage message: chatMessages){
                        if (message.id.equals(documentChange.getDocument().getId())){
                            position = chatMessages.indexOf(message);
                            chatMessages.remove(position);
                            chatAdapter.notifyItemRemoved(position);
                            break;
                        }
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
                    chatMessage.type =documentChange.getDocument().getString("type");
                    chatMessage.clicked = false;
                    int position =-1;
                    for( ChatMessage message: chatMessages){
                        if (message.id.equals(documentChange.getDocument().getId())){
                            position = chatMessages.indexOf(message);
                            chatMessages.set(position, chatMessage);
                            chatAdapter.notifyItemChanged(position);
                            break;
                        }
                    }
                }
            }
            Collections.sort(chatMessages, (obj1, obj2)-> obj1.dateObject.compareTo(obj2.dateObject));
            if (count==0){
                chatAdapter.notifyDataSetChanged();
            }
            else{
                chatAdapter.notifyItemRangeInserted(count, chatMessages.size());
                chatAdapter = new ChatAdapter(
                        chatMessages,
                        preferencesManager.getString(Constants.KEY_USER_ID), this
                );
                    binding.chatRecycleView.setAdapter(chatAdapter);
                    binding.chatRecycleView.smoothScrollToPosition(chatMessages.size());
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
        if(preferencesManager.getBoolean(Constants.KEY_IS_ACTIVATED)&&preferencesManager.getString(Constants.KEY_VISITOR_ID).equals(receiverUser.id)){
             binding.layoutSendLocation.setVisibility(View.VISIBLE);
        }
    }

    private void setListeners() {
        binding.imageBack.setOnClickListener(v -> startActivity(new Intent(getApplicationContext(), RecentConversationsActivity.class)));
        binding.layoutSend.setOnClickListener(v -> sendMessage());
        binding.layoutSendLocation.setOnClickListener(v->sendLocation());

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
                Glide.with(getApplicationContext()).load(R.drawable.logo).into(binding.userStatus);
            } else {
                Glide.with(getApplicationContext()).load(R.drawable.coffee_colorless).into(binding.userStatus);
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

    private void checkForConversation(){
        checkForConversationRemotely(
                    preferencesManager.getString(Constants.KEY_USER_ID),
                    receiverUser.id
            );
            checkForConversationRemotely(
                    receiverUser.id,
                    preferencesManager.getString(Constants.KEY_USER_ID)
            );
    }

    private void checkForConversationRemotely(String senderId, String receiverId){
        database.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
                .whereEqualTo(Constants.KEY_SENDER_ID,senderId)
                .whereEqualTo(Constants.KEY_RECEIVER_ID, receiverId)
                .get()
                .addOnCompleteListener(conversationCompleteListener);
    }
    private final OnCompleteListener<QuerySnapshot> conversationCompleteListener = task -> {
        if(task.isSuccessful() && task.getResult() != null && task.getResult().getDocuments().size() > 0){
            DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
            conversationId = documentSnapshot.getId();
        }
    };

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
    private void editMessage(ChatMessage message, int position){
        binding.inputMessage.setText(message.message);
        binding.layoutSend.setVisibility(View.INVISIBLE);
        binding.layoutEdit.setVisibility(View.VISIBLE);
        binding.layoutEdit.setOnClickListener(v -> {
            message.message= binding.inputMessage.getText().toString();
            message.clicked=false;
            message.type="message";
            chatMessages.set(position, message);
            chatAdapter = new ChatAdapter(
                    chatMessages,
                    preferencesManager.getString(Constants.KEY_USER_ID), this
            );
            binding.chatRecycleView.setAdapter(chatAdapter);
            binding.layoutSend.setVisibility(View.VISIBLE);
            binding.layoutEdit.setVisibility(View.GONE);
            binding.actionsWithMessage.setVisibility(View.GONE);
            binding.inputMessage.setText(null);
            updateMessage(message);
            if(position==chatMessages.size()-1){
            updateConversation(message.message);
            }
        });
        ChatAdapter.selectedItem = -1;

    }
    @Override
    public void onMessageClick(ChatMessage message, int position) {
        if (chatMessages.get(position).type != null) {
            if (message.type.equals("message")) {
                if (!message.clicked) {
                    binding.actionsWithMessage.setVisibility(View.VISIBLE);
                    binding.deleteMessage.setOnClickListener(v -> deleteMessage(message, position));
                    binding.editMessage.setOnClickListener(v -> editMessage(message, position));
                } else {
                    binding.actionsWithMessage.setVisibility(View.GONE);
                }
            } else {
                preferencesManager.putBoolean(Constants.KEY_IS_GOING, true);
                preferencesManager.putString(Constants.KEY_VISITED_ID, receiverUser.id);
                preferencesManager.putString(Constants.KEY_COFFEESHOP_ID, message.coffeeshopId);
                deleteMessage(message, position);
                addVisit();
                Intent intent = new Intent(getApplicationContext(), RouteActivity.class);
                startActivity(intent);
            }
        }
    }
    private void addVisit(){
        HashMap<String, Object> updt = new HashMap<>();
        updt.put(Constants.KEY_VISITOR_NAME, preferencesManager.getString(Constants.KEY_NAME));
        updt.put(Constants.KEY_VISITOR_ID, preferencesManager.getString(Constants.KEY_USER_ID));
        updt.put(Constants.KEY_VISITED_ID, preferencesManager.getString(Constants.KEY_VISITED_ID));
        updt.put(Constants.KEY_VISITOR_IMAGE, preferencesManager.getString(Constants.KEY_IMAGE));
        updt.put(Constants.KEY_COFFEESHOP_ID, preferencesManager.getString(Constants.KEY_COFFEESHOP_ID));
        database.collection(Constants.KEY_COLLECTION_VISITS)
                .document(preferencesManager.getString(Constants.KEY_VISITED_ID))
                .set(updt);
    }
    private void makeToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }
    @Override
    protected void onPause(){
        super.onPause();
        ChatAdapter.selectedItem = -1;
    }
    @Override
    protected void onResume(){
        super.onResume();
        listenAvailabilityOfReceiver();
    }
}