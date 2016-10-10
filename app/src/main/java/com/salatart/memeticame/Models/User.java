package com.salatart.memeticame.Models;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Andres Matte on 8/10/2016.
 */
public class User implements Parcelable {
    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        public User[] newArray(int size) {
            return new User[size];
        }
    };

    public static String PARCELABLE_KEY = "com.salatart.memeticamea.Models.User";

    private final int mId;
    private String mName;
    private String mPhoneNumber;

    public User(int id, String name, String phoneNumber) {
        this.mId = id;
        this.mName = name;
        this.mPhoneNumber = phoneNumber;
    }

    public User(Parcel in) {
        this.mId = in.readInt();
        this.mName = in.readString();
        this.mPhoneNumber = in.readString();
    }

    public static ArrayList<User> intersect(ArrayList<User> localUsers, ArrayList<User> externalUsers) {
        ArrayList<User> users = new ArrayList<>();

        for (User lU : localUsers) {
            for (User eU : externalUsers) {
                if (User.comparePhones(lU.getPhoneNumber(), eU.getPhoneNumber())) {
                    users.add(eU);
                    break;
                }
            }
        }

        return users;
    }

    public static boolean isPresent(List<User> users, User newUser) {
        for (User user : users) {
            if (User.comparePhones(user.getPhoneNumber(), newUser.getPhoneNumber())) {
                return true;
            }
        }
        return false;
    }

    public static boolean comparePhones(String phone1, String phone2) {
        return phone1.replaceAll("[^\\d.]", "").equals(phone2.replaceAll("[^\\d.]", ""));
    }

    public int getId() {
        return mId;
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

    public ArrayList<Chat> findChats(ArrayList<Chat> allChats) {
        if (allChats == null) {
            return null;
        }

        ArrayList<Chat> chats = new ArrayList<>();
        for (Chat chat : allChats) {
            for (User participant : chat.getParticipants()) {
                if (mPhoneNumber.equals(participant.getPhoneNumber())) {
                    chats.add(chat);
                }
            }
        }

        return chats;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(mId);
        dest.writeString(mName);
        dest.writeString(mPhoneNumber);
    }
}
