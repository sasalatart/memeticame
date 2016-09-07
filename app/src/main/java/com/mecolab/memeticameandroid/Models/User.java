package com.mecolab.memeticameandroid.Models;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by Andres Matte on 8/10/2016.
 */
public class User implements Parcelable {
    public static String PARCELABLE_KEY = "com.mecolab.memeticameandroid.Models.User";
    private String mName;
    private String mPhoneNumber;

    public User(String name, String phoneNumber) {
        this.mName = name;
        this.mPhoneNumber = phoneNumber;
    }

    public User(Parcel in) {
        String[] data = new String[2];

        in.readStringArray(data);
        this.mName = data[0];
        this.mPhoneNumber = data[1];
    }

    public static ArrayList<User> fromJsonArray(JSONArray jsonResponse) throws JSONException {
        ArrayList<User> users = new ArrayList<>();

        for (int i = 0; i < jsonResponse.length(); i++) {
            JSONObject jsonUser = jsonResponse.getJSONObject(i);
            users.add(new User(jsonUser.getString("name"), jsonUser.getString("phone_number")));
        }

        return users;
    }

    public static ArrayList<User> intersect(ArrayList<User> localUsers, ArrayList<User> externalUsers) {
        ArrayList<User> users = new ArrayList<>();

        for (User lU : localUsers) {
            String lUPhoneNumber = lU.getPhoneNumber().replaceAll("[^\\d.]", "");
            for (User eU : externalUsers) {
                String eUPhoneNumber = eU.getPhoneNumber().replaceAll("[^\\d.]", "");
                if (lUPhoneNumber.equals(eUPhoneNumber)) {
                    users.add(eU);
                    break;
                }
            }
        }

        return users;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        this.mName = name;
    }

    public String getPhoneNumber() {
        return mPhoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.mPhoneNumber = phoneNumber;
    }

    public Chat findChat(ArrayList<Chat> chats) {
        if (chats == null) {
            return null;
        }

        for (Chat chat : chats) {
            if (chat.isGroup()) {
                continue;
            }

            for (User participant : chat.getParticipants()) {
                if (this.mPhoneNumber.equals(participant.getPhoneNumber())) {
                    return chat;
                }
            }
        }

        return null;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeStringArray(new String[]{this.mName, this.mPhoneNumber});
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        public User[] newArray(int size) {
            return new User[size];
        }
    };
}
