package cl.mecolab.memeticame.Models;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by sasalatart on 8/29/16.
 */
public class Chat {
    private final int mId;
    private String mTitle;
    private boolean mIsGroup;
    private String mCreatedAt;
    private ArrayList<User> mParticipants;

    public Chat(int mId, String mTitle, boolean mIsGroup, String mCreatedAt, ArrayList<User> participants) {
        this.mId = mId;
        this.mTitle = mTitle;
        this.mIsGroup = mIsGroup;
        this.mCreatedAt = mCreatedAt;
        this.mParticipants = participants;
    }

    public int getId() {
        return mId;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        this.mTitle = title;
    }

    public boolean isGroup() {
        return mIsGroup;
    }

    public void setIsGroup(boolean isGroup) {
        this.mIsGroup = isGroup;
    }

    public String getCreatedAt() {
        return mCreatedAt;
    }

    public void setCreatedAt(String createdAt) {
        this.mCreatedAt = createdAt;
    }

    public static ArrayList<Chat> fromJsonArray(JSONArray jsonResponse) throws JSONException {
        ArrayList<Chat> chats = new ArrayList<>();

        for (int i = 0; i < jsonResponse.length(); i++) {
            JSONObject jsonChat = jsonResponse.getJSONObject(i);

            ArrayList<User> users = new ArrayList<>();
            JSONArray jsonUsers = new JSONArray(jsonChat.getString("users"));
            for (int j = 0; j < jsonUsers.length(); j++) {
                JSONObject jsonParticipant = jsonUsers.getJSONObject(j);
                users.add(new User(jsonParticipant.getString("name"), jsonParticipant.getString("phone_number")));
            }

            chats.add(new Chat(Integer.parseInt(jsonChat.getString("id")),
                    jsonChat.getString("title"),
                    Boolean.parseBoolean(jsonChat.getString("group")),
                    jsonChat.getString("created_at"),
                    users));
        }

        return chats;
    }

    public ArrayList<User> getParticipants() {
        return mParticipants;
    }
}
