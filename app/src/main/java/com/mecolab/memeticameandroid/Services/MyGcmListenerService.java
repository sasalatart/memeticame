package com.mecolab.memeticameandroid.Services;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.gms.gcm.GcmListenerService;
import com.mecolab.memeticameandroid.Activities.ChatActivity;

/**
 * Created by sasalatart on 9/5/16.
 */
public class MyGcmListenerService extends GcmListenerService {
    @Override
    public void onMessageReceived(String from, Bundle data) {
        String targetBroadcast = data.getString("collapse_key");

        if (targetBroadcast.equals("new_message")) {
            broadcastNewMessage(data);
        }

        /*NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_people_black_24dp)
                .setContentTitle("Test")
                .setContentText(message);
        notificationManager.notify(1, mBuilder.build());*/
    }

    public void broadcastNewMessage(Bundle data) {
        Intent intent = new Intent(ChatActivity.CHAT_MESSAGE_FILTER);
        intent.putExtra(ChatActivity.IF_CHAT_ID, data.getString(ChatActivity.IF_CHAT_ID));
        intent.putExtra(ChatActivity.IF_MESSAGE_ID, data.getString(ChatActivity.IF_MESSAGE_ID));
        intent.putExtra(ChatActivity.IF_MESSAGE_CONTENT, data.getString(ChatActivity.IF_MESSAGE_CONTENT));
        intent.putExtra(ChatActivity.IF_MESSAGE_SENDER_PHONE, data.getString(ChatActivity.IF_MESSAGE_SENDER_PHONE));
        getApplicationContext().sendBroadcast(intent);
    }
}
