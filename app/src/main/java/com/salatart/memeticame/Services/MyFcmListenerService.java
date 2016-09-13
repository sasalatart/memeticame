package com.salatart.memeticame.Services;

import android.content.Intent;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.salatart.memeticame.Activities.ChatActivity;
import com.salatart.memeticame.Fragments.ChatsFragment;
import com.salatart.memeticame.Models.Chat;
import com.salatart.memeticame.Models.Message;
import com.salatart.memeticame.Models.User;
import com.salatart.memeticame.Utils.SessionUtils;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
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
        }
    }

    public void broadcastNewMessage(Map data) {
        Intent intent = new Intent(ChatActivity.NEW_MESSAGE_FILTER);

        intent.putExtra(Message.PARCELABLE_KEY,
                new Message(Integer.parseInt(data.get("id").toString()),
                        data.get("sender_phone").toString(),
                        data.get("content").toString(),
                        Integer.parseInt(data.get("chat_id").toString()),
                        data.get("created_at").toString()));

        getApplicationContext().sendBroadcast(intent);
    }

    public void broadcastNewChat(Map data) {
        Intent intent = new Intent(ChatsFragment.NEW_CHAT_FILTER);

        try {
            ArrayList<User> users = User.fromJsonArray(new JSONArray(data.get("users").toString()));
            intent.putExtra(Chat.PARCELABLE_KEY,
                    new Chat(Integer.parseInt(data.get("id").toString()),
                            data.get("title").toString(),
                            Boolean.parseBoolean(data.get("group").toString()),
                            data.get("created_at").toString(),
                            users));
        } catch (JSONException e) {
            Log.e("ERROR", e.toString());
        }

        getApplicationContext().sendBroadcast(intent);
    }
}
