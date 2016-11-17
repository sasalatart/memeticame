package com.salatart.memeticame.Models;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

/**
 * Created by sasalatart on 11/12/16.
 */

public class Channel implements Parcelable {
    public static final Creator<Channel> CREATOR = new Creator<Channel>() {
        @Override
        public Channel createFromParcel(Parcel in) {
            return new Channel(in);
        }

        @Override
        public Channel[] newArray(int size) {
            return new Channel[size];
        }
    };

    public static String PARCELABLE_KEY = "com.salatart.memeticame.Models.Channel";

    private final int mId;
    private String mName;
    private ArrayList<Category> mCategories;
    private User mOwner;
    private double mRating;
    private String mCreatedAt;

    public Channel(int id, String name, ArrayList<Category> categories, User owner, double rating, String createdAt) {
        this.mId = id;
        this.mName = name;
        this.mCategories = categories;
        this.mOwner = owner;
        this.mRating = rating;
        this.mCreatedAt = createdAt;
    }

    public Channel(Parcel in) {
        this.mId = in.readInt();
        this.mName = in.readString();
        this.mCategories = in.readArrayList(Category.class.getClassLoader());
        this.mOwner = in.readParcelable(User.class.getClassLoader());
        this.mRating = in.readDouble();
        this.mCreatedAt = in.readString();
    }

    public int getId() {
        return mId;
    }

    public String getName() {
        return mName;
    }

    public ArrayList<Category> getCategories() {
        return mCategories;
    }

    public User getOwner() {
        return mOwner;
    }

    public double getRating() {
        return mRating;
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
        dest.writeString(mName);
        dest.writeTypedList(mCategories);
        dest.writeParcelable(mOwner, flags);
        dest.writeDouble(mRating);
        dest.writeString(mCreatedAt);
    }
}
