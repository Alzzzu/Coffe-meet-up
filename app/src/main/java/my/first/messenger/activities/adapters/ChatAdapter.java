package my.first.messenger.activities.adapters;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import my.first.messenger.R;
import my.first.messenger.activities.listeners.ChatMessageListener;
import my.first.messenger.activities.models.ChatMessage;
import my.first.messenger.databinding.ItemContainerReceivedMessageBinding;
import my.first.messenger.databinding.ItemContainerSentMessageBinding;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
    //private final Bitmap receiverProfileImage;
    private List<ChatMessage> chatMessages;
    private final String senderId;
    private int focusedItem = 0;

    private  int selectedPos = RecyclerView.NO_POSITION;
    public final ChatMessageListener chatMessageListener;
    public static final int VIEW_TYPE_SENT=1;
    public static final int VIEW_TYPE_RECEIVED=2;
    public ChatAdapter(List<ChatMessage> chatMessages, String senderId, ChatMessageListener chatMessageListener){
        this.chatMessages = chatMessages;
        this.senderId=senderId;
        this.chatMessageListener = chatMessageListener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(viewType==VIEW_TYPE_SENT){
            return new SentMessageViewHolder(
                    ItemContainerSentMessageBinding.inflate(
                            LayoutInflater.from(parent.getContext()),
                            parent,
                            false), chatMessageListener);
        }
        return new ReceivedMessageViewHolder(ItemContainerReceivedMessageBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false),chatMessageListener);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if(getItemViewType(position)==VIEW_TYPE_SENT){
            //  holder.itemView.setSelected(focusedItem == position);
            ((SentMessageViewHolder)holder).setData(chatMessages.get(position));

        } else{
            ((ReceivedMessageViewHolder)holder).setData(chatMessages.get(position));
        }
    }

    @Override
    public int getItemCount() {
        return chatMessages.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (chatMessages.get(position).senderId.equals(senderId)){
            return VIEW_TYPE_SENT;
        }
        else {
            return VIEW_TYPE_RECEIVED;
        }
    }

    static class SentMessageViewHolder extends RecyclerView.ViewHolder {
        private final ItemContainerSentMessageBinding binding;
        private final ChatMessageListener chatMessageListener;
        SentMessageViewHolder(ItemContainerSentMessageBinding itemContainerSentMessageBinding, ChatMessageListener chatMessageListener) {
            super(itemContainerSentMessageBinding.getRoot());
            binding = itemContainerSentMessageBinding;
            this.chatMessageListener = chatMessageListener;
        }

        void setData(ChatMessage chatMessage) {
            binding.textMessageSent.setText(chatMessage.message);
            binding.textDateTime.setText(chatMessage.dateTime);

            binding.getRoot().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!chatMessage.clicked) {
                        chatMessageListener.onMessageClick(chatMessage, getAdapterPosition());
                        binding.textMessageSent.setBackground(ContextCompat.getDrawable(v.getContext(), R.drawable.background_selected_message));
                        chatMessage.clicked = true;
                    }
                    else{
                        chatMessageListener.onMessageClick(chatMessage, getAdapterPosition());
                        chatMessage.clicked = false;
                        binding.textMessageSent.setBackground(ContextCompat.getDrawable(v.getContext(), R.drawable.background_sent_message));
                    }
                }
            });
        }


    }
    static class ReceivedMessageViewHolder extends RecyclerView.ViewHolder {
        private final ItemContainerReceivedMessageBinding binding;
        private final ChatMessageListener chatMessageListener;
        ReceivedMessageViewHolder(ItemContainerReceivedMessageBinding itemContainerReceivedMessageBinding, ChatMessageListener chatMessageListener){
            super(itemContainerReceivedMessageBinding.getRoot());
            binding = itemContainerReceivedMessageBinding;
            this.chatMessageListener = chatMessageListener;
        }
        void setData(ChatMessage chatMessage){
            binding.textMessageReceived.setText(chatMessage.message);
            binding.textDateTime.setText(chatMessage.dateTime);
            if(chatMessage.type.equals("location")){
                binding.getRoot().setOnClickListener(v->{chatMessageListener.onMessageClick(chatMessage, getAdapterPosition());});
            }
        }
    }
    public void removeAt(int position) {
        chatMessages.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, chatMessages.size());
    }
    public void editAt(int position, ChatMessage message){
        chatMessages.set(position, message);
        notifyItemChanged(position);
    }


}