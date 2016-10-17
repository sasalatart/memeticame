package com.salatart.memeticame.Utils;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.salatart.memeticame.Models.Attachment;
import com.salatart.memeticame.Models.Chat;
import com.salatart.memeticame.Models.ChatInvitation;
import com.salatart.memeticame.Models.Message;
import com.salatart.memeticame.Models.User;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

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
                    Routes.DOMAIN + jsonAttachment.getString("url"),
                    jsonAttachment.getLong("size")));
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

        String name = FileUtils.getName(context, uri);
        String mimeType = FileUtils.getMimeType(uri);
        if (name.contains(".zip") && name.contains(ZipManager.SEPARATOR)) {
            mimeType = "zip/memeaudio";
        }

        try {
            return new Attachment(name,
                    mimeType,
                    FileUtils.encodeToBase64FromUri(context, uri),
                    uri.toString(),
                    new File(uri.getPath()).length());
        } catch (IOException e) {
            Log.e("ERROR", e.toString());
            return null;
        }
    }

    public static ChatInvitation chatInvitationFromJson(JSONObject jsonChatInvitation) throws JSONException {
        return new ChatInvitation(jsonChatInvitation.getInt("id"),
                ParserUtils.userFromJson(new JSONObject(jsonChatInvitation.getString("user"))),
                jsonChatInvitation.getInt("chat_id"),
                jsonChatInvitation.getString("chat_title"),
                jsonChatInvitation.getString("created_at"));
    }

    public static ArrayList<ChatInvitation> chatInvitationsFromJsonArray(JSONArray jsonResponse) throws JSONException {
        ArrayList<ChatInvitation> chatInvitations = new ArrayList<>();

        for (int i = 0; i < jsonResponse.length(); i++) {
            chatInvitations.add(chatInvitationFromJson(jsonResponse.getJSONObject(i)));
        }

        return chatInvitations;
    }
}
