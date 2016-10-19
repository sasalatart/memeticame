package com.salatart.memeticame.Services;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.salatart.memeticame.Activities.ChatActivity;
import com.salatart.memeticame.Models.Chat;
import com.salatart.memeticame.Models.ChatInvitation;
import com.salatart.memeticame.Models.Message;
import com.salatart.memeticame.Models.MessageCount;
import com.salatart.memeticame.Models.User;
import com.salatart.memeticame.R;
import com.salatart.memeticame.Utils.FilterUtils;
import com.salatart.memeticame.Utils.ParserUtils;
import com.salatart.memeticame.Utils.SessionUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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

        if (!SessionUtils.loggedIn(getApplicationContext())) {
            return;
        }

        if (collapseKey.equals("message_created")) {
            broadcastNewMessage(data);
        } else if (collapseKey.equals("chat_created")) {
            broadcastNewChat(data);
        } else if (collapseKey.equals("user_created")) {
            broadcastNewUser(data);
        } else if (collapseKey.equals("user_kicked")) {
            broadcastUserKicked(data);
        } else if (collapseKey.equals("chat_invitation_accepted")) {
            broadcastChatInvitationAccepted(data);
        } else if (collapseKey.equals("chat_invitation_rejected")) {
            broadcastChatInvitationRejected(data);
        } else if (collapseKey.equals("users_invited")) {
            broadcastUsersInvited(data);
        }
    }

    public void broadcastNewMessage(Map data) {
        Intent intent = new Intent(FilterUtils.NEW_MESSAGE_FILTER);
        try {
            Message message = ParserUtils.messageFromJson(new JSONObject(data.get("message").toString()));
            MessageCount.addOneUnreadMessage(message.getChatId());
            intent.putExtra(Message.PARCELABLE_KEY, message);
            getApplicationContext().sendBroadcast(intent);

            if (!ChatActivity.sIsActive) {
                NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_textsms_black_24dp)
                        .setContentTitle("Memeticame New Message")
                        .setContentText(message.getContent());
                notificationManager.notify(1, mBuilder.build());
            }
        } catch (JSONException e) {
            Log.e("ERROR", e.toString());
        }
    }

    public void broadcastNewChat(Map data) {
        Intent intent = new Intent(FilterUtils.NEW_CHAT_FILTER);
        try {
            intent.putExtra(Chat.PARCELABLE_KEY, ParserUtils.chatFromJson(new JSONObject(data.get("chat").toString())));
            getApplicationContext().sendBroadcast(intent);
        } catch (JSONException e) {
            Log.e("ERROR", e.toString());
        }
    }

    public void broadcastNewUser(Map data) {
        Intent intent = new Intent(FilterUtils.NEW_USER_FILTER);
        try {
            intent.putExtra(User.PARCELABLE_KEY, ParserUtils.userFromJson(new JSONObject(data.get("user").toString())));
        } catch (JSONException e) {
            Log.e("ERROR", e.toString());
        }
        getApplicationContext().sendBroadcast(intent);
    }

    public void broadcastUserKicked(Map data) {
        Intent intent = new Intent(FilterUtils.USER_KICKED_FILTER);
        try {
            intent.putExtra(User.PARCELABLE_KEY, ParserUtils.userFromJson(new JSONObject(data.get("user").toString())));
            intent.putExtra(Chat.PARCELABLE_KEY, ParserUtils.chatFromJson(new JSONObject(data.get("chat").toString())));
            getApplicationContext().sendBroadcast(intent);
        } catch (JSONException e) {
            Log.e("ERROR", e.toString());
        }
    }

    public void broadcastChatInvitationAccepted(Map data) {
        Intent intent = new Intent(FilterUtils.CHAT_INVITATION_ACCEPTED_FILTER);
        try {
            intent.putExtra(ChatInvitation.PARCELABLE_KEY, ParserUtils.chatInvitationFromJson(new JSONObject(data.get("chat_invitation").toString())));
            getApplicationContext().sendBroadcast(intent);
        } catch (JSONException e) {
            Log.e("ERROR", e.toString());
        }
    }

    public void broadcastChatInvitationRejected(Map data) {
        Intent intent = new Intent(FilterUtils.CHAT_INVITATION_REJECTED_FILTER);
        try {
            intent.putExtra(ChatInvitation.PARCELABLE_KEY, ParserUtils.chatInvitationFromJson(new JSONObject(data.get("chat_invitation").toString())));
            getApplicationContext().sendBroadcast(intent);
        } catch (JSONException e) {
            Log.e("ERROR", e.toString());
        }
    }

    public void broadcastUsersInvited(Map data) {
        Intent intent = new Intent(FilterUtils.NEW_CHAT_INVITATION_FILTER);
        try {
            ArrayList<ChatInvitation> chatInvitations = ParserUtils.chatInvitationsFromJsonArray(new JSONArray(data.get("chat_invitations").toString()));
            intent.putExtra(ChatInvitation.PARCELABLE_KEY_ARRAY_LIST, chatInvitations);
            getApplicationContext().sendBroadcast(intent);

            String myPhoneNumber = SessionUtils.getPhoneNumber(getApplicationContext());
            for (ChatInvitation chatInvitation : chatInvitations) {
                if (User.comparePhones(chatInvitation.getUser().getPhoneNumber(), myPhoneNumber)) {
                    NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                    NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                            .setSmallIcon(R.drawable.ic_card_membership_black_24dp)
                            .setContentTitle("Memeticame New Chat Invitation")
                            .setContentText(chatInvitations.get(0).getChatTitle());
                    notificationManager.notify(1, mBuilder.build());
                    break;
                }
            }
        } catch (JSONException e) {
            Log.e("ERROR", e.toString());
        }
    }
}
