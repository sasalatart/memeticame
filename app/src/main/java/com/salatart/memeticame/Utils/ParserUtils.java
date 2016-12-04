package com.salatart.memeticame.Utils;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.salatart.memeticame.Managers.ZipManager;
import com.salatart.memeticame.Models.Attachment;
import com.salatart.memeticame.Models.Category;
import com.salatart.memeticame.Models.Channel;
import com.salatart.memeticame.Models.Chat;
import com.salatart.memeticame.Models.ChatInvitation;
import com.salatart.memeticame.Models.FaceEmotion;
import com.salatart.memeticame.Models.Meme;
import com.salatart.memeticame.Models.Message;
import com.salatart.memeticame.Models.User;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Created by sasalatart on 10/10/16.
 */

public class ParserUtils {
    public static Chat chatFromJson(JSONObject jsonObject) throws JSONException {
        ArrayList<User> users = usersFromJsonArray(new JSONArray(jsonObject.getString("users")));
        User admin = userFromJson(new JSONObject(jsonObject.getString("admin")));
        ArrayList<Message> messages = messagesFromJsonArray(new JSONArray(jsonObject.getString("messages")));

        return new Chat(Integer.parseInt(jsonObject.getString("id")),
                jsonObject.getString("title"),
                Boolean.parseBoolean(jsonObject.getString("group")),
                jsonObject.getString("created_at"),
                admin.getPhoneNumber(),
                users,
                messages);
    }

    public static ArrayList<Chat> chatsFromJsonArray(JSONArray jsonArray) throws JSONException {
        ArrayList<Chat> chats = new ArrayList<>();

        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonChat = jsonArray.getJSONObject(i);
            chats.add(chatFromJson(jsonChat));
        }

        return chats;
    }

    public static Message messageFromJson(JSONObject jsonObject) throws JSONException {
        Message message = new Message(jsonObject.getInt("id"),
                jsonObject.getString("sender_phone"),
                jsonObject.getString("content"),
                jsonObject.getInt("chat_id"),
                jsonObject.getString("created_at"));

        JSONObject jsonAttachment = jsonObject.getJSONObject("attachment_link");
        if (!jsonAttachment.getString("name").equals("null")) {
            message.setAttachment(new Attachment(jsonAttachment.getString("name"),
                    jsonAttachment.getString("mime_type"),
                    null,
                    Routes.DOMAIN + jsonAttachment.getString("url"),
                    jsonAttachment.getLong("size"),
                    -1,
                    false));
        }

        return message;
    }

    public static ArrayList<Message> messagesFromJsonArray(JSONArray jsonArray) throws JSONException {
        ArrayList<Message> messages = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++) {
            messages.add(messageFromJson(jsonArray.getJSONObject(i)));
        }

        return messages;
    }

    public static User userFromJson(JSONObject jsonObject) throws JSONException {
        return new User(jsonObject.getInt("id"), jsonObject.getString("name"), jsonObject.getString("phone_number"));
    }

    public static ArrayList<User> usersFromJsonArray(JSONArray jsonArray) throws JSONException {
        ArrayList<User> users = new ArrayList<>();

        for (int i = 0; i < jsonArray.length(); i++) {
            users.add(userFromJson(jsonArray.getJSONObject(i)));
        }

        return users;
    }

    public static Attachment attachmentFromUri(Context context, Uri uri) {
        if (uri == null) {
            return null;
        }

        String name = FileUtils.getName(context, uri);
        String mimeType = FileUtils.getMimeType(context, uri);
        if (name.contains(".zip") && name.contains(ZipManager.SEPARATOR)) {
            mimeType = "zip/memeaudio";
        }

        try {
            return new Attachment(name, mimeType, FileUtils.encodeToBase64FromUri(context, uri), uri.toString(), new File(uri.getPath()).length(), -1, false);
        } catch (IOException e) {
            Log.e("ERROR", e.toString());
            return null;
        }
    }

    public static ChatInvitation chatInvitationFromJson(JSONObject jsonObject) throws JSONException {
        return new ChatInvitation(jsonObject.getInt("id"),
                ParserUtils.userFromJson(new JSONObject(jsonObject.getString("user"))),
                jsonObject.getInt("chat_id"),
                jsonObject.getString("chat_title"),
                jsonObject.getString("created_at"));
    }

    public static ArrayList<ChatInvitation> chatInvitationsFromJsonArray(JSONArray jsonArray) throws JSONException {
        ArrayList<ChatInvitation> chatInvitations = new ArrayList<>();

        for (int i = 0; i < jsonArray.length(); i++) {
            chatInvitations.add(chatInvitationFromJson(jsonArray.getJSONObject(i)));
        }

        return chatInvitations;
    }

    public static ArrayList<String[]> plainMemesFromJsonArray(JSONArray jsonArray) throws JSONException {
        ArrayList<String[]> memes = new ArrayList<>();

        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            String thumbMeme = jsonObject.getString("thumb");
            String originalMeme = jsonObject.getString("original");
            memes.add(new String[]{thumbMeme, originalMeme});
        }

        return memes;
    }

    public static Meme memeFromJson(JSONObject jsonObject) throws JSONException {

        ArrayList<String> tags = new ArrayList();
        JSONArray jsonTags = jsonObject.getJSONArray("tags");
        for (int i = 0; i < jsonTags.length(); i++) {
            tags.add(jsonTags.getJSONObject(i).getString("text"));
        }

        return new Meme(jsonObject.getInt("id"),
                jsonObject.getInt("category_id"),
                jsonObject.getString("name"),
                userFromJson(jsonObject.getJSONObject("owner")),
                jsonObject.getString("thumb_url"),
                jsonObject.getString("original_url"),
                jsonObject.getDouble("rating"),
                tags,
                jsonObject.getString("created_at"));
    }

    public static ArrayList<Meme> memesFromJsonArray(JSONArray jsonArray) throws JSONException {
        ArrayList<Meme> memes = new ArrayList<>();

        for (int i = 0; i < jsonArray.length(); i++) {
            memes.add(memeFromJson(jsonArray.getJSONObject(i)));
        }

        return memes;
    }

    public static Category categoryFromJson(JSONObject jsonObject) throws JSONException {
        return new Category(jsonObject.getInt("id"),
                jsonObject.getInt("channel_id"),
                jsonObject.getString("name"),
                memesFromJsonArray(jsonObject.getJSONArray("memes")),
                jsonObject.getString("created_at"));
    }

    public static ArrayList<Category> categoriesFromJsonArray(JSONArray jsonArray) throws JSONException {
        ArrayList<Category> categories = new ArrayList<>();

        for (int i = 0; i < jsonArray.length(); i++) {
            categories.add(categoryFromJson(jsonArray.getJSONObject(i)));
        }

        return categories;
    }

    public static Channel channelFromJson(JSONObject jsonObject, boolean lazyLoading) throws JSONException {
        ArrayList<Category> categories = new ArrayList<>();
        if (!lazyLoading) {
            categories = categoriesFromJsonArray(new JSONArray(jsonObject.getString("categories")));
        }

        return new Channel(jsonObject.getInt("id"),
                jsonObject.getString("name"),
                categories,
                userFromJson(jsonObject.getJSONObject("owner")),
                jsonObject.getDouble("rating"),
                jsonObject.getString("created_at"));
    }

    public static ArrayList<Channel> channelsFromJsonArray(JSONArray jsonArray) throws JSONException {
        ArrayList<Channel> channels = new ArrayList<>();

        for (int i = 0; i < jsonArray.length(); i++) {
            channels.add(channelFromJson(jsonArray.getJSONObject(i), true));
        }

        return channels;
    }

    public static FaceEmotion faceEmotionFromJson(JSONObject jsonObject) throws JSONException {
        JSONObject jsonFaceRectangle = jsonObject.getJSONObject("faceRectangle");
        JSONObject jsonScores = jsonObject.getJSONObject("scores");

        HashMap<FaceEmotion.Emotions, Float> scores = new HashMap();

        Iterator<?> keys = jsonScores.keys();

        while (keys.hasNext()) {
            String key = (String) keys.next();

            float score = (float) jsonScores.getDouble(key);
            if (key.equals("neutral")) {
                scores.put(FaceEmotion.Emotions.NEUTRAL, score);
            } else if (key.equals("sadness")) {
                scores.put(FaceEmotion.Emotions.SADNESS, score);
            } else if (key.equals("happiness")) {
                scores.put(FaceEmotion.Emotions.HAPPINESS, score);
            } else if (key.equals("contempt")) {
                scores.put(FaceEmotion.Emotions.CONTEMPT, score);
            } else if (key.equals("fear")) {
                scores.put(FaceEmotion.Emotions.FEAR, score);
            } else if (key.equals("anger")) {
                scores.put(FaceEmotion.Emotions.ANGER, score);
            } else if (key.equals("disgust")) {
                scores.put(FaceEmotion.Emotions.DISGUST, score);
            } else if (key.equals("surprise")) {
                scores.put(FaceEmotion.Emotions.SURPRISE, score);
            }
        }

        return new FaceEmotion(jsonFaceRectangle.getInt("height"),
                jsonFaceRectangle.getInt("width"),
                jsonFaceRectangle.getInt("top"),
                jsonFaceRectangle.getInt("left"),
                scores);
    }

    public static ArrayList<FaceEmotion> faceEmotionsFromJsonArray(JSONArray jsonArray) throws JSONException {
        ArrayList<FaceEmotion> faceEmotions = new ArrayList<>();

        for (int i = 0; i < jsonArray.length(); i++) {
            faceEmotions.add(faceEmotionFromJson(jsonArray.getJSONObject(i)));
        }

        return faceEmotions;
    }
}
