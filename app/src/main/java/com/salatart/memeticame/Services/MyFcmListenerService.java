package com.salatart.memeticame.Services;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.salatart.memeticame.Activities.ChatActivity;

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
        broadcastNewMessage(data);
    }

    public void broadcastNewMessage(Map data) {
        Intent intent = new Intent(ChatActivity.CHAT_MESSAGE_FILTER);
        intent.putExtra(ChatActivity.IF_CHAT_ID, data.get(ChatActivity.IF_CHAT_ID).toString());
        intent.putExtra(ChatActivity.IF_MESSAGE_ID, data.get(ChatActivity.IF_MESSAGE_ID).toString());
        intent.putExtra(ChatActivity.IF_MESSAGE_CONTENT, data.get(ChatActivity.IF_MESSAGE_CONTENT).toString());
        intent.putExtra(ChatActivity.IF_MESSAGE_SENDER_PHONE, data.get(ChatActivity.IF_MESSAGE_SENDER_PHONE).toString());
        getApplicationContext().sendBroadcast(intent);
    }
}
