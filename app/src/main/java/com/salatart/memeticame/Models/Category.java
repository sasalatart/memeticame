package com.salatart.memeticame.Models;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

/**
 * Created by sasalatart on 11/12/16.
 */

public class Category implements Parcelable {
    public static final Creator<Category> CREATOR = new Creator<Category>() {
        @Override
        public Category createFromParcel(Parcel in) {
            return new Category(in);
        }

        @Override
        public Category[] newArray(int size) {
            return new Category[size];
        }
    };

    public static String PARCELABLE_KEY = "com.salatart.memeticame.Models.Category";

    private final int mId;
    private int mChannelId;
    private String mName;
    private ArrayList<Meme> mMemes;
    private String mCreatedAt;

    public Category(int id, int channelId, String name, ArrayList<Meme> memes, String createdAt) {
        this.mId = id;
        this.mChannelId = channelId;
        this.mName = name;
        this.mMemes = memes;
        this.mCreatedAt = createdAt;
    }

    protected Category(Parcel in) {
        this.mId = in.readInt();
        this.mChannelId = in.readInt();
        this.mName = in.readString();
        this.mMemes = new ArrayList<>();
        in.readTypedList(mMemes, Meme.CREATOR);
        this.mCreatedAt = in.readString();
    }

    public int getId() {
        return mId;
    }

    public int getChannelId() {
        return mChannelId;
    }

    public String getName() {
        return mName;
    }

    public ArrayList<Meme> getMemes() {
        return mMemes;
    }

    public String getCreatedAt() {
        return mCreatedAt;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(mId);
        dest.writeInt(mChannelId);
        dest.writeString(mName);
        dest.writeTypedList(mMemes);
        dest.writeString(mCreatedAt);
    }
}
