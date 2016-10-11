package com.salatart.memeticame.Services;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.salatart.memeticame.Activities.ChatActivity;
import com.salatart.memeticame.Activities.ParticipantsActivity;
import com.salatart.memeticame.Fragments.ChatsFragment;
import com.salatart.memeticame.Fragments.ContactsFragment;
import com.salatart.memeticame.Models.Chat;
import com.salatart.memeticame.Models.Message;
import com.salatart.memeticame.Models.User;
import com.salatart.memeticame.R;
import com.salatart.memeticame.Utils.ParserUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

/**
 * Created by sasalatart on 9/5/16.
 */
public class MyFcmListenerService extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage message) {
        String from = message.getFrom();
        Map data = message.getData();
        String collapseKey = message.getCollapseKey();

        if (collapseKey.equals("message_created")) {
            broadcastNewMessage(data);
        } else if (collapseKey.equals("chat_created")) {
            broadcastNewChat(data);
        } else if (collapseKey.equals("user_created")) {
            broadcastNewUser(data);
        } else if (collapseKey.equals("user_kicked")) {
            broadcastUserKicked(data);
        }
    }

    public void broadcastNewMessage(Map data) {
        if (!ChatActivity.sIsActive) {
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                    .setSmallIcon(R.drawable.ic_textsms_black_24dp)
                    .setContentTitle("Memeticame New Message")
                    .setContentText(data.get("content").toString());
            notificationManager.notify(1, mBuilder.build());
        }

        Intent intent = new Intent(ChatActivity.NEW_MESSAGE_FILTER);
        try {
            intent.putExtra(Message.PARCELABLE_KEY, ParserUtils.messageFromMap(data));
            getApplicationContext().sendBroadcast(intent);
        } catch (JSONException e) {
            Log.e("ERROR", e.toString());
        }
    }

    public void broadcastNewChat(Map data) {
        Intent intent = new Intent(ChatsFragment.NEW_CHAT_FILTER);
        try {
            intent.putExtra(Chat.PARCELABLE_KEY, ParserUtils.chatFromMap(data));
            getApplicationContext().sendBroadcast(intent);
        } catch (JSONException e) {
            Log.e("ERROR", e.toString());
        }
    }

    public void broadcastNewUser(Map data) {
        Intent intent = new Intent(ContactsFragment.NEW_USER_FILTER);
        intent.putExtra(User.PARCELABLE_KEY, ParserUtils.userFromMap(data));
        getApplicationContext().sendBroadcast(intent);
    }

    public void broadcastUserKicked(Map data) {
        Intent intent = new Intent(ParticipantsActivity.USER_KICKED_FILTER);
        try {
            intent.putExtra(User.PARCELABLE_KEY, ParserUtils.userFromJson(new JSONObject(data.get("user").toString())));
            intent.putExtra(Chat.PARCELABLE_KEY, ParserUtils.chatFromJson(new JSONObject(data.get("chat").toString())));
            getApplicationContext().sendBroadcast(intent);
        } catch (JSONException e) {
            Log.e("ERROR", e.toString());
        }
    }
}
