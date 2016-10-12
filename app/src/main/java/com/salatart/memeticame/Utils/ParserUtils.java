package com.salatart.memeticame.Utils;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.salatart.memeticame.Models.Attachment;
import com.salatart.memeticame.Models.Chat;
import com.salatart.memeticame.Models.Message;
import com.salatart.memeticame.Models.User;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

/**
 * Created by sasalatart on 10/10/16.
 */

public class ParserUtils {
    public static Chat chatFromJson(JSONObject jsonResponse) throws JSONException {
        ArrayList<User> users = usersFromJsonArray(new JSONArray(jsonResponse.getString("users")));
        User admin = userFromJson(new JSONObject(jsonResponse.getString("admin")));
        ArrayList<Message> messages = messagesFromJsonArray(new JSONArray(jsonResponse.getString("messages")));

        return new Chat(Integer.parseInt(jsonResponse.getString("id")),
                jsonResponse.getString("title"),
                Boolean.parseBoolean(jsonResponse.getString("group")),
                jsonResponse.getString("created_at"),
                admin.getPhoneNumber(),
                users,
                messages);
    }

    public static ArrayList<Chat> chatsFromJsonArray(JSONArray jsonResponse) throws JSONException {
        ArrayList<Chat> chats = new ArrayList<>();

        for (int i = 0; i < jsonResponse.length(); i++) {
            JSONObject jsonChat = jsonResponse.getJSONObject(i);
            chats.add(chatFromJson(jsonChat));
        }

        return chats;
    }

    public static Chat chatFromMap(Map mapChat) throws JSONException {
        ArrayList<User> users = usersFromJsonArray(new JSONArray(mapChat.get("users").toString()));
        User admin = userFromJson(new JSONObject(mapChat.get("admin").toString()));
        ArrayList<Message> messages = messagesFromJsonArray(new JSONArray(mapChat.get("messages").toString()));

        return new Chat(Integer.parseInt(mapChat.get("id").toString()),
                mapChat.get("title").toString(),
                Boolean.parseBoolean(mapChat.get("group").toString()),
                mapChat.get("created_at").toString(),
                admin.getPhoneNumber(),
                users,
                messages);
        }

    public static Message messageFromJson(JSONObject jsonMessage) throws JSONException {
        Message message = new Message(jsonMessage.getInt("id"),
                jsonMessage.getString("sender_phone"),
                jsonMessage.getString("content"),
                jsonMessage.getInt("chat_id"),
                jsonMessage.getString("created_at"));

        JSONObject jsonAttachment = jsonMessage.getJSONObject("attachment_link");
        if (!jsonAttachment.getString("name").equals("null")) {
            message.setAttachment(new Attachment(jsonAttachment.getString("name"),
                    jsonAttachment.getString("mime_type"),
                    null,
                    Routes.DOMAIN + jsonAttachment.getString("url")));
        }

        return message;
    }

    public static ArrayList<Message> messagesFromJsonArray(JSONArray jsonResponse) throws JSONException {
        ArrayList<Message> messages = new ArrayList<>();
        for (int i = 0; i < jsonResponse.length(); i++) {
            messages.add(messageFromJson(jsonResponse.getJSONObject(i)));
        }

        return messages;
    }

    public static Message messageFromMap(Map mapMessage) throws JSONException {
        Message message = new Message(Integer.parseInt(mapMessage.get("id").toString()),
                mapMessage.get("sender_phone").toString(),
                mapMessage.get("content").toString(),
                Integer.parseInt(mapMessage.get("chat_id").toString()),
                mapMessage.get("created_at").toString());

        JSONObject jsonAttachment = new JSONObject(mapMessage.get("attachment_link").toString());
        if (!jsonAttachment.getString("name").equals("null")) {
            message.setAttachment(new Attachment(jsonAttachment.getString("name"),
                    jsonAttachment.getString("mime_type"),
                    null,
                    Routes.DOMAIN + jsonAttachment.getString("url")));
        }

        return message;
    }

    public static User userFromMap(Map mapUser) {
        return new User(Integer.parseInt(mapUser.get("id").toString()), mapUser.get("name").toString(), mapUser.get("phone_number").toString());
    }

    public static User userFromJson(JSONObject jsonUser) throws JSONException {
        return new User(jsonUser.getInt("id"), jsonUser.getString("name"), jsonUser.getString("phone_number"));
    }

    public static ArrayList<User> usersFromJsonArray(JSONArray jsonResponse) throws JSONException {
        ArrayList<User> users = new ArrayList<>();

        for (int i = 0; i < jsonResponse.length(); i++) {
            users.add(userFromJson(jsonResponse.getJSONObject(i)));
        }

        return users;
    }

    public static Attachment attachmentFromUri(Context context, Uri uri) {
        if (uri == null) {
            return null;
        }

        try {
            return new Attachment(FileUtils.getName(context, uri),
                    FileUtils.getMimeType(context, uri),
                    FileUtils.encodeToBase64FromUri(context, uri),
                    uri.toString());
        } catch (IOException e) {
            Log.e("ERROR", e.toString());
            return null;
        }
    }
}
