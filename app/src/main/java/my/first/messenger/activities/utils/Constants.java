package my.first.messenger.activities.utils;

import java.util.HashMap;

public class Constants {

    public static final String KEY_PREFERENCES_NAME = "coffeeChatpreferences";

    // collections
    public static final String KEY_COLLECTION_USERS = "users";
    public static final String KEY_COLLECTION_CHAT = "chat";
    public static final String KEY_COLLECTION_CONVERSATIONS="conversations";
    public static final String KEY_COLLECTION_COFFEE_SHOPS="coffeeshops";
    public static final String KEY_COLLECTION_MEET_UP_OFFERS="meetUpOffers";
    public static final String KEY_COLLECTION_VISITS="visits";




    // User
    public static final String KEY_EMAIL = "email";
    public static final String KEY_PASSWORD = "password";
    public static final String KEY_NAME = "name";
    public static final String KEY_USER_ID = "userId";
    public static final String KEY_IMAGE = "image";
    public static final String KEY_GENDER = "gender";
    public static final String KEY_AGE = "age";
    public static final String KEY_HOBBIES = "hobbies";
    public static final String KEY_ABOUT = "about";
    public static final String KEY_FCM_TOKEN = "fcmToken";



    // statuses
    public static final String KEY_IS_SIGNED_IN = "is_signed_in";
    public static final String KEY_IS_GOING = "is_going";
    public static final String KEY_IS_ACTIVATED = "is_activated";
    public static final String KEY_IS_VISITED = "is_visited";

    public static final String KEY_USER = "user";




    // Message
    public static final String KEY_IS_PRESSED = "is_pressed";
    public static final String KEY_SENDER_ID = "senderId";
    public static final String KEY_RECEIVER_ID = "receiverId";
    public static final String KEY_MESSAGE = "message";
    public static final String KEY_TIMESTAMP="timestamp";
    public static final String KEY_SENDER_NAME="senderName";
    public static final String KEY_RECEIVER_NAME="receiverName";
    public static final String KEY_SENDER_IMAGE="senderImage";
    public static final String KEY_RECEIVER_IMAGE="receiverImage";
    public static final String KEY_LAST_MESSAGE="lastMessage";




    // Meet-ups
    public static final String KEY_VISITOR_ID = "visitorId";
    public static final String KEY_VISITED_ID = "visitedId";
    public static final String KEY_VISITOR_NAME="visitorName";
    public static final String KEY_VISITED_NAME="visitedName";
    public static final String KEY_VISITOR_IMAGE="visitorImage";
    public static final String KEY_VISITED_IMAGE="visitedImage";




    // Map
    public static final String KEY_USER_LATITUDE = "user_latitude";
    public static final String KEY_USER_LONGITUDE = "user_longitude";
    public static final String KEY_COFFEESHOP_LATITUDE = "coffeeshop_latitude";
    public static final String KEY_COFFEESHOP_LONGITUDE = "coffeeshop_longitude";
    public static final String KEY_LATITUDE = "latitude";
    public static final String KEY_LONGITUDE = "ongitude";
    public static final String KEY_ADDRESS ="address";
    public static final String KEY_COFFEESHOP_ID="coffee_shop_id";





    // Filter
    public static final String KEY_SEARCH_MAX_AGE="maxAge";
    public static final String KEY_SEARCH_GENDER="searchedGender";
    public static final String KEY_SEARCH_MIN_AGE="minAge";
    public static final String KEY_SEARCH_PURPOSE="searchedPurpose";



    // Push Notifications
    public static HashMap<String, String> remoteMsgHeaders = null;
    public static HashMap<String, String>getRemoteMsgHeaders(){
        if(remoteMsgHeaders==null){
            remoteMsgHeaders = new HashMap<>();
            remoteMsgHeaders.put(
                    REMOTE_MSG_AUTHORIZATION,
                    "key=AAAALhUEoWI:APA91bEfEiiKX5dr4qeD8w0Cg7XIuuQ3vyRsI083MRlB9WEDR7VM8904cX7vEeUYHrorw3__iz1IqZpO3bXv4SAKvfIojOgTIMpLO0jkQZT4CygZCCuhCyAgQKdl5fLWt3w-IDT7yU74"
            );
            remoteMsgHeaders.put(
                    REMOTE_MSG_CONTENT_TYPE,
                    "application/json"
            );

        }
        return remoteMsgHeaders;

    }
    public static final String KEY_AVAILABILITY ="availability";
    public static final String REMOTE_MSG_AUTHORIZATION="Authorization";
    public static final String REMOTE_MSG_CONTENT_TYPE="Content-Type";
    public static final String REMOTE_MSG_DATA="data";
    public static final String REMOTE_MSG_REGISTRATION_IDS="registration_ids";




    //Numeric CONST
    public static final int TIME_GAP=1000*60*5;


}
