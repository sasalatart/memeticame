package com.salatart.memeticame.Utils;

import android.content.Context;
import android.util.Log;

import com.salatart.memeticame.Models.Attachment;
import com.salatart.memeticame.Models.Chat;
import com.salatart.memeticame.Models.ChatInvitation;
import com.salatart.memeticame.Models.Message;
import com.salatart.memeticame.Models.User;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;

/**
 * Created by sasalatart on 8/27/16.
 */
public class Routes {
    public static String DOMAIN = "https://memeticame.salatart.com";
    // static String DOMAIN = "http://10.0.2.2:3000";
    private static MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    public static Request login(String phoneNumber, String password) {
        Map<String, String> params = new HashMap<>();
        params.put("phone_number", phoneNumber);
        params.put("password", password);

        RequestBody body = RequestBody.create(JSON, new JSONObject(params).toString());
        return new Request.Builder()
                .url(DOMAIN + "/login")
                .addHeader("content-type", "application/json")
                .post(body)
                .build();
    }

    public static Request signup(String name, String phoneNumber, String password, String passwordConfirmation) {
        Map<String, String> params = new HashMap<>();
        params.put("name", name);
        params.put("phone_number", phoneNumber);
        params.put("password", password);
        params.put("password_confirmation", passwordConfirmation);

        RequestBody body = RequestBody.create(JSON, new JSONObject(params).toString());
        return new Request.Builder()
                .url(DOMAIN + "/signup")
                .addHeader("content-type", "application/json")
                .post(body)
                .build();
    }

    public static Request usersIndex(Context context, ArrayList<String> phoneNumbers) {
        FormBody.Builder formBuilder = new FormBody.Builder();
        for (int i = 0; i < phoneNumbers.size(); i++) {
            formBuilder.add("phone_numbers[" + i + "]", phoneNumbers.get(i));
        }

        return new Request.Builder()
                .url(DOMAIN + "/users")
                .addHeader("content-type", "application/json")
                .addHeader("authorization", "Token token=" + SessionUtils.getToken(context))
                .post(formBuilder.build())
                .build();
    }

    public static Request chatsIndex(Context context) {
        return new Request.Builder()
                .url(DOMAIN + "/chats")
                .addHeader("content-type", "application/json")
                .addHeader("authorization", "Token token=" + SessionUtils.getToken(context))
                .build();
    }

    public static Request chatsCreate(Context context, String title, ArrayList<User> participants, boolean isGroup) {
        ArrayList<String> phoneNumbers = new ArrayList<>();
        for (User u : participants) {
            phoneNumbers.add(u.getPhoneNumber());
        }

        FormBody.Builder formBuilder = new FormBody.Builder();

        formBuilder.add("admin", SessionUtils.getPhoneNumber(context));
        formBuilder.add("group", String.valueOf(isGroup));
        formBuilder.add("title", title);

        for (int i = 0; i < phoneNumbers.toArray().length; i++) {
            formBuilder.add("users[" + i + "]", phoneNumbers.get(i));
        }

        return new Request.Builder()
                .url(DOMAIN + "/chats")
                .addHeader("content-type", "application/json")
                .addHeader("authorization", "Token token=" + SessionUtils.getToken(context))
                .post(formBuilder.build())
                .build();
    }

    public static Request chatShow(Context context, Chat chat) {
        return new Request.Builder()
                .url(DOMAIN + "/chats/" + chat.getId())
                .addHeader("content-type", "application/json")
                .addHeader("authorization", "Token token=" + SessionUtils.getToken(context))
                .build();
    }

    public static Request messagesCreate(Context context, Message message) {

        JSONObject params = new JSONObject();
        try {
            params.put("content", message.getContent());

            if (message.getAttachment() != null) {
                Attachment attachment = message.getAttachment();
                JSONObject jsonAttachment = new JSONObject();
                jsonAttachment.put("base64", attachment.getBase64Content());
                jsonAttachment.put("mime_type", attachment.getMimeType());
                jsonAttachment.put("name", attachment.getName());
                params.put("attachment", jsonAttachment);
            }
        } catch (JSONException e) {
            Log.e("ERROR", e.toString());
        }

        RequestBody body = RequestBody.create(JSON, params.toString());
        return new Request.Builder()
                .url(DOMAIN + "/chats/" + message.getChatId() + "/messages")
                .addHeader("content-type", "application/json")
                .addHeader("authorization", "Token token=" + SessionUtils.getToken(context))
                .post(body)
                .build();
    }

    public static Request chatLeave(Context context, int chatId) {
        return new Request.Builder()
                .url(DOMAIN + "/chats/" + chatId + "/leave")
                .addHeader("content-type", "application/json")
                .addHeader("authorization", "Token token=" + SessionUtils.getToken(context))
                .post(new FormBody.Builder().build())
                .build();
    }

    public static Request kickUser(Context context, Chat chat, User user) {
        return new Request.Builder()
                .url(DOMAIN + "/chats/" + chat.getId() + "/users/" + user.getId() + "/kick")
                .addHeader("content-type", "application/json")
                .addHeader("authorization", "Token token=" + SessionUtils.getToken(context))
                .post(new FormBody.Builder().build())
                .build();
    }

    public static Request inviteUsers(Context context, Chat chat, ArrayList<User> users) {
        FormBody.Builder formBuilder = new FormBody.Builder();
        for (int i = 0; i < users.size(); i++) {
            formBuilder.add("users[" + i + "]", users.get(i).getPhoneNumber());
        }

        return new Request.Builder()
                .url(DOMAIN + "/chats/" + chat.getId() + "/invite")
                .addHeader("content-type", "application/json")
                .addHeader("authorization", "Token token=" + SessionUtils.getToken(context))
                .post(formBuilder.build())
                .build();
    }

    public static Request chatInvitationsIndex(Context context) {
        return new Request.Builder()
                .url(DOMAIN + "/chat_invitations")
                .addHeader("content-type", "application/json")
                .addHeader("authorization", "Token token=" + SessionUtils.getToken(context))
                .build();
    }

    public static Request chatInvitationsFromChat(Context context, Chat chat) {
        return new Request.Builder()
                .url(DOMAIN + "/chats/" + chat.getId() + "/invitations")
                .addHeader("content-type", "application/json")
                .addHeader("authorization", "Token token=" + SessionUtils.getToken(context))
                .build();
    }

    public static Request rejectChatInvitation(Context context, ChatInvitation chatInvitation) {
        return new Request.Builder()
                .url(DOMAIN + "/chat_invitations/" + chatInvitation.getId() + "/reject")
                .addHeader("content-type", "application/json")
                .addHeader("authorization", "Token token=" + SessionUtils.getToken(context))
                .post(new FormBody.Builder().build())
                .build();
    }

    public static Request acceptChatInvitation(Context context, ChatInvitation chatInvitation) {
        return new Request.Builder()
                .url(DOMAIN + "/chat_invitations/" + chatInvitation.getId() + "/accept")
                .addHeader("content-type", "application/json")
                .addHeader("authorization", "Token token=" + SessionUtils.getToken(context))
                .post(new FormBody.Builder().build())
                .build();
    }

    public static Request logout(Context context) {
        return new Request.Builder()
                .url(DOMAIN + "/logout")
                .addHeader("content-type", "application/json")
                .addHeader("authorization", "Token token=" + SessionUtils.getToken(context))
                .build();
    }

    public static Request plainMemesIndex(Context context) {
        return new Request.Builder()
                .url(DOMAIN + "/plain_memes")
                .addHeader("content-type", "application/json")
                .addHeader("authorization", "Token token=" + SessionUtils.getToken(context))
                .build();
    }

    public static Request fcmRegister(Context context, String token) {
        Map<String, String> params = new HashMap<String, String>();
        params.put("registration_token", token);

        RequestBody body = RequestBody.create(JSON, new JSONObject(params).toString());
        return new Request.Builder()
                .url(DOMAIN + "/fcm_register")
                .addHeader("content-type", "application/json")
                .addHeader("authorization", "Token token=" + SessionUtils.getToken(context))
                .post(body)
                .build();
    }
}
