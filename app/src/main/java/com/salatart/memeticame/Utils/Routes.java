package com.salatart.memeticame.Utils;

import android.content.Context;
import android.util.Log;

import com.salatart.memeticame.Models.Attachment;
import com.salatart.memeticame.Models.Chat;
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
    //public static String DOMAIN = "http://10.0.2.2:3000";
    public static String LOGIN_PATH = "/login";
    public static String SIGNUP_PATH = "/signup";
    public static String LOGOUT_PATH = "/logout";
    public static String USERS_INDEX_PATH = "/users";
    public static String CHATS_INDEX_PATH = "/chats";
    public static String CHATS_CREATE_PATH = "/chats";
    public static String FCM_REGISTRATION_PATH = "/fcm_register";
    public static MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    public static Request loginRequest(String phoneNumber, String password) {
        Map<String, String> params = new HashMap<String, String>();
        params.put("phone_number", phoneNumber);
        params.put("password", password);

        RequestBody body = RequestBody.create(JSON, new JSONObject(params).toString());
        return new Request.Builder()
                .url(DOMAIN + LOGIN_PATH)
                .addHeader("content-type", "application/json")
                .post(body)
                .build();
    }

    public static Request signupRequest(String name, String phoneNumber, String password, String passwordConfirmation) {
        Map<String, String> params = new HashMap<String, String>();
        params.put("name", name);
        params.put("phone_number", phoneNumber);
        params.put("password", password);
        params.put("password_confirmation", passwordConfirmation);

        RequestBody body = RequestBody.create(JSON, new JSONObject(params).toString());
        return new Request.Builder()
                .url(DOMAIN + SIGNUP_PATH)
                .addHeader("content-type", "application/json")
                .post(body)
                .build();
    }

    public static Request userIndexRequest(Context context) {
        return new Request.Builder()
                .url(DOMAIN + USERS_INDEX_PATH)
                .addHeader("content-type", "application/json")
                .addHeader("authorization", "Token token=" + SessionUtils.getToken(context))
                .build();
    }

    public static Request chatsIndexRequest(Context context) {
        return new Request.Builder()
                .url(DOMAIN + CHATS_INDEX_PATH)
                .addHeader("content-type", "application/json")
                .addHeader("authorization", "Token token=" + SessionUtils.getToken(context))
                .build();
    }

    public static Request chatsCreateRequest(Context context, String title, ArrayList<User> participants, boolean isGroup) {
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
                .url(DOMAIN + CHATS_CREATE_PATH)
                .addHeader("content-type", "application/json")
                .addHeader("authorization", "Token token=" + SessionUtils.getToken(context))
                .post(formBuilder.build())
                .build();
    }

    public static Request chatMessagesRequest(Context context, int chatId) {
        return new Request.Builder()
                .url(DOMAIN + "/chats/" + chatId + "/messages")
                .addHeader("content-type", "application/json")
                .addHeader("authorization", "Token token=" + SessionUtils.getToken(context))
                .build();
    }

    public static Request messagesCreateRequest(Context context, Message message) {

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

    public static Request chatLeaveRequest(Context context, int chatId) {
        return new Request.Builder()
                .url(DOMAIN + "/chats/" + chatId + "/leave")
                .addHeader("content-type", "application/json")
                .addHeader("authorization", "Token token=" + SessionUtils.getToken(context))
                .post(new FormBody.Builder().build())
                .build();
    }

    public static Request kickUserRequest(Context context, Chat chat, User user) {
        return new Request.Builder()
                .url(DOMAIN + "/chats/" + chat.getId() + "/users/" + user.getId() + "/kick")
                .addHeader("content-type", "application/json")
                .addHeader("authorization", "Token token=" + SessionUtils.getToken(context))
                .post(new FormBody.Builder().build())
                .build();
    }

    public static Request logoutRequest(Context context) {
        return new Request.Builder()
                .url(DOMAIN + LOGOUT_PATH)
                .addHeader("content-type", "application/json")
                .addHeader("authorization", "Token token=" + SessionUtils.getToken(context))
                .build();
    }

    public static Request fcmRegisterRequest(Context context, String token) {
        Map<String, String> params = new HashMap<String, String>();
        params.put("registration_token", token);

        RequestBody body = RequestBody.create(JSON, new JSONObject(params).toString());
        return new Request.Builder()
                .url(DOMAIN + FCM_REGISTRATION_PATH)
                .addHeader("content-type", "application/json")
                .addHeader("authorization", "Token token=" + SessionUtils.getToken(context))
                .post(body)
                .build();
    }

    public interface OnLogout {
        void OnLogout();
    }
}
