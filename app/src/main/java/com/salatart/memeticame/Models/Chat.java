package com.salatart.memeticame.Models;

import android.app.Activity;
import android.content.Intent;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.salatart.memeticame.Activities.ChatActivity;
import com.salatart.memeticame.Activities.MainActivity;
import com.salatart.memeticame.Utils.HttpClient;
import com.salatart.memeticame.Utils.ParserUtils;
import com.salatart.memeticame.Utils.SessionUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by sasalatart on 8/29/16.
 */
public class Chat implements Parcelable {
    public static final Parcelable.Creator<Chat> CREATOR = new Parcelable.Creator<Chat>() {
        public Chat createFromParcel(Parcel in) {
            return new Chat(in);
        }

        public Chat[] newArray(int size) {
            return new Chat[size];
        }
    };

    public static String PARCELABLE_KEY = "com.salatart.memeticame.Models.Chat";
    public static String PARCELABLE_ARRAY_KEY = "com.salatart.memeticame.Models.ChatArrayList";

    private final int mId;
    private String mTitle;
    private boolean mIsGroup;
    private String mCreatedAt;
    private String mAdmin;
    private ArrayList<User> mParticipants;
    private ArrayList<Message> mMessages;

    public Chat(int mId, String mTitle, boolean mIsGroup, String mCreatedAt, String admin, ArrayList<User> participants, ArrayList<Message> messages) {
        this.mId = mId;
        this.mTitle = mTitle;
        this.mIsGroup = mIsGroup;
        this.mCreatedAt = mCreatedAt;
        this.mAdmin = admin;
        this.mParticipants = participants;
        this.mMessages = messages;
    }

    public Chat(Parcel in) {
        this.mId = in.readInt();
        this.mTitle = in.readString();
        this.mIsGroup = in.readByte() != 0;
        this.mCreatedAt = in.readString();
        this.mAdmin = in.readString();
        this.mParticipants = new ArrayList<>();
        in.readTypedList(this.mParticipants, User.CREATOR);
        this.mMessages = new ArrayList<>();
        in.readTypedList(this.mMessages, Message.CREATOR);
    }

    public static void createFromRequest(final Activity activity, Request request) {
        HttpClient.getInstance().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("ERROR", e.toString());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    JSONObject jsonChat = new JSONObject(response.body().string());
                    activity.startActivity(ChatActivity.getIntent(activity, ParserUtils.chatFromJson(jsonChat)));
                    activity.finish();
                } catch (JSONException e) {
                    Log.e("ERROR", e.toString());
                } finally {
                    response.body().close();
                }
            }
        });
    }

    public boolean onUserRemoved(final Activity activity, User user) {

        for (User localUser : mParticipants) {
            if (localUser.getId() == user.getId()) {
                mParticipants.remove(localUser);
                break;
            }
        }

        if (User.comparePhones(user.getPhoneNumber(), SessionUtils.getPhoneNumber(activity))) {
            activity.startActivity(new Intent(activity, MainActivity.class));
            activity.finish();
        }

        return true;
    }

    public boolean userPresent(String phoneNumber) {
        for (User user : mParticipants) {
            if (User.comparePhones(user.getPhoneNumber(), phoneNumber)) {
                return true;
            }
        }
        return false;
    }

    public int getId() {
        return mId;
    }

    public String getTitle() {
        return mTitle;
    }

    public boolean isGroup() {
        return mIsGroup;
    }

    public String getAdmin() {
        return mAdmin;
    }

    public ArrayList<User> getParticipants() {
        return mParticipants;
    }

    public HashMap<String, String> getParticipantsHash() {
        HashMap<String, String> participantsHash = new HashMap<>();
        for (User user : mParticipants) {
            participantsHash.put(user.getPhoneNumber(), user.getName());
        }
        return participantsHash;
    }

    public ArrayList<Message> getMessages() {
        return mMessages;
    }

    public void addUsers(ArrayList<User> newUsers) {
        for (User newUser : newUsers) {
            boolean isPresent = false;
            for (User user : mParticipants) {
                if (user.getId() == newUser.getId()) {
                    isPresent = true;
                }
            }
            if (!isPresent) {
                mParticipants.add(newUser);
            }
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(mId);
        dest.writeString(mTitle);
        dest.writeByte((byte) (mIsGroup ? 1 : 0));
        dest.writeString(mCreatedAt);
        dest.writeString(mAdmin);
        dest.writeTypedList(mParticipants);
        dest.writeTypedList(mMessages);
    }
}
