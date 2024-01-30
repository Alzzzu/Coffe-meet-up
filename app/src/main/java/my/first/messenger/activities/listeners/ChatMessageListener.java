package my.first.messenger.activities.listeners;

import my.first.messenger.activities.models.ChatMessage;

public interface ChatMessageListener {
    void onMessageClick(ChatMessage message, int position);
}
