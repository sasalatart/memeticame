package com.salatart.memeticame.Utils;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.salatart.memeticame.Models.Attachment;
import com.salatart.memeticame.Models.Channel;
import com.salatart.memeticame.Models.Chat;
import com.salatart.memeticame.Models.ChatInvitation;
import com.salatart.memeticame.Models.Meme;
import com.salatart.memeticame.Models.Message;
import com.salatart.memeticame.Models.User;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;

/**
 * Created by sasalatart on 8/27/16.
 */
public class Routes {
    public static String DOMAIN = "https://memeticame.salatart.com";
    // public static String DOMAIN = "http://10.0.2.2:3000";
    public static String UNZIP_ROUTE = DOMAIN + "/system/unzipped";

    private static MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    public static Request buildGetRequest(Context context, String url) {
        return new Request.Builder()
                .url(DOMAIN + url)
                .addHeader("content-type", "application/json")
                .addHeader("authorization", "Token token=" + SessionUtils.getToken(context))
                .build();
    }

    public static Request buildPostRequest(Context context, String url, FormBody.Builder formBuilder) {
        return new Request.Builder()
                .url(DOMAIN + url)
                .addHeader("content-type", "application/json")
                .addHeader("authorization", "Token token=" + SessionUtils.getToken(context))
                .post(formBuilder.build())
                .build();
    }

    public static Request buildTokenlessPostRequest(String url, FormBody.Builder formBuilder) {
        return new Request.Builder()
                .url(DOMAIN + url)
                .addHeader("content-type", "application/json")
                .post(formBuilder.build())
                .build();
    }

    public static Request login(String phoneNumber, String password) {
        FormBody.Builder formBuilder = new FormBody.Builder();
        formBuilder.add("phone_number", phoneNumber);
        formBuilder.add("password", password);

        return buildTokenlessPostRequest("/login", formBuilder);
    }

    public static Request signup(String name, String phoneNumber, String password, String passwordConfirmation) {
        FormBody.Builder formBuilder = new FormBody.Builder();
        formBuilder.add("name", name);
        formBuilder.add("phone_number", phoneNumber);
        formBuilder.add("password", password);
        formBuilder.add("password_confirmation", passwordConfirmation);

        return buildTokenlessPostRequest("/signup", formBuilder);
    }

    public static Request usersIndex(Context context, ArrayList<String> phoneNumbers) {
        FormBody.Builder formBuilder = new FormBody.Builder();
        for (int i = 0; i < phoneNumbers.size(); i++) {
            formBuilder.add("phone_numbers[" + i + "]", phoneNumbers.get(i));
        }

        return buildPostRequest(context, "/users", formBuilder);
    }

    public static Request usersShow(Context context, User user) {
        String path = "/users/" + user.getPhoneNumber();
        return buildGetRequest(context, path);
    }

    public static Request chatsIndex(Context context) {
        return buildGetRequest(context, "/chats");
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

        return buildPostRequest(context, "/chats", formBuilder);
    }

    public static Request chatShow(Context context, Chat chat) {
        String path = "/chats/" + chat.getId();
        return buildGetRequest(context, path);
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

            RequestBody body = RequestBody.create(JSON, params.toString());
            return new Request.Builder()
                    .url(DOMAIN + "/chats/" + message.getChatId() + "/messages")
                    .addHeader("content-type", "application/json")
                    .addHeader("authorization", "Token token=" + SessionUtils.getToken(context))
                    .post(body)
                    .build();
        } catch (JSONException e) {
            Log.e("ERROR", e.toString());
            return null;
        }
    }

    public static Request chatLeave(Context context, int chatId) {
        String path = "/chats/" + chatId + "/leave";
        return buildPostRequest(context, path, new FormBody.Builder());
    }

    public static Request kickUser(Context context, Chat chat, User user) {
        String path = "/chats/" + chat.getId() + "/users/" + user.getId() + "/kick";
        return buildPostRequest(context, path, new FormBody.Builder());
    }

    public static Request inviteUsers(Context context, Chat chat, ArrayList<User> users) {
        FormBody.Builder formBuilder = new FormBody.Builder();
        for (int i = 0; i < users.size(); i++) {
            formBuilder.add("users[" + i + "]", users.get(i).getPhoneNumber());
        }

        String path = "/chats/" + chat.getId() + "/invite";
        return buildPostRequest(context, path, formBuilder);
    }

    public static Request chatInvitationsIndex(Context context) {
        return buildGetRequest(context, "/chat_invitations");
    }

    public static Request chatInvitationsFromChat(Context context, Chat chat) {
        String path = "/chats/" + chat.getId() + "/invitations";
        return buildGetRequest(context, path);
    }

    public static Request rejectChatInvitation(Context context, ChatInvitation chatInvitation) {
        String path = "/chat_invitations/" + chatInvitation.getId() + "/reject";
        return buildPostRequest(context, path, new FormBody.Builder());
    }

    public static Request acceptChatInvitation(Context context, ChatInvitation chatInvitation) {
        String path = "/chat_invitations/" + chatInvitation.getId() + "/accept";
        return buildPostRequest(context, path, new FormBody.Builder());
    }

    public static Request plainMemesIndex(Context context) {
        return buildGetRequest(context, "/plain_memes");
    }

    public static Request channelsIndex(Context context) {
        return buildGetRequest(context, "/channels");
    }

    public static Request channelsShow(Context context, Channel channel) {
        String path = "/channels/" + channel.getId();
        return buildGetRequest(context, path);
    }

    public static Request channelsCreate(Context context, String channelName, ArrayList<String> categories) {
        FormBody.Builder formBuilder = new FormBody.Builder();
        formBuilder.add("name", channelName);
        for (int i = 0; i < categories.size(); i++) {
            formBuilder.add("categories[" + i + "]", categories.get(i));
        }

        return buildPostRequest(context, "/channels", formBuilder);
    }

    public static Request memesCreate(Context context, int categoryId, String name, String[] tags, Uri memeUri) {
        FormBody.Builder formBuilder = new FormBody.Builder();
        for (int i = 0; i < tags.length; i++) {
            formBuilder.add("tags[" + i + "]", tags[i]);
        }

        try {
            formBuilder.add("base64", FileUtils.encodeToBase64FromUri(context, memeUri));
            formBuilder.add("mime_type", FileUtils.getMimeType(context, memeUri));
            formBuilder.add("name", name);

            String path = "/categories/" + categoryId + "/memes";
            return buildPostRequest(context, path, formBuilder);
        } catch (IOException e) {
            Log.e("ERROR", e.toString());
            return null;
        }
    }

    public static Request memesSearch(Context context, ArrayList<String> searchTags) {
        FormBody.Builder formBuilder = new FormBody.Builder();
        for (int i = 0; i < searchTags.size(); i++) {
            formBuilder.add("tags[" + i + "]", searchTags.get(i));
        }

        return buildPostRequest(context, "/search_memes", formBuilder);
    }

    public static Request myRating(Context context, Meme meme) {
        String path = "/memes/" + meme.getId() + "/my_rating";
        return buildGetRequest(context, path);
    }

    public static Request ratingsCreate(Context context, Meme meme, float value) {
        FormBody.Builder formBuilder = new FormBody.Builder();
        formBuilder.add("value", value + "");

        String path = "/memes/" + meme.getId() + "/ratings";
        return buildPostRequest(context, path, formBuilder);
    }

    public static Request logout(Context context) {
        return buildGetRequest(context, "/logout");
    }

    public static Request fcmRegister(Context context, String token) {
        FormBody.Builder formBuilder = new FormBody.Builder();
        formBuilder.add("registration_token", token);

        return buildPostRequest(context, "/fcm_register", formBuilder);
    }
}
