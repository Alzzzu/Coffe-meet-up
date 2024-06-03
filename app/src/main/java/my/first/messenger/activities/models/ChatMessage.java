package my.first.messenger.activities.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.Date;

import my.first.messenger.activities.utils.Constants;

public class ChatMessage {

    @SerializedName("type")
    @Expose
    public String type;
    @SerializedName(Constants.KEY_SENDER_ID)
    @Expose
    public String senderId;
    @SerializedName(Constants.KEY_RECEIVER_ID)
    @Expose
    public String receiverId;
    @SerializedName("dateTime")
    @Expose
    public String dateTime;
    @SerializedName("id")
    @Expose
    public String id;
    @SerializedName("message")
    @Expose
    public String message;
    @SerializedName("date")
    @Expose
    public Date dateObject;
    @SerializedName(Constants.KEY_COFFEESHOP_ID)
    @Expose
    public String coffeeshopId;
    @SerializedName("clicked")
    @Expose
    public boolean clicked;
    @SerializedName("conversationId")
    @Expose
    public String conversationId;
    @SerializedName("conversationName")
    @Expose
    public String conversationName;
    @SerializedName("conversationImage")
    @Expose
    public String conversationImage;

}
